package id.edu.homeschool.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class StudySession {
    private String id = UUID.randomUUID().toString();
    private String studentId = "";
    private String teacherId = "";
    private String subjectId;
    private LocalDate date;
    private LocalTime startTime;
    private int durationMinutes;
    private String notes = "";

    public StudySession() { }
    public StudySession(String subjectId, LocalDate date, LocalTime startTime, int durationMinutes, String notes) {
        setSubjectId(subjectId); setDate(date); setStartTime(startTime); setDurationMinutes(durationMinutes); setNotes(notes);
    }
    public StudySession(String studentId, String teacherId, String subjectId, LocalDate date,
                        LocalTime startTime, int durationMinutes, String notes) {
        setStudentId(studentId); setTeacherId(teacherId); setSubjectId(subjectId);
        setDate(date); setStartTime(startTime); setDurationMinutes(durationMinutes); setNotes(notes);
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = LearningItem.requireText(id, "ID jadwal"); }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = LearningItem.requireText(studentId, "Siswa"); }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = LearningItem.requireText(teacherId, "Pengajar"); }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = LearningItem.requireText(subjectId, "Mata pelajaran"); }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { if (date == null) throw new IllegalArgumentException("Tanggal wajib diisi."); this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime value) { if (value == null) throw new IllegalArgumentException("Waktu mulai wajib diisi."); startTime = value; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int value) { if (value < 1 || value > 1440) throw new IllegalArgumentException("Durasi harus antara 1 dan 1440 menit."); durationMinutes = value; }
    public String getNotes() { return notes; }
    public void setNotes(String value) { notes = value == null ? "" : value.trim(); }
}
