package id.edu.homeschool.ui;

import id.edu.homeschool.model.AccountRole;
import id.edu.homeschool.service.AccountSummary;
import id.edu.homeschool.ui.design.NavIcon;
import id.edu.homeschool.ui.design.Sidebar;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.TopBar;
import id.edu.homeschool.ui.design.Ui;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The authenticated application shell: a floating glass sidebar, a floating glass top bar with
 * the student scope and a content area that swaps between pages. Every page is registered with a
 * refresher so it always shows data that comes from the repositories.
 */
public final class AppShell extends JPanel {

    private static final String DASHBOARD = "dashboard";
    private static final String SUBJECTS = "subjects";
    private static final String SCHEDULE = "schedule";
    private static final String MATERIALS = "materials";
    private static final String TASKS = "tasks";
    private static final String GOALS = "goals";
    private static final String EVALUATIONS = "evaluations";
    private static final String PROGRESS = "progress";
    private static final String TEACHERS = "teachers";
    private static final String STUDENTS = "students";
    private static final String PROFILE = "profile";
    private static final String REQUESTS = "requests";
    private static final String INVITATIONS = "invitations";
    private static final String CONNECTIONS = "connections";

    private final Workspace workspace;
    private final Runnable onLogout;
    private final Sidebar sidebar = new Sidebar();
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel content = new JPanel(contentLayout);
    private final Map<String, JPanel> pages = new LinkedHashMap<>();
    private final Map<String, Runnable> refreshers = new LinkedHashMap<>();
    private final TopBar topBar = new TopBar();
    private final JComboBox<AccountSummary> scopeCombo = Ui.combo();
    private boolean updatingScope;
    private String currentId = DASHBOARD;

