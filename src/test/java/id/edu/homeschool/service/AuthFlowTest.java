package id.edu.homeschool.service;

import id.edu.homeschool.model.AccountRole;
import id.edu.homeschool.model.AccountStatus;
import id.edu.homeschool.support.TestEnvironment;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Registration, login, session and profile behaviour of the authentication service. */
class AuthFlowTest {

    @TempDir Path temp;
    private TestEnvironment environment;

    @BeforeEach void setUp() {
        environment = new TestEnvironment(temp);
    }

    @Test void registerLoginLogoutWorksAndAccountSurvivesRestart() throws Exception {
        var registered = environment.auth.registerStudent("Siswa Uji", "siswa@uji.test", "SiswaPassword2040".toCharArray(), "SMA");
        assertEquals(AccountRole.SISWA, registered.role());

        UserSession session = environment.auth.login("SISWA@uji.test", "SiswaPassword2040".toCharArray());
        assertEquals(registered.id(), environment.auth.currentAccount(session.token()).id());

        environment.auth.logout(session.token());
        assertThrows(SecurityException.class, () -> environment.auth.currentAccount(session.token()));

        // The account must still be stored and usable after the application is restarted.
        TestEnvironment restarted = new TestEnvironment(temp);
        UserSession again = restarted.auth.login("siswa@uji.test", "SiswaPassword2040".toCharArray());
        assertEquals(registered.id(), again.accountId());
        assertTrue(restarted.repositories.accounts().findById(registered.id()).isPresent());
    }

    @Test void duplicateEmailIsRejectedWithAReadableMessage() {
        environment.auth.registerStudent("Siswa Satu", "kembar@uji.test", "SiswaPassword2040".toCharArray(), "SMA");
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> environment.auth.registerTeacher("Pengajar", "KEMBAR@uji.test", "GuruPassword2040".toCharArray()));
        assertTrue(error.getMessage().contains("tidak dapat digunakan"), error.getMessage());
        assertTrue(environment.auth.emailExists("kembar@uji.test"));
    }

    @Test void weakPasswordAndInvalidEmailAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> environment.auth.registerStudent("Siswa", "siswa@uji.test", "pendek123".toCharArray(), "SMA"));
        assertThrows(IllegalArgumentException.class,
                () -> environment.auth.registerStudent("Siswa", "bukan-email", "SiswaPassword2040".toCharArray(), "SMA"));
        assertThrows(IllegalArgumentException.class,
                () -> environment.auth.registerStudent("Siswa", "siswa@uji.test", "abcdefghijklmn".toCharArray(), "SMA"));
    }

    @Test void passwordIsStoredAsHashAndNeverInPlainText() throws Exception {
        environment.auth.registerTeacher("Pengajar", "guru@uji.test", "GuruPassword2040".toCharArray());
        String stored = environment.repositories.accounts().findAll().getFirst().getPasswordHash();
        assertFalse(stored.contains("GuruPassword2040"));
        assertTrue(stored.startsWith("pbkdf2-sha256$"));
        assertNotEquals("GuruPassword2040", environment.repositories.accounts().findAll().getFirst().getPasswordHash());
    }

    @Test void profileCanBeUpdatedAndPasswordChanged() {
        UserSession student = environment.registerStudent("Siswa Lama", "siswa@uji.test", "SiswaPassword2040", "SD");
        environment.auth.updateProfile(student.token(), "Siswa Baru", "SMP");
        var summary = environment.auth.currentAccount(student.token());
        assertEquals("Siswa Baru", summary.name());
        assertEquals("SMP", summary.educationLevel());

        assertThrows(SecurityException.class,
                () -> environment.auth.changePassword(student.token(), "SalahPassword2040".toCharArray(), "KataSandiBaru2040".toCharArray()));
        environment.auth.changePassword(student.token(), "SiswaPassword2040".toCharArray(), "KataSandiBaru2040".toCharArray());
        assertNotEquals(null, environment.auth.login("siswa@uji.test", "KataSandiBaru2040".toCharArray()));
    }

    @Test void disabledAccountCannotSignIn() {
        environment.auth.registerTeacher("Pengajar", "guru@uji.test", "GuruPassword2040".toCharArray());
        var account = environment.repositories.accounts().findAll().getFirst();
        account.setStatus(AccountStatus.DISABLED);
        environment.repositories.accounts().save(account);
        assertThrows(SecurityException.class,
                () -> environment.auth.login("guru@uji.test", "GuruPassword2040".toCharArray()));
    }
}
