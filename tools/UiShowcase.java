import id.edu.homeschool.repository.ApplicationRepositories;
import id.edu.homeschool.service.AuthService;
import id.edu.homeschool.service.ConnectionService;
import id.edu.homeschool.service.DemoDataService;
import id.edu.homeschool.service.LearningWorkspaceService;
import id.edu.homeschool.service.SessionRegistry;
import id.edu.homeschool.service.UserSession;
import id.edu.homeschool.ui.AppShell;
import id.edu.homeschool.ui.AuthScreen;
import id.edu.homeschool.ui.Workspace;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Manual showcase harness. It seeds a data directory with the built-in demo curriculum, opens the
 * real application screens and writes PNG screenshots so the rendered interface can be reviewed
 * outside the IDE.
 */
public final class UiShowcase {

    private static Path outputDirectory;

    public static void main(String[] args) throws Exception {
        Path dataDirectory = Path.of("target", "showcase-data");
        deleteDirectory(dataDirectory);
        Files.createDirectories(dataDirectory);
        outputDirectory = Path.of("target", "screenshots");
        Files.createDirectories(outputDirectory);

        ApplicationRepositories repositories = new ApplicationRepositories(dataDirectory);
        SessionRegistry sessions = new SessionRegistry(repositories.accounts());
        AuthService auth = new AuthService(repositories.accounts(), sessions);
        ConnectionService connections = new ConnectionService(repositories.accounts(), repositories.invitations(),
                repositories.connectionRequests(), repositories.connections(), sessions);
        LearningWorkspaceService learning = new LearningWorkspaceService(repositories, sessions);
        DemoDataService demo = new DemoDataService(repositories, auth, connections, learning);
        demo.seedIfNeeded();

        UserSession teacher = auth.login("rani@demo.test", "DemoGuru2024!".toCharArray());
        UserSession student = auth.login("dimas@demo.test", "DemoSiswa2024!".toCharArray());

        SwingUtilities.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // The showcase falls back to the default look and feel.
            }
            id.edu.homeschool.ui.design.LookAndFeel.apply();
        });

        captureAuth(auth, demo);
        captureRegister(auth, demo);
        captureTeacher(auth, connections, learning, demo, teacher);
        captureStudent(auth, connections, learning, demo, student);
        captureEmptyState(auth, connections, learning, demo);
        captureMaterialDetail(auth, connections, learning, demo, teacher);
        captureAddForm(auth, connections, learning, demo, teacher);
        captureNarrow(auth, connections, learning, demo, teacher);
        System.out.println("Screenshots written to " + outputDirectory.toAbsolutePath());
        System.exit(0);
    }

    /** Captures the smallest supported window size to check the responsive layout. */
    private static void captureNarrow(AuthService auth, ConnectionService connections,
            LearningWorkspaceService learning, DemoDataService demo, UserSession teacher) throws Exception {
        AppShell shell = new AppShell(new Workspace(auth, connections, learning, null, demo, teacher), () -> { });
        JFrame frame = shellFrame(shell);
        frame.setSize(1120, 720);
        shell.showPage("dashboard");
        capture(frame, "23-ringan-dashboard");
        shell.showPage("tasks");
        capture(frame, "24-ringan-tugas");
        frame.setSize(1024, 700);
        shell.showPage("materials");
        capture(frame, "25-sempit-materi");
    }

    /** Opens the real "Tambah" dialog of the subject page and captures it. */
    private static void captureAddForm(AuthService auth, ConnectionService connections,
            LearningWorkspaceService learning, DemoDataService demo, UserSession teacher) throws Exception {
        AppShell shell = new AppShell(new Workspace(auth, connections, learning, null, demo, teacher), () -> { });
        JFrame frame = shellFrame(shell);
        shell.showPage("subjects");
        SwingUtilities.invokeAndWait(() -> {
            frame.setVisible(true);
            frame.invalidate();
            frame.validate();
        });
        Thread.sleep(600);

        javax.swing.JButton add = findButton(shell.page("subjects"), "Tambah");
        if (add == null) {
            System.out.println("Tombol Tambah tidak ditemukan.");
            return;
        }

        Thread watcher = new Thread(() -> {
            try {
                Thread.sleep(2200);
                java.awt.Window dialog = findDialog();
                if (dialog == null) return;
                java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                        dialog.getWidth(), dialog.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
                SwingUtilities.invokeAndWait(() -> {
                    Graphics2D graphics = image.createGraphics();
                    dialog.printAll(graphics);
                    graphics.dispose();
                });
                File target = outputDirectory.resolve("22-form-tambah.png").toFile();
                ImageIO.write(image, "png", target);
                System.out.println("Captured " + target.getAbsolutePath());
                javax.swing.JButton cancel = findButton(dialog, "Batal");
                if (cancel != null) SwingUtilities.invokeAndWait(cancel::doClick);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        watcher.setDaemon(true);
        watcher.start();

        SwingUtilities.invokeLater(add::doClick);
        watcher.join(8000);
        frame.setVisible(false);
    }

    /** Finds a button by its text inside a container tree. */
    private static javax.swing.JButton findButton(java.awt.Container root, String text) {
        for (java.awt.Component component : root.getComponents()) {
            if (component instanceof javax.swing.JButton button && text.equals(button.getText())) return button;
            if (component instanceof java.awt.Container container) {
                javax.swing.JButton found = findButton(container, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    /** Opens the real material detail window and captures what the user sees. */
    private static void captureMaterialDetail(AuthService auth, ConnectionService connections,
            LearningWorkspaceService learning, DemoDataService demo, UserSession teacher) throws Exception {
        String studentId = new ApplicationRepositories(Path.of("target", "showcase-data")).accounts().findAll().stream()
                .filter(account -> account.getRole() == id.edu.homeschool.model.AccountRole.SISWA)
                .map(id.edu.homeschool.model.UserAccount::getId).findFirst().orElseThrow();
        var material = learning.materials(teacher.token(), studentId).getFirst();

        JFrame frame = new JFrame("Detail");
        frame.setSize(1360, 860);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        Thread.sleep(400);

        Thread watcher = new Thread(() -> {
            try {
                Thread.sleep(1800);
                java.awt.Window dialog = findDialog();
                if (dialog == null) return;
                java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                        dialog.getWidth(), dialog.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
                SwingUtilities.invokeAndWait(() -> {
                    Graphics2D graphics = image.createGraphics();
                    dialog.printAll(graphics);
                    graphics.dispose();
                });
                File target = outputDirectory.resolve("21-detail-materi.png").toFile();
                ImageIO.write(image, "png", target);
                System.out.println("Captured " + target.getAbsolutePath());
                SwingUtilities.invokeAndWait(dialog::dispose);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        watcher.setDaemon(true);
        watcher.start();

        SwingUtilities.invokeAndWait(() -> id.edu.homeschool.ui.design.DetailDialog.show(frame, material.getTitle(),
                "Mata pelajaran Matematika",
                id.edu.homeschool.ui.design.DetailDialog.sections(
                        new id.edu.homeschool.ui.design.DetailDialog.Section("Tujuan pembelajaran", material.getObjectives()),
                        new id.edu.homeschool.ui.design.DetailDialog.Section("Materi", material.getContent()),
                        new id.edu.homeschool.ui.design.DetailDialog.Section("Lokasi / URL", material.getLocation())),
                List.of(id.edu.homeschool.ui.design.Ui.tag("Data demo"))));
        watcher.join(6000);
        frame.setVisible(false);
    }

    private static java.awt.Window findDialog() {
        for (java.awt.Window window : java.awt.Window.getWindows()) {
            if (window instanceof javax.swing.JDialog && window.isVisible()) return window;
        }
        return null;
    }

    /** Captures the registration mode of the authentication screen. */
    private static void captureRegister(AuthService auth, DemoDataService demo) throws Exception {
        JFrame frame = new JFrame("Auth");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1360, 860);
        frame.setLocationRelativeTo(null);
        id.edu.homeschool.ui.design.GlassCanvas canvas = new id.edu.homeschool.ui.design.GlassCanvas();
        canvas.setLayout(new java.awt.CardLayout());
        AuthScreen screen = new AuthScreen(auth, session -> { }, demo);
        canvas.add(screen, "auth");
        ((java.awt.CardLayout) canvas.getLayout()).show(canvas, "auth");
        frame.setContentPane(canvas);

        SwingUtilities.invokeAndWait(() -> {
            frame.setVisible(true);
            frame.invalidate();
            frame.validate();
        });
        Thread.sleep(500);
        // The link that switches to the registration mode is part of the real screen.
        javax.swing.JButton switcher = findButton(screen, "Belum punya akun? Daftar");
        if (switcher != null) SwingUtilities.invokeAndWait(switcher::doClick);
        capture(frame, "01b-register");
    }

    private static void captureAuth(AuthService auth, DemoDataService demo) throws Exception {
        JFrame frame = new JFrame("Auth");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1360, 860);
        frame.setLocationRelativeTo(null);
        id.edu.homeschool.ui.design.GlassCanvas canvas = new id.edu.homeschool.ui.design.GlassCanvas();
        canvas.setLayout(new java.awt.CardLayout());
        canvas.add(new AuthScreen(auth, session -> { }, demo), "auth");
        ((java.awt.CardLayout) canvas.getLayout()).show(canvas, "auth");
        frame.setContentPane(canvas);
        capture(frame, "01-login");
    }

    private static void captureTeacher(AuthService auth, ConnectionService connections, LearningWorkspaceService learning,
            DemoDataService demo, UserSession teacher) throws Exception {
        AppShell shell = new AppShell(new Workspace(auth, connections, learning, null, demo, teacher), () -> { });
        JFrame frame = shellFrame(shell);
        capture(frame, "02-pengajar-dashboard");
        shell.showPage("subjects");
        capture(frame, "03-pengajar-mata-pelajaran");
        shell.showPage("materials");
        capture(frame, "04-pengajar-materi");
        shell.showPage("tasks");
        capture(frame, "05-pengajar-tugas");
        shell.showPage("schedule");
        capture(frame, "06-pengajar-jadwal");
        shell.showPage("evaluations");
        capture(frame, "07-pengajar-evaluasi");
        shell.showPage("progress");
        capture(frame, "08-pengajar-laporan-progres");
        shell.showPage("students");
        capture(frame, "09-pengajar-data-siswa");
        shell.showPage("requests");
        capture(frame, "10-pengajar-permintaan");
        shell.showPage("invitations");
        capture(frame, "11-pengajar-undangan");
        shell.showPage("profile");
        capture(frame, "12-pengajar-profil");
    }

    private static void captureStudent(AuthService auth, ConnectionService connections, LearningWorkspaceService learning,
            DemoDataService demo, UserSession student) throws Exception {
        AppShell shell = new AppShell(new Workspace(auth, connections, learning, null, demo, student), () -> { });
        JFrame frame = shellFrame(shell);
        capture(frame, "13-siswa-dashboard");
        shell.showPage("materials");
        capture(frame, "14-siswa-materi");
        shell.showPage("tasks");
        capture(frame, "15-siswa-tugas");
        shell.showPage("progress");
        capture(frame, "16-siswa-laporan-progres");
        shell.showPage("teachers");
        capture(frame, "17-siswa-data-pengajar");
        shell.showPage("connections");
        capture(frame, "18-siswa-koneksi");
    }

    /** A teacher without connected students must see an informative empty state. */
    private static void captureEmptyState(AuthService auth, ConnectionService connections,
            LearningWorkspaceService learning, DemoDataService demo) throws Exception {
        auth.registerTeacher("Budi Santoso", "budi@contoh.test", "GuruPassword2042".toCharArray());
        UserSession lonelyTeacher = auth.login("budi@contoh.test", "GuruPassword2042".toCharArray());
        AppShell shell = new AppShell(new Workspace(auth, connections, learning, null, demo, lonelyTeacher), () -> { });
        JFrame frame = shellFrame(shell);
        capture(frame, "19-kosong-dashboard-pengajar");
        shell.showPage("subjects");
        capture(frame, "20-kosong-mata-pelajaran");
    }

    private static JFrame shellFrame(AppShell shell) {
        JFrame frame = new JFrame("App");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1360, 860);
        frame.setLocationRelativeTo(null);
        id.edu.homeschool.ui.design.GlassCanvas canvas = new id.edu.homeschool.ui.design.GlassCanvas();
        canvas.setLayout(new java.awt.CardLayout());
        canvas.add(shell, "app");
        ((java.awt.CardLayout) canvas.getLayout()).show(canvas, "app");
        frame.setContentPane(canvas);
        return frame;
    }

    private static void capture(JFrame frame, String name) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame.setVisible(true);
            frame.invalidate();
            frame.validate();
        });
        Thread.sleep(700);
        BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        frame.printAll(graphics);
        graphics.dispose();
        File target = outputDirectory.resolve(name + ".png").toFile();
        ImageIO.write(image, "png", target);
        SwingUtilities.invokeAndWait(() -> frame.setVisible(false));
        System.out.println("Captured " + target.getAbsolutePath());
    }

    private static void deleteDirectory(Path directory) throws Exception {
        if (!Files.exists(directory)) return;
        try (var paths = Files.walk(directory)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception ignored) {
                    // Nothing to clean up further.
                }
            });
        }
    }
}
