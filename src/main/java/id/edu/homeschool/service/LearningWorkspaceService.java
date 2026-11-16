package id.edu.homeschool.service;

import id.edu.homeschool.model.AccountRole;
import id.edu.homeschool.model.Evaluation;
import id.edu.homeschool.model.LearningAccessGrant;
import id.edu.homeschool.model.LearningGoal;
import id.edu.homeschool.model.LearningMaterial;
import id.edu.homeschool.model.LearningTask;
import id.edu.homeschool.model.StudySession;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.model.UserAccount;
import id.edu.homeschool.repository.ApplicationRepositories;
import id.edu.homeschool.repository.Repository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Session-scoped facade for learning records. Every read and write is checked against the
 * signed-in account and against the ownership grant that links a record to a student.
 */
public final class LearningWorkspaceService {
    public static final String TYPE_SUBJECT = "subject";
    public static final String TYPE_MATERIAL = "material";
    public static final String TYPE_TASK = "task";
    public static final String TYPE_SESSION = "session";
    public static final String TYPE_EVALUATION = "evaluation";
    public static final String TYPE_GOAL = "goal";

    private final ApplicationRepositories repos;
    private final SessionRegistry sessions;

    public LearningWorkspaceService(ApplicationRepositories repos, SessionRegistry sessions) {
        this.repos = Objects.requireNonNull(repos);
        this.sessions = Objects.requireNonNull(sessions);
    }

    public List<AccountSummary> connectedStudents(String token) {
        UserAccount teacher = require(token, AccountRole.PENGAJAR);
        return repos.connections().findAll().stream()
                .filter(c -> c.getStatus() == id.edu.homeschool.model.ConnectionStatus.ACTIVE
                        && c.getTeacherAccountId().equals(teacher.getId()))
                .map(id.edu.homeschool.model.TeacherStudentConnection::getStudentAccountId).distinct()
                .map(id -> repos.accounts().findById(id).orElse(null))
                .filter(Objects::nonNull)
                .map(a -> new AccountSummary(a.getId(), a.getName(), a.getEmail(), a.getRole(), a.getEducationLevel()))
                .toList();
    }

    public List<Subject> subjects(String token, String studentId) {
        return list(token, studentId, TYPE_SUBJECT, repos.subjects()::findAll, Subject::getId);
    }
    public List<LearningMaterial> materials(String token, String studentId) {
        return list(token, studentId, TYPE_MATERIAL, repos.materials()::findAll, LearningMaterial::getId);
    }
    public List<LearningTask> tasks(String token, String studentId) {
        return list(token, studentId, TYPE_TASK, repos.tasks()::findAll, LearningTask::getId);
    }
    public List<StudySession> sessions(String token, String studentId) {
        return list(token, studentId, TYPE_SESSION, repos.sessions()::findAll, StudySession::getId);
    }
    public List<Evaluation> evaluations(String token, String studentId) {
        return list(token, studentId, TYPE_EVALUATION, repos.evaluations()::findAll, Evaluation::getId);
    }
    public List<LearningGoal> goals(String token, String studentId) {
        return list(token, studentId, TYPE_GOAL, repos.goals()::findAll, LearningGoal::getId);
    }

    public Subject saveSubject(String token, String studentId, Subject value) {
        return save(token, studentId, TYPE_SUBJECT, value.getId(), repos.subjects()::save, repos.subjects(), value);
    }
    public LearningMaterial saveMaterial(String token, String studentId, LearningMaterial value) {
        requireSubject(token, studentId, value.getSubjectId());
        return save(token, studentId, TYPE_MATERIAL, value.getId(), repos.materials()::save, repos.materials(), value);
    }
    public LearningTask saveTask(String token, String studentId, LearningTask value) {
        requireSubject(token, studentId, value.getSubjectId());
        return save(token, studentId, TYPE_TASK, value.getId(), repos.tasks()::save, repos.tasks(), value);
    }
    public StudySession saveSession(String token, String studentId, StudySession value) {
        requireSubject(token, studentId, value.getSubjectId());
        value.setStudentId(studentId);
        value.setTeacherId(require(token, AccountRole.PENGAJAR).getId());
        return save(token, studentId, TYPE_SESSION, value.getId(), repos.sessions()::save, repos.sessions(), value);
    }
    public Evaluation saveEvaluation(String token, String studentId, Evaluation value) {
        requireSubject(token, studentId, value.getSubjectId());
        return save(token, studentId, TYPE_EVALUATION, value.getId(), repos.evaluations()::save, repos.evaluations(), value);
    }
    public LearningGoal saveGoal(String token, String studentId, LearningGoal value) {
        return save(token, studentId, TYPE_GOAL, value.getId(), repos.goals()::save, repos.goals(), value);
    }

