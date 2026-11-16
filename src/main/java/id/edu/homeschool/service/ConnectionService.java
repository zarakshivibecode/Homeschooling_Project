package id.edu.homeschool.service;

import id.edu.homeschool.model.AccountRole;
import id.edu.homeschool.model.AccountStatus;
import id.edu.homeschool.model.ConnectionRequestStatus;
import id.edu.homeschool.model.ConnectionStatus;
import id.edu.homeschool.model.InvitationStatus;
import id.edu.homeschool.model.TeacherConnectionRequest;
import id.edu.homeschool.model.TeacherInvitation;
import id.edu.homeschool.model.TeacherStudentConnection;
import id.edu.homeschool.model.UserAccount;
import id.edu.homeschool.repository.Repository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

/** Invitation-based teacher/student connection workflow for the local single-process prototype. */
public final class ConnectionService {
    private static final Duration INVITATION_LIFETIME = Duration.ofDays(7);
    private final Repository<UserAccount> accounts;
    private final Repository<TeacherInvitation> invitations;
    private final Repository<TeacherConnectionRequest> requests;
    private final Repository<TeacherStudentConnection> connections;
    private final SessionRegistry sessions;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public ConnectionService(Repository<UserAccount> accounts, Repository<TeacherInvitation> invitations,
            Repository<TeacherConnectionRequest> requests, Repository<TeacherStudentConnection> connections,
            SessionRegistry sessions) {
        this(accounts, invitations, requests, connections, sessions, Clock.systemUTC());
    }

    public ConnectionService(Repository<UserAccount> accounts, Repository<TeacherInvitation> invitations,
            Repository<TeacherConnectionRequest> requests, Repository<TeacherStudentConnection> connections,
            SessionRegistry sessions, Clock clock) {
        this.accounts = Objects.requireNonNull(accounts);
        this.invitations = Objects.requireNonNull(invitations);
        this.requests = Objects.requireNonNull(requests);
        this.connections = Objects.requireNonNull(connections);
        this.sessions = Objects.requireNonNull(sessions);
        this.clock = Objects.requireNonNull(clock);
    }

    public synchronized CreatedInvitation createInvitation(String sessionToken) {
        UserAccount teacher = requireRole(sessionToken, AccountRole.PENGAJAR);
        byte[] bytes = new byte[18];
        random.nextBytes(bytes);
        String code = HexFormat.of().withUpperCase().formatHex(bytes);
        Instant now = clock.instant();
        Instant expires = now.plus(INVITATION_LIFETIME);
        invitations.save(new TeacherInvitation(teacher.getId(), hashCode(code), now, expires));
        return new CreatedInvitation(code, expires);
    }

    /**
     * Submits a connection request for the teacher that owns the invitation code.
     * The code stays active until a request is approved, so a rejected request can be resubmitted.
     */
    public synchronized ConnectionRequestSummary requestConnection(String sessionToken, String invitationCode) {
        UserAccount student = requireRole(sessionToken, AccountRole.SISWA);
        if (invitationCode == null || invitationCode.isBlank()) throw new IllegalArgumentException("Kode undangan wajib diisi.");
        String normalizedCode = invitationCode.trim().toUpperCase(java.util.Locale.ROOT);
        if (!normalizedCode.matches("[0-9A-F]{36}")) throw new IllegalArgumentException("Kode undangan tidak valid atau sudah tidak tersedia.");
        String codeHash = hashCode(normalizedCode);
        TeacherInvitation invitation = invitations.findAll().stream()
                .filter(value -> MessageDigest.isEqual(value.getCodeHash().getBytes(StandardCharsets.US_ASCII), codeHash.getBytes(StandardCharsets.US_ASCII)))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Kode undangan tidak valid atau sudah tidak tersedia."));
        if (invitation.getStatus() == InvitationStatus.ACTIVE && invitation.isExpiredAt(clock.instant())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitations.save(invitation);
        }
        if (invitation.getStatus() != InvitationStatus.ACTIVE) {
            throw new IllegalArgumentException("Kode undangan tidak valid atau sudah tidak tersedia.");
        }
        String teacherId = invitation.getTeacherAccountId();
        boolean teacherActive = accounts.findById(teacherId)
                .filter(value -> value.getRole() == AccountRole.PENGAJAR && value.getStatus() == AccountStatus.ACTIVE).isPresent();
        if (!teacherActive) throw new IllegalArgumentException("Kode undangan tidak valid atau sudah tidak tersedia.");
        boolean duplicateRequest = requests.findAll().stream().anyMatch(request ->
                request.getStudentAccountId().equals(student.getId())
                && request.getTeacherAccountId().equals(teacherId)
                && request.getStatus() != ConnectionRequestStatus.REJECTED);
        boolean alreadyConnected = connections.findAll().stream().anyMatch(link ->
                link.getStudentAccountId().equals(student.getId()) && link.getTeacherAccountId().equals(teacherId)
                && link.getStatus() == ConnectionStatus.ACTIVE);
        if (duplicateRequest || alreadyConnected) throw new IllegalArgumentException("Permintaan koneksi untuk pengajar ini sudah pernah dibuat.");

        Instant now = clock.instant();
        TeacherConnectionRequest saved = requests.save(new TeacherConnectionRequest(student.getId(), teacherId, invitation.getId(), now));
        return summarize(saved);
    }

