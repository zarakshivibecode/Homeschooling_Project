package id.edu.homeschool.service;

import id.edu.homeschool.model.Evaluation;
import id.edu.homeschool.model.LearningGoal;
import id.edu.homeschool.model.LearningMaterial;
import id.edu.homeschool.model.LearningTask;
import id.edu.homeschool.model.StudySession;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.support.TestEnvironment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Learning records: creation, editing, deletion, visibility rules and persistence. */
class LearningWorkspaceFlowTest {

    @TempDir Path temp;
    private TestEnvironment environment;
    private UserSession teacher;
    private UserSession otherTeacher;
    private UserSession student;
    private UserSession otherStudent;
    private Subject subject;

    @BeforeEach void setUp() {
        environment = new TestEnvironment(temp);
        teacher = environment.registerTeacher("Pengajar Satu", "guru1@uji.test", "GuruPassword2040");
        otherTeacher = environment.registerTeacher("Pengajar Dua", "guru2@uji.test", "GuruPassword2041");
        student = environment.registerStudent("Siswa Satu", "siswa@uji.test", "SiswaPassword2040", "SMA");
        otherStudent = environment.registerStudent("Siswa Dua", "siswa2@uji.test", "SiswaPassword2042", "SMP");
        environment.connect(teacher, student);
        subject = environment.learning.saveSubject(teacher.token(), student.accountId(), new Subject("Matematika", "Aljabar"));
    }

    @Test void teacherCreatesRecordsAndStudentSeesThem() {
        LearningMaterial material = environment.learning.saveMaterial(teacher.token(), student.accountId(),
                new LearningMaterial(subject.getId(), "Bab Satu", "PDF", "https://contoh.test", "Baca"));
        LearningTask task = environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Latihan Aljabar", subject.getId(), LocalDate.now().plusDays(3)));
        StudySession session = environment.learning.saveSession(teacher.token(), student.accountId(),
                new StudySession(subject.getId(), LocalDate.now().plusDays(1), LocalTime.of(9, 0), 60, "Pagi"));
        Evaluation evaluation = environment.learning.saveEvaluation(teacher.token(), student.accountId(),
                new Evaluation(subject.getId(), LocalDate.now(), 85, 100, "Baik"));

        assertEquals(1, environment.learning.materials(student.token(), student.accountId()).size());
        assertEquals(1, environment.learning.tasks(student.token(), student.accountId()).size());
        assertEquals(1, environment.learning.sessions(student.token(), student.accountId()).size());
        assertEquals(1, environment.learning.evaluations(student.token(), student.accountId()).size());
        assertEquals(material.getId(), environment.learning.materials(student.token(), student.accountId()).getFirst().getId());
        assertEquals(task.getId(), environment.learning.tasks(student.token(), student.accountId()).getFirst().getId());
        assertEquals(session.getId(), environment.learning.sessions(student.token(), student.accountId()).getFirst().getId());
        assertEquals(evaluation.getId(), environment.learning.evaluations(student.token(), student.accountId()).getFirst().getId());
        assertEquals(student.accountId(), environment.learning.sessions(student.token(), student.accountId()).getFirst().getStudentId());
    }

    @Test void studentCannotReadAnotherStudentAndCannotWriteAnything() {
        environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Tugas Rahasia", subject.getId(), LocalDate.now().plusDays(1)));

        assertThrows(SecurityException.class, () -> environment.learning.tasks(otherStudent.token(), student.accountId()));
        assertThrows(SecurityException.class, () -> environment.learning.subjects(otherStudent.token(), student.accountId()));
        assertThrows(SecurityException.class, () -> environment.learning.evaluations(otherStudent.token(), student.accountId()));
        assertThrows(SecurityException.class, () -> environment.learning.saveSubject(student.token(), student.accountId(),
                new Subject("Milik Sendiri", "")));
        assertThrows(SecurityException.class, () -> environment.learning.delete(student.token(), student.accountId(),
                LearningWorkspaceService.TYPE_SUBJECT, subject.getId()));
    }

    @Test void teacherWithoutConnectionCannotAccessStudentData() {
        assertThrows(SecurityException.class,
                () -> environment.learning.subjects(otherTeacher.token(), student.accountId()));
        assertThrows(SecurityException.class,
                () -> environment.learning.saveSubject(otherTeacher.token(), student.accountId(), new Subject("Fisika", "")));
        assertThrows(SecurityException.class,
                () -> environment.learning.delete(otherTeacher.token(), student.accountId(),
                        LearningWorkspaceService.TYPE_SUBJECT, subject.getId()));
    }

