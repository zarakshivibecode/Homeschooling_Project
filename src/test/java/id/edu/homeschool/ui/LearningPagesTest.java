package id.edu.homeschool.ui;

import id.edu.homeschool.model.LearningMaterial;
import id.edu.homeschool.model.LearningTask;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.service.UserSession;
import id.edu.homeschool.support.TestEnvironment;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The list pages: column filters, detail entries and the role based navigation. */
class LearningPagesTest {

    @TempDir Path temp;
    private TestEnvironment environment;
    private UserSession teacher;
    private UserSession student;

    @BeforeEach void setUp() {
        environment = new TestEnvironment(temp);
        teacher = environment.registerTeacher("Pengajar Satu", "guru1@uji.test", "GuruPassword2040");
        student = environment.registerStudent("Siswa Satu", "siswa@uji.test", "SiswaPassword2040", "SMA");
        environment.connect(teacher, student);
    }

    @Test void materialPageShowsTheRecordsOfTheConnectedStudent() {
        Subject math = environment.learning.saveSubject(teacher.token(), student.accountId(),
                new Subject("Matematika", "Aljabar"));
        Subject indonesia = environment.learning.saveSubject(teacher.token(), student.accountId(),
                new Subject("Bahasa Indonesia", "Teks eksplanasi"));
        environment.learning.saveMaterial(teacher.token(), student.accountId(),
                new LearningMaterial(math.getId(), "Persamaan Linear", "Materi baca", "Bab 1", "Ringkasan",
                        "Tujuan pembelajaran", "Isi materi yang panjang"));
        environment.learning.saveMaterial(teacher.token(), student.accountId(),
                new LearningMaterial(indonesia.getId(), "Teks Eksplanasi", "Materi baca", "Bab 2", "Ringkasan",
                        "Tujuan pembelajaran", "Isi materi yang panjang"));

        AppShell shell = new AppShell(new Workspace(environment.auth, environment.connections,
                environment.learning, teacher), () -> { });
        shell.showPage("materials");
        MaterialPage page = (MaterialPage) shell.page("materials");
        assertEquals(2, page.table.table().getRowCount());
    }

    @Test void theColumnFilterNarrowsTheTable() {
        Subject math = environment.learning.saveSubject(teacher.token(), student.accountId(),
                new Subject("Matematika", "Aljabar"));
        Subject indonesia = environment.learning.saveSubject(teacher.token(), student.accountId(),
                new Subject("Bahasa Indonesia", "Teks eksplanasi"));
        environment.learning.saveMaterial(teacher.token(), student.accountId(),
                new LearningMaterial(math.getId(), "Persamaan Linear", "Materi baca", "Bab 1", "Ringkasan"));
        environment.learning.saveMaterial(teacher.token(), student.accountId(),
                new LearningMaterial(indonesia.getId(), "Teks Eksplanasi", "Materi baca", "Bab 2", "Ringkasan"));

        AppShell shell = new AppShell(new Workspace(environment.auth, environment.connections,
                environment.learning, teacher), () -> { });
        shell.showPage("materials");
        MaterialPage page = (MaterialPage) shell.page("materials");
        assertEquals(2, page.table.table().getRowCount());

        JComboBox<String> filter = filterCombo(page);
        assertNotNull(filter, "Filter mata pelajaran harus tersedia.");
        filter.setSelectedItem("Matematika");
        assertEquals(1, page.table.table().getRowCount());
        assertEquals("Persamaan Linear", page.table.table().getValueAt(0, 0));

        filter.setSelectedItem("Semua mata pelajaran");
        assertEquals(2, page.table.table().getRowCount());
    }

    @Test void theStudentSeesTheSameTasksAsTheTeacher() {
        Subject math = environment.learning.saveSubject(teacher.token(), student.accountId(),
                new Subject("Matematika", "Aljabar"));
        environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Latihan Aljabar", math.getId(), LocalDate.now().plusDays(2)));

        AppShell teacherShell = new AppShell(new Workspace(environment.auth, environment.connections,
                environment.learning, teacher), () -> { });
        teacherShell.showPage("tasks");
        AppShell studentShell = new AppShell(new Workspace(environment.auth, environment.connections,
                environment.learning, student), () -> { });
        studentShell.showPage("tasks");

        JTable teacherTable = ((TaskPage) teacherShell.page("tasks")).table.table();
        JTable studentTable = ((TaskPage) studentShell.page("tasks")).table.table();
        assertEquals(1, teacherTable.getRowCount());
        assertEquals(1, studentTable.getRowCount());
        assertEquals(teacherTable.getValueAt(0, 0), studentTable.getValueAt(0, 0));
    }

    @Test void theStudentCannotReachTheTeacherOnlyPages() {
        AppShell shell = new AppShell(new Workspace(environment.auth, environment.connections,
                environment.learning, student), () -> { });
        assertTrue(shell.page("requests") == null);
        assertTrue(shell.page("invitations") == null);
        assertNotNull(shell.page("connections"));
        for (String id : List.of("dashboard", "subjects", "schedule", "materials", "tasks", "goals",
                "evaluations", "progress", "teachers", "students", "profile", "connections")) {
            shell.showPage(id);
            assertEquals(id, shell.currentPage());
        }
    }

    /** Finds the dropdown filter of a page. */
    @SuppressWarnings("unchecked")
    private JComboBox<String> filterCombo(javax.swing.JComponent root) {
        for (java.awt.Component child : root.getComponents()) {
            if (child instanceof JComboBox<?> combo) return (JComboBox<String>) combo;
            if (child instanceof javax.swing.JComponent component) {
                JComboBox<String> found = filterCombo(component);
                if (found != null) return found;
            }
        }
        return null;
    }
}
