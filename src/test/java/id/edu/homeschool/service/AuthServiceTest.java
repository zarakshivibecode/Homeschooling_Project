package id.edu.homeschool.service;

import id.edu.homeschool.model.AccountRole;
import id.edu.homeschool.model.AccountStatus;
import id.edu.homeschool.model.UserAccount;
import id.edu.homeschool.repository.ApplicationRepositories;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    @TempDir Path temp;
    private ApplicationRepositories repositories;
    private SessionRegistry sessions;
    private AuthService auth;

    @BeforeEach void setUp() {
        repositories = new ApplicationRepositories(temp);
        sessions = new SessionRegistry(repositories.accounts());
        auth = new AuthService(repositories.accounts(), sessions);
    }

    @Test void registrationHashesPasswordAndLoginCreatesRevocableSession() throws Exception {
        char[] input = "StudentPass2040".toCharArray();
        var registered = auth.registerStudent("Sample Learner", "Learner@Example.test", input, "SMA");
        UserAccount stored = repositories.accounts().findById(registered.id()).orElseThrow();
        assertEquals(AccountRole.SISWA, stored.getRole());
        assertEquals("learner@example.test", stored.getEmail());
        assertNotEquals(new String(input), stored.getPasswordHash());
        assertTrue(stored.getPasswordHash().startsWith("pbkdf2-sha256$"));
        assertFalse(Files.readString(temp.resolve("accounts.json")).contains(new String(input)));

        UserSession session = auth.login("LEARNER@example.test", input);
        assertEquals(registered.id(), auth.currentAccount(session.token()).id());
        auth.logout(session.token());
        assertThrows(SecurityException.class, () -> auth.currentAccount(session.token()));
    }

    @Test void duplicateEmailAndWeakPasswordAreRejectedWithoutSensitiveDetails() {
        auth.registerStudent("Sample Learner", "same@example.test", "StudentPass2040".toCharArray(), "SMA");
        IllegalArgumentException duplicate = assertThrows(IllegalArgumentException.class,
                () -> auth.registerTeacher("Sample Teacher", "SAME@example.test", "TeacherPass2040".toCharArray()));
        assertTrue(duplicate.getMessage().contains("tidak dapat digunakan"));
        assertThrows(IllegalArgumentException.class,
                () -> auth.registerStudent("Other", "other@example.test", "short".toCharArray(), "SMP"));
    }

    @Test void loginDoesNotDistinguishMissingAccountOrWrongPasswordAndDisabledAccountsCannotLogin() {
        auth.registerTeacher("Sample Teacher", "teacher@example.test", "TeacherPass2040".toCharArray());
        assertThrows(SecurityException.class, () -> auth.login("missing@example.test", "OtherPass2040".toCharArray()));
        assertThrows(SecurityException.class, () -> auth.login("teacher@example.test", "OtherPass2040".toCharArray()));
        UserAccount teacher = repositories.accounts().findAll().getFirst();
        teacher.setStatus(AccountStatus.DISABLED);
        repositories.accounts().save(teacher);
        assertThrows(SecurityException.class, () -> auth.login("teacher@example.test", "TeacherPass2040".toCharArray()));
    }
}
