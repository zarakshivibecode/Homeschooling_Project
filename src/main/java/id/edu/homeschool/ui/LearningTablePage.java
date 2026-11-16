package id.edu.homeschool.ui;

import id.edu.homeschool.ui.design.DataTable;
import id.edu.homeschool.ui.design.Dialogs;
import id.edu.homeschool.ui.design.FormDialog;
import id.edu.homeschool.ui.design.PageHeader;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Shared behaviour for every learning record page: a searchable table, teacher only create,
 * edit and delete actions, and the guard that keeps a teacher from touching data while no
 * student is selected.
 */
public abstract class LearningTablePage<T> extends JPanel implements AppShell.Refreshable {

    protected final Workspace workspace;
    protected final PageHeader header;
    protected final DataTable<T> table;
    private final JButton addButton = Ui.button("Tambah", Ui.Variant.PRIMARY);
    private final JButton editButton = Ui.button("Edit", Ui.Variant.SECONDARY);
    private final JButton deleteButton = Ui.button("Hapus", Ui.Variant.DANGER);
    private final List<JButton> extraActions = new java.util.ArrayList<>();

    protected LearningTablePage(Workspace workspace, String title, String description, String[] columns,
            Function<Workspace, List<T>> loader, java.util.function.BiFunction<Workspace, T, Object[]> mapper,
            String emptyTitle, String emptyDescription) {
        super(new BorderLayout(0, Theme.SPACE_4));
        this.workspace = workspace;
        setBackground(Theme.CANVAS);
        setOpaque(false);

        header = new PageHeader(title, description);
        table = new DataTable<>(columns, () -> loadRows(loader), value -> mapper.apply(workspace, value));
        table.setEmptyState(emptyTitle, emptyDescription);

        add(header, BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);

        contributeActions(table);

        if (workspace.isTeacher()) {
            addButton.addActionListener(event -> createRecord());
            editButton.addActionListener(event -> editSelected());
            deleteButton.addActionListener(event -> deleteSelected());
            table.addAction(addButton);
            table.addAction(editButton);
            table.addAction(deleteButton);
        }
        workspace.onChange(this::refresh);
    }

    /**
     * Hook for pages that need their own toolbar buttons, for example "Lihat materi" which opens
     * the full content of a record.
     */
    protected void contributeActions(DataTable<T> table) {
    }

    private List<T> loadRows(Function<Workspace, List<T>> loader) {
        if (!workspace.hasScope()) return List.of();
        return loader.apply(workspace);
    }

    /** The label used by the delete and edit dialogs. */
    protected abstract String entityName();

    /** Resource type used by the workspace service when deleting. */
    protected abstract String resourceType();

    /** Form definition for creating or editing a record. The template is null when creating. */
    protected abstract List<FormDialog.Field> formFields(T template);

    /**
     * Persists the submitted values. {@code existing} is null when a new record is created and
     * holds the stored record when an existing one is edited.
     */
    protected abstract void persist(T existing, Map<String, Object> values);

    /** The record currently shown in the form; null when the form creates a new record. */
    protected T selectedRecord() { return table.selected(); }

    /** Extra toolbar buttons a page needs, for example "Tandai selesai" for students. */
    protected void addToolbarAction(JButton button) {
        extraActions.add(button);
        table.addAction(button);
    }

    public void refresh() {
        if (!workspace.hasScope() && workspace.isTeacher()) {
            table.setEmptyState("Belum ada siswa terpilih",
                    "Pilih siswa pada bagian atas halaman untuk melihat dan mengelola datanya.");
            table.refresh();
            updateButtonState();
            return;
        }
        table.refresh();
        updateButtonState();
    }

    private void updateButtonState() {
        boolean enabled = workspace.isTeacher() && workspace.hasScope();
        addButton.setEnabled(enabled);
        editButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        // The extra actions belong to the page itself, so they only follow the selected scope.
        for (JButton action : extraActions) action.setEnabled(workspace.hasScope());
    }

