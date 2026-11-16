package id.edu.homeschool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.UUID;

/** Hashed, time-limited, single-use code created by a teacher account. */
public final class TeacherInvitation {
    private String id = UUID.randomUUID().toString();
    private String teacherAccountId;
    private String codeHash;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant usedAt;
    private InvitationStatus status = InvitationStatus.ACTIVE;

    public TeacherInvitation() { }
    public TeacherInvitation(String teacherAccountId, String codeHash, Instant createdAt, Instant expiresAt) {
        setTeacherAccountId(teacherAccountId); setCodeHash(codeHash); setCreatedAt(createdAt); setExpiresAt(expiresAt);
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = required(id); }
    public String getTeacherAccountId() { return teacherAccountId; }
    public void setTeacherAccountId(String value) { teacherAccountId = required(value); }
    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String value) {
        String hash = required(value);
        if (!hash.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Hash kode undangan tidak valid.");
        codeHash = hash;
    }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant value) { createdAt = required(value); }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant value) { expiresAt = required(value); }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant value) { usedAt = value; }
    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus value) {
        if (value == null) throw new IllegalArgumentException("Status undangan wajib diisi.");
        status = value;
    }
    @JsonIgnore public boolean isExpiredAt(Instant now) { return !now.isBefore(expiresAt); }
    private static String required(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Data undangan wajib diisi.");
        return value.trim();
    }
    private static Instant required(Instant value) {
        if (value == null) throw new IllegalArgumentException("Waktu undangan wajib diisi.");
        return value;
    }
}
