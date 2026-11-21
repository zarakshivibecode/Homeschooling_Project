package id.edu.homeschool.ui;

import id.edu.homeschool.model.LearningTask;
import id.edu.homeschool.service.LearningWorkspaceService;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import id.edu.homeschool.ui.design.ProgressTrack;
import id.edu.homeschool.ui.design.StatusBadge;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** A compact, current-state summary of task completion, outstanding and overdue counts. */
final class TaskStatusSummary extends JPanel {

    private final Workspace workspace;
    private final JPanel heading = Ui.transparent(new BorderLayout(Theme.SPACE_3, 0));
    private JComponent demoNotice;
    private final JLabel summary = Ui.muted("Status tugas saat ini.");
    private final JPanel statuses = Ui.transparent(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_2, 0));
    private final JPanel body = Ui.transparent(new BorderLayout(0, Theme.SPACE_2));
    private final ProgressTrack progress = new ProgressTrack();
    private int completed;
    private int pending;
    private int overdue;
    private int total;

    TaskStatusSummary(Workspace workspace) {
        super(new BorderLayout());
        this.workspace = workspace;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SPACE_3, 0));

        JPanel card = Ui.card();
        card.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_3, Theme.SPACE_4,
                Theme.SPACE_3, Theme.SPACE_4));
        JLabel title = Ui.label("Status tugas");
        title.setFont(Theme.FONT_14_SEMIBOLD);
        heading.add(title, BorderLayout.WEST);
        body.add(summary, BorderLayout.NORTH);
        body.add(progress, BorderLayout.CENTER);
        body.add(statuses, BorderLayout.SOUTH);
        card.add(heading, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
        refresh();
    }

    void refresh() {
        completed = 0;
        pending = 0;
        overdue = 0;
        total = 0;
        statuses.removeAll();
        setDemoNotice(null);
        if (!workspace.hasScope()) {
            showMessage("Pilih siswa untuk melihat status tugas.");
            return;
        }
        try {
            List<LearningTask> tasks = workspace.learning().tasks(
                    workspace.session().token(), workspace.studentId());
            total = tasks.size();
            LocalDate today = LocalDate.now();
            for (LearningTask task : tasks) {
                if (task.isCompleted()) completed++;
                else if (task.getDueDate().isBefore(today)) overdue++;
                else pending++;
            }
            if (total == 0) {
                showMessage("Belum ada tugas pada siswa ini.");
                return;
            }
            int percentage = (int) Math.round(completed * 100.0 / total);
            summary.setText(completed + " dari " + total + " tugas selesai (" + percentage + "%).");
            progress.setProgress(completed, total, Theme.SUCCESS);
            statuses.add(statusBadge("Selesai: " + completed, "Selesai"));
            statuses.add(statusBadge("Belum selesai: " + pending, "Belum selesai"));
            statuses.add(statusBadge("Terlambat: " + overdue, "Terlambat"));
            if (tasks.stream().anyMatch(task ->
                    workspace.isDemo(LearningWorkspaceService.TYPE_TASK, task.getId()))) {
                boolean allDemo = tasks.stream().allMatch(task ->
                        workspace.isDemo(LearningWorkspaceService.TYPE_TASK, task.getId()));
                setDemoNotice(allDemo ? "Data contoh" : "Termasuk data contoh");
            }
        } catch (RuntimeException exception) {
            showMessage(exception.getMessage() == null
                    ? "Status tugas tidak dapat dimuat." : exception.getMessage());
        }
        revalidate();
        repaint();
    }

    private void showMessage(String message) {
        summary.setText(message);
        progress.setProgress(0, 0, Theme.SUCCESS);
        statuses.removeAll();
        revalidate();
        repaint();
    }

    private void setDemoNotice(String text) {
        if (demoNotice != null) heading.remove(demoNotice);
        demoNotice = text == null ? null : sizedBadge(Ui.tag(text), 140);
        if (demoNotice != null) heading.add(demoNotice, BorderLayout.EAST);
    }

    private static StatusBadge statusBadge(String text, String status) {
        return sizedBadge(Ui.badge(text, Theme.statusOf(status)), 140);
    }

    private static StatusBadge sizedBadge(StatusBadge badge, int minimumWidth) {
        Dimension preferred = badge.getPreferredSize();
        badge.setPreferredSize(new Dimension(Math.max(minimumWidth, preferred.width), preferred.height));
        return badge;
    }

    int completedCount() { return completed; }
    int pendingCount() { return pending; }
    int overdueCount() { return overdue; }
    int totalCount() { return total; }
}