    private void createRecord() {
        if (!requireScope()) return;
        openDialog("Tambah " + entityName(), null);
    }

    private void editSelected() {
        if (!requireScope()) return;
        T selected = selectedRecord();
        if (selected == null) {
            Dialogs.info(this, "Belum ada pilihan", "Pilih satu baris pada tabel sebelum mengubah data.");
            return;
        }
        openDialog("Edit " + entityName(), selected);
    }

    private void openDialog(String title, T existing) {
        List<FormDialog.Field> fields;
        try {
            fields = formFields(existing);
        } catch (RuntimeException exception) {
            Dialogs.error(this, "Form tidak dapat dibuka",
                    exception.getMessage() == null ? "Lengkapi data lain sebelum menambah data ini." : exception.getMessage());
            return;
        }
        FormDialog.show(this, title, "Lengkapi isian lalu tekan Simpan untuk menyimpan perubahan.", fields,
                values -> {
                    persist(existing, values);
                    Dialogs.success(this, "Data tersimpan",
                            entityName() + " berhasil disimpan dan langsung tampil di tabel.");
                });
        refresh();
    }

    private void deleteSelected() {
        if (!requireScope()) return;
        T selected = selectedRecord();
        if (selected == null) {
            Dialogs.info(this, "Belum ada pilihan", "Pilih satu baris pada tabel sebelum menghapus data.");
            return;
        }
        boolean confirmed = Dialogs.confirm(this, "Konfirmasi penghapusan",
                "Hapus " + entityName().toLowerCase(java.util.Locale.ROOT) + " ini? Tindakan ini tidak dapat dibatalkan.");
        if (!confirmed) return;
        try {
            workspace.learning().delete(workspace.session().token(), workspace.studentId(), resourceType(), idOf(selected));
            Dialogs.success(this, "Data dihapus", entityName() + " berhasil dihapus dari daftar.");
            refresh();
        } catch (RuntimeException exception) {
            Dialogs.error(this, "Data tidak dapat dihapus", exception.getMessage());
        }
    }

    /** Identifier of the record; subclasses that use the default delete flow must implement it. */
    protected String idOf(T record) {
        if (record instanceof id.edu.homeschool.model.Subject value) return value.getId();
        if (record instanceof id.edu.homeschool.model.LearningMaterial value) return value.getId();
        if (record instanceof id.edu.homeschool.model.LearningTask value) return value.getId();
        if (record instanceof id.edu.homeschool.model.StudySession value) return value.getId();
        if (record instanceof id.edu.homeschool.model.Evaluation value) return value.getId();
        if (record instanceof id.edu.homeschool.model.LearningGoal value) return value.getId();
        throw new IllegalStateException("Jenis data tidak didukung.");
    }

    private boolean requireScope() {
        if (workspace.hasScope()) return true;
        Dialogs.warn(this, "Siswa belum dipilih",
                "Pilih siswa terlebih dahulu pada pemilih siswa di bagian atas halaman.");
        return false;
    }

    /** Parses a date entered as YYYY-MM-DD and reports a readable error. */
    protected static java.time.LocalDate parseDate(String value, String label) {
        try {
            return java.time.LocalDate.parse(value.trim());
        } catch (java.time.format.DateTimeParseException exception) {
            throw new IllegalArgumentException(label + " harus berformat YYYY-MM-DD.");
        }
    }

    protected static java.time.LocalTime parseTime(String value, String label) {
        try {
            return java.time.LocalTime.parse(value.trim());
        } catch (java.time.format.DateTimeParseException exception) {
            throw new IllegalArgumentException(label + " harus berformat HH:MM.");
        }
    }

    protected static int parseInteger(String value, String label) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " harus berupa angka bulat.");
        }
    }

    protected static double parseDecimal(String value, String label) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " harus berupa angka.");
        }
    }
}
