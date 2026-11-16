package id.edu.homeschool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.UUID;

public final class Evaluation {
    private String id = UUID.randomUUID().toString();
    private String subjectId;
    private LocalDate date;
    private double score;
    private double maximumScore;
    private String notes = "";

    public Evaluation() { }
    @JsonCreator
    public Evaluation(@JsonProperty("id") String id,
                      @JsonProperty("subjectId") String subjectId,
                      @JsonProperty("date") LocalDate date,
                      @JsonProperty("score") double score,
                      @JsonProperty("maximumScore") double maximumScore,
                      @JsonProperty("notes") String notes) {
        if (id != null) setId(id);
        setSubjectId(subjectId);
        setDate(date);
        setScores(score, maximumScore);
        setNotes(notes);
    }
    public Evaluation(String subjectId, LocalDate date, double score, double maximumScore, String notes) {
        setSubjectId(subjectId); setDate(date); setScores(score, maximumScore); setNotes(notes);
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = LearningItem.requireText(id, "ID evaluasi"); }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String value) { subjectId = LearningItem.requireText(value, "Mata pelajaran"); }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate value) { if (value == null) throw new IllegalArgumentException("Tanggal evaluasi wajib diisi."); date = value; }
    public double getScore() { return score; }
    public void setScore(double value) { if (!Double.isFinite(value) || value < 0 || value > maximumScore) throw new IllegalArgumentException("Nilai harus di antara 0 dan nilai maksimum."); score = value; }
    public double getMaximumScore() { return maximumScore; }
    public void setMaximumScore(double value) {
        if (!Double.isFinite(value) || value <= 0 || score > value) throw new IllegalArgumentException("Nilai maksimum harus lebih besar dari 0 dan tidak di bawah nilai.");
        maximumScore = value;
    }
    public void setScores(double score, double maximumScore) {
        if (!Double.isFinite(maximumScore) || maximumScore <= 0 || !Double.isFinite(score) || score < 0 || score > maximumScore)
            throw new IllegalArgumentException("Nilai harus di antara 0 dan nilai maksimum yang lebih besar dari 0.");
        this.score = score;
        this.maximumScore = maximumScore;
    }
    public String getNotes() { return notes; }
    public void setNotes(String value) { notes = value == null ? "" : value.trim(); }
    @JsonIgnore
    public double getPercentage() { return maximumScore == 0 ? 0 : score / maximumScore * 100.0; }
}
