package id.edu.homeschool.ui;

import id.edu.homeschool.model.AccountRole;
import id.edu.homeschool.service.AuthService;
import id.edu.homeschool.service.DemoDataService;
import id.edu.homeschool.service.UserSession;
import id.edu.homeschool.ui.design.AppLogo;
import id.edu.homeschool.ui.design.GlassPanel;
import id.edu.homeschool.ui.design.ModernTextField;
import id.edu.homeschool.ui.design.Notice;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import id.edu.homeschool.ui.design.WrappingLabel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Login and registration screen. The screen pairs a glass brand panel with the form card, so the
 * identity of the application and the task of signing in are both visible at a glance. Switching
 * between the two modes never loses the layout.
 */
public final class AuthScreen extends JPanel {

    private static final String LOGIN = "login";
    private static final String REGISTER = "register";
    private static final int FIELD_WIDTH = 340;
    private static final int HERO_WIDTH = 380;
    /** Below this width the brand panel is hidden so the form always stays readable. */
    private static final int HERO_BREAKPOINT = 880;

    private final AuthService auth;
    private final Consumer<UserSession> onAuthenticated;
    private final CardLayout modes = new CardLayout();
    private final JPanel modeArea = new JPanel(modes);
    private final Notice notice = new Notice();
    private final JPanel hero = new JPanel(new BorderLayout(0, Theme.SPACE_4));

    public AuthScreen(AuthService auth, Consumer<UserSession> onAuthenticated) {
        this(auth, onAuthenticated, null);
    }

