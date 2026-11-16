package id.edu.homeschool.ui;

import id.edu.homeschool.model.AccountRole;
import id.edu.homeschool.model.Pengajar;
import id.edu.homeschool.model.Siswa;
import id.edu.homeschool.service.PersonSummary;
import id.edu.homeschool.ui.design.DataTable;
import id.edu.homeschool.ui.design.DetailDialog;
import id.edu.homeschool.ui.design.Dialogs;
import id.edu.homeschool.ui.design.PageHeader;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Directory of the people connected to the signed-in account. The list is rebuilt on every visit
 * so a newly approved connection appears immediately. Records that predate the account system are
 * listed separately as an archive so the older data files stay reachable.
 */
public final class DirectoryPage extends JPanel implements AppShell.Refreshable {

    private final Workspace workspace;
    private final PageHeader header;
    private final DataTable<PersonSummary> table;
    private final JPanel archive = new JPanel(new BorderLayout(0, Theme.SPACE_3));
    private final boolean teacherDirectory;

    /** The table of connected people; exposed so the interface tests can read the rows. */
    DataTable<?> table() { return table; }

    public DirectoryPage(Workspace workspace, boolean teacherDirectory) {
        super(new BorderLayout(0, Theme.SPACE_4));
        this.workspace = workspace;
        this.teacherDirectory = teacherDirectory;
        setBackground(Theme.CANVAS);
        setOpaque(false);

        header = new PageHeader(teacherDirectory ? "Data pengajar" : "Data siswa",
                teacherDirectory
                        ? "Pengajar yang terhubung dengan akun Anda beserta informasi pembelajarannya."
                        : "Siswa yang terhubung dengan akun Anda beserta ringkasan progres belajarnya.");
        table = new DataTable<>(teacherDirectory
                        ? new String[]{"Nama", "Email", "Materi dibagi", "Nilai rata-rata"}
                        : new String[]{"Nama", "Email", "Jenjang", "Materi", "Tugas selesai", "Nilai rata-rata"},
                this::load, this::row);
        table.setEmptyState(teacherDirectory ? "Belum ada pengajar terhubung" : "Belum ada siswa terhubung",
                teacherDirectory
                        ? "Masukkan kode undangan dari pengajar pada halaman Koneksi pengajar untuk melihat datanya di sini."
                        : "Siswa muncul di sini setelah pengajar menyetujui permintaan koneksi.");
        // The teacher table has no education column, so the filter only belongs to the student list.
        if (!teacherDirectory) {
            table.addFilter("Jenjang", this::levels, summary -> summary.educationLevel());
        }

        JButton view = Ui.button("Lihat detail", Ui.Variant.SECONDARY);
        view.addActionListener(event -> showDetail());
        table.addAction(view);
        if (workspace.isTeacher()) {
            JButton end = Ui.button("Akhiri koneksi", Ui.Variant.SECONDARY);
            end.addActionListener(event -> endConnection());
            table.addAction(end);
        }

        archive.setBackground(Theme.CANVAS);
        archive.setOpaque(false);

        JPanel body = new JPanel(new BorderLayout(0, Theme.SPACE_4));
        body.setBackground(Theme.CANVAS);
        body.setOpaque(false);
        body.add(table, BorderLayout.CENTER);
        body.add(archive, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        workspace.onChange(this::refresh);
        refresh();
    }

    private List<PersonSummary> load() {
        // A failing repository call is not swallowed here: the table shows the error state.
        if (teacherDirectory) {
            if (workspace.isStudent()) return workspace.learning().teacherSummaries(workspace.session().token());
            return List.of(ownSummary());
        }
        if (workspace.isTeacher()) return workspace.learning().studentSummaries(workspace.session().token());
        return List.of(ownSummary());
    }

    private PersonSummary ownSummary() {
        return new PersonSummary(workspace.session().accountId(), workspace.session().name(),
                workspace.session().email(), workspace.session().role(), workspace.session().educationLevel(),
                0, 0, 0, 0, 0, 0);
    }

    private List<String> levels() {
        List<String> values = new ArrayList<>();
        for (PersonSummary summary : load()) {
            String level = summary.educationLevel();
            if (level != null && !level.isBlank() && !values.contains(level)) values.add(level);
        }
        return values;
    }

    private Object[] row(PersonSummary summary) {
        String score = summary.evaluationCount() == 0 ? "—"
                : String.format(java.util.Locale.ROOT, "%.1f%%", summary.averagePercent());
        if (teacherDirectory) {
            return new Object[]{summary.name(), summary.email(), summary.subjectCount(), score};
        }
        return new Object[]{summary.name(), summary.email(),
                summary.educationLevel() == null || summary.educationLevel().isBlank() ? "—" : summary.educationLevel(),
                summary.materialCount(), summary.completedTaskCount() + " / " + summary.taskCount(), score};
    }

    /** Opens a readable summary of the selected person. */
    private void showDetail() {
        PersonSummary selected = table.selected();
        if (selected == null) {
            Dialogs.info(this, "Belum ada pilihan", "Pilih satu baris pada tabel sebelum melihat detailnya.");
            return;
        }
        List<JComponent> badges = new ArrayList<>();
        badges.add(Ui.badge(selected.role() == AccountRole.PENGAJAR ? "Pengajar" : "Siswa",
                new Theme.StatusStyle(Theme.ACCENT, Theme.ACCENT_SOFT)));
        if (workspace.demo() != null && workspace.demo().isDemoAccount(selected.id())) badges.add(Ui.tag("Akun demo"));
        DetailDialog.show(this, selected.name(), selected.email(),
                DetailDialog.sections(
                        new DetailDialog.Section("Peran",
                                selected.role() == AccountRole.PENGAJAR ? "Pengajar" : "Siswa"),
                        new DetailDialog.Section("Jenjang pendidikan",
                                selected.educationLevel() == null || selected.educationLevel().isBlank()
                                        ? "—" : selected.educationLevel()),
                        new DetailDialog.Section("Mata pelajaran", String.valueOf(selected.subjectCount())),
                        new DetailDialog.Section("Materi", String.valueOf(selected.materialCount())),
                        new DetailDialog.Section("Tugas selesai",
                                selected.completedTaskCount() + " dari " + selected.taskCount()),
                        new DetailDialog.Section("Evaluasi", String.valueOf(selected.evaluationCount())),
                        new DetailDialog.Section("Rata-rata nilai", selected.evaluationCount() == 0 ? "—"
                                : String.format(java.util.Locale.ROOT, "%.1f%%", selected.averagePercent()))),
                badges);
    }

    /** Ends the connection between the teacher and the selected student. */
    private void endConnection() {
        PersonSummary selected = table.selected();
        if (selected == null) {
            Dialogs.info(this, "Belum ada pilihan", "Pilih satu siswa pada tabel sebelum mengakhiri koneksi.");
            return;
        }
        if (!Dialogs.confirm(this, "Konfirmasi akhiri koneksi",
                "Akhiri koneksi dengan " + selected.name()
                        + "? Record belajar tetap disimpan dan dapat dibuka lagi bila koneksi dipulihkan.")) return;
        try {
            workspace.connections().removeConnection(workspace.session().token(), selected.id());
            Dialogs.success(this, "Koneksi diakhiri", selected.name() + " tidak lagi dapat diakses oleh akun ini.");
            refresh();
        } catch (RuntimeException exception) {
            Dialogs.error(this, "Koneksi tidak dapat diakhiri", exception.getMessage());
        }
    }

    public void refresh() {
        table.refresh();
        refreshArchive();
    }

    /** Rebuilds the archive table from the pre-account data files when they contain records. */
    private void refreshArchive() {
        archive.removeAll();
        if (workspace.legacy() == null) {
            archive.setVisible(false);
            return;
        }
        DefaultTableModel model = new DefaultTableModel(
                teacherDirectory
                        ? new Object[]{"Nama", "Email", "Telepon", "Bidang keahlian"}
                        : new Object[]{"Nama", "Email", "Telepon", "Tanggal lahir", "Jenjang", "Preferensi belajar"},
                0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        int rows = 0;
        try {
            if (teacherDirectory) {
                for (Pengajar teacher : workspace.legacy().teachers()) {
                    model.addRow(new Object[]{teacher.getName(), teacher.getEmail(), teacher.getPhone(), teacher.getExpertise()});
                    rows++;
                }
            } else {
                for (Siswa student : workspace.legacy().students()) {
                    model.addRow(new Object[]{student.getName(), student.getEmail(), student.getPhone(),
                            student.getBirthDate(), student.getEducationLevel(), student.getLearningPreferences()});
                    rows++;
                }
            }
        } catch (RuntimeException exception) {
            archive.setVisible(false);
            return;
        }
        if (rows == 0) {
            archive.setVisible(false);
            archive.revalidate();
            archive.repaint();
            return;
        }
        JTable legacyTable = new JTable(model);
        Ui.styleTable(legacyTable);
        JPanel card = Ui.card();
        card.add(Ui.cardHeader("Arsip data lokal",
                "Catatan tersimpan sebelum sistem akun digunakan. Data ini hanya dapat dibaca."),
                BorderLayout.NORTH);
        // The archive is capped in height so it never squeezes the main table away.
        javax.swing.JScrollPane scroll = Ui.scroll(legacyTable);
        scroll.setPreferredSize(new Dimension(0, 220));
        scroll.setMinimumSize(new Dimension(0, 140));
        card.add(scroll, BorderLayout.CENTER);
        archive.add(card, BorderLayout.CENTER);
        archive.setVisible(true);
        archive.revalidate();
        archive.repaint();
    }
}
