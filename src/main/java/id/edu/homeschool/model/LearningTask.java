package id.edu.homeschool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;

/** An assignment with its instruction text, due date and completion state. */
public class LearningTask extends LearningItem {
    private String subjectId;
    private LocalDate dueDate;
    private boolean completed;
    private String instructions = "";

    public LearningTask() { super(); }

    public LearningTask(String title, String subjectId, LocalDate dueDate) {
        super(title);
        setSubjectId(subjectId);
        setDueDate(dueDate);
    }

    public LearningTask(String title, String subjectId, LocalDate dueDate, String instructions) {
        this(title, subjectId, dueDate);
        setInstructions(instructions);
    }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = requireText(subjectId, "Mata pelajaran"); }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) {
        if (dueDate == null) throw new IllegalArgumentException("Tenggat tugas wajib diisi.");
        this.dueDate = dueDate;
    }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public boolean isOverdue(LocalDate today) { return !completed && dueDate != null && dueDate.isBefore(today); }

    /** The assignment instructions shown to the student. */
    public String getInstructions() { return instructions; }
    public void setInstructions(String value) { this.instructions = value == null ? "" : value.trim(); }

    @JsonIgnore
    @Override public String getStatusLabel() {
        if (completed) return "Selesai";
        return isOverdue(LocalDate.now()) ? "Terlambat" : "Belum selesai";
    }
}
