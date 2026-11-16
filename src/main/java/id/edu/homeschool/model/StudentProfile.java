package id.edu.homeschool.model;

import java.time.LocalDate;

/**
 * Legacy single-profile model kept compatible with earlier project data.
 * New student records should use {@link Siswa}.
 */
@Deprecated(forRemoval = false)
public final class StudentProfile extends Siswa {
    public StudentProfile() {
        super();
        setId("student-profile");
    }
    public StudentProfile(String name, LocalDate birthDate, String educationLevel, String learningPreferences) {
        super(name, "", "", birthDate, educationLevel, learningPreferences);
        setId("student-profile");
    }
}
