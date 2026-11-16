package id.edu.homeschool.support;

import id.edu.homeschool.repository.ApplicationRepositories;
import id.edu.homeschool.service.AuthService;
import id.edu.homeschool.service.ConnectionService;
import id.edu.homeschool.service.LearningWorkspaceService;
import id.edu.homeschool.service.SessionRegistry;
import id.edu.homeschool.service.UserSession;
import java.nio.file.Path;

/** Shared wiring for the service level tests. */
public final class TestEnvironment {

    public final ApplicationRepositories repositories;
    public final SessionRegistry sessions;
    public final AuthService auth;
    public final ConnectionService connections;
    public final LearningWorkspaceService learning;

    public TestEnvironment(Path directory) {
        repositories = new ApplicationRepositories(directory);
        sessions = new SessionRegistry(repositories.accounts());
        auth = new AuthService(repositories.accounts(), sessions);
        connections = new ConnectionService(repositories.accounts(), repositories.invitations(),
                repositories.connectionRequests(), repositories.connections(), sessions);
        learning = new LearningWorkspaceService(repositories, sessions);
    }

    public UserSession registerTeacher(String name, String email, String password) {
        auth.registerTeacher(name, email, password.toCharArray());
        return auth.login(email, password.toCharArray());
    }

    public UserSession registerStudent(String name, String email, String password, String level) {
        auth.registerStudent(name, email, password.toCharArray(), level);
        return auth.login(email, password.toCharArray());
    }

    /** Connects a student to a teacher through the invitation flow. */
    public void connect(UserSession teacher, UserSession student) {
        var invitation = connections.createInvitation(teacher.token());
        var request = connections.requestConnection(student.token(), invitation.code());
        connections.approve(teacher.token(), request.id());
    }
}
