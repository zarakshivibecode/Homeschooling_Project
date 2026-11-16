package id.edu.homeschool.ui;

import id.edu.homeschool.model.AccountRole;
import id.edu.homeschool.service.AccountSummary;
import id.edu.homeschool.service.AuthService;
import id.edu.homeschool.service.ConnectionService;
import id.edu.homeschool.service.DemoDataService;
import id.edu.homeschool.service.LearningService;
import id.edu.homeschool.service.LearningWorkspaceService;
import id.edu.homeschool.service.UserSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Holds the services, the signed-in account and the student that the teacher is currently looking at.
 * Pages register a listener so they refresh whenever the selected student changes.
 */
public final class Workspace {

    private final AuthService auth;
    private final ConnectionService connections;
    private final LearningWorkspaceService learning;
    private final LearningService legacy;
    private final DemoDataService demo;
    private final List<Runnable> listeners = new ArrayList<>();
    private UserSession session;
    private String studentId;

    public Workspace(AuthService auth, ConnectionService connections, LearningWorkspaceService learning, UserSession session) {
        this(auth, connections, learning, null, session);
    }

    public Workspace(AuthService auth, ConnectionService connections, LearningWorkspaceService learning,
            LearningService legacy, UserSession session) {
        this(auth, connections, learning, legacy, null, session);
    }

    public Workspace(AuthService auth, ConnectionService connections, LearningWorkspaceService learning,
            LearningService legacy, DemoDataService demo, UserSession session) {
        this.auth = Objects.requireNonNull(auth);
        this.connections = Objects.requireNonNull(connections);
        this.learning = Objects.requireNonNull(learning);
        this.legacy = legacy;
        this.demo = demo;
        this.session = Objects.requireNonNull(session);
        this.studentId = session.role() == AccountRole.SISWA ? session.accountId() : null;
    }

    public AuthService auth() { return auth; }
    public ConnectionService connections() { return connections; }
    public LearningWorkspaceService learning() { return learning; }

    /** Access to the pre-account data files; null when the application has no legacy store. */
    public LearningService legacy() { return legacy; }

    /** The demo seeder; null when the application runs without demo support. */
    public DemoDataService demo() { return demo; }

    public UserSession session() { return session; }

    public boolean isTeacher() { return session.role() == AccountRole.PENGAJAR; }
    public boolean isStudent() { return session.role() == AccountRole.SISWA; }
    public String teacherId() { return session.accountId(); }

    /** The student whose learning data is shown. Always the signed-in student for the student role. */
    public String studentId() { return studentId; }

    public boolean hasScope() { return studentId != null && !studentId.isBlank(); }

    /** True when the record was created by the built-in demo seeder. */
    public boolean isDemo(String resourceType, String resourceId) {
        return demo != null && demo.isDemoResource(resourceType, resourceId);
    }

    /** True when the signed-in account was created by the built-in demo seeder. */
    public boolean isDemoAccount() {
        return demo != null && demo.isDemoAccount(session.accountId());
    }

    public void setStudentId(String value) {
        String next = value == null || value.isBlank() ? null : value;
        if (Objects.equals(next, studentId)) return;
        studentId = next;
        notifyListeners();
    }

    /** Reloads the signed-in account details after the profile was edited. */
    public void refreshSession() {
        AccountSummary summary = auth.currentAccount(session.token());
        session = new UserSession(session.token(), summary.id(), summary.name(), summary.email(),
                summary.role(), summary.educationLevel());
        notifyListeners();
    }

    public void onChange(Runnable listener) {
        listeners.add(listener);
    }

    public void notifyListeners() {
        for (Runnable listener : List.copyOf(listeners)) {
            try {
                listener.run();
            } catch (RuntimeException ignored) {
                // A failing page must not stop the other pages from refreshing.
            }
        }
    }
}
