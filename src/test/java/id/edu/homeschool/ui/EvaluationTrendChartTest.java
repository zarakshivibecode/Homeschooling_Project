package id.edu.homeschool.ui;

import id.edu.homeschool.model.Evaluation;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.service.UserSession;
import id.edu.homeschool.support.TestEnvironment;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationTrendChartTest {

    @TempDir Path temp;
    private TestEnvironment environment;
    private UserSession teacher;
    private UserSession student;
    private Subject math;

    @BeforeEach
    void setUp() {
        environment = new TestEnvironment(temp);
        teacher = environment.registerTeacher("Pengajar", "guru@chart.test", "GuruPassword2040");
        student = environment.registerStudent("Siswa", "siswa@chart.test", "SiswaPassword2040", "SMA");
        environment.connect(teacher, student);
        math = environment.learning.saveSubject(teacher.token(), student.accountId(),
                new Subject("Matematika", "Aljabar"));
    }

    @Test
    void sortsEvaluationsAndComparesNormalizedPercentages() {
        Evaluation later = new Evaluation(math.getId(), LocalDate.of(2025, 3, 10), 8, 10, "");
        Evaluation earlier = new Evaluation(math.getId(), LocalDate.of(2025, 2, 10), 60, 100, "");
        environment.learning.saveEvaluation(teacher.token(), student.accountId(), later);
        environment.learning.saveEvaluation(teacher.token(), student.accountId(), earlier);

        EvaluationTrendChart chart = new EvaluationTrendChart(workspace());

        assertEquals(List.of(earlier.getId(), later.getId()),
                chart.visibleEvaluations().stream().map(Evaluation::getId).toList());
        assertEquals(60.0, chart.visibleEvaluations().getFirst().getPercentage());
        assertEquals(80.0, chart.visibleEvaluations().getLast().getPercentage());
        assertTrue(chart.trendSummaryText().contains("meningkat"));
        paint(chart);
    }

    @Test
    void subjectAndPeriodFiltersOnlyShowMatchingPersistedEvaluations() {
        Subject science = environment.learning.saveSubject(teacher.token(), student.accountId(),
                new Subject("IPA", "Sains"));
        Evaluation recentMath = new Evaluation(math.getId(), LocalDate.now().minusDays(5), 80, 100, "");
        Evaluation oldScience = new Evaluation(science.getId(), LocalDate.now().minusDays(80), 90, 100, "");
        environment.learning.saveEvaluation(teacher.token(), student.accountId(), recentMath);
        environment.learning.saveEvaluation(teacher.token(), student.accountId(), oldScience);

        EvaluationTrendChart chart = new EvaluationTrendChart(workspace());
        List<JComboBox<?>> filters = findCombos(chart);
        assertEquals(2, filters.size());

        filters.get(0).setSelectedIndex(1);
        assertEquals(1, chart.visibleEvaluations().size());

        filters.get(0).setSelectedIndex(0);
        filters.get(1).setSelectedIndex(1);
        assertEquals(List.of(recentMath.getId()),
                chart.visibleEvaluations().stream().map(Evaluation::getId).toList());
    }

    @Test
    void emptyAndSingleEvaluationDoNotClaimATrend() {
        EvaluationTrendChart chart = new EvaluationTrendChart(workspace());
        assertTrue(chart.visibleEvaluations().isEmpty());
        assertFalse(findCombos(chart).get(1).isEnabled());
        assertTrue(findLabels(chart).stream().anyMatch(label -> label.getText() != null
                && label.getText().contains("Belum ada evaluasi")));
        assertFalse(chart.trendSummaryText().contains("meningkat"));

        Evaluation only = new Evaluation(math.getId(), LocalDate.now(), 75, 100, "");
        environment.learning.saveEvaluation(teacher.token(), student.accountId(), only);
        chart.refresh();
        assertEquals(1, chart.visibleEvaluations().size());
        assertFalse(findCombos(chart).get(1).isEnabled());
        assertTrue(chart.trendSummaryText().contains("belum dapat disimpulkan"));
        paint(chart);
    }

    private Workspace workspace() {
        return new Workspace(environment.auth, environment.connections, environment.learning, student);
    }

    private static void paint(JComponent component) {
        component.setSize(900, 320);
        component.doLayout();
        BufferedImage image = new BufferedImage(900, 320, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            component.paint(graphics);
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

    private static List<JLabel> findLabels(Container parent) {
        List<JLabel> result = new ArrayList<>();
        for (Component component : parent.getComponents()) {
            if (component instanceof JLabel label) result.add(label);
            if (component instanceof Container container) result.addAll(findLabels(container));
        }
        return result;
    }
}
