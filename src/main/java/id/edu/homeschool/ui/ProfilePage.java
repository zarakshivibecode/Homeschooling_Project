package id.edu.homeschool.ui;

import id.edu.homeschool.model.AccountRole;
import id.edu.homeschool.ui.design.Dialogs;
import id.edu.homeschool.ui.design.FormDialog;
import id.edu.homeschool.ui.design.PageHeader;
import id.edu.homeschool.ui.design.ResponsiveGrid;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Profile page: the account details of the signed-in user with inline edit actions. */
public final class ProfilePage extends JPanel implements AppShell.Refreshable {

    private final Workspace workspace;
    private final PageHeader header = new PageHeader("Profil saya",
            "Data akun yang dipakai untuk masuk ke aplikasi.");
    private final ResponsiveGrid details = new ResponsiveGrid(300, 2, Theme.SPACE_5);
    private final JPanel body = new JPanel(new BorderLayout(0, Theme.SPACE_4));

    public ProfilePage(Workspace workspace) {
        super(new BorderLayout(0, Theme.SPACE_4));
        this.workspace = workspace;
        setBackground(Theme.CANVAS);
        setOpaque(false);

        body.setBackground(Theme.CANVAS);
        body.setOpaque(false);

        JPanel card = Ui.card();
        card.add(details, BorderLayout.CENTER);

        JButton edit = Ui.button("Ubah profil", Ui.Variant.PRIMARY);
        JButton password = Ui.button("Ubah kata sandi", Ui.Variant.SECONDARY);
        edit.addActionListener(event -> editProfile());
        password.addActionListener(event -> changePassword());
        JPanel actions = Ui.row();
        actions.add(edit);
        actions.add(password);
        card.add(actions, BorderLayout.SOUTH);

        // The card keeps the height of its content instead of stretching over the whole page.
        body.add(card, BorderLayout.NORTH);
        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        workspace.onChange(this::refresh);
        refresh();
    }

    public void refresh() {
        details.removeAll();
        var session = workspace.session();
        details.add(Ui.definition("Nama lengkap", session.name()));
        details.add(Ui.definition("Email", session.email()));
        details.add(Ui.definition("Peran", session.role() == AccountRole.PENGAJAR ? "Pengajar" : "Siswa"));
        details.add(Ui.definition("Jenjang pendidikan",
                session.educationLevel() == null || session.educationLevel().isBlank() ? "—" : session.educationLevel()));
        details.add(statusDefinition("Status akun", "Aktif"));
        details.add(Ui.definition("Jenis data", workspace.isDemoAccount() ? "Akun demo" : "Akun dibuat sendiri"));
        details.revalidate();
        details.repaint();
    }

    /** A caption with a coloured badge instead of a plain value. */
    private JPanel statusDefinition(String caption, String value) {
        JPanel row = new JPanel(new BorderLayout(0, 3));
        row.setBackground(Theme.CANVAS);
        row.setOpaque(false);
        JLabel label = Ui.subtle(caption);
        label.setFont(Theme.FONT_11);
        row.add(label, BorderLayout.NORTH);
        // The badge keeps its own size instead of being stretched over the whole column.
        JPanel holder = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        holder.setBackground(Theme.CANVAS);
        holder.setOpaque(false);
        holder.add(Ui.badge(value, Theme.statusOf(value)));
        row.add(holder, BorderLayout.CENTER);
        return row;
    }

    private void editProfile() {
        var session = workspace.session();
        List<FormDialog.Field> fields = FormDialog.fields(
                FormDialog.Field.text("name", "Nama lengkap", session.name()),
                FormDialog.Field.optionalText("education", "Jenjang pendidikan", session.educationLevel()));
        FormDialog.show(this, "Ubah profil", "Perbarui data akun Anda.", fields, values -> {
            String name = FormDialog.text(values, "name");
            String education = FormDialog.text(values, "education");
            workspace.auth().updateProfile(workspace.session().token(), name, education);
            workspace.refreshSession();
            Dialogs.success(this, "Profil diperbarui", "Perubahan nama langsung terlihat pada bilah aplikasi.");
            refresh();
        });
    }

    private void changePassword() {
        List<FormDialog.Field> fields = FormDialog.fields(
                FormDialog.Field.password("current", "Kata sandi saat ini"),
                FormDialog.Field.password("next", "Kata sandi baru"),
                FormDialog.Field.password("confirm", "Ulangi kata sandi baru"));
        FormDialog.show(this, "Ubah kata sandi",
                "Kata sandi baru minimal 12 karakter dan memuat huruf serta angka.", fields, values -> {
            char[] current = FormDialog.password(values, "current");
            char[] next = FormDialog.password(values, "next");
            char[] confirm = FormDialog.password(values, "confirm");
            try {
                if (!java.util.Arrays.equals(next, confirm)) {
                    throw new IllegalArgumentException("Konfirmasi kata sandi tidak sama.");
                }
                workspace.auth().changePassword(workspace.session().token(), current, next);
                Dialogs.success(this, "Kata sandi diperbarui", "Gunakan kata sandi baru saat masuk berikutnya.");
            } finally {
                java.util.Arrays.fill(current, '\0');
                java.util.Arrays.fill(next, '\0');
                java.util.Arrays.fill(confirm, '\0');
            }
        });
    }
}
