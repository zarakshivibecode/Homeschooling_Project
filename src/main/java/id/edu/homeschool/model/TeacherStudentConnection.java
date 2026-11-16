package id.edu.homeschool.model;

import java.time.Instant;
import java.util.UUID;

public final class TeacherStudentConnection {
    private String id = UUID.randomUUID().toString();
    private String studentAccountId;
    private String teacherAccountId;
    private ConnectionStatus status = ConnectionStatus.ACTIVE;
    private Instant createdAt;
    private Instant updatedAt;

    public TeacherStudentConnection() { }
    public TeacherStudentConnection(String studentId, String teacherId, Instant now) {
        setStudentAccountId(studentId); setTeacherAccountId(teacherId); setCreatedAt(now); setUpdatedAt(now);
    }
    public String getId() { return id; }
    public void setId(String value) { id = required(value); }
    public String getStudentAccountId() { return studentAccountId; }
    public void setStudentAccountId(String value) { studentAccountId = required(value); }
    public String getTeacherAccountId() { return teacherAccountId; }
    public void setTeacherAccountId(String value) { teacherAccountId = required(value); }
    public ConnectionStatus getStatus() { return status; }
    public void setStatus(ConnectionStatus value) {
        if (value == null) throw new IllegalArgumentException("Status relasi wajib diisi.");
        status = value;
    }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant value) { createdAt = required(value); }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant value) { updatedAt = required(value); }
    private static String required(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Identitas koneksi wajib diisi.");
        return value.trim();
    }
    private static Instant required(Instant value) {
        if (value == null) throw new IllegalArgumentException("Waktu relasi wajib diisi.");
        return value;
    }
}
