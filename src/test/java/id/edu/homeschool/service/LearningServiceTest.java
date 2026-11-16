package id.edu.homeschool.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import id.edu.homeschool.model.Evaluation;
import id.edu.homeschool.model.LearningGoal;
import id.edu.homeschool.model.LearningTask;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.repository.ApplicationRepositories;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LearningServiceTest {
    @TempDir Path directory;

    @Test void producesDashboardAndFilteredProgressFromRepositories() {
        LearningService service = new LearningService(new ApplicationRepositories(directory));
        Subject subject = service.saveSubject(new Subject("Sains", "Eksperimen"));
        LearningTask task = new LearningTask("Laporan", subject.getId(), LocalDate.now().plusDays(2));
        task.setCompleted(true); service.saveTask(task);
        LearningGoal goal = new LearningGoal("Selesaikan bab satu", LocalDate.now().plusDays(3));
        goal.setAchieved(true); service.saveGoal(goal);
        service.saveEvaluation(new Evaluation(subject.getId(), LocalDate.now(), 8, 10, "Kuis"));

        DashboardSummary dashboard = service.dashboard();
        ProgressSummary progress = service.progress(subject.getId(), LocalDate.now().minusDays(1), LocalDate.now().plusDays(7));
        assertEquals(1, dashboard.subjectCount());
        assertEquals(1, dashboard.achievedGoals());
        assertEquals(80.0, dashboard.averageScorePercent(), 0.0001);
        assertEquals(1, progress.completedTasks());
        assertEquals(80.0, progress.averagePercent(), 0.0001);
        assertThrows(IllegalArgumentException.class,
                () -> service.progress(null, LocalDate.now(), LocalDate.now().minusDays(1)));
    }
}
