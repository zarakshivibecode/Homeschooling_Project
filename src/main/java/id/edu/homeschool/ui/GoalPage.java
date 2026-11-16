package id.edu.homeschool.ui;

import id.edu.homeschool.model.LearningGoal;
import id.edu.homeschool.service.LearningWorkspaceService;
import id.edu.homeschool.ui.design.DataTable;
import id.edu.homeschool.ui.design.Dialogs;
import id.edu.homeschool.ui.design.FormDialog;
import id.edu.homeschool.ui.design.Ui;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;

/** Target page: learning goals with a target date. */
public final class GoalPage extends LearningTablePage<LearningGoal> {

    public GoalPage(Workspace workspace) {
        super(workspace, "Target belajar",
                "Sasaran belajar yang ditetapkan beserta tanggal target dan capainya.",
                new String[]{"Target", "Tanggal", "Status"},
                current -> current.learning().goals(current.session().token(), current.studentId()),
                (current, goal) -> new Object[]{goal.getDescription(), goal.getTargetDate(), goal.getStatusLabel()},
                "Belum ada target",
                "Tetapkan target belajar pertama agar perkembangan siswa dapat diukur.");
        table.setColumnRenderer(2, DataTable.statusRenderer());
        table.addFilter("Status", List.of("Tercapai", "Dalam proses"),
                goal -> goal.getStatusLabel());

        if (workspace.isTeacher()) {
            JButton toggle = Ui.button("Tandai tercapai", Ui.Variant.SECONDARY);
            toggle.setToolTipText("Menandai target yang sudah tercapai atau membatalkannya");
            toggle.addActionListener(event -> toggleAchievement());
            addToolbarAction(toggle);
        }
    }

    private void toggleAchievement() {
        LearningGoal selected = selectedRecord();
        if (selected == null) {
            Dialogs.info(this, "Belum ada pilihan", "Pilih satu target pada tabel sebelum mengubah statusnya.");
            return;
        }
        try {
            boolean next = !selected.isAchieved();
            workspace.learning().setGoalAchieved(workspace.session().token(), selected.getId(), next);
            Dialogs.success(this, "Status target diperbarui",
                    next ? "Target ditandai sudah tercapai." : "Target dikembalikan ke daftar yang belum tercapai.");
            refresh();
        } catch (RuntimeException exception) {
            Dialogs.error(this, "Status target tidak dapat diubah", exception.getMessage());
        }
    }

    @Override protected String entityName() { return "Target"; }

    @Override protected String resourceType() { return LearningWorkspaceService.TYPE_GOAL; }

    @Override protected List<FormDialog.Field> formFields(LearningGoal current) {
        return FormDialog.fields(
                FormDialog.Field.text("description", "Deskripsi target", current == null ? "" : current.getDescription()),
                FormDialog.Field.text("date", "Tanggal target (YYYY-MM-DD)",
                        current == null ? LocalDate.now().plusDays(14).toString() : current.getTargetDate().toString()));
    }

    @Override protected void persist(LearningGoal existing, Map<String, Object> values) {
        String description = FormDialog.text(values, "description");
        LocalDate date = parseDate(FormDialog.text(values, "date"), "Tanggal target");
        LearningGoal record = existing == null ? new LearningGoal(description, date) : existing;
        if (existing != null) {
            record.setDescription(description);
            record.setTargetDate(date);
        }
        workspace.learning().saveGoal(workspace.session().token(), workspace.studentId(), record);
    }
}
