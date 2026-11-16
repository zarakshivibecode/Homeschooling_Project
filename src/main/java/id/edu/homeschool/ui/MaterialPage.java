package id.edu.homeschool.ui;

import id.edu.homeschool.model.LearningMaterial;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.service.LearningWorkspaceService;
import id.edu.homeschool.ui.design.DataTable;
import id.edu.homeschool.ui.design.DetailDialog;
import id.edu.homeschool.ui.design.Dialogs;
import id.edu.homeschool.ui.design.FormDialog;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;

/** Materi page: learning material attached to a subject, with the full content available. */
public final class MaterialPage extends LearningTablePage<LearningMaterial> {

    public MaterialPage(Workspace workspace) {
        super(workspace, "Materi",
                "Bahan belajar lengkap dengan tujuan pembelajaran, penjelasan, contoh, dan latihan.",
                new String[]{"Judul", "Mata pelajaran", "Jenis", "Ringkasan"},
                current -> current.learning().materials(current.session().token(), current.studentId()),
                (current, material) -> new Object[]{material.getTitle(),
                        Lookups.subjectName(current, material.getSubjectId()),
                        material.getType(), material.getNotes()},
                "Belum ada materi",
                "Tambahkan materi pertama agar siswa dapat mengakses bahan belajar yang disiapkan.");
        table.addFilter("Mata pelajaran", () -> Lookups.subjectNames(workspace),
                material -> Lookups.subjectName(workspace, material.getSubjectId()));
    }

    @Override protected void contributeActions(DataTable<LearningMaterial> table) {
        JButton view = Ui.button("Lihat materi", Ui.Variant.SECONDARY);
        view.addActionListener(event -> showDetail());
        table.addAction(view);
    }

    /** Opens the full content of the selected material. */
    private void showDetail() {
        LearningMaterial selected = selectedRecord();
        if (selected == null) {
            Dialogs.info(this, "Belum ada pilihan",
                    "Pilih satu materi pada tabel sebelum membuka isinya.");
            return;
        }
        List<JComponent> badges = new ArrayList<>();
        badges.add(Ui.badge(selected.getType(), Theme.neutral()));
        if (workspace.isDemo(LearningWorkspaceService.TYPE_MATERIAL, selected.getId())) {
            badges.add(Ui.tag("Data demo"));
        }
        DetailDialog.show(this, selected.getTitle(),
                "Mata pelajaran " + Lookups.subjectName(workspace, selected.getSubjectId()),
                DetailDialog.sections(
                        new DetailDialog.Section("Tujuan pembelajaran", withoutTitle(selected.getObjectives())),
                        new DetailDialog.Section("Materi", selected.getContent()),
                        new DetailDialog.Section("Ringkasan", selected.getNotes()),
                        new DetailDialog.Section("Lokasi / URL", selected.getLocation())),
                badges);
    }

    /** Removes a repeated heading so the dialog does not show the same title twice. */
    private static String withoutTitle(String text) {
        if (text == null) return "";
        String value = text.trim();
        String prefix = "Tujuan pembelajaran:";
        return value.toLowerCase(java.util.Locale.ROOT).startsWith(prefix.toLowerCase(java.util.Locale.ROOT))
                ? value.substring(prefix.length()).trim() : value;
    }

    @Override protected String entityName() { return "Materi"; }

    @Override protected String resourceType() { return LearningWorkspaceService.TYPE_MATERIAL; }

    @Override protected List<FormDialog.Field> formFields(LearningMaterial current) {
        List<Subject> subjects = workspace.learning().subjects(workspace.session().token(), workspace.studentId());
        if (subjects.isEmpty()) throw new IllegalStateException("Buat mata pelajaran terlebih dahulu.");
        Subject selected = current == null ? subjects.getFirst()
                : subjects.stream().filter(subject -> subject.getId().equals(current.getSubjectId())).findFirst().orElse(subjects.getFirst());
        return FormDialog.fields(
                FormDialog.Field.choice("subject", "Mata pelajaran", subjects, selected),
                FormDialog.Field.text("title", "Judul materi", current == null ? "" : current.getTitle()),
                FormDialog.Field.text("type", "Jenis materi", current == null ? "Materi baca" : current.getType()),
                FormDialog.Field.optionalText("location", "Lokasi atau URL",
                        current == null ? "" : current.getLocation()),
                FormDialog.Field.optionalText("notes", "Ringkasan", current == null ? "" : current.getNotes()),
                FormDialog.Field.multiline("objectives", "Tujuan pembelajaran",
                        current == null ? "" : current.getObjectives(), 4),
                FormDialog.Field.multiline("content", "Isi materi (penjelasan, contoh, dan latihan)",
                        current == null ? "" : current.getContent(), 9));
    }

    @Override protected void persist(LearningMaterial existing, Map<String, Object> values) {
        Subject subject = FormDialog.choice(values, "subject");
        String title = FormDialog.text(values, "title");
        String type = FormDialog.text(values, "type");
        String location = FormDialog.text(values, "location");
        String notes = FormDialog.text(values, "notes");
        String objectives = FormDialog.text(values, "objectives");
        String content = FormDialog.text(values, "content");
        LearningMaterial record = existing == null
                ? new LearningMaterial(subject.getId(), title, type, location, notes, objectives, content) : existing;
        if (existing != null) {
            record.setSubjectId(subject.getId());
            record.setTitle(title);
            record.setType(type);
            record.setLocation(location);
            record.setNotes(notes);
            record.setObjectives(objectives);
            record.setContent(content);
        }
        workspace.learning().saveMaterial(workspace.session().token(), workspace.studentId(), record);
    }
}
