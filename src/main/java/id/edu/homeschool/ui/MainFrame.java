package id.edu.homeschool.ui;

import id.edu.homeschool.service.AuthService;
import id.edu.homeschool.service.ConnectionService;
import id.edu.homeschool.service.DemoDataService;
import id.edu.homeschool.service.LearningService;
import id.edu.homeschool.service.LearningWorkspaceService;
import id.edu.homeschool.service.UserSession;
import id.edu.homeschool.ui.design.AppLogo;
import id.edu.homeschool.ui.design.GlassCanvas;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;

/**
 * Application window. It hosts the authentication screen and, after a successful sign in, the
 * authenticated application shell. Signing out invalidates the session and returns to the login.
 */
public final class MainFrame extends JFrame {

    private final AuthService auth;
    private final ConnectionService connections;
    private final LearningWorkspaceService learning;
    private final LearningService legacy;
    private final DemoDataService demo;
    private final CardLayout layout = new CardLayout();
    private final GlassCanvas root = new GlassCanvas();
    private UserSession session;

    public MainFrame(AuthService auth, ConnectionService connections, LearningWorkspaceService learning) {
        this(auth, connections, learning, null, null);
    }

    public MainFrame(AuthService auth, ConnectionService connections, LearningWorkspaceService learning,
            LearningService legacy) {
        this(auth, connections, learning, legacy, null);
    }

    public MainFrame(AuthService auth, ConnectionService connections, LearningWorkspaceService learning,
            LearningService legacy, DemoDataService demo) {
        super("Personal Homeschooling Learning System");
        this.auth = auth;
        this.connections = connections;
        this.learning = learning;
        this.legacy = legacy;
        this.demo = demo;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setSize(1360, 860);
        setLocationRelativeTo(null);
        setIconImage(AppLogo.windowIcon());
        root.setLayout(layout);
        setContentPane(root);
        showAuth();
    }

    private void showAuth() {
        root.removeAll();
        root.add(new AuthScreen(auth, this::showApplication, demo), "auth");
        layout.show(root, "auth");
        root.revalidate();
        root.repaint();
    }

    private void showApplication(UserSession authenticated) {
        session = authenticated;
        root.removeAll();
        root.add(new AppShell(new Workspace(auth, connections, learning, legacy, demo, authenticated),
                this::signOut), "app");
        layout.show(root, "app");
        root.revalidate();
        root.repaint();
    }

    private void signOut() {
        if (session != null) auth.logout(session.token());
        session = null;
        showAuth();
    }
}
