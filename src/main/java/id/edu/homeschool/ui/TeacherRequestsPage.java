package id.edu.homeschool.ui;

import id.edu.homeschool.service.ConnectionRequestSummary;
import id.edu.homeschool.ui.design.DataTable;
import id.edu.homeschool.ui.design.Dialogs;
import id.edu.homeschool.ui.design.PageHeader;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/** Teacher page listing the connection requests waiting for a decision. */
public final class TeacherRequestsPage extends JPanel implements AppShell.Refreshable {

    private final Workspace workspace;
    private final PageHeader header = new PageHeader("Permintaan siswa",
            "Tinjau permintaan koneksi siswa sebelum memberikan akses ke data belajarnya.");
    private final DataTable<ConnectionRequestSummary> table;

    public TeacherRequestsPage(Workspace workspace) {
        super(new BorderLayout(0, Theme.SPACE_4));
        this.workspace = workspace;
        setBackground(Theme.CANVAS);
        setOpaque(false);

        table = new DataTable<>(new String[]{"Nama siswa", "Diajukan", "Status"},
                () -> workspace.connections().pendingForTeacher(workspace.session().token()),
                request -> new Object[]{request.studentName(), format(request.createdAt()), statusLabel(request)});
        table.setEmptyState("Tidak ada permintaan tertunda",
                "Permintaan koneksi dari siswa akan muncul di sini setelah kode undangan digunakan.");
        table.setColumnRenderer(2, DataTable.statusRenderer());

        JButton approve = Ui.button("Setujui", Ui.Variant.PRIMARY);
        JButton reject = Ui.button("Tolak", Ui.Variant.DANGER);
        approve.addActionListener(event -> decide(true));
        reject.addActionListener(event -> decide(false));
        table.addAction(approve);
        table.addAction(reject);

        add(header, BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
        workspace.onChange(this::refresh);
    }

    private void decide(boolean approve) {
        ConnectionRequestSummary selected = table.selected();
        if (selected == null) {
            Dialogs.info(this, "Belum ada pilihan", "Pilih satu permintaan pada tabel sebelum memprosesnya.");
            return;
        }
        try {
            if (approve) {
                workspace.connections().approve(workspace.session().token(), selected.id());
                Dialogs.success(this, "Koneksi disetujui",
                        "Siswa " + selected.studentName() + " kini dapat melihat materi, tugas, dan evaluasi Anda.");
            } else {
                workspace.connections().reject(workspace.session().token(), selected.id());
                Dialogs.info(this, "Permintaan ditolak",
                        "Siswa " + selected.studentName() + " belum menerima akses. Kode undangan tetap dapat dipakai untuk mengulang permintaan.");
            }
            table.refresh();
        } catch (RuntimeException exception) {
            Dialogs.error(this, "Permintaan tidak dapat diproses", exception.getMessage());
        }
    }

    public void refresh() {
        table.refresh();
    }

    static String format(java.time.Instant instant) {
        return Lookups.moment(instant);
    }

    static String statusLabel(ConnectionRequestSummary request) {
        return Lookups.teacherStatus(request.status());
    }
}
