package id.edu.homeschool.ui;

import id.edu.homeschool.model.StudySession;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.service.UserSession;
import id.edu.homeschool.support.TestEnvironment;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudySessionDurationChartTest {

    @TempDir Path temp;
    private TestEnvironment environment;
    private UserSession teacher;
    private UserSession student;
    private Subject math;

    @BeforeEach
    void setUp() {
        environment = new TestEnvironment(temp);
        teacher = environment.registerTeacher("Pengajar", "guru@sessions.test", "GuruPassword2040");
        student = environment.registerStudent("Siswa", "siswa@sessions.test", "SiswaPassword2040", "SMA");
        environment.connect(teacher, student);
        math = environment.learning.saveSubject(teacher.token(), student.accountId(),
                new Subject("Matematika", "Aljabar"));
    }

    @Test
    void aggregatesStoredDurationsByDayAndExcludesFutureSchedule() {
        LocalDate recent = LocalDate.now().minusDays(2);
        saveSession(recent, 45);
        saveSession(recent, 30);
        saveSession(LocalDate.now().minusDays(8), 60);
        saveSession(LocalDate.now().plusDays(1), 120);

        StudySessionDurationChart chart = new StudySessionDurationChart(workspace());

        assertEquals(3, chart.visibleSessions().size());
        assertEquals(75, chart.buckets().getLast().durationMinutes());
        assertEquals(2, chart.buckets().getLast().sessionCount());
        assertTrue(chart.summaryText().contains("tidak menunjukkan durasi aktual"));
        paint(chart);
    }

    @Test
    void periodFilterChangesTheAggregationToWeeks() {
        LocalDate recent = LocalDate.now().with(DayOfWeek.MONDAY).minusDays(63).plusDays(1);
        saveSession(recent, 40);
        saveSession(recent.plusDays(2), 50);

        StudySessionDurationChart chart = new StudySessionDurationChart(workspace());
        List<JComboBox<?>> filters = findCombos(chart);
        assertEquals(2, filters.size());
        filters.get(1).setSelectedIndex(2);

        assertEquals(1, chart.buckets().size());
        assertEquals(90, chart.buckets().getFirst().durationMinutes());
        assertEquals(2, chart.buckets().getFirst().sessionCount());
    }

    @Test
    void futureOnlyDataShowsAnEmptyStateInsteadOfAHistoricalBar() {
        saveSession(LocalDate.now().plusDays(1), 60);
        StudySessionDurationChart chart = new StudySessionDurationChart(workspace());

        assertTrue(chart.visibleSessions().isEmpty());
        assertTrue(chart.buckets().isEmpty());
        assertFalse(chart.summaryText().contains("60 menit"));
        paint(chart);
    }

    private void saveSession(LocalDate date, int minutes) {
        environment.learning.saveSession(teacher.token(), student.accountId(),
                new StudySession(math.getId(), date, LocalTime.of(9, 0), minutes, ""));
    }

    private Workspace workspace() {
        return new Workspace(environment.auth, environment.connections, environment.learning, student);
    }

    private static void paint(StudySessionDurationChart chart) {
        chart.setSize(900, 320);
        chart.doLayout();
        BufferedImage image = new BufferedImage(900, 320, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            chart.paint(graphics);
        } finally {
            graphics.dispose();
        }
    }

    private static List<JComboBox<?>> findCombos(Container parent) {
        List<JComboBox<?>> result = new ArrayList<>();
        for (Component component : parent.getComponents()) {
            if (component instanceof JComboBox<?> combo) result.add(combo);
            if (component instanceof Container container) result.addAll(findCombos(container));
        }
        return result;
    }
}
