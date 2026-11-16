package id.edu.homeschool.service;

import id.edu.homeschool.model.InvitationStatus;
import id.edu.homeschool.model.TeacherInvitation;
import id.edu.homeschool.repository.ApplicationRepositories;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionServiceTest {
    @TempDir Path temp;
    private ApplicationRepositories repositories;
    private SessionRegistry sessions;
    private AuthService auth;
    private MutableClock clock;
    private ConnectionService connections;
    private UserSession teacher;
    private UserSession anotherTeacher;
    private UserSession student;

    @BeforeEach void setUp() {
        repositories = new ApplicationRepositories(temp);
        sessions = new SessionRegistry(repositories.accounts());
        auth = new AuthService(repositories.accounts(), sessions);
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        connections = new ConnectionService(repositories.accounts(), repositories.invitations(), repositories.connectionRequests(),
                repositories.connections(), sessions, clock);
        auth.registerTeacher("Teacher One", "teacher@example.test", "TeacherPass2040".toCharArray());
        auth.registerTeacher("Teacher Two", "teacher2@example.test", "TeacherPass2041".toCharArray());
        auth.registerStudent("Student One", "student@example.test", "StudentPass2040".toCharArray(), "SMA");
        teacher = auth.login("teacher@example.test", "TeacherPass2040".toCharArray());
        anotherTeacher = auth.login("teacher2@example.test", "TeacherPass2041".toCharArray());
        student = auth.login("student@example.test", "StudentPass2040".toCharArray());
    }

    @Test void codeIsStoredAsHashStudentRequestsAndTeacherApprovalCreatesActiveRelation() throws Exception {
        CreatedInvitation issued = connections.createInvitation(teacher.token());
        String storedJson = Files.readString(temp.resolve("teacher-invitations.json"));
        assertFalse(storedJson.contains(issued.code()));
        assertTrue(storedJson.contains("codeHash"));

        var request = connections.requestConnection(student.token(), issued.code());
        assertEquals("Student One", connections.pendingForTeacher(teacher.token()).getFirst().studentName());
        assertThrows(SecurityException.class, () -> connections.approve(anotherTeacher.token(), request.id()));
        connections.approve(teacher.token(), request.id());
        assertEquals("Teacher One", connections.activeTeachersForStudent(student.token()).getFirst().name());
        assertThrows(IllegalArgumentException.class, () -> connections.requestConnection(student.token(), issued.code()));
    }

    @Test void duplicatePendingRequestIsRejectedAndInvitationCannotBeReused() {
        CreatedInvitation first = connections.createInvitation(teacher.token());
        connections.requestConnection(student.token(), first.code());
        CreatedInvitation second = connections.createInvitation(teacher.token());
        assertThrows(IllegalArgumentException.class, () -> connections.requestConnection(student.token(), second.code()));
        TeacherInvitation secondSaved = repositories.invitations().findAll().stream()
                .filter(invitation -> invitation.getStatus() == InvitationStatus.ACTIVE).findFirst().orElseThrow();
        assertEquals(InvitationStatus.ACTIVE, secondSaved.getStatus());
    }

    @Test void expiredInvitationIsRejectedAndMarkedExpired() {
        CreatedInvitation issued = connections.createInvitation(teacher.token());
        clock.advanceSeconds(8 * 24 * 60 * 60L);
        assertThrows(IllegalArgumentException.class, () -> connections.requestConnection(student.token(), issued.code()));
        assertEquals(InvitationStatus.EXPIRED, repositories.invitations().findAll().getFirst().getStatus());
    }

    @Test void roleChecksRejectTeacherUsingStudentAction() {
        assertThrows(SecurityException.class, () -> connections.requestConnection(teacher.token(), "untrusted"));
    }

    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> instant;
        MutableClock(Instant initial) { instant = new AtomicReference<>(initial); }
        void advanceSeconds(long seconds) { instant.updateAndGet(value -> value.plusSeconds(seconds)); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant.get(); }
    }
}
