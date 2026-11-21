package id.edu.homeschool.ui;

import id.edu.homeschool.model.Evaluation;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.service.LearningWorkspaceService;
import id.edu.homeschool.ui.design.DataTable;
import id.edu.homeschool.ui.design.DetailDialog;
import id.edu.homeschool.ui.design.Dialogs;
import id.edu.homeschool.ui.design.FormDialog;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;

/** Evaluasi page: scores recorded by the teacher for the selected student. */
public final class EvaluationPage extends LearningTablePage<Evaluation> {

    final EvaluationTrendChart trendChart;

    public EvaluationPage(Workspace workspace) {
        super(workspace, "Evaluasi",
                "Nilai dan catatan hasil belajar yang dicatat untuk setiap mata pelajaran.",
                new String[]{"Tanggal", "Mata pelajaran", "Nilai", "Persentase", "Catatan"},
                current -> current.learning().evaluations(current.session().token(), current.studentId()),
                (current, evaluation) -> new Object[]{evaluation.getDate(),
                        Lookups.subjectName(current, evaluation.getSubjectId()),
                        formatScore(evaluation), String.format(java.util.Locale.ROOT, "%.1f%%", evaluation.getPercentage()),
                        evaluation.getNotes()},
                "Belum ada evaluasi",
                "Catat nilai pertama agar laporan progres siswa dapat dihitung.");
        table.addFilter("Mata pelajaran", () -> Lookups.subjectNames(workspace),
                evaluation -> Lookups.subjectName(workspace, evaluation.getSubjectId()));

        trendChart = new EvaluationTrendChart(workspace);
        remove(header);
        javax.swing.JPanel top = Ui.transparent(new BorderLayout(0, Theme.SPACE_3));
        top.add(header, BorderLayout.NORTH);
        top.add(trendChart, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        refresh();
    }

    @Override public void refresh() {
        super.refresh();
        if (trendChart != null) trendChart.refresh();
    }

    @Override protected void contributeActions(DataTable<Evaluation> table) {
        JButton view = Ui.button("Lihat nilai", Ui.Variant.SECONDARY);
        view.addActionListener(event -> showDetail());
        table.addAction(view);
    }

    /** Opens the full feedback of the selected evaluation. */
    private void showDetail() {
        Evaluation selected = selectedRecord();
        if (selected == null) {
            Dialogs.info(this, "Belum ada pilihan", "Pilih satu evaluasi pada tabel sebelum membuka detailnya.");
            return;
        }
        List<JComponent> badges = new ArrayList<>();
        badges.add(Ui.badge(String.format(java.util.Locale.ROOT, "%.0f%%", selected.getPercentage()),
                Theme.statusOf(String.format(java.util.Locale.ROOT, "%.0f%%", selected.getPercentage()))));
        if (workspace.isDemo(LearningWorkspaceService.TYPE_EVALUATION, selected.getId())) badges.add(Ui.tag("Data demo"));
        DetailDialog.show(this, "Evaluasi " + Lookups.subjectName(workspace, selected.getSubjectId()),
                "Tanggal " + selected.getDate(),
                DetailDialog.sections(
                        new DetailDialog.Section("Nilai", formatScore(selected)),
                        new DetailDialog.Section("Persentase",
                                String.format(java.util.Locale.ROOT, "%.1f%%", selected.getPercentage())),
                        new DetailDialog.Section("Catatan pengajar", selected.getNotes()),
                        new DetailDialog.Section("Siswa", targetStudent(workspace, selected))),
                badges);
    }

    private static String formatScore(Evaluation evaluation) {
        return String.format(java.util.Locale.ROOT, "%.1f / %.1f", evaluation.getScore(), evaluation.getMaximumScore());
    }

    private static String targetStudent(Workspace workspace, Evaluation evaluation) {
        try {
            return workspace.learning().targetStudentName(workspace.session().token(),
                    LearningWorkspaceService.TYPE_EVALUATION, evaluation.getId());
        } catch (RuntimeException exception) {
            return "—";
        }
    }

    @Override protected String entityName() { return "Evaluasi"; }

    @Override protected String resourceType() { return LearningWorkspaceService.TYPE_EVALUATION; }

    @Override protected List<FormDialog.Field> formFields(Evaluation current) {
        List<Subject> subjects = workspace.learning().subjects(workspace.session().token(), workspace.studentId());
        if (subjects.isEmpty()) throw new IllegalStateException("Buat mata pelajaran terlebih dahulu.");
        Subject selected = current == null ? subjects.getFirst()
                : subjects.stream().filter(subject -> subject.getId().equals(current.getSubjectId())).findFirst().orElse(subjects.getFirst());
        return FormDialog.fields(
                FormDialog.Field.choice("subject", "Mata pelajaran", subjects, selected),
                FormDialog.Field.text("date", "Tanggal (YYYY-MM-DD)",
                        current == null ? LocalDate.now().toString() : current.getDate().toString()),
                FormDialog.Field.text("score", "Nilai", current == null ? "80" : String.valueOf(current.getScore())),
                FormDialog.Field.text("maximum", "Nilai maksimum", current == null ? "100" : String.valueOf(current.getMaximumScore())),
                FormDialog.Field.multiline("notes", "Catatan pengajar", current == null ? "" : current.getNotes(), 5));
    }

    @Override protected void persist(Evaluation existing, Map<String, Object> values) {
        Subject subject = FormDialog.choice(values, "subject");
        LocalDate date = parseDate(FormDialog.text(values, "date"), "Tanggal");
        double score = parseDecimal(FormDialog.text(values, "score"), "Nilai");
        double maximum = parseDecimal(FormDialog.text(values, "maximum"), "Nilai maksimum");
        String notes = FormDialog.text(values, "notes");
        Evaluation record = existing == null
                ? new Evaluation(subject.getId(), date, score, maximum, notes) : existing;
        if (existing != null) {
            record.setSubjectId(subject.getId());
            record.setDate(date);
            record.setScores(score, maximum);
            record.setNotes(notes);
        }
        workspace.learning().saveEvaluation(workspace.session().token(), workspace.studentId(), record);
    }
}
