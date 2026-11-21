package id.edu.homeschool.ui;

import id.edu.homeschool.model.StudySession;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.service.LearningWorkspaceService;
import id.edu.homeschool.ui.design.FormDialog;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Jadwal page: scheduled study sessions for the selected student. */
public final class SchedulePage extends LearningTablePage<StudySession> {

    final StudySessionDurationChart durationChart;

    public SchedulePage(Workspace workspace) {
        super(workspace, "Jadwal belajar",
                "Sesi belajar yang telah dijadwalkan beserta tanggal, waktu, dan durasinya.",
                new String[]{"Tanggal", "Hari", "Waktu", "Mata pelajaran", "Durasi", "Catatan"},
                current -> current.learning().sessions(current.session().token(), current.studentId()),
                (current, session) -> new Object[]{session.getDate(), dayName(session.getDate()),
                        session.getStartTime(), Lookups.subjectName(current, session.getSubjectId()),
                        session.getDurationMinutes() + " menit", session.getNotes()},
                "Belum ada jadwal",
                "Tambahkan sesi belajar pertama agar agenda belajar siswa terlihat jelas.");
        table.addFilter("Mata pelajaran", () -> Lookups.subjectNames(workspace),
                session -> Lookups.subjectName(workspace, session.getSubjectId()));

        durationChart = new StudySessionDurationChart(workspace);
        remove(header);
        javax.swing.JPanel top = id.edu.homeschool.ui.design.Ui.transparent(
                new java.awt.BorderLayout(0, id.edu.homeschool.ui.design.Theme.SPACE_3));
        top.add(header, java.awt.BorderLayout.NORTH);
        top.add(durationChart, java.awt.BorderLayout.CENTER);
        add(top, java.awt.BorderLayout.NORTH);
        refresh();
    }

    @Override public void refresh() {
        super.refresh();
        if (durationChart != null) durationChart.refresh();
    }

    /** The Indonesian name of the weekday, for example "Rabu". */
    static String dayName(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("id"));
    }

    @Override protected String entityName() { return "Jadwal"; }

    @Override protected String resourceType() { return LearningWorkspaceService.TYPE_SESSION; }

    @Override protected List<FormDialog.Field> formFields(StudySession current) {
        List<Subject> subjects = workspace.learning().subjects(workspace.session().token(), workspace.studentId());
        if (subjects.isEmpty()) throw new IllegalStateException("Buat mata pelajaran terlebih dahulu.");
        Subject selected = current == null ? subjects.getFirst()
                : subjects.stream().filter(subject -> subject.getId().equals(current.getSubjectId())).findFirst().orElse(subjects.getFirst());
        return FormDialog.fields(
                FormDialog.Field.choice("subject", "Mata pelajaran", subjects, selected),
                FormDialog.Field.text("date", "Tanggal (YYYY-MM-DD)",
                        current == null ? LocalDate.now().plusDays(1).toString() : current.getDate().toString()),
                FormDialog.Field.text("time", "Waktu mulai (HH:MM)",
                        current == null ? "09:00" : current.getStartTime().toString()),
                FormDialog.Field.text("duration", "Durasi (menit)",
                        current == null ? "60" : String.valueOf(current.getDurationMinutes())),
                FormDialog.Field.optionalText("notes", "Catatan", current == null ? "" : current.getNotes()));
    }

    @Override protected void persist(StudySession existing, Map<String, Object> values) {
        Subject subject = FormDialog.choice(values, "subject");
        LocalDate date = parseDate(FormDialog.text(values, "date"), "Tanggal");
        LocalTime time = parseTime(FormDialog.text(values, "time"), "Waktu mulai");
        int duration = parseInteger(FormDialog.text(values, "duration"), "Durasi");
        String notes = FormDialog.text(values, "notes");
        StudySession record = existing == null
                ? new StudySession(subject.getId(), date, time, duration, notes) : existing;
        if (existing != null) {
            record.setSubjectId(subject.getId());
            record.setDate(date);
            record.setStartTime(time);
            record.setDurationMinutes(duration);
            record.setNotes(notes);
        }
        workspace.learning().saveSession(workspace.session().token(), workspace.studentId(), record);
    }
}
