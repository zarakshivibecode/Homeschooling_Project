package id.edu.homeschool.service;

import id.edu.homeschool.model.LearningTask;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.model.LearningMaterial;
import id.edu.homeschool.model.Evaluation;
import java.time.LocalDate;
import id.edu.homeschool.repository.ApplicationRepositories;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class LearningWorkspaceServiceTest {
    @TempDir Path temp;
    private ApplicationRepositories repos;
    private AuthService auth;
    private ConnectionService connections;
    private LearningWorkspaceService workspace;
    private UserSession teacher, otherTeacher, student, otherStudent;

    @BeforeEach void setup() {
        repos = new ApplicationRepositories(temp);
        SessionRegistry registry = new SessionRegistry(repos.accounts());
        auth = new AuthService(repos.accounts(), registry);
        connections = new ConnectionService(repos.accounts(), repos.invitations(), repos.connectionRequests(), repos.connections(), registry);
        workspace = new LearningWorkspaceService(repos, registry);
        auth.registerTeacher("Teacher One", "one@example.test", "TeacherPassword2040".toCharArray());
        auth.registerTeacher("Teacher Two", "two@example.test", "TeacherPassword2041".toCharArray());
        auth.registerStudent("Student One", "student@example.test", "StudentPassword2040".toCharArray(), "SMA");
        auth.registerStudent("Student Two", "other@example.test", "StudentPassword2042".toCharArray(), "SMA");
        teacher = auth.login("one@example.test", "TeacherPassword2040".toCharArray());
        otherTeacher = auth.login("two@example.test", "TeacherPassword2041".toCharArray());
        student = auth.login("student@example.test", "StudentPassword2040".toCharArray());
        otherStudent = auth.login("other@example.test", "StudentPassword2042".toCharArray());
        var request = connections.requestConnection(student.token(), connections.createInvitation(teacher.token()).code());
        connections.approve(teacher.token(), request.id());
    }

    @Test void teacherCanCreateAndStudentCanOnlySeeOwnedLearningRecords() {
        Subject subject = workspace.saveSubject(teacher.token(), student.accountId(), new Subject("Matematika", "Aljabar"));
        LearningTask task = workspace.saveTask(teacher.token(), student.accountId(), new LearningTask("Latihan", subject.getId(), LocalDate.now().plusDays(2)));
        assertEquals(1, workspace.tasks(student.token(), student.accountId()).size());
        assertThrows(SecurityException.class, () -> workspace.tasks(otherStudent.token(), student.accountId()));
        assertThrows(SecurityException.class, () -> workspace.saveSubject(otherTeacher.token(), student.accountId(), new Subject("Fisika", "")));
        assertThrows(SecurityException.class, () -> workspace.setTaskCompleted(otherStudent.token(), task.getId(), true));
        workspace.setTaskCompleted(student.token(), task.getId(), true);
        assertTrue(workspace.tasks(student.token(), student.accountId()).getFirst().isCompleted());
    }

    @Test void legacyRecordsWithoutGrantStayInvisible() {
        repos.tasks().save(new LearningTask("Legacy", "subject-legacy", LocalDate.now()));
        assertTrue(workspace.tasks(student.token(), student.accountId()).isEmpty());
    }

    @Test void legacyIdsCannotBeClaimedAndMaterialAndEvaluationPersistWithinScope() {
        Subject legacy = repos.subjects().save(new Subject("Data lama", ""));
        assertThrows(SecurityException.class, () -> workspace.saveSubject(teacher.token(), student.accountId(), legacy));
        Subject subject = workspace.saveSubject(teacher.token(), student.accountId(), new Subject("Sains", "Eksperimen"));
        var material = workspace.saveMaterial(teacher.token(), student.accountId(), new LearningMaterial(subject.getId(), "Bab 1", "PDF", "https://example.test", "Baca"));
        var evaluation = workspace.saveEvaluation(teacher.token(), student.accountId(), new Evaluation(subject.getId(), LocalDate.now(), 18, 20, "Baik"));
        assertEquals(material.getId(), workspace.materials(student.token(), student.accountId()).getFirst().getId());
        assertEquals(evaluation.getId(), workspace.evaluations(teacher.token(), student.accountId()).getFirst().getId());
        // Session tokens are process-local, so verify the persisted access map and record files directly after reload.
        var persisted = new ApplicationRepositories(temp);
        assertTrue(persisted.learningAccessGrants().findAll().stream().anyMatch(g -> g.getResourceId().equals(material.getId())));
        assertEquals(material.getId(), persisted.materials().findById(material.getId()).orElseThrow().getId());
    }
}
