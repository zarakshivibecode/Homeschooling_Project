package id.edu.homeschool.ui;

import id.edu.homeschool.model.Subject;
import java.util.ArrayList;
import java.util.List;

/**
 * Small lookups that several pages share. The helpers only read the stored records, so the
 * pages keep showing the same information as before.
 */
final class Lookups {

    private Lookups() { }

    /** The name of a subject, or a readable fallback when it is not available. */
    static String subjectName(Workspace workspace, String subjectId) {
        try {
            for (Subject subject : workspace.learning().subjects(workspace.session().token(), workspace.studentId())) {
                if (subject.getId().equals(subjectId)) return subject.getName();
            }
        } catch (RuntimeException exception) {
            // The subject list is not readable, so the fallback below is used.
        }
        return "Mata pelajaran tidak tersedia";
    }

    /** The distinct names of the subjects of the current scope, used by the column filters. */
    static List<String> subjectNames(Workspace workspace) {
        List<String> names = new ArrayList<>();
        try {
            for (Subject subject : workspace.learning().subjects(workspace.session().token(), workspace.studentId())) {
                if (!names.contains(subject.getName())) names.add(subject.getName());
            }
        } catch (RuntimeException exception) {
            // The options stay empty when the data cannot be read.
        }
        return names;
    }

    /** Formats a moment in the Indonesian date format used by the connection pages. */
    static String moment(java.time.Instant instant) {
        if (instant == null) return "—";
        return java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")
                .withLocale(java.util.Locale.forLanguageTag("id"))
                .withZone(java.time.ZoneId.systemDefault())
                .format(instant);
    }

    /** The status of a request as the teacher sees it. */
    static String teacherStatus(id.edu.homeschool.model.ConnectionRequestStatus status) {
        return switch (status) {
            case PENDING -> "Menunggu";
            case APPROVED -> "Disetujui";
            case REJECTED -> "Ditolak";
        };
    }

    /** The status of a request as the student sees it. */
    static String studentStatus(id.edu.homeschool.model.ConnectionRequestStatus status) {
        return switch (status) {
            case PENDING -> "Menunggu";
            case APPROVED -> "Terhubung";
            case REJECTED -> "Ditolak";
        };
    }
}