    public AppShell(Workspace workspace, Runnable onLogout) {
        super(new BorderLayout());
        this.workspace = workspace;
        this.onLogout = onLogout;
        setBackground(Theme.CANVAS);
        setOpaque(false);

        content.setBackground(Theme.CANVAS);
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACE_4, Theme.SPACE_4, Theme.SPACE_6, Theme.SPACE_6));

        buildTopBar();

        JPanel centre = new JPanel(new BorderLayout(0, Theme.SPACE_3));
        centre.setBackground(Theme.CANVAS);
        centre.setOpaque(false);

        JPanel barHolder = new JPanel(new BorderLayout());
        barHolder.setBackground(Theme.CANVAS);
        barHolder.setOpaque(false);
        barHolder.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_4, Theme.SPACE_4, 0, Theme.SPACE_6));
        barHolder.add(topBar, BorderLayout.CENTER);
        centre.add(barHolder, BorderLayout.NORTH);
        centre.add(content, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(centre, BorderLayout.CENTER);

        buildNavigation();
        show(DASHBOARD);
    }

    /* ---------------- navigation ---------------- */

    private void buildNavigation() {
        register(DASHBOARD, new DashboardPage(workspace));
        register(SUBJECTS, new SubjectPage(workspace));
        register(SCHEDULE, new SchedulePage(workspace));
        register(MATERIALS, new MaterialPage(workspace));
        register(TASKS, new TaskPage(workspace));
        register(GOALS, new GoalPage(workspace));
        register(EVALUATIONS, new EvaluationPage(workspace));
        register(PROGRESS, new ProgressPage(workspace));
        register(TEACHERS, new DirectoryPage(workspace, true));
        register(STUDENTS, new DirectoryPage(workspace, false));
        register(PROFILE, new ProfilePage(workspace));
        if (workspace.isTeacher()) {
            register(REQUESTS, new TeacherRequestsPage(workspace));
            register(INVITATIONS, new InvitationsPage(workspace));
        } else {
            register(CONNECTIONS, new StudentConnectionsPage(workspace));
        }

        sidebar.addSection("Ringkasan");
        sidebar.addItem(DASHBOARD, "Dashboard", NavIcon.DASHBOARD, () -> show(DASHBOARD));
        sidebar.addSection("Pembelajaran");
        sidebar.addItem(SUBJECTS, "Mata pelajaran", NavIcon.SUBJECT, () -> show(SUBJECTS));
        sidebar.addItem(SCHEDULE, "Jadwal belajar", NavIcon.SCHEDULE, () -> show(SCHEDULE));
        sidebar.addItem(MATERIALS, "Materi", NavIcon.MATERIAL, () -> show(MATERIALS));
        sidebar.addItem(TASKS, "Tugas", NavIcon.TASK, () -> show(TASKS));
        sidebar.addItem(GOALS, "Target belajar", NavIcon.GOAL, () -> show(GOALS));
        sidebar.addItem(EVALUATIONS, "Evaluasi", NavIcon.EVALUATION, () -> show(EVALUATIONS));
        sidebar.addItem(PROGRESS, "Laporan progres", NavIcon.PROGRESS, () -> show(PROGRESS));
        sidebar.addSection("Akun");
        sidebar.addItem(TEACHERS, "Data pengajar", NavIcon.TEACHERS, () -> show(TEACHERS));
        sidebar.addItem(STUDENTS, "Data siswa", NavIcon.STUDENTS, () -> show(STUDENTS));
        sidebar.addItem(PROFILE, "Profil saya", NavIcon.PROFILE, () -> show(PROFILE));
        sidebar.addSection("Koneksi");
        if (workspace.isTeacher()) {
            sidebar.addItem(REQUESTS, "Permintaan siswa", NavIcon.REQUESTS, () -> show(REQUESTS));
            sidebar.addItem(INVITATIONS, "Undangan", NavIcon.INVITATION, () -> show(INVITATIONS));
        } else {
            sidebar.addItem(CONNECTIONS, "Koneksi pengajar", NavIcon.CONNECTION, () -> show(CONNECTIONS));
        }
    }

    private void register(String id, JPanel page) {
        pages.put(id, page);
        refreshers.put(id, () -> {
            try {
                if (page instanceof Refreshable refreshable) refreshable.refresh();
            } catch (SecurityException exception) {
                // The session ended while the page was open, so the user returns to the login.
                onLogout.run();
            }
        });
        content.add(page, id);
    }

    private void show(String id) {
        JPanel page = pages.get(id);
        if (page == null) return;
        currentId = id;
        contentLayout.show(content, id);
        sidebar.select(id);
        Runnable refresher = refreshers.get(id);
        if (refresher != null) refresher.run();
    }

    /** Shows a page and refreshes it; used by the shell when the account changes. */
    public void showPage(String id) { show(id); }

    public String currentPage() { return currentId; }

    /** The registered page for a navigation id; used by the interface tests. */
    public JPanel page(String id) { return pages.get(id); }

    /* ---------------- top bar ---------------- */

    private void buildTopBar() {
        if (workspace.isTeacher()) {
            scopeCombo.setRenderer(new javax.swing.DefaultListCellRenderer() {
                @Override public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                        int index, boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, selected, focus);
                    setText(value instanceof AccountSummary account ? account.name() : "Pilih siswa");
                    setFont(Theme.FONT_13);
                    setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                    return this;
                }
            });
            scopeCombo.setPreferredSize(new Dimension(220, Theme.CONTROL_HEIGHT));
            scopeCombo.addActionListener(event -> {
                if (updatingScope) return;
                AccountSummary selected = (AccountSummary) scopeCombo.getSelectedItem();
                workspace.setStudentId(selected == null ? null : selected.id());
            });

            JLabel caption = Ui.muted("Siswa:");
            caption.setFont(Theme.FONT_12);
            topBar.addLeft(caption);
            topBar.addLeft(scopeCombo);
            reloadScope();
            workspace.onChange(this::reloadScope);
        } else {
            topBar.addLeft(Ui.muted("Ruang belajar siswa"));
        }

        topBar.setIdentity(TopBar.accountChip(workspace.session().name(),
                workspace.session().role() == AccountRole.PENGAJAR ? "Pengajar" : "Siswa"));
        javax.swing.JButton logout = TopBar.logoutButton();
        logout.addActionListener(event -> onLogout.run());
        topBar.addRight(logout);
        workspace.onChange(this::refreshIdentity);
    }

    /** Keeps the account chip in sync after the profile was edited. */
    private void refreshIdentity() {
        topBar.setIdentity(TopBar.accountChip(workspace.session().name(),
                workspace.session().role() == AccountRole.PENGAJAR ? "Pengajar" : "Siswa"));
    }

    /** Rebuilds the student picker from the repository so newly approved students show up. */
    private void reloadScope() {
        if (!workspace.isTeacher()) return;
        // The workspace scope is the source of truth; the combo follows it, never the other way round.
        String wanted = workspace.studentId();
        // Rebuilding the combo fires action events, so the listener is muted while it runs.
        updatingScope = true;
        try {
            scopeCombo.removeAllItems();
            List<AccountSummary> students = workspace.learning().connectedStudents(workspace.session().token());
            for (AccountSummary student : students) scopeCombo.addItem(student);
            int restore = -1;
            if (wanted != null) {
                for (int index = 0; index < scopeCombo.getItemCount(); index++) {
                    if (scopeCombo.getItemAt(index).id().equals(wanted)) { restore = index; break; }
                }
            }
            if (restore < 0 && scopeCombo.getItemCount() > 0) restore = 0;
            if (restore >= 0) scopeCombo.setSelectedIndex(restore);
        } finally {
            updatingScope = false;
        }
        AccountSummary chosen = (AccountSummary) scopeCombo.getSelectedItem();
        workspace.setStudentId(chosen == null ? null : chosen.id());
    }

    /** A page that can reload its own content. */
    public interface Refreshable {
        void refresh();
    }
}
