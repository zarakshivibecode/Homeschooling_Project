package id.edu.homeschool.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ModelValidationTest {
    @Test void requiresValidProfileAndSessionFields() {
        assertThrows(IllegalArgumentException.class, () -> new StudentProfile(" ", LocalDate.now().minusYears(10), "SMP", "Visual"));
        assertThrows(IllegalArgumentException.class, () -> new StudySession("math", LocalDate.now(), null, 30, ""));
        assertThrows(IllegalArgumentException.class, () -> new StudySession("math", LocalDate.now(), java.time.LocalTime.NOON, 0, ""));
    }

    @Test void polymorphicStatusReflectsTaskAndGoalState() {
        LearningItem task = new LearningTask("Latihan", "math", LocalDate.now().plusDays(1));
        LearningItem goal = new LearningGoal("Membaca buku", LocalDate.now().plusDays(5));
        assertEquals("Belum selesai", task.getStatusLabel());
        assertEquals("Dalam proses", goal.getStatusLabel());
        ((LearningTask) task).setCompleted(true);
        ((LearningGoal) goal).setAchieved(true);
        assertEquals("Selesai", task.getStatusLabel());
        assertEquals("Tercapai", goal.getStatusLabel());
    }
}
