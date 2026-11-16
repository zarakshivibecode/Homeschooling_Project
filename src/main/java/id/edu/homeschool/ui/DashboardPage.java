package id.edu.homeschool.ui;

import id.edu.homeschool.model.LearningTask;
import id.edu.homeschool.model.StudySession;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.service.AccountSummary;
import id.edu.homeschool.service.ActivityEntry;
import id.edu.homeschool.service.ProgressReport;
import id.edu.homeschool.ui.design.EmptyState;
import id.edu.homeschool.ui.design.PageHeader;
import id.edu.homeschool.ui.design.ResponsiveGrid;
import id.edu.homeschool.ui.design.StatCard;
import id.edu.homeschool.ui.design.StatIcon;
import id.edu.homeschool.ui.design.StatusBadge;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Dashboard page: a compact summary of the selected student's learning activity. */
public final class DashboardPage extends JPanel implements AppShell.Refreshable {

    private final Workspace workspace;
    private final PageHeader header = new PageHeader("Dashboard", "Ringkasan jadwal, tugas, dan progres dari data tersimpan.");
    private final JPanel body = new JPanel(new BorderLayout(0, Theme.SPACE_4));
    private final StatCard subjectCount = new StatCard("Mata pelajaran", "—", StatIcon.SUBJECT, Theme.TINT_INDIGO);
    private final StatCard sessionCount = new StatCard("Jadwal belajar", "—", StatIcon.SESSION, Theme.TINT_SKY);
    private final StatCard openTasks = new StatCard("Tugas tertunda", "—", StatIcon.TASK, Theme.TINT_AMBER);
    private final StatCard goalCount = new StatCard("Target tercapai", "—", StatIcon.GOAL, Theme.TINT_VIOLET);
    private final StatCard averageScore = new StatCard("Rata-rata nilai", "—", StatIcon.SCORE, Theme.TINT_ROSE);
    private final DefaultTableModel scheduleModel = new DefaultTableModel(new Object[]{"Tanggal", "Waktu", "Mata pelajaran"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final DefaultTableModel taskModel = new DefaultTableModel(new Object[]{"Tugas", "Tenggat", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public DashboardPage(Workspace workspace) {
        super(new BorderLayout(0, Theme.SPACE_4));
        this.workspace = workspace;
        setBackground(Theme.CANVAS);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        body.setBackground(Theme.CANVAS);
        body.setOpaque(false);

        add(header, BorderLayout.NORTH);
        // The summary can grow taller than the window, so it stays reachable with the scroll bar.
        add(Ui.scrollPage(body), BorderLayout.CENTER);
        workspace.onChange(this::refresh);
        refresh();
    }

    public void refresh() {
        body.removeAll();
        updateHeader();
        if (workspace.isTeacher() && !workspace.hasScope()) {
            body.add(new EmptyState("Belum ada siswa terpilih",
                    "Pilih siswa pada pemilih siswa di bagian atas halaman untuk melihat ringkasan pembelajarannya."),
                    BorderLayout.CENTER);
            body.revalidate();
            body.repaint();
            return;
        }
        if (!workspace.hasScope()) {
            body.add(new EmptyState("Belum ada data",
                    "Data belajar untuk akun ini belum tersedia. Pengajar dapat menambahkannya setelah terhubung."),
                    BorderLayout.CENTER);
            body.revalidate();
            body.repaint();
            return;
        }
        try {
            ProgressReport report = workspace.learning().progress(workspace.session().token(), workspace.studentId());
            subjectCount.setValue(String.valueOf(report.subjectCount()));
            subjectCount.setHint(report.materialCount() + " materi");
            sessionCount.setValue(String.valueOf(report.sessionCount()));
            sessionCount.setHint("total sesi");
            openTasks.setValue(String.valueOf(report.taskCount() - report.completedTaskCount()));
            openTasks.setHint(report.overdueTaskCount() + " terlambat");
            goalCount.setValue(report.achievedGoalCount() + " / " + report.goalCount());
            goalCount.setHint(Math.round(report.goalAchievementPercent()) + "% tercapai");
            averageScore.setValue(report.evaluationCount() == 0 ? "—"
                    : String.format(java.util.Locale.ROOT, "%.0f%%", report.averagePercent()));
            averageScore.setHint(report.evaluationCount() + " evaluasi");

            // The cards reflow when the window becomes narrower.
            ResponsiveGrid metrics = new ResponsiveGrid(190, 5, Theme.SPACE_3);
            metrics.add(subjectCount);
            metrics.add(sessionCount);
            metrics.add(openTasks);
            metrics.add(goalCount);
            metrics.add(averageScore);
            body.add(metrics, BorderLayout.NORTH);

            JPanel left = new JPanel(new BorderLayout(0, Theme.SPACE_4));
            left.setBackground(Theme.CANVAS);
            left.setOpaque(false);
            left.add(scheduleCard(), BorderLayout.NORTH);
            left.add(taskCard(), BorderLayout.CENTER);

            ResponsiveGrid lists = new ResponsiveGrid(440, 2, Theme.SPACE_4);
            lists.add(left);
            lists.add(activityCard());
            body.add(lists, BorderLayout.CENTER);
        } catch (RuntimeException exception) {
            body.add(new EmptyState("Ringkasan tidak dapat dimuat",
                    exception.getMessage() == null ? "Terjadi kesalahan saat membaca data." : exception.getMessage()),
                    BorderLayout.CENTER);
        }
        body.revalidate();
        body.repaint();
    }

    /** Greets the signed-in account and names the student whose data is shown. */
    private void updateHeader() {
        String name = workspace.session().name();
        String greeting = name == null || name.isBlank() ? "Halo" : "Halo, " + firstName(name);
        header.setEyebrow(todayLabel());
        if (workspace.isTeacher() && workspace.hasScope()) {
            String student = studentName();
            header.setTitle(greeting);
            header.setDescription(student == null
                    ? "Ringkasan jadwal, tugas, dan progres dari data tersimpan."
                    : "Ringkasan jadwal, tugas, dan progres untuk " + student + ".");
        } else if (workspace.isStudent()) {
            header.setTitle(greeting);
            header.setDescription("Ringkasan jadwal, tugas, dan progres dari data tersimpan.");
        }
    }

    /** The name of the student that is currently shown, or null when it is unknown. */
    private String studentName() {
        try {
            for (AccountSummary summary : workspace.learning().connectedStudents(workspace.session().token())) {
                if (summary.id().equals(workspace.studentId())) return summary.name();
            }
        } catch (RuntimeException exception) {
            // The scope list is not available, so the greeting keeps the generic wording.
        }
        return null;
    }

    /** The first name of an account, used for the greeting. */
    private static String firstName(String name) {
        String trimmed = name.trim();
        int space = trimmed.indexOf(' ');
        return space > 0 ? trimmed.substring(0, space) : trimmed;
    }

    /** The current date written in Indonesian, used as the small label of the header. */
    private static String todayLabel() {
        LocalDate today = LocalDate.now();
        String day = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("id"));
        String month = today.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("id"));
        return day + ", " + today.getDayOfMonth() + " " + month + " " + today.getYear();
    }

    private JPanel scheduleCard() {
        JPanel card = Ui.card();
        card.add(Ui.cardHeader("Jadwal terdekat", "Sesi berikutnya berdasarkan tanggal dan waktu."), BorderLayout.NORTH);

        scheduleModel.setRowCount(0);
        Map<String, String> subjectNames = subjectNames();
        LocalDate today = LocalDate.now();
        List<StudySession> upcoming = workspace.learning().sessions(workspace.session().token(), workspace.studentId()).stream()
                .filter(session -> !session.getDate().isBefore(today))
                .sorted(java.util.Comparator.comparing(StudySession::getDate).thenComparing(StudySession::getStartTime))
                .limit(6).toList();
        for (StudySession session : upcoming) {
            scheduleModel.addRow(new Object[]{session.getDate(), session.getStartTime(),
                    subjectNames.getOrDefault(session.getSubjectId(), "Mata pelajaran")});
        }
        JTable table = new JTable(scheduleModel);
        Ui.styleTable(table);
        table.getTableHeader().setReorderingAllowed(false);
        card.add(upcoming.isEmpty()
                ? new EmptyState("Belum ada jadwal mendatang", "Tambahkan sesi belajar pada halaman Jadwal belajar.")
                : Ui.scroll(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel taskCard() {
        JPanel card = Ui.card();
        card.add(Ui.cardHeader("Tugas terdekat", "Tugas yang belum selesai beserta tenggatnya."), BorderLayout.NORTH);

        taskModel.setRowCount(0);
        List<LearningTask> pending = workspace.learning().tasks(workspace.session().token(), workspace.studentId()).stream()
                .filter(task -> !task.isCompleted())
                .sorted(java.util.Comparator.comparing(LearningTask::getDueDate))
                .limit(6).toList();
        for (LearningTask task : pending) {
            taskModel.addRow(new Object[]{task.getTitle(), task.getDueDate(), task.getStatusLabel()});
        }
        JTable table = new JTable(taskModel);
        Ui.styleTable(table);
        card.add(pending.isEmpty()
                ? new EmptyState("Tidak ada tugas tertunda", "Semua tugas yang diberikan sudah diselesaikan.")
                : Ui.scroll(table), BorderLayout.CENTER);
        return card;
    }

    /** The activity feed is built from the stored records, so it always matches the data. */
    private JPanel activityCard() {
        JPanel card = Ui.card();
        card.add(Ui.cardHeader("Aktivitas terbaru", "Riwayat jadwal, tugas, evaluasi, dan target dari data tersimpan."),
                BorderLayout.NORTH);

        List<ActivityEntry> entries = workspace.learning()
                .recentActivity(workspace.session().token(), workspace.studentId(), 6);
        if (entries.isEmpty()) {
            card.add(new EmptyState("Belum ada aktivitas", "Aktivitas akan muncul setelah jadwal, tugas, atau evaluasi dicatat."),
                    BorderLayout.CENTER);
            return card;
        }
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Theme.CANVAS);
        list.setOpaque(false);
        for (ActivityEntry entry : entries) list.add(activityRow(entry));
        card.add(Ui.scrollTransparent(list), BorderLayout.CENTER);
        return card;
    }

    private JPanel activityRow(ActivityEntry entry) {
        JPanel row = new JPanel(new BorderLayout(Theme.SPACE_3, 0));
        row.setBackground(Theme.CANVAS);
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_2, 0, Theme.SPACE_2, 0));
        row.setAlignmentX(LEFT_ALIGNMENT);

        StatusBadge category = Ui.tag(entry.category());
        // The badge keeps its own height instead of being stretched by the row.
        JPanel badgeHolder = new JPanel(new BorderLayout());
        badgeHolder.setBackground(Theme.CANVAS);
        badgeHolder.setOpaque(false);
        badgeHolder.add(category, BorderLayout.NORTH);

        // Both lines sit at the top so the gap of a taller row does not push them apart.
        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(Theme.CANVAS);
        text.setOpaque(false);
        text.setAlignmentX(LEFT_ALIGNMENT);
        JLabel title = Ui.label(shorten(entry.title(), 46));
        title.setFont(Theme.FONT_13);
        JLabel detail = Ui.subtle(entry.detail());
        detail.setFont(Theme.FONT_11);
        title.setAlignmentX(LEFT_ALIGNMENT);
        detail.setAlignmentX(LEFT_ALIGNMENT);
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title.getPreferredSize().height));
        detail.setMaximumSize(new Dimension(Integer.MAX_VALUE, detail.getPreferredSize().height));
        text.add(title);
        text.add(detail);

        JLabel when = Ui.subtle(entry.when().toLocalDate().toString());
        when.setFont(Theme.FONT_11);
        when.setPreferredSize(new Dimension(88, 0));
        when.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        when.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        row.add(badgeHolder, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(when, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    /** Shortens a text so it fits the activity feed without being cut off. */
    private static String shorten(String text, int limit) {
        if (text == null) return "";
        return text.length() <= limit ? text : text.substring(0, limit - 1) + "…";
    }

    private Map<String, String> subjectNames() {
        Map<String, String> names = new java.util.LinkedHashMap<>();
        for (Subject subject : workspace.learning().subjects(workspace.session().token(), workspace.studentId())) {
            names.put(subject.getId(), subject.getName());
        }
        return names;
    }
}
