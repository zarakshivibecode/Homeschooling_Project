package id.edu.homeschool;

import id.edu.homeschool.repository.ApplicationRepositories;
import id.edu.homeschool.service.AuthService;
import id.edu.homeschool.service.ConnectionService;
import id.edu.homeschool.service.DemoDataService;
import id.edu.homeschool.service.LearningService;
import id.edu.homeschool.service.LearningWorkspaceService;
import id.edu.homeschool.service.SessionRegistry;
import id.edu.homeschool.ui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entry point of the Personal Homeschooling Learning System. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();
            ApplicationRepositories repositories = ApplicationRepositories.inDefaultLocation();
            SessionRegistry sessions = new SessionRegistry(repositories.accounts());
            AuthService authentication = new AuthService(repositories.accounts(), sessions);
            ConnectionService connections = new ConnectionService(repositories.accounts(), repositories.invitations(),
                    repositories.connectionRequests(), repositories.connections(), sessions);
            LearningWorkspaceService learning = new LearningWorkspaceService(repositories, sessions);
            DemoDataService demo = new DemoDataService(repositories, authentication, connections, learning);
            // The sample curriculum is only created when the stores are still empty.
            demo.seedIfNeeded();
            new MainFrame(authentication, connections, learning, new LearningService(repositories), demo).setVisible(true);
        });
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            // Swing memakai tampilan default jika tema sistem tidak tersedia.
        }
        // The shared defaults align the parts that are painted by the platform with the theme.
        id.edu.homeschool.ui.design.LookAndFeel.apply();
    }
}