    /** A student may only flip the completion flag of a task that belongs to them. */
    public void setTaskCompleted(String token, String taskId, boolean completed) {
        UserAccount student = require(token, AccountRole.SISWA);
        LearningTask task = repos.tasks().findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tugas tidak ditemukan."));
        LearningAccessGrant grant = grants().stream()
                .filter(g -> TYPE_TASK.equals(g.getResourceType()) && taskId.equals(g.getResourceId()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Tugas tidak ditemukan."));
        if (!student.getId().equals(grant.getStudentAccountId())) throw denied();
        task.setCompleted(completed);
        repos.tasks().save(task);
    }

    /** A teacher may only flip the achievement flag of a goal that belongs to their student. */
    public void setGoalAchieved(String token, String goalId, boolean achieved) {
        LearningGoal goal = repos.goals().findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Target tidak ditemukan."));
        LearningAccessGrant grant = grants().stream()
                .filter(g -> TYPE_GOAL.equals(g.getResourceType()) && goalId.equals(g.getResourceId()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Target tidak ditemukan."));
        requireTeacherForStudent(token, grant.getStudentAccountId());
        goal.setAchieved(achieved);
        repos.goals().save(goal);
    }

    public boolean delete(String token, String studentId, String type, String id) {
        UserAccount actor = requireTeacherForStudent(token, studentId);
        LearningAccessGrant grant = grants().stream()
                .filter(g -> type.equals(g.getResourceType()) && id.equals(g.getResourceId()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Data tidak ditemukan."));
        if (!actor.getId().equals(grant.getTeacherAccountId()) || !studentId.equals(grant.getStudentAccountId())) throw denied();
        if (TYPE_SUBJECT.equals(type)) {
            boolean referenced = repos.materials().findAll().stream().anyMatch(x -> x.getSubjectId().equals(id))
                    || repos.tasks().findAll().stream().anyMatch(x -> x.getSubjectId().equals(id))
                    || repos.sessions().findAll().stream().anyMatch(x -> x.getSubjectId().equals(id))
                    || repos.evaluations().findAll().stream().anyMatch(x -> x.getSubjectId().equals(id));
            if (referenced) throw new IllegalStateException("Mata pelajaran masih dipakai oleh materi, jadwal, tugas, atau evaluasi.");
        }
        boolean removed = switch (type) {
            case TYPE_SUBJECT -> repos.subjects().deleteById(id);
            case TYPE_MATERIAL -> repos.materials().deleteById(id);
            case TYPE_TASK -> repos.tasks().deleteById(id);
            case TYPE_SESSION -> repos.sessions().deleteById(id);
            case TYPE_EVALUATION -> repos.evaluations().deleteById(id);
            case TYPE_GOAL -> repos.goals().deleteById(id);
            default -> throw new IllegalArgumentException("Jenis data tidak didukung.");
        };
        if (removed) repos.learningAccessGrants().deleteById(grant.getId());
        return removed;
    }

    /**
     * Builds a chronological feed of the learning activity of one student. The dashboard uses it
     * so the "recent activity" list always reflects the records that are really stored.
     */
    public List<ActivityEntry> recentActivity(String token, String studentId, int limit) {
        Map<String, String> subjectNames = new LinkedHashMap<>();
        subjects(token, studentId).forEach(subject -> subjectNames.put(subject.getId(), subject.getName()));
        List<ActivityEntry> entries = new ArrayList<>();

        for (StudySession session : sessions(token, studentId)) {
            entries.add(new ActivityEntry(session.getDate().atTime(session.getStartTime()), "Jadwal",
                    "Sesi " + subjectNames.getOrDefault(session.getSubjectId(), "Mata pelajaran"),
                    session.getStartTime() + " · " + session.getDurationMinutes() + " menit"
                            + (session.getNotes().isBlank() ? "" : " · " + session.getNotes()),
                    session.getDate().isBefore(LocalDate.now()) ? "Selesai" : "Terjadwal"));
        }
        for (LearningTask task : tasks(token, studentId)) {
            entries.add(new ActivityEntry(task.getDueDate().atStartOfDay(), "Tugas", task.getTitle(),
                    subjectNames.getOrDefault(task.getSubjectId(), "Mata pelajaran")
                            + " · tenggat " + task.getDueDate(),
                    task.getStatusLabel()));
        }
        for (Evaluation evaluation : evaluations(token, studentId)) {
            entries.add(new ActivityEntry(evaluation.getDate().atStartOfDay(), "Evaluasi",
                    "Penilaian " + subjectNames.getOrDefault(evaluation.getSubjectId(), "Mata pelajaran"),
                    String.format(java.util.Locale.ROOT, "%.1f / %.1f · %s", evaluation.getScore(),
                            evaluation.getMaximumScore(),
                            evaluation.getNotes().isBlank() ? "tanpa catatan" : evaluation.getNotes()),
                    String.format(java.util.Locale.ROOT, "%.0f%%", evaluation.getPercentage())));
        }
        for (LearningGoal goal : goals(token, studentId)) {
            entries.add(new ActivityEntry(goal.getTargetDate().atStartOfDay(), "Target", goal.getDescription(),
                    "Tanggal target " + goal.getTargetDate(), goal.getStatusLabel()));
        }

        entries.sort(java.util.Comparator.comparing(ActivityEntry::when).reversed());
        int size = Math.max(1, limit);
        return entries.size() <= size ? List.copyOf(entries) : List.copyOf(entries.subList(0, size));
    }

    /** The students connected to the signed-in teacher together with their learning figures. */
    public List<PersonSummary> studentSummaries(String token) {
        List<PersonSummary> summaries = new ArrayList<>();
        for (AccountSummary student : connectedStudents(token)) {
            ProgressReport report = progress(token, student.id());
            summaries.add(new PersonSummary(student.id(), student.name(), student.email(), student.role(),
                    student.educationLevel(), report.subjectCount(), report.materialCount(), report.taskCount(),
                    report.completedTaskCount(), report.evaluationCount(), report.averagePercent()));
        }
        return summaries;
    }

    /** The teachers connected to the signed-in student together with the shared learning figures. */
    public List<PersonSummary> teacherSummaries(String token) {
        UserAccount student = require(token, AccountRole.SISWA);
        ProgressReport report = progress(token, student.getId());
        List<PersonSummary> summaries = new ArrayList<>();
        for (id.edu.homeschool.model.TeacherStudentConnection link : repos.connections().findAll()) {
            if (!student.getId().equals(link.getStudentAccountId())
                    || link.getStatus() != id.edu.homeschool.model.ConnectionStatus.ACTIVE) continue;
            UserAccount teacher = repos.accounts().findById(link.getTeacherAccountId()).orElse(null);
            if (teacher == null) continue;
            int shared = (int) grants().stream()
                    .filter(grant -> grant.getTeacherAccountId().equals(teacher.getId())
                            && grant.getStudentAccountId().equals(student.getId())
                            && TYPE_SUBJECT.equals(grant.getResourceType()))
                    .count();
            summaries.add(new PersonSummary(teacher.getId(), teacher.getName(), teacher.getEmail(), teacher.getRole(),
                    teacher.getEducationLevel(), shared, report.materialCount(), report.taskCount(),
                    report.completedTaskCount(), report.evaluationCount(), report.averagePercent()));
        }
        return summaries;
    }

    /**
     * The student a learning record belongs to. Teachers may only ask about records they own and
     * students may only ask about their own records.
     */
    public String targetStudentName(String token, String resourceType, String resourceId) {        UserAccount actor = sessions.requireAccount(token);
        LearningAccessGrant grant = grants().stream()
                .filter(g -> resourceType.equals(g.getResourceType()) && resourceId.equals(g.getResourceId()))
                .findFirst().orElse(null);
        if (grant == null) return "—";
        if (actor.getRole() == AccountRole.SISWA && !actor.getId().equals(grant.getStudentAccountId())) throw denied();
        if (actor.getRole() == AccountRole.PENGAJAR && !actor.getId().equals(grant.getTeacherAccountId())) throw denied();
        return repos.accounts().findById(grant.getStudentAccountId())
                .map(UserAccount::getName).orElse("Siswa tidak tersedia");
    }

    /** Builds the progress report for one student from the records the caller is allowed to see. */
    public ProgressReport progress(String token, String studentId) {
        List<Subject> subjectList = subjects(token, studentId);
        List<LearningTask> taskList = tasks(token, studentId);
        List<LearningGoal> goalList = goals(token, studentId);
        List<Evaluation> evaluationList = evaluations(token, studentId);
        List<LearningMaterial> materialList = materials(token, studentId);
        List<StudySession> sessionList = sessions(token, studentId);
        LocalDate today = LocalDate.now();

        Map<String, String> subjectNames = new LinkedHashMap<>();
        subjectList.forEach(subject -> subjectNames.put(subject.getId(), subject.getName()));

        List<ProgressReport.SubjectProgress> perSubject = new ArrayList<>();
        for (Subject subject : subjectList) {
            List<LearningTask> subjectTasks = taskList.stream().filter(t -> subject.getId().equals(t.getSubjectId())).toList();
            List<Evaluation> subjectEvaluations = evaluationList.stream().filter(e -> subject.getId().equals(e.getSubjectId())).toList();
            perSubject.add(new ProgressReport.SubjectProgress(
                    subject.getId(), subject.getName(),
                    subjectTasks.size(), (int) subjectTasks.stream().filter(LearningTask::isCompleted).count(),
                    subjectEvaluations.size(),
                    subjectEvaluations.stream().mapToDouble(Evaluation::getPercentage).average().orElse(0)));
        }

        return new ProgressReport(
                subjectList.size(), materialList.size(), sessionList.size(),
                taskList.size(), (int) taskList.stream().filter(LearningTask::isCompleted).count(),
                (int) taskList.stream().filter(t -> t.isOverdue(today)).count(),
                goalList.size(), (int) goalList.stream().filter(LearningGoal::isAchieved).count(),
                evaluationList.size(),
                evaluationList.stream().mapToDouble(Evaluation::getPercentage).average().orElse(0),
                subjectNames.size(),
                perSubject);
    }

    private <T> List<T> list(String token, String studentId, String type, java.util.function.Supplier<List<T>> all,
            Function<T, String> id) {
        UserAccount actor = sessions.requireAccount(token);
        if (actor.getRole() == AccountRole.PENGAJAR) {
            requireTeacherForStudent(token, studentId);
        } else if (actor.getRole() != AccountRole.SISWA || !actor.getId().equals(studentId)) {
            throw denied();
        }
        List<LearningAccessGrant> grants = grants();
        return all.get().stream()
                .filter(value -> grants.stream().anyMatch(g -> type.equals(g.getResourceType())
                        && id.apply(value).equals(g.getResourceId())
                        && studentId.equals(g.getStudentAccountId())
                        && (actor.getRole() == AccountRole.SISWA || g.getTeacherAccountId().equals(actor.getId()))))
                .toList();
    }

    private <T> T save(String token, String studentId, String type, String id, Function<T, T> writer,
            Repository<T> repository, T value) {
        UserAccount teacher = requireTeacherForStudent(token, studentId);
        boolean recordExists = repository.findById(id).isPresent();
        LearningAccessGrant existing = grants().stream()
                .filter(g -> type.equals(g.getResourceType()) && id.equals(g.getResourceId()))
                .findFirst().orElse(null);
        if (existing == null && recordExists) {
            throw new SecurityException("ID data tersebut sudah digunakan oleh data lama dan tidak dapat ditautkan otomatis.");
        }
        if (existing != null && (!existing.getTeacherAccountId().equals(teacher.getId())
                || !existing.getStudentAccountId().equals(studentId))) {
            throw denied();
        }
        T saved = writer.apply(value);
        if (existing == null) {
            repos.learningAccessGrants().save(new LearningAccessGrant(type, id, teacher.getId(), studentId));
        }
        return saved;
    }

    private void requireSubject(String token, String studentId, String subjectId) {
        if (subjects(token, studentId).stream().noneMatch(s -> s.getId().equals(subjectId))) {
            throw new IllegalArgumentException("Mata pelajaran belum terdaftar untuk siswa ini.");
        }
    }

    private UserAccount requireTeacherForStudent(String token, String studentId) {
        UserAccount teacher = require(token, AccountRole.PENGAJAR);
        if (studentId == null || studentId.isBlank()) throw denied();
        boolean active = repos.connections().findAll().stream().anyMatch(c ->
                c.getStatus() == id.edu.homeschool.model.ConnectionStatus.ACTIVE
                && c.getTeacherAccountId().equals(teacher.getId()) && c.getStudentAccountId().equals(studentId));
        if (!active) throw denied();
        return teacher;
    }

    private UserAccount require(String token, AccountRole role) {
        UserAccount actor = sessions.requireAccount(token);
        if (actor.getRole() != role) throw denied();
        return actor;
    }

    private List<LearningAccessGrant> grants() { return repos.learningAccessGrants().findAll(); }

    private static SecurityException denied() { return new SecurityException("Anda tidak memiliki akses untuk data ini."); }
}
