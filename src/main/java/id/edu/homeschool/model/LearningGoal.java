package id.edu.homeschool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;

public final class LearningGoal extends LearningItem {
    private LocalDate targetDate;
    private boolean achieved;

    public LearningGoal() { super(); }

    public LearningGoal(String description, LocalDate targetDate) {
        super(description);
        setTargetDate(targetDate);
    }

    public String getDescription() { return getTitle(); }
    public void setDescription(String description) { setTitle(description); }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) {
        if (targetDate == null) throw new IllegalArgumentException("Tanggal target wajib diisi.");
        this.targetDate = targetDate;
    }
    public boolean isAchieved() { return achieved; }
    public void setAchieved(boolean achieved) { this.achieved = achieved; }
    @JsonIgnore
    @Override public String getStatusLabel() { return achieved ? "Tercapai" : "Dalam proses"; }
}
