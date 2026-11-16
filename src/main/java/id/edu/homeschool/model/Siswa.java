package id.edu.homeschool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;

public class Siswa extends Pengguna {
    private LocalDate birthDate;
    private String educationLevel = "";
    private String learningPreferences = "";

    public Siswa() { super(); }
    public Siswa(String name, String email, String phone, LocalDate birthDate,
                 String educationLevel, String learningPreferences) {
        super(name, email, phone);
        setBirthDate(birthDate); setEducationLevel(educationLevel); setLearningPreferences(learningPreferences);
    }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) {
        if (birthDate == null || birthDate.isAfter(LocalDate.now())) throw new IllegalArgumentException("Tanggal lahir tidak valid.");
        this.birthDate = birthDate;
    }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = required(educationLevel, "Jenjang pendidikan"); }
    public String getLearningPreferences() { return learningPreferences; }
    public void setLearningPreferences(String preferences) { learningPreferences = required(preferences, "Preferensi belajar"); }
    @JsonIgnore
    @Override public String getRoleLabel() { return "Siswa"; }
}
