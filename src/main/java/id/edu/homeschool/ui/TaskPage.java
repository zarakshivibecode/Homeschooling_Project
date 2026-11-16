package id.edu.homeschool.ui;

import id.edu.homeschool.model.LearningTask;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.service.LearningWorkspaceService;
import id.edu.homeschool.ui.design.DataTable;
import id.edu.homeschool.ui.design.DetailDialog;
import id.edu.homeschool.ui.design.Dialogs;
import id.edu.homeschool.ui.design.FormDialog;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;

/** Tugas page: assignments with a due date, the target student and the full instructions. */
public final class TaskPage extends LearningTablePage<LearningTask> {

    public TaskPage(Workspace workspace) {
        super(workspace, "Tugas",
                "Tugas yang perlu diselesaikan beserta tenggat, target siswa, dan status pengerjaannya.",
                new String[]{"Tugas", "Mata pelajaran", "Siswa", "Tenggat", "Status"},
                current -> current.learning().tasks(current.session().token(), current.studentId()),
                (current, task) -> new Object[]{task.getTitle(),
                        Lookups.subjectName(current, task.getSubjectId()),
                        targetStudent(current, task), task.getDueDate(), task.getStatusLabel()},
                "Belum ada tugas",
                "Tambahkan tugas pertama agar siswa tahu apa yang perlu diselesaikan.");
        table.setColumnRenderer(4, DataTable.statusRenderer());
        table.addFilter("Mata pelajaran", () -> Lookups.subjectNames(workspace),
                task -> Lookups.subjectName(workspace, task.getSubjectId()));
    }

    @Override protected void contributeActions(DataTable<LearningTask> table) {
        JButton view = Ui.button("Lihat tugas", Ui.Variant.SECONDARY);
        view.addActionListener(event -> showDetail());
        table.addAction(view);
        if (workspace.isStudent()) {
            JButton toggle = Ui.button("Tandai selesai", Ui.Variant.SECONDARY);
            toggle.addActionListener(event -> toggleCompletion());
            addToolbarAction(toggle);
        }
    }

    /** Opens the instruction sheet of the selected assignment. */
    private void showDetail() {
        LearningTask selected = selectedRecord();
        if (selected == null) {
            Dialogs.info(this, "Belum ada pilihan", "Pilih satu tugas pada tabel sebelum membuka detailnya.");
            return;
        }
        List<JComponent> badges = new ArrayList<>();
        badges.add(Ui.badge(selected.getStatusLabel(), Theme.statusOf(selected.getStatusLabel())));
        if (workspace.isDemo(LearningWorkspaceService.TYPE_TASK, selected.getId())) badges.add(Ui.tag("Data demo"));
        DetailDialog.show(this, selected.getTitle(),
                "Mata pelajaran " + Lookups.subjectName(workspace, selected.getSubjectId()),
                DetailDialog.sections(
                        new DetailDialog.Section("Instruksi", selected.getInstructions()),
                        new DetailDialog.Section("Tenggat", String.valueOf(selected.getDueDate())),
                        new DetailDialog.Section("Siswa target", targetStudent(workspace, selected)),
                        new DetailDialog.Section("Status", selected.getStatusLabel())),
                badges);
    }

    /** The student that has to finish the assignment, resolved from the ownership grant. */
    private static String targetStudent(Workspace workspace, LearningTask task) {
        try {
            return workspace.learning().targetStudentName(workspace.session().token(),
                    LearningWorkspaceService.TYPE_TASK, task.getId());
        } catch (RuntimeException exception) {
            return "—";
        }
    }

    private void toggleCompletion() {
        LearningTask selected = selectedRecord();
        if (selected == null) {
            Dialogs.info(this, "Belum ada pilihan", "Pilih satu tugas pada tabel sebelum menandai statusnya.");
            return;
        }
        try {
            boolean next = !selected.isCompleted();
            workspace.learning().setTaskCompleted(workspace.session().token(), selected.getId(), next);
            Dialogs.success(this, "Status tugas diperbarui",
                    next ? "Tugas ditandai selesai." : "Tugas dikembalikan ke daftar yang belum selesai.");
            refresh();
        } catch (RuntimeException exception) {
            Dialogs.error(this, "Status tugas tidak dapat diubah", exception.getMessage());
        }
    }

    @Override protected String entityName() { return "Tugas"; }

    @Override protected String resourceType() { return LearningWorkspaceService.TYPE_TASK; }

    @Override protected List<FormDialog.Field> formFields(LearningTask current) {
        List<Subject> subjects = workspace.learning().subjects(workspace.session().token(), workspace.studentId());
        if (subjects.isEmpty()) throw new IllegalStateException("Buat mata pelajaran terlebih dahulu.");
        Subject selected = current == null ? subjects.getFirst()
                : subjects.stream().filter(subject -> subject.getId().equals(current.getSubjectId())).findFirst().orElse(subjects.getFirst());
        return FormDialog.fields(
                FormDialog.Field.text("title", "Judul tugas", current == null ? "" : current.getTitle()),
                FormDialog.Field.choice("subject", "Mata pelajaran", subjects, selected),
                FormDialog.Field.text("due", "Tenggat (YYYY-MM-DD)",
                        current == null ? LocalDate.now().plusDays(7).toString() : current.getDueDate().toString()),
                FormDialog.Field.multiline("instructions", "Instruksi tugas",
                        current == null ? "" : current.getInstructions(), 6));
    }

    @Override protected void persist(LearningTask existing, Map<String, Object> values) {
        Subject subject = FormDialog.choice(values, "subject");
        String title = FormDialog.text(values, "title");
        LocalDate due = parseDate(FormDialog.text(values, "due"), "Tenggat");
        String instructions = FormDialog.text(values, "instructions");
        LearningTask record = existing == null
                ? new LearningTask(title, subject.getId(), due, instructions) : existing;
        if (existing != null) {
            record.setTitle(title);
            record.setSubjectId(subject.getId());
            record.setDueDate(due);
            record.setInstructions(instructions);
        }
        workspace.learning().saveTask(workspace.session().token(), workspace.studentId(), record);
    }
}