    public List<InvitationSummary> ownInvitations(String sessionToken) {
        UserAccount teacher = requireRole(sessionToken, AccountRole.PENGAJAR);
        List<InvitationSummary> result = new ArrayList<>();
        for (TeacherInvitation invitation : invitations.findAll()) {
            if (!teacher.getId().equals(invitation.getTeacherAccountId())) continue;
            if (invitation.getStatus() == InvitationStatus.ACTIVE && invitation.isExpiredAt(clock.instant())) {
                invitation.setStatus(InvitationStatus.EXPIRED);
                invitations.save(invitation);
            }
            int pending = (int) requests.findAll().stream()
                    .filter(request -> invitation.getId().equals(request.getInvitationId())
                            && request.getStatus() == ConnectionRequestStatus.PENDING).count();
            result.add(new InvitationSummary(invitation.getId(), invitation.getCreatedAt(), invitation.getExpiresAt(),
                    invitation.getStatus(), pending));
        }
        result.sort((left, right) -> right.createdAt().compareTo(left.createdAt()));
        return List.copyOf(result);
    }

    public List<ConnectionRequestSummary> pendingForTeacher(String sessionToken) {
        UserAccount teacher = requireRole(sessionToken, AccountRole.PENGAJAR);
        return requests.findAll().stream()
                .filter(request -> teacher.getId().equals(request.getTeacherAccountId()) && request.getStatus() == ConnectionRequestStatus.PENDING)
                .map(this::summarize).toList();
    }

    /** Every request the student ever sent, newest first, so the UI can show rejected history too. */
    public List<ConnectionRequestSummary> requestsForStudent(String sessionToken) {
        UserAccount student = requireRole(sessionToken, AccountRole.SISWA);
        return requests.findAll().stream().filter(request -> student.getId().equals(request.getStudentAccountId()))
                .map(this::summarize)
                .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
                .toList();
    }

    public List<AccountSummary> activeTeachersForStudent(String sessionToken) {
        UserAccount student = requireRole(sessionToken, AccountRole.SISWA);
        return connections.findAll().stream()
                .filter(link -> student.getId().equals(link.getStudentAccountId()) && link.getStatus() == ConnectionStatus.ACTIVE)
                .map(TeacherStudentConnection::getTeacherAccountId).distinct()
                .map(id -> accounts.findById(id).filter(account -> account.getStatus() == AccountStatus.ACTIVE).orElse(null)).filter(Objects::nonNull)
                .map(this::summary).toList();
    }

    public List<AccountSummary> activeStudentsForTeacher(String sessionToken) {
        UserAccount teacher = requireRole(sessionToken, AccountRole.PENGAJAR);
        return connections.findAll().stream()
                .filter(link -> teacher.getId().equals(link.getTeacherAccountId()) && link.getStatus() == ConnectionStatus.ACTIVE)
                .map(TeacherStudentConnection::getStudentAccountId).distinct()
                .map(id -> accounts.findById(id).filter(account -> account.getStatus() == AccountStatus.ACTIVE).orElse(null)).filter(Objects::nonNull)
                .map(this::summary).toList();
    }

