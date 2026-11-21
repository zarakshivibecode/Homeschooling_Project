package id.edu.homeschool.ui;

import id.edu.homeschool.model.LearningGoal;
import id.edu.homeschool.service.LearningWorkspaceService;
import id.edu.homeschool.ui.design.ProgressTrack;
import id.edu.homeschool.ui.design.StatusBadge;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** An honest ratio of goals currently achieved; this model has no partial progress history. */
final class GoalProgressSummary extends JPanel {

    private final Workspace workspace;
    private final JPanel heading = Ui.transparent(new BorderLayout(Theme.SPACE_3, 0));
    private final JLabel summary = Ui.muted("Status target belajar saat ini.");
    private final JPanel footer = Ui.transparent(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_2, 0));
    private final ProgressTrack progress = new ProgressTrack();
    private int achieved;
    private int total;
    private JComponent demoNotice;

    GoalProgressSummary(Workspace workspace) {
        super(new BorderLayout());
        this.workspace = workspace;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SPACE_3, 0));

        JPanel card = Ui.card();
        card.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_3, Theme.SPACE_4,
                Theme.SPACE_3, Theme.SPACE_4));
        JLabel title = Ui.label("Pencapaian target belajar");
        title.setFont(Theme.FONT_14_SEMIBOLD);
        heading.add(title, BorderLayout.WEST);
        footer.add(Ui.subtle("Hanya status tercapai yang tersimpan; progres parsial dan histori tidak tersedia."));

        JPanel body = Ui.transparent(new BorderLayout(0, Theme.SPACE_2));
        body.add(summary, BorderLayout.NORTH);
        body.add(progress, BorderLayout.CENTER);
        body.add(footer, BorderLayout.SOUTH);
        card.add(heading, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
        refresh();
    }

    void refresh() {
        achieved = 0;
        total = 0;
        setDemoNotice(null);
        if (!workspace.hasScope()) {
            summary.setText("Pilih siswa untuk melihat target belajar.");
            progress.setProgress(0, 0, Theme.ACCENT);
            return;
        }
        try {
            List<LearningGoal> goals = workspace.learning().goals(
                    workspace.session().token(), workspace.studentId());
            total = goals.size();
            achieved = (int) goals.stream().filter(LearningGoal::isAchieved).count();
            if (total == 0) {
                summary.setText("Belum ada target belajar pada siswa ini.");
                progress.setProgress(0, 0, Theme.ACCENT);
                return;
            }
            int percentage = (int) Math.round(achieved * 100.0 / total);
            summary.setText(achieved + " dari " + total + " target tercapai (" + percentage + "%).");
            progress.setProgress(achieved, total, Theme.SUCCESS);
            if (goals.stream().anyMatch(goal ->
                    workspace.isDemo(LearningWorkspaceService.TYPE_GOAL, goal.getId()))) {
                boolean allDemo = goals.stream().allMatch(goal ->
                        workspace.isDemo(LearningWorkspaceService.TYPE_GOAL, goal.getId()));
                setDemoNotice(allDemo ? "Data contoh" : "Termasuk data contoh");
            }
        } catch (RuntimeException exception) {
            summary.setText(exception.getMessage() == null
                    ? "Target belajar tidak dapat dimuat." : exception.getMessage());
            progress.setProgress(0, 0, Theme.ACCENT);
        }
        revalidate();
        repaint();
    }

    int achievedCount() { return achieved; }
    int totalCount() { return total; }
    String summaryText() { return summary.getText(); }
    String disclaimerText() { return ((JLabel) footer.getComponent(0)).getText(); }

    private void setDemoNotice(String text) {
        if (demoNotice != null) heading.remove(demoNotice);
        demoNotice = text == null ? null : sizedBadge(Ui.tag(text), 140);
        if (demoNotice != null) heading.add(demoNotice, BorderLayout.EAST);
    }

    private static StatusBadge sizedBadge(StatusBadge badge, int minimumWidth) {
        Dimension preferred = badge.getPreferredSize();
        badge.setPreferredSize(new Dimension(Math.max(minimumWidth, preferred.width), preferred.height));
        return badge;
    }
}
