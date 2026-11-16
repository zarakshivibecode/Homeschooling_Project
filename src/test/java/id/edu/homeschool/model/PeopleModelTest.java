package id.edu.homeschool.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PeopleModelTest {
    @Test void studentAndTeacherShareUserContractWithDifferentRoles() {
        Pengguna student = new Siswa("Ayu", "ayu@example.test", "0812", LocalDate.now().minusYears(12), "SMP", "Visual");
        Pengguna teacher = new Pengajar("Budi", "budi@example.test", "0813", "Matematika", "S.Pd.");
        assertEquals("Siswa", student.getRoleLabel());
        assertEquals("Pengajar", teacher.getRoleLabel());
    }

    @Test void validatesContactAndStudentSpecificData() {
        assertThrows(IllegalArgumentException.class,
                () -> new Siswa("Ayu", "email-salah", "", LocalDate.now().minusYears(12), "SMP", "Visual"));
        assertThrows(IllegalArgumentException.class,
                () -> new Pengajar("Budi", "", "", " ", ""));
    }
}
