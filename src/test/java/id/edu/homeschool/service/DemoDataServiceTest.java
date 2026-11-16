package id.edu.homeschool.service;

import id.edu.homeschool.model.AccountRole;
import id.edu.homeschool.repository.ApplicationRepositories;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The built-in demo curriculum: it must be created once, must not overwrite existing data and
 * must produce valid relationships between subjects, materials, tasks, schedules and evaluations.
 */
class DemoDataServiceTest {

    @TempDir Path temp;
    private ApplicationRepositories repositories;
    private SessionRegistry sessions;
    private AuthService auth;
    private ConnectionService connections;
    private LearningWorkspaceService learning;
    private DemoDataService demo;

    @BeforeEach void setUp() {
        repositories = new ApplicationRepositories(temp);
        sessions = new SessionRegistry(repositories.accounts());
        auth = new AuthService(repositories.accounts(), sessions);
        connections = new ConnectionService(repositories.accounts(), repositories.invitations(),
                repositories.connectionRequests(), repositories.connections(), sessions);
        learning = new LearningWorkspaceService(repositories, sessions);
        demo = new DemoDataService(repositories, auth, connections, learning);
    }

    @Test void seedingCreatesAccountsConnectionsAndLearningRecords() {
        demo.seedIfNeeded();

        assertEquals(3, demo.demoAccounts().stream()
                .filter(account -> account.role() == AccountRole.PENGAJAR).count());
        assertEquals(4, demo.demoAccounts().stream()
                .filter(account -> account.role() == AccountRole.SISWA).count());

        UserSession teacher = auth.login("rani@demo.test", "DemoGuru2024!".toCharArray());
        UserSession student = auth.login("dimas@demo.test", "DemoSiswa2024!".toCharArray());

        // The teacher can reach three connected students.
        assertEquals(3, learning.connectedStudents(teacher.token()).size());

        // The curriculum of the demo student is complete and internally consistent.
        assertEquals(7, learning.subjects(student.token(), student.accountId()).size());
        assertTrue(learning.materials(student.token(), student.accountId()).size() >= 8);
        assertTrue(learning.tasks(student.token(), student.accountId()).size() >= 6);
        assertTrue(learning.sessions(student.token(), student.accountId()).size() >= 6);
        assertTrue(learning.evaluations(student.token(), student.accountId()).size() >= 6);
        assertTrue(learning.goals(student.token(), student.accountId()).size() >= 4);

        // Every material points at a subject that belongs to the same student.
        var subjectIds = learning.subjects(student.token(), student.accountId()).stream()
                .map(id.edu.homeschool.model.Subject::getId).toList();
        learning.materials(student.token(), student.accountId()).forEach(material ->
                assertTrue(subjectIds.contains(material.getSubjectId()),
                        "Materi harus terhubung ke mata pelajaran siswa yang sama."));
        learning.tasks(student.token(), student.accountId()).forEach(task ->
                assertTrue(subjectIds.contains(task.getSubjectId()),
                        "Tugas harus terhubung ke mata pelajaran siswa yang sama."));
        learning.evaluations(student.token(), student.accountId()).forEach(evaluation ->
                assertTrue(subjectIds.contains(evaluation.getSubjectId()),
                        "Evaluasi harus terhubung ke mata pelajaran siswa yang sama."));
    }

    @Test void everyMaterialCarriesUsableContent() {
        demo.seedIfNeeded();
        UserSession student = auth.login("dimas@demo.test", "DemoSiswa2024!".toCharArray());
        List<id.edu.homeschool.model.LearningMaterial> materials =
                learning.materials(student.token(), student.accountId());
        assertFalse(materials.isEmpty());
        for (id.edu.homeschool.model.LearningMaterial material : materials) {
            assertFalse(material.getTitle().isBlank(), "Judul materi wajib diisi.");
            assertFalse(material.getNotes().isBlank(), "Deskripsi materi wajib diisi.");
            assertTrue(material.getObjectives().length() > 40, "Tujuan pembelajaran materi wajib ada.");
            assertTrue(material.getContent().length() > 200, "Isi materi wajib memuat penjelasan dan latihan.");
        }
    }

