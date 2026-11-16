package id.edu.homeschool.ui;

import id.edu.homeschool.service.ProgressReport;
import id.edu.homeschool.ui.design.EmptyState;
import id.edu.homeschool.ui.design.PageHeader;
import id.edu.homeschool.ui.design.ResponsiveGrid;
import id.edu.homeschool.ui.design.StatCard;
import id.edu.homeschool.ui.design.StatIcon;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Laporan progres page: a real report built from the stored tasks, goals and evaluations. */
public final class ProgressPage extends JPanel implements AppShell.Refreshable {

    private final Workspace workspace;
    private final PageHeader header = new PageHeader("Laporan progres",
            "Rekap achievement tugas, target, dan nilai untuk siswa terpilih.");
    private final JPanel body = new JPanel(new BorderLayout(0, Theme.SPACE_4));
    private final StatCard taskCard = new StatCard("Tugas selesai", "—", StatIcon.TASK, Theme.TINT_AMBER);
    private final StatCard goalCard = new StatCard("Target tercapai", "—", StatIcon.GOAL, Theme.TINT_VIOLET);
    private final StatCard scoreCard = new StatCard("Rata-rata nilai", "—", StatIcon.SCORE, Theme.TINT_ROSE);
    private final StatCard materialCard = new StatCard("Materi", "—", StatIcon.MATERIAL, Theme.TINT_TEAL);
    private final DefaultTableModel subjectModel = new DefaultTableModel(
            new Object[]{"Mata pelajaran", "Tugas", "Selesai", "Evaluasi", "Rata-rata"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public ProgressPage(Workspace workspace) {
        super(new BorderLayout(0, Theme.SPACE_4));
        this.workspace = workspace;
        setBackground(Theme.CANVAS);
        setOpaque(false);

        body.setBackground(Theme.CANVAS);
        body.setOpaque(false);

        add(header, BorderLayout.NORTH);
        // The report can grow, so the whole page stays reachable with the scroll bar.
        add(Ui.scrollPage(body), BorderLayout.CENTER);
        workspace.onChange(this::refresh);
        refresh();
    }

    public void refresh() {
        body.removeAll();
        if (!workspace.hasScope()) {
            body.add(new EmptyState("Laporan belum dapat dibuat",
                    "Pilih siswa terlebih dahulu agar laporan progres dapat dihitung dari data yang tersimpan."),
                    BorderLayout.CENTER);
            body.revalidate();
            body.repaint();
            return;
        }
        try {
            ProgressReport report = workspace.learning().progress(workspace.session().token(), workspace.studentId());
            taskCard.setValue(report.completedTaskCount() + " / " + report.taskCount());
            taskCard.setHint(Math.round(report.taskCompletionPercent()) + "% selesai");
            goalCard.setValue(report.achievedGoalCount() + " / " + report.goalCount());
            goalCard.setHint(Math.round(report.goalAchievementPercent()) + "% tercapai");
            scoreCard.setValue(report.evaluationCount() == 0 ? "—"
                    : String.format(java.util.Locale.ROOT, "%.1f%%", report.averagePercent()));
            scoreCard.setHint(report.evaluationCount() + " evaluasi");
            materialCard.setValue(String.valueOf(report.materialCount()));
            materialCard.setHint(report.sessionCount() + " sesi");

            // The cards reflow when the window becomes narrower.
            ResponsiveGrid metrics = new ResponsiveGrid(190, 4, Theme.SPACE_3);
            metrics.add(taskCard);
            metrics.add(goalCard);
            metrics.add(scoreCard);
            metrics.add(materialCard);
            body.add(metrics, BorderLayout.NORTH);

            JPanel lower = new JPanel(new BorderLayout(0, Theme.SPACE_4));
            lower.setBackground(Theme.CANVAS);
            lower.setOpaque(false);
            lower.add(summaryCard(report), BorderLayout.NORTH);
            lower.add(detailCard(report), BorderLayout.CENTER);
            body.add(lower, BorderLayout.CENTER);
        } catch (RuntimeException exception) {
            body.add(new EmptyState("Laporan tidak dapat dimuat",
                    exception.getMessage() == null ? "Terjadi kesalahan saat menghitung laporan." : exception.getMessage()),
                    BorderLayout.CENTER);
        }
        body.revalidate();
        body.repaint();
    }

    /** A readable conclusion that is generated from the stored numbers. */
    private JPanel summaryCard(ProgressReport report) {
        JPanel card = Ui.card();
        card.add(Ui.cardHeader("Ringkasan laporan", "Kesimpulan dihitung langsung dari data yang tersimpan."),
                BorderLayout.NORTH);

        javax.swing.JLabel summary = Ui.paragraph(report.summary());
        summary.setFont(Theme.FONT_13);
        summary.setForeground(Theme.TEXT);
        card.add(summary, BorderLayout.CENTER);
        return card;
    }

    private JPanel detailCard(ProgressReport report) {
        JPanel tableCard = Ui.card();
        tableCard.add(Ui.cardHeader("Rincian per mata pelajaran",
                "Perbandingan tugas dan nilai pada setiap mata pelajaran."), BorderLayout.NORTH);

        subjectModel.setRowCount(0);
        for (ProgressReport.SubjectProgress subject : report.subjects()) {
            subjectModel.addRow(new Object[]{subject.subjectName(), subject.taskCount(), subject.completedTaskCount(),
                    subject.evaluationCount(), subject.evaluationCount() == 0 ? "—"
                            : String.format(java.util.Locale.ROOT, "%.1f%%", subject.averagePercent())});
        }
        JTable table = new JTable(subjectModel);
        Ui.styleTable(table);
        tableCard.add(report.subjects().isEmpty()
                ? new EmptyState("Belum ada rincian", "Tambahkan mata pelajaran beserta tugas dan evaluasi untuk melihat rinciannya.")
                : Ui.scroll(table), BorderLayout.CENTER);
        return tableCard;
    }
}
