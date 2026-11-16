package id.edu.homeschool.ui;

import id.edu.homeschool.model.Subject;
import id.edu.homeschool.ui.design.FormDialog;
import java.util.List;
import java.util.Map;

/** Mata pelajaran page: the catalogue of subjects used by the selected student. */
public final class SubjectPage extends LearningTablePage<Subject> {

    public SubjectPage(Workspace workspace) {
        super(workspace, "Mata pelajaran",
                "Katalog mata pelajaran yang dipakai untuk materi, jadwal, tugas, dan evaluasi.",
                new String[]{"Mata pelajaran", "Deskripsi"},
                current -> current.learning().subjects(current.session().token(), current.studentId()),
                (current, subject) -> new Object[]{subject.getName(), subject.getDescription()},
                "Belum ada mata pelajaran",
                "Tambahkan mata pelajaran pertama untuk mulai mencatat materi, jadwal, tugas, dan evaluasi.");
    }

    @Override protected String entityName() { return "Mata pelajaran"; }

    @Override protected String resourceType() { return id.edu.homeschool.service.LearningWorkspaceService.TYPE_SUBJECT; }

    @Override protected List<FormDialog.Field> formFields(Subject current) {
        return FormDialog.fields(
                FormDialog.Field.text("name", "Nama mata pelajaran", current == null ? "" : current.getName()),
                FormDialog.Field.optionalText("description", "Deskripsi", current == null ? "" : current.getDescription()));
    }

    @Override protected void persist(Subject existing, Map<String, Object> values) {
        String name = FormDialog.text(values, "name");
        String description = FormDialog.text(values, "description");
        Subject record = existing == null ? new Subject(name, description) : existing;
        if (existing != null) {
            record.setName(name);
            record.setDescription(description);
        }
        workspace.learning().saveSubject(workspace.session().token(), workspace.studentId(), record);
    }
}