    public synchronized void approve(String sessionToken, String requestId) {
        UserAccount teacher = requireRole(sessionToken, AccountRole.PENGAJAR);
        TeacherConnectionRequest request = requireOwnedRequest(teacher, requestId);
        if (request.getStatus() != ConnectionRequestStatus.PENDING) throw new IllegalStateException("Permintaan ini sudah diproses.");
        boolean studentActive = accounts.findById(request.getStudentAccountId())
                .filter(value -> value.getRole() == AccountRole.SISWA && value.getStatus() == AccountStatus.ACTIVE).isPresent();
        if (!studentActive) throw new IllegalStateException("Akun siswa tidak aktif sehingga koneksi tidak dapat disetujui.");
        boolean linkExists = connections.findAll().stream().anyMatch(link ->
                link.getStudentAccountId().equals(request.getStudentAccountId())
                && link.getTeacherAccountId().equals(teacher.getId()) && link.getStatus() == ConnectionStatus.ACTIVE);
        if (!linkExists) connections.save(new TeacherStudentConnection(request.getStudentAccountId(), teacher.getId(), clock.instant()));
        request.setStatus(ConnectionRequestStatus.APPROVED);
        request.setUpdatedAt(clock.instant());
        requests.save(request);
        consumeInvitation(request.getInvitationId());
    }

    public synchronized void reject(String sessionToken, String requestId) {
        UserAccount teacher = requireRole(sessionToken, AccountRole.PENGAJAR);
        TeacherConnectionRequest request = requireOwnedRequest(teacher, requestId);
        if (request.getStatus() != ConnectionRequestStatus.PENDING) throw new IllegalStateException("Permintaan ini sudah diproses.");
        request.setStatus(ConnectionRequestStatus.REJECTED);
        request.setUpdatedAt(clock.instant());
        requests.save(request);
    }

    public synchronized void revokeInvitation(String sessionToken, String invitationId) {
        UserAccount teacher = requireRole(sessionToken, AccountRole.PENGAJAR);
        TeacherInvitation invitation = invitations.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Undangan tidak ditemukan."));
        if (!teacher.getId().equals(invitation.getTeacherAccountId())) throw new SecurityException("Anda tidak berhak mengubah undangan ini.");
        if (invitation.getStatus() != InvitationStatus.ACTIVE) throw new IllegalStateException("Undangan ini sudah tidak aktif.");
        invitation.setStatus(invitation.isExpiredAt(clock.instant()) ? InvitationStatus.EXPIRED : InvitationStatus.REVOKED);
        invitations.save(invitation);
    }

    /** Ends an active teacher/student link. Learning records are kept so the link can be restored later. */
    public synchronized void removeConnection(String sessionToken, String studentAccountId) {
        UserAccount teacher = requireRole(sessionToken, AccountRole.PENGAJAR);
        TeacherStudentConnection link = connections.findAll().stream()
                .filter(value -> value.getStatus() == ConnectionStatus.ACTIVE
                        && value.getTeacherAccountId().equals(teacher.getId())
                        && value.getStudentAccountId().equals(studentAccountId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Koneksi tidak ditemukan."));
        link.setStatus(ConnectionStatus.ENDED);
        link.setUpdatedAt(clock.instant());
        connections.save(link);
    }

    private void consumeInvitation(String invitationId) {
        if (invitationId == null) return;
        invitations.findById(invitationId).ifPresent(invitation -> {
            if (invitation.getStatus() == InvitationStatus.ACTIVE) {
                invitation.setStatus(InvitationStatus.USED);
                invitation.setUsedAt(clock.instant());
                invitations.save(invitation);
            }
        });
    }

    private TeacherConnectionRequest requireOwnedRequest(UserAccount teacher, String requestId) {
        TeacherConnectionRequest request = requests.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Permintaan koneksi tidak ditemukan."));
        if (!teacher.getId().equals(request.getTeacherAccountId())) throw new SecurityException("Anda tidak berhak memproses permintaan ini.");
        return request;
    }

    private UserAccount requireRole(String token, AccountRole role) {
        UserAccount account = sessions.requireAccount(token);
        if (account.getRole() != role) throw new SecurityException("Akses ini tidak tersedia untuk peran akun Anda.");
        return account;
    }

    private ConnectionRequestSummary summarize(TeacherConnectionRequest request) {
        String student = accounts.findById(request.getStudentAccountId()).map(UserAccount::getName).orElse("Akun siswa tidak tersedia");
        String teacher = accounts.findById(request.getTeacherAccountId()).map(UserAccount::getName).orElse("Akun pengajar tidak tersedia");
        return new ConnectionRequestSummary(request.getId(), student, teacher, request.getStatus(), request.getCreatedAt(), request.getUpdatedAt());
    }
    private AccountSummary summary(UserAccount account) {
        return new AccountSummary(account.getId(), account.getName(), account.getEmail(), account.getRole(), account.getEducationLevel());
    }

    private static String hashCode(String code) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 tidak tersedia pada runtime ini.", exception);
        }
    }
}
