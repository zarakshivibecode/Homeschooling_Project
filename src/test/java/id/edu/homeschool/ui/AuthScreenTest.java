package id.edu.homeschool.ui;

import id.edu.homeschool.service.UserSession;
import id.edu.homeschool.support.TestEnvironment;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** The login form must really call the authentication service and hand over a session. */
class AuthScreenTest {

    @TempDir Path temp;
    private TestEnvironment environment;

    @BeforeEach void setUp() {
        environment = new TestEnvironment(temp);
        environment.registerTeacher("Pengajar Satu", "guru1@uji.test", "GuruPassword2040");
    }

    @Test void correctCredentialsOpenTheApplicationAndWrongCredentialsShowAnError() throws Exception {
        AtomicReference<UserSession> captured = new AtomicReference<>();
        AuthScreen screen = new AuthScreen(environment.auth, captured::set);

        JButton submit = findButton(screen, "Sign In");
        JTextField email = findField(submit, JTextField.class);
        JPasswordField password = findField(submit, JPasswordField.class);
        assertNotNull(submit);
        assertNotNull(email);
        assertNotNull(password);

        email.setText("guru1@uji.test");
        password.setText("KataSandiSalah123");
        submit.doClick();
        assertNull(captured.get(), "Sesi tidak boleh terbentuk untuk kata sandi salah.");

        password.setText("GuruPassword2040");
        submit.doClick();
        assertNotNull(captured.get());
        assertEquals("Pengajar Satu", captured.get().name());
        assertEquals("guru1@uji.test", captured.get().email());
    }

    private static JButton findButton(Container root, String text) {
        for (Component component : root.getComponents()) {
            if (component instanceof JButton button && text.equals(button.getText())) return button;
            if (component instanceof Container container) {
                JButton found = findButton(container, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    /** Walks up from the button until the container holds exactly one field of the wanted type. */
    private static <T extends Component> T findField(JButton button, Class<T> type) {
        Container container = button.getParent();
        while (container != null) {
            List<T> found = new ArrayList<>();
            collect(container, type, found);
            if (found.size() == 1) return found.getFirst();
            container = container.getParent();
        }
        return null;
    }

    private static <T extends Component> void collect(Container root, Class<T> type, List<T> result) {
        for (Component component : root.getComponents()) {
            // The password field is a subclass of the text field, so it is skipped when a plain
            // text field is requested.
            boolean matches = type.isInstance(component)
                    && !(type == JTextField.class && component instanceof JPasswordField);
            if (matches) result.add(type.cast(component));
            if (component instanceof Container container) collect(container, type, result);
        }
    }
}