    public AuthScreen(AuthService auth, Consumer<UserSession> onAuthenticated, DemoDataService demo) {
        super(new BorderLayout());
        this.auth = auth;
        this.onAuthenticated = onAuthenticated;
        setBackground(Theme.CANVAS);
        setOpaque(true);

        modeArea.setBackground(Theme.CANVAS);
        modeArea.setOpaque(false);
        modeArea.add(loginPanel(demo), LOGIN);
        modeArea.add(registerPanel(), REGISTER);
        modes.show(modeArea, LOGIN);

        hero.setBackground(Theme.CANVAS);
        hero.setOpaque(false);
        hero.setPreferredSize(new Dimension(HERO_WIDTH, 0));
        hero.add(brandPanel(demo), BorderLayout.CENTER);

        // The brand panel and the form sit next to each other as one centred pair.
        JPanel pair = new JPanel(new BorderLayout(Theme.SPACE_5, 0));
        pair.setBackground(Theme.CANVAS);
        pair.setOpaque(false);
        pair.add(hero, BorderLayout.WEST);
        pair.add(modeArea, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Theme.CANVAS);
        wrapper.setOpaque(false);
        GridBagConstraints pairConstraints = new GridBagConstraints();
        pairConstraints.gridx = 0;
        pairConstraints.gridy = 0;
        wrapper.add(pair, pairConstraints);

        add(wrapper, BorderLayout.CENTER);
        add(noticeArea(), BorderLayout.SOUTH);

        // The brand panel is dropped on a narrow window so the form is never squeezed.
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent event) {
                hero.setVisible(getWidth() >= HERO_BREAKPOINT);
            }
        });
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        id.edu.homeschool.ui.design.Glass.prepare(g);
        id.edu.homeschool.ui.design.Glass.paintCanvas(g, getWidth(), getHeight());
        g.dispose();
    }

    /** The area that carries the validation message below the form. */
    private JComponent noticeArea() {
        JPanel area = new JPanel(new GridBagLayout());
        area.setBackground(Theme.CANVAS);
        area.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, Theme.SPACE_6, Theme.SPACE_5, Theme.SPACE_6);
        area.add(notice, constraints);
        return area;
    }

    /* ---------------- brand ---------------- */

    /** The glass panel that carries the identity of the application. */
    private JComponent brandPanel(DemoDataService demo) {
        GlassPanel panel = new GlassPanel(new BorderLayout(0, Theme.SPACE_5));
        panel.setFill(Theme.GLASS_CARD);
        panel.setRadius(Theme.RADIUS_LG);
        panel.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_6, Theme.SPACE_6,
                Theme.SPACE_6, Theme.SPACE_6));

        JPanel head = new JPanel(new BorderLayout(0, Theme.SPACE_4));
        head.setBackground(Theme.CANVAS);
        head.setOpaque(false);

        JPanel mark = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        mark.setBackground(Theme.CANVAS);
        mark.setOpaque(false);
        mark.setAlignmentX(LEFT_ALIGNMENT);
        mark.add(AppLogo.mark(56));

        JLabel wordmark = new JLabel(AppLogo.WORDMARK);
        wordmark.setFont(new java.awt.Font(Theme.FONT_20.getFamily(), java.awt.Font.BOLD, 20));
        wordmark.setForeground(Theme.TEXT);

        JLabel tagline = Ui.muted("Your Learning Journey Starts Here");
        tagline.setFont(Theme.FONT_12);

        JPanel names = new JPanel(new BorderLayout(0, 2));
        names.setBackground(Theme.CANVAS);
        names.setOpaque(false);
        names.add(wordmark, BorderLayout.NORTH);
        names.add(tagline, BorderLayout.CENTER);

        head.add(mark, BorderLayout.NORTH);
        head.add(names, BorderLayout.CENTER);

        JLabel intro = Ui.paragraph("Satu ruang untuk mengelola perjalanan belajar homeschooling dengan lebih mudah dan terorganisir.");
        intro.setFont(Theme.FONT_14);
        intro.setForeground(Theme.TEXT_MUTED);
        JPanel features = new JPanel();
        features.setLayout(new BoxLayout(features, BoxLayout.Y_AXIS));
        features.setBackground(Theme.CANVAS);
        features.setOpaque(false);
        for (String feature : new String[]{
                "Jadwal belajar, materi, dan tugas dalam satu tempat",
                "Evaluasi dan laporan progres untuk setiap siswa",
                "Koneksi aman antara pengajar dan siswa"}) {
            features.add(featureRow(feature));
            features.add(Box.createVerticalStrut(Theme.SPACE_3));
        }

        panel.add(head, BorderLayout.NORTH);
        JPanel story = new JPanel();
        story.setLayout(new BoxLayout(story, BoxLayout.Y_AXIS));
        story.setOpaque(false);
        story.add(intro);
        story.add(Box.createVerticalStrut(Theme.SPACE_6));
        story.add(features);
        panel.add(story, BorderLayout.CENTER);
        if (demo != null) {
            List<DemoDataService.DemoAccount> accounts = demo.demoAccounts();
            if (!accounts.isEmpty()) panel.add(demoPanel(accounts), BorderLayout.SOUTH);
        }
        return panel;
    }

    /** One line of the feature list, marked with a small accent dot. */
    private JComponent featureRow(String text) {
        JPanel row = new JPanel(new BorderLayout(Theme.SPACE_3, 0));
        row.setBackground(Theme.CANVAS);
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.SPACE_8));

        JLabel dot = new JLabel("•");
        dot.setFont(Theme.FONT_14);
        dot.setForeground(Theme.ACCENT);
        dot.setPreferredSize(new Dimension(10, 0));

        // The text wraps to the width of the panel instead of being cut off.
        WrappingLabel label = Ui.paragraph(text);
        label.setFont(Theme.FONT_12);
        label.setForeground(Theme.TEXT_MUTED);
        row.add(dot, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        return row;
    }

    /* ---------------- login ---------------- */

    private JPanel loginPanel(DemoDataService demo) {
        GlassPanel card = formCard("Welcome Back!", "Masuk ke akun HomeLearn Anda.");

        JTextField email = Ui.textField();
        email.setToolTipText("Alamat email yang dipakai saat registrasi");
        if (email instanceof ModernTextField modern) {
            modern.setPlaceholder("nama@email.com");
        }
        JPasswordField password = Ui.passwordField();

        JButton submit = Ui.button("Sign In", Ui.Variant.PRIMARY);
        password.addActionListener(event -> submit.doClick());
        email.addActionListener(event -> password.requestFocusInWindow());
        submit.addActionListener(event -> {
            char[] secret = password.getPassword();
            try {
                UserSession session = auth.login(email.getText(), secret);
                onAuthenticated.accept(session);
            } catch (RuntimeException exception) {
                showError(exception.getMessage());
            } finally {
                clear(secret);
                password.setText("");
            }
        });

        JButton switcher = linkButton("Belum punya akun? Daftar");
        switcher.addActionListener(event -> {
            clearError();
            modes.show(modeArea, REGISTER);
        });

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBackground(Theme.CANVAS);
        fields.setOpaque(false);
        fields.add(field("Email", email));
        fields.add(field("Kata sandi", password));

        JPanel buttons = new JPanel(new BorderLayout(0, Theme.SPACE_2));
        buttons.setBackground(Theme.CANVAS);
        buttons.setOpaque(false);
        buttons.add(submit, BorderLayout.NORTH);
        buttons.add(switcher, BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(0, Theme.SPACE_4));
        body.setBackground(Theme.CANVAS);
        body.setOpaque(false);
        body.add(fields, BorderLayout.CENTER);
        body.add(buttons, BorderLayout.SOUTH);
        card.add(body, BorderLayout.CENTER);
        return assemble(card);
    }

    /** A small glass strip that lists the demo accounts so the application can be tried at once. */
    private JComponent demoPanel(List<DemoDataService.DemoAccount> accounts) {
        GlassPanel panel = new GlassPanel(new BorderLayout(0, Theme.SPACE_2));
        panel.setFill(Theme.alpha(Theme.ACCENT_SOFT, 190));
        panel.setShadow(false);
        panel.setRadius(Theme.RADIUS_SM);
        panel.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_3, Theme.SPACE_3,
                Theme.SPACE_3, Theme.SPACE_3));

        JLabel title = Ui.subtle("Akun demo siap pakai");
        title.setFont(Theme.FONT_11);
        panel.add(title, BorderLayout.NORTH);

        JPanel rows = new JPanel(new java.awt.GridLayout(0, 1, 0, Theme.SPACE_1));
        rows.setBackground(Theme.CANVAS);
        rows.setOpaque(false);
        for (DemoDataService.DemoAccount account : accounts) {
            String label = account.role() == AccountRole.PENGAJAR
                    ? "Pengajar " + account.name() : "Siswa " + account.name();
            JLabel row = Ui.subtle(label + " · " + account.email() + " · " + account.password());
            row.setFont(Theme.FONT_10);
            row.setToolTipText(label + " · " + account.email() + " · " + account.password());
            rows.add(row);
        }
        panel.add(rows, BorderLayout.CENTER);
        return panel;
    }

    /* ---------------- registration ---------------- */

    private JPanel registerPanel() {
        GlassPanel card = formCard("Buat akun baru",
                "Pilih peran akun. Data dapat dilengkapi kembali setelah akun dibuat.");

        JTextField name = Ui.textField();
        if (name instanceof ModernTextField modern) {
            modern.setPlaceholder("Nama lengkap");
        }
        JTextField email = Ui.textField();
        if (email instanceof ModernTextField modern) {
            modern.setPlaceholder("nama@email.com");
        }
        JComboBox<AccountRole> role = Ui.combo();
        role.addItem(AccountRole.SISWA);
        role.addItem(AccountRole.PENGAJAR);
        JTextField education = Ui.textField();
        education.setToolTipText("Contoh: SD, SMP, SMA");
        if (education instanceof ModernTextField modern) {
            modern.setPlaceholder("Contoh: SMP");
        }

        JPanel educationRow = field("Jenjang pendidikan *", education);
        role.addActionListener(event -> {
            boolean student = role.getSelectedItem() == AccountRole.SISWA;
            educationRow.setVisible(student);
            education.setEnabled(student);
            revalidate();
            repaint();
        });

        JPasswordField password = Ui.passwordField();
        JPasswordField confirmation = Ui.passwordField();

        JButton submit = Ui.button("Daftar", Ui.Variant.PRIMARY);
        submit.addActionListener(event -> {
            char[] secret = password.getPassword();
            char[] confirm = confirmation.getPassword();
            try {
                if (!java.util.Arrays.equals(secret, confirm)) {
                    throw new IllegalArgumentException("Konfirmasi kata sandi tidak sama.");
                }
                AccountRole selected = (AccountRole) role.getSelectedItem();
                if (selected == AccountRole.SISWA) {
                    auth.registerStudent(name.getText(), email.getText(), secret, education.getText());
                } else {
                    auth.registerTeacher(name.getText(), email.getText(), secret);
                }
                name.setText("");
                email.setText("");
                education.setText("");
                showNotice("Akun berhasil dibuat. Silakan masuk dengan akun baru Anda.");
                modes.show(modeArea, LOGIN);
            } catch (RuntimeException exception) {
                showError(exception.getMessage());
            } finally {
                clear(secret);
                clear(confirm);
                password.setText("");
                confirmation.setText("");
            }
        });

        JButton switcher = linkButton("Sudah punya akun? Masuk");
        switcher.addActionListener(event -> {
            clearError();
            modes.show(modeArea, LOGIN);
        });

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBackground(Theme.CANVAS);
        fields.setOpaque(false);
        fields.add(field("Nama lengkap", name));
        fields.add(field("Email", email));
        fields.add(field("Peran akun", role));
        fields.add(educationRow);
        fields.add(field("Kata sandi", password));
        fields.add(field("Ulangi kata sandi", confirmation));
        JLabel hint = Ui.subtle("Kata sandi minimal 12 karakter dan memuat huruf serta angka.");
        hint.setAlignmentX(LEFT_ALIGNMENT);
        fields.add(hint);
        fields.add(Box.createVerticalStrut(Theme.SPACE_2));

        JPanel buttons = new JPanel(new BorderLayout(0, Theme.SPACE_2));
        buttons.setBackground(Theme.CANVAS);
        buttons.setOpaque(false);
        buttons.add(submit, BorderLayout.NORTH);
        buttons.add(switcher, BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(0, Theme.SPACE_4));
        body.setBackground(Theme.CANVAS);
        body.setOpaque(false);
        body.add(fields, BorderLayout.CENTER);
        body.add(buttons, BorderLayout.SOUTH);
        card.add(body, BorderLayout.CENTER);
        return assemble(card);
    }

    /* ---------------- shared building blocks ---------------- */

    private GlassPanel formCard(String title, String description) {
        GlassPanel card = new GlassPanel(new BorderLayout(0, Theme.SPACE_5));
        card.setFill(Theme.GLASS_CARD);
        card.setRadius(Theme.RADIUS_LG);
        card.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_6, Theme.SPACE_6,
                Theme.SPACE_6, Theme.SPACE_6));

        JPanel head = new JPanel(new BorderLayout(0, Theme.SPACE_2));
        head.setBackground(Theme.CANVAS);
        head.setOpaque(false);
        JPanel brand = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, Theme.SPACE_2, 0));
        brand.setBackground(Theme.CANVAS);
        brand.setOpaque(false);
        brand.add(AppLogo.mark(40));

        JLabel wordmark = new JLabel(AppLogo.WORDMARK);
        wordmark.setFont(Theme.FONT_14_SEMIBOLD);
        wordmark.setForeground(Theme.TEXT);
        brand.add(wordmark);

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.FONT_20);
        heading.setForeground(Theme.TEXT);
        JLabel body = Ui.muted(description);
        body.setFont(Theme.FONT_12);

        head.add(brand, BorderLayout.NORTH);
        head.add(heading, BorderLayout.CENTER);
        head.add(body, BorderLayout.SOUTH);
        card.add(head, BorderLayout.NORTH);
        return card;
    }

    /** Places the form card in the centre of the canvas. */
    private JPanel assemble(JComponent card) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Theme.CANVAS);
        wrapper.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(Theme.SPACE_6, Theme.SPACE_6, Theme.SPACE_6, Theme.SPACE_6);
        wrapper.add(card, constraints);
        return wrapper;
    }

    private static JPanel field(String label, JComponent editor) {
        JPanel row = new JPanel(new BorderLayout(0, 6));
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setBackground(Theme.CANVAS);
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel caption = new JLabel(label);
        caption.setFont(Theme.FONT_13);
        caption.setForeground(Theme.TEXT);
        caption.setAlignmentX(LEFT_ALIGNMENT);
        row.add(caption);
        row.add(editor);
        row.add(Box.createVerticalStrut(Theme.SPACE_3));
        editor.setAlignmentX(LEFT_ALIGNMENT);
        editor.setPreferredSize(new Dimension(FIELD_WIDTH, Theme.CONTROL_HEIGHT));
        editor.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.CONTROL_HEIGHT));
        return row;
    }

    private void showError(String message) {
        notice.show(message == null || message.isBlank() ? "Operasi tidak dapat dilanjutkan." : message,
                new Theme.StatusStyle(Theme.DANGER, Theme.DANGER_SOFT));
    }

    private void showNotice(String message) {
        notice.show(message == null ? "" : message,
                new Theme.StatusStyle(Theme.SUCCESS, Theme.SUCCESS_SOFT));
    }

    private void clearError() {
        notice.clear();
    }

    private static void clear(char[] secret) {
        java.util.Arrays.fill(secret, '\0');
    }

    private static JButton linkButton(String text) {
        JButton button = Ui.button(text, Ui.Variant.LINK);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        return button;
    }
}
