package id.edu.homeschool.service;

import id.edu.homeschool.model.Evaluation;
import id.edu.homeschool.model.LearningTask;
import id.edu.homeschool.model.StudySession;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.support.TestEnvironment;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Activity feed, target student lookup and the progress narrative. */
class LearningActivityTest {

    @TempDir Path temp;
    private TestEnvironment environment;
    private UserSession teacher;
    private UserSession student;
    private UserSession otherStudent;
    private Subject subject;

    @BeforeEach void setUp() {
        environment = new TestEnvironment(temp);
        teacher = environment.registerTeacher("Pengajar Satu", "guru1@uji.test", "GuruPassword2040");
        student = environment.registerStudent("Siswa Satu", "siswa@uji.test", "SiswaPassword2040", "SMA");
        otherStudent = environment.registerStudent("Siswa Dua", "siswa2@uji.test", "SiswaPassword2042", "SMP");
        environment.connect(teacher, student);
        subject = environment.learning.saveSubject(teacher.token(), student.accountId(),
                new Subject("Matematika", "Aljabar"));
    }

    @Test void activityFeedIsBuiltFromTheStoredRecords() {
        environment.learning.saveSession(teacher.token(), student.accountId(),
                new StudySession(subject.getId(), LocalDate.now(), LocalTime.of(9, 0), 60, "Sesi pagi"));
        environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Latihan", subject.getId(), LocalDate.now().plusDays(2)));
        environment.learning.saveEvaluation(teacher.token(), student.accountId(),
                new Evaluation(subject.getId(), LocalDate.now().minusDays(1), 88, 100, "Bagus"));

        List<ActivityEntry> entries = environment.learning.recentActivity(student.token(), student.accountId(), 10);
        assertEquals(3, entries.size());
        assertTrue(entries.stream().allMatch(entry -> entry.title() != null && !entry.title().isBlank()));
        // The feed is sorted from the newest event to the oldest one.
        assertTrue(entries.get(0).when().isAfter(entries.get(entries.size() - 1).when())
                || entries.get(0).when().isEqual(entries.get(entries.size() - 1).when()));
    }

    @Test void activityFeedIsEmptyForAStudentWithoutRecords() {
        assertTrue(environment.learning.recentActivity(student.token(), student.accountId(), 5).isEmpty());
    }

    @Test void targetStudentIsResolvedFromTheOwnershipGrant() {
        LearningTask task = environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Latihan", subject.getId(), LocalDate.now().plusDays(2)));
        assertEquals("Siswa Satu",
                environment.learning.targetStudentName(teacher.token(), LearningWorkspaceService.TYPE_TASK, task.getId()));
        assertEquals("Siswa Satu",
                environment.learning.targetStudentName(student.token(), LearningWorkspaceService.TYPE_TASK, task.getId()));
    }

    @Test void targetStudentCannotBeReadByAnotherStudent() {
        LearningTask task = environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Latihan", subject.getId(), LocalDate.now().plusDays(2)));
        assertThrows(SecurityException.class, () -> environment.learning.targetStudentName(
                otherStudent.token(), LearningWorkspaceService.TYPE_TASK, task.getId()));
    }

    @Test void progressReportProducesAReadableSummary() {
        environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Latihan", subject.getId(), LocalDate.now().plusDays(2)));
        environment.learning.saveEvaluation(teacher.token(), student.accountId(),
                new Evaluation(subject.getId(), LocalDate.now(), 90, 100, "Sangat baik"));

        ProgressReport report = environment.learning.progress(student.token(), student.accountId());
        String summary = report.summary();
        assertTrue(summary.contains("mata pelajaran"), "Ringkasan harus menjelaskan jumlah mata pelajaran.");
        assertTrue(summary.contains("tugas"), "Ringkasan harus menjelaskan jumlah tugas.");
        assertFalse(summary.isBlank());
    }

    @Test void directorySummariesFollowTheConnectedPeople() {
        environment.learning.saveTask(teacher.token(), student.accountId(),
                new LearningTask("Latihan", subject.getId(), LocalDate.now().plusDays(2)));

        List<PersonSummary> students = environment.learning.studentSummaries(teacher.token());
        assertEquals(1, students.size());
        assertEquals("Siswa Satu", students.getFirst().name());
        assertEquals(1, students.getFirst().taskCount());

        List<PersonSummary> teachers = environment.learning.teacherSummaries(student.token());
        assertEquals(1, teachers.size());
        assertEquals("Pengajar Satu", teachers.getFirst().name());
    }
}
