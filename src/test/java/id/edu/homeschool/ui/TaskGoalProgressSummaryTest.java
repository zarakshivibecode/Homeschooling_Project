package id.edu.homeschool.ui;

import id.edu.homeschool.model.LearningGoal;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskGoalProgressSummaryTest {

    @TempDir Path temp;
    private TestEnvironment environment;
    private UserSession teacher;
    private UserSession student;
    private Subject math;

    @BeforeEach
    void setUp() {
        environment = new TestEnvironment(temp);
        teacher = environment.registerTeacher("Pengajar", "guru@summary.test", "GuruPassword2040");
        student = environment.registerStudent("Siswa", "siswa@summary.test", "SiswaPassword2040", "SMA");
        environment.connect(teacher, student);
        math = environment.learning.saveSubject(teacher.token(), student.accountId(),
                new Subject("Matematika", "Aljabar"));
    }

    @Test
    void taskSummaryUsesCurrentCompletionAndOverdueStates() {
        LearningTask done = task("Selesai", LocalDate.now().minusDays(1), true);
        LearningTask upcoming = task("Berjalan", LocalDate.now().plusDays(2), false);
        LearningTask late = task("Terlambat", LocalDate.now().minusDays(2), false);
        environment.learning.saveTask(teacher.token(), student.accountId(), done);
        environment.learning.saveTask(teacher.token(), student.accountId(), upcoming);
        environment.learning.saveTask(teacher.token(), student.accountId(), late);

        TaskStatusSummary summary = new TaskStatusSummary(workspace());

        assertEquals(1, summary.completedCount());
        assertEquals(1, summary.pendingCount());
        assertEquals(1, summary.overdueCount());
        assertEquals(3, summary.totalCount());
    }

    @Test
    void goalSummaryReportsOnlyThePersistedAchievedRatio() {
        LearningGoal achieved = new LearningGoal("Menguasai aljabar", LocalDate.now().plusDays(20));
        achieved.setAchieved(true);
        environment.learning.saveGoal(teacher.token(), student.accountId(), achieved);
        environment.learning.saveGoal(teacher.token(), student.accountId(),
                new LearningGoal("Mempelajari geometri", LocalDate.now().plusDays(30)));

        GoalProgressSummary summary = new GoalProgressSummary(workspace());

        assertEquals(1, summary.achievedCount());
        assertEquals(2, summary.totalCount());
        assertTrue(summary.summaryText().contains("50%"));
        assertTrue(summary.disclaimerText().contains("progres parsial dan histori tidak tersedia"));
    }

    @Test
    void emptySummariesDoNotInventProgress() {
        TaskStatusSummary tasks = new TaskStatusSummary(workspace());
        GoalProgressSummary goals = new GoalProgressSummary(workspace());

        assertEquals(0, tasks.totalCount());
        assertEquals(0, goals.totalCount());
        assertEquals(0, goals.achievedCount());
    }

    private LearningTask task(String title, LocalDate dueDate, boolean completed) {
        LearningTask task = new LearningTask(title, math.getId(), dueDate);
        task.setCompleted(completed);
        return task;
    }

    private Workspace workspace() {
        return new Workspace(environment.auth, environment.connections, environment.learning, student);
    }
}
