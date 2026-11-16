package id.edu.homeschool.ui;

import id.edu.homeschool.service.ConnectionRequestSummary;
import id.edu.homeschool.ui.design.DataTable;
import id.edu.homeschool.ui.design.Dialogs;
import id.edu.homeschool.ui.design.ModernTextField;
import id.edu.homeschool.ui.design.PageHeader;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Student page: submit an invitation code and follow the status of every connection request. */
public final class StudentConnectionsPage extends JPanel implements AppShell.Refreshable {

    private final Workspace workspace;
    private final PageHeader header = new PageHeader("Koneksi pengajar",
            "Masukkan kode undangan dari pengajar. Koneksi aktif setelah permintaan disetujui.");
    private final JTextField codeField = Ui.textField();
    private final DataTable<ConnectionRequestSummary> table;
    private final JPanel body = new JPanel(new BorderLayout(0, Theme.SPACE_4));

    public StudentConnectionsPage(Workspace workspace) {
        super(new BorderLayout(0, Theme.SPACE_4));
        this.workspace = workspace;
        setBackground(Theme.CANVAS);
        setOpaque(false);

        table = new DataTable<>(new String[]{"Pengajar", "Status", "Diajukan"},
                () -> workspace.connections().requestsForStudent(workspace.session().token()),
                request -> new Object[]{request.teacherName(), statusLabel(request.status()),
                        TeacherRequestsPage.format(request.createdAt())});
        table.setEmptyState("Belum ada permintaan koneksi",
                "Masukkan kode undangan pada formulir di atas untuk mengajukan koneksi ke pengajar.");
        table.setColumnRenderer(1, DataTable.statusRenderer());

        body.setBackground(Theme.CANVAS);
        body.setOpaque(false);
        body.add(codeCard(), BorderLayout.NORTH);
        body.add(table, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        workspace.onChange(this::refresh);
    }

    private JPanel codeCard() {
        JPanel card = Ui.card();
        card.add(Ui.cardHeader("Kirim kode undangan", "Kode terdiri dari 36 karakter huruf dan angka."),
                BorderLayout.NORTH);

        JPanel form = new JPanel(new BorderLayout(Theme.SPACE_3, 0));
        form.setBackground(Theme.CANVAS);
        form.setOpaque(false);
        codeField.setToolTipText("Contoh: 3F9A... (36 karakter)");
        if (codeField instanceof ModernTextField modern) {
            modern.setPlaceholder("Tempel kode undangan di sini");
        }
        codeField.setPreferredSize(new Dimension(320, Theme.CONTROL_HEIGHT));
        JButton submit = Ui.button("Kirim permintaan", Ui.Variant.PRIMARY);
        submit.addActionListener(event -> submit());
        form.add(codeField, BorderLayout.CENTER);
        form.add(submit, BorderLayout.EAST);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private void submit() {
        String code = codeField.getText().trim().toUpperCase(java.util.Locale.ROOT);
        if (code.isEmpty()) {
            Dialogs.warn(this, "Kode belum diisi", "Masukkan kode undangan yang Anda terima dari pengajar.");
            return;
        }
        try {
            ConnectionRequestSummary request = workspace.connections().requestConnection(workspace.session().token(), code);
            codeField.setText("");
            Dialogs.success(this, "Permintaan terkirim",
                    "Permintaan koneksi ke " + request.teacherName() + " sedang menunggu persetujuan.");
            table.refresh();
        } catch (RuntimeException exception) {
            Dialogs.error(this, "Permintaan tidak dapat dikirim", exception.getMessage());
        }
    }

    public void refresh() {
        table.refresh();
    }

    static String statusLabel(id.edu.homeschool.model.ConnectionRequestStatus status) {
        return Lookups.studentStatus(status);
    }
}