    @Test void assignmentsCarryInstructionsAndDifferentStatuses() {
        demo.seedIfNeeded();
        UserSession student = auth.login("dimas@demo.test", "DemoSiswa2024!".toCharArray());
        List<id.edu.homeschool.model.LearningTask> tasks =
                learning.tasks(student.token(), student.accountId());
        assertTrue(tasks.stream().anyMatch(task -> task.isCompleted()));
        assertTrue(tasks.stream().anyMatch(task -> !task.isCompleted()));
        assertTrue(tasks.stream().anyMatch(task -> task.isOverdue(LocalDate.now())));
        for (id.edu.homeschool.model.LearningTask task : tasks) {
            assertFalse(task.getInstructions().isBlank(), "Instruksi tugas wajib diisi.");
            assertNotNull(task.getDueDate());
        }
    }

    @Test void seedingIsNotRepeatedAndUserDataIsNotOverwritten() {
        demo.seedIfNeeded();
        int accountsAfterFirstRun = repositories.accounts().findAll().size();
        int subjectsAfterFirstRun = repositories.subjects().findAll().size();

        // The user deletes one demo record and adds a record of their own.
        UserSession teacher = auth.login("rani@demo.test", "DemoGuru2024!".toCharArray());
        UserSession student = auth.login("dimas@demo.test", "DemoSiswa2024!".toCharArray());
        learning.saveSubject(teacher.token(), student.accountId(),
                new id.edu.homeschool.model.Subject("Mata Pelajaran Buatan_User", "Dibuat manual"));
        int subjectsBeforeSecondRun = repositories.subjects().findAll().size();

        demo.seedIfNeeded();

        assertEquals(accountsAfterFirstRun, repositories.accounts().findAll().size(),
                "Akun demo tidak boleh dibuat dua kali.");
        assertEquals(subjectsBeforeSecondRun, repositories.subjects().findAll().size(),
                "Mata pelajaran pengguna tidak boleh ditimpa oleh data demo.");
        assertTrue(subjectsAfterFirstRun < subjectsBeforeSecondRun);
        assertTrue(repositories.subjects().findAll().stream()
                .anyMatch(subject -> subject.getName().equals("Mata Pelajaran Buatan_User")));
    }

    @Test void demoRecordsAreMarkedSoTheyCanBeToldApart() {
        demo.seedIfNeeded();
        UserSession student = auth.login("dimas@demo.test", "DemoSiswa2024!".toCharArray());
        var material = learning.materials(student.token(), student.accountId()).getFirst();
        assertTrue(demo.isDemoResource(LearningWorkspaceService.TYPE_MATERIAL, material.getId()),
                "Materi demo harus ditandai pada berkas penanda.");
        assertTrue(demo.isDemoAccount(student.accountId()), "Akun demo harus ditandai.");
        assertTrue(java.nio.file.Files.exists(temp.resolve("demo-data.json")));
    }

    @Test void aStudentCannotReachAnotherStudentThroughTheDemoCurriculum() {
        demo.seedIfNeeded();
        UserSession first = auth.login("dimas@demo.test", "DemoSiswa2024!".toCharArray());
        UserSession second = auth.login("nadia@demo.test", "DemoSiswa2024!".toCharArray());
        assertThrows(SecurityException.class,
                () -> learning.tasks(second.token(), first.accountId()),
                "Siswa lain tidak boleh membaca data siswa pertama.");
        assertThrows(SecurityException.class,
                () -> learning.materials(second.token(), first.accountId()));
        assertThrows(SecurityException.class,
                () -> learning.evaluations(second.token(), first.accountId()));
        assertTrue(learning.tasks(first.token(), first.accountId()).size() > 0);
    }
}
