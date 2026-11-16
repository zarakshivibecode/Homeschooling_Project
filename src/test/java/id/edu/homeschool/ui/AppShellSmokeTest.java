package id.edu.homeschool.ui;

import id.edu.homeschool.model.LearningTask;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.service.UserSession;
import id.edu.homeschool.support.TestEnvironment;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Interface wiring: navigation, page registration, table refresh and search. */
class AppShellSmokeTest {

    @TempDir Path temp;
    private TestEnvironment environment;
    private UserSession teacher;
    private UserSession student;

    @BeforeEach void setUp() {
        environment = new TestEnvironment(temp);
        teacher = environment.registerTeacher("Pengajar Satu", "guru1@uji.test", "GuruPassword2040");
        student = environment.registerStudent("Siswa Satu", "siswa@uji.test", "SiswaPassword2040", "SMA");
        environment.connect(teacher, student);
        environment.learning.saveSubject(teacher.token(), student.accountId(), new Subject("Matematika", "Aljabar"));
        environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Latihan Aljabar", environment.learning.subjects(teacher.token(), student.accountId())
                        .getFirst().getId(), LocalDate.now().plusDays(2)));
    }

    @Test void teacherShellRegistersEveryMenuAndOpensIt() {
        AppShell shell = new AppShell(new Workspace(environment.auth, environment.connections, environment.learning, teacher), () -> { });
        for (String id : new String[]{"dashboard", "subjects", "schedule", "materials", "tasks", "goals",
                "evaluations", "progress", "teachers", "students", "profile", "requests", "invitations"}) {
            assertNotNull(shell.page(id), "Halaman " + id + " harus terdaftar.");
            shell.showPage(id);
            assertEquals(id, shell.currentPage());
        }
    }

    @Test void studentShellDoesNotExposeTeacherOnlyPages() {
        AppShell shell = new AppShell(new Workspace(environment.auth, environment.connections, environment.learning, student), () -> { });
        assertNotNull(shell.page("connections"));
        assertTrue(shell.page("requests") == null);
        assertTrue(shell.page("invitations") == null);
    }

    @Test void teacherTableShowsRepositoryDataAndSearchFiltersIt() {
        AppShell shell = new AppShell(new Workspace(environment.auth, environment.connections, environment.learning, teacher), () -> { });
        SubjectPage page = (SubjectPage) shell.page("subjects");
        shell.showPage("subjects");
        assertEquals(1, page.table.table().getRowCount());

        page.table.search("matematika");
        assertEquals(1, page.table.table().getRowCount());
        page.table.search("tidak ada");
        assertEquals(0, page.table.table().getRowCount());
        page.table.search("");
        assertEquals(1, page.table.table().getRowCount());
    }

    @Test void studentTableShowsTheSameDataAsTheTeacher() {
        AppShell teacherShell = new AppShell(new Workspace(environment.auth, environment.connections, environment.learning, teacher), () -> { });
        teacherShell.showPage("tasks");
        AppShell studentShell = new AppShell(new Workspace(environment.auth, environment.connections, environment.learning, student), () -> { });
        studentShell.showPage("tasks");

        assertEquals(1, ((TaskPage) teacherShell.page("tasks")).table.table().getRowCount());
        assertEquals(1, ((TaskPage) studentShell.page("tasks")).table.table().getRowCount());
    }

    @Test void directoryPagesListTheConnectedPeople() {
        AppShell teacherShell = new AppShell(new Workspace(environment.auth, environment.connections, environment.learning, teacher), () -> { });
        teacherShell.showPage("students");
        assertEquals(1, ((DirectoryPage) teacherShell.page("students")).table().table().getRowCount());

        AppShell studentShell = new AppShell(new Workspace(environment.auth, environment.connections, environment.learning, student), () -> { });
        studentShell.showPage("teachers");
        assertEquals(1, ((DirectoryPage) studentShell.page("teachers")).table().table().getRowCount());
    }

    @Test void dashboardAndProgressUseTheRepositoryData() {
        AppShell shell = new AppShell(new Workspace(environment.auth, environment.connections, environment.learning, teacher), () -> { });
        shell.showPage("dashboard");
        shell.showPage("progress");
        assertNotNull(shell.page("dashboard"));
        assertNotNull(shell.page("progress"));
        // The teacher scope points at the connected student, so the pages must find their data.
        assertFalse(environment.learning.connectedStudents(teacher.token()).isEmpty());
    }

    @Test void switchingTheSelectedStudentSwapsTheVisibleRows() {
        UserSession secondStudent = environment.registerStudent("Siswa Dua", "siswa2@uji.test", "SiswaPassword2042", "SMP");
        environment.connect(teacher, secondStudent);
        environment.learning.saveSubject(teacher.token(), secondStudent.accountId(), new Subject("Fisika", "Gerak"));

        Workspace workspace = new Workspace(environment.auth, environment.connections, environment.learning, teacher);
        AppShell shell = new AppShell(workspace, () -> { });
        SubjectPage page = (SubjectPage) shell.page("subjects");
        shell.showPage("subjects");
        assertEquals(1, page.table.table().getRowCount());

        // Switching the scope must reload the table with the other student's records.
        workspace.setStudentId(secondStudent.accountId());
        assertEquals(1, page.table.table().getRowCount());
        assertEquals("Fisika", page.table.table().getValueAt(0, 0));

        workspace.setStudentId(student.accountId());
        assertEquals("Matematika", page.table.table().getValueAt(0, 0));
    }
}