    @Test void recordsCanBeEditedAndDeletedAndTheTableSeesTheChange() {
        LearningTask task = environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Judul Lama", subject.getId(), LocalDate.now().plusDays(1)));
        task.setTitle("Judul Baru");
        environment.learning.saveTask(teacher.token(), student.accountId(), task);
        assertEquals("Judul Baru", environment.learning.tasks(teacher.token(), student.accountId()).getFirst().getTitle());

        assertTrue(environment.learning.delete(teacher.token(), student.accountId(),
                LearningWorkspaceService.TYPE_TASK, task.getId()));
        assertTrue(environment.learning.tasks(teacher.token(), student.accountId()).isEmpty());
        assertFalse(environment.repositories.learningAccessGrants().findAll().stream()
                .anyMatch(grant -> grant.getResourceId().equals(task.getId())));
    }

    @Test void subjectInUseCannotBeDeleted() {
        environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Latihan", subject.getId(), LocalDate.now()));
        assertThrows(IllegalStateException.class, () -> environment.learning.delete(teacher.token(), student.accountId(),
                LearningWorkspaceService.TYPE_SUBJECT, subject.getId()));
    }

    @Test void studentOnlyMarksOwnTaskAndOnlyTheCompletionFlag() {
        LearningTask task = environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Latihan", subject.getId(), LocalDate.now()));
        assertThrows(SecurityException.class, () -> environment.learning.setTaskCompleted(otherStudent.token(), task.getId(), true));
        environment.learning.setTaskCompleted(student.token(), task.getId(), true);
        assertTrue(environment.learning.tasks(student.token(), student.accountId()).getFirst().isCompleted());
    }

    @Test void goalAchievementIsTeacherControlled() {
        LearningGoal goal = environment.learning.saveGoal(teacher.token(), student.accountId(),
                new LearningGoal("Kuasai pecahan", LocalDate.now().plusDays(30)));
        assertThrows(SecurityException.class, () -> environment.learning.setGoalAchieved(student.token(), goal.getId(), true));
        environment.learning.setGoalAchieved(teacher.token(), goal.getId(), true);
        assertTrue(environment.learning.goals(student.token(), student.accountId()).getFirst().isAchieved());
    }

    @Test void recordsSurviveApplicationRestart() {
        LearningTask task = environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Tugas Bertahan", subject.getId(), LocalDate.now().plusDays(2)));
        Evaluation evaluation = environment.learning.saveEvaluation(teacher.token(), student.accountId(),
                new Evaluation(subject.getId(), LocalDate.now(), 75, 100, "Cukup"));

        TestEnvironment restarted = new TestEnvironment(temp);
        assertEquals(1, restarted.repositories.tasks().findAll().size());
        assertEquals(task.getId(), restarted.repositories.tasks().findAll().getFirst().getId());
        assertEquals(evaluation.getId(), restarted.repositories.evaluations().findAll().getFirst().getId());
        // One grant per record: the subject from the setup plus the task and the evaluation.
        assertEquals(3, restarted.repositories.learningAccessGrants().findAll().size());
        assertTrue(restarted.repositories.learningAccessGrants().findAll().stream()
                .anyMatch(grant -> grant.getResourceId().equals(task.getId())
                        && grant.getStudentAccountId().equals(student.accountId())));
    }

    @Test void progressReportReflectsRepositoryData() {
        environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Tugas Satu", subject.getId(), LocalDate.now().plusDays(1)));
        LearningTask done = environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Tugas Dua", subject.getId(), LocalDate.now().plusDays(2)));
        environment.learning.setTaskCompleted(student.token(), done.getId(), true);
        environment.learning.saveEvaluation(teacher.token(), student.accountId(),
                new Evaluation(subject.getId(), LocalDate.now(), 90, 100, "Sangat baik"));

        ProgressReport report = environment.learning.progress(teacher.token(), student.accountId());
        assertEquals(1, report.subjectCount());
        assertEquals(2, report.taskCount());
        assertEquals(1, report.completedTaskCount());
        assertEquals(1, report.evaluationCount());
        assertEquals(90.0, report.averagePercent(), 0.01);
        assertEquals(1, report.subjects().size());
        assertEquals("Matematika", report.subjects().getFirst().subjectName());
        assertEquals(50.0, report.taskCompletionPercent(), 0.01);
    }

    @Test void legacyRecordsWithoutGrantStayInvisible() {
        environment.repositories.tasks().save(new LearningTask("Lama", "subjek-lama", LocalDate.now()));
        assertTrue(environment.learning.tasks(student.token(), student.accountId()).isEmpty());
    }
}
