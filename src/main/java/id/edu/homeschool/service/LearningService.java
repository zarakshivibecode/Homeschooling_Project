package id.edu.homeschool.service;

import id.edu.homeschool.model.Evaluation;
import id.edu.homeschool.model.LearningGoal;
import id.edu.homeschool.model.LearningMaterial;
import id.edu.homeschool.model.LearningTask;
import id.edu.homeschool.model.Pengajar;
import id.edu.homeschool.model.Siswa;
import id.edu.homeschool.model.StudentProfile;
import id.edu.homeschool.model.StudySession;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.repository.ApplicationRepositories;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Application logic shared by Swing pages; all persistent access goes through repositories. */
public final class LearningService {
    private final ApplicationRepositories repositories;

    public LearningService(ApplicationRepositories repositories) { this.repositories = Objects.requireNonNull(repositories); }
    public ApplicationRepositories repositories() { return repositories; }
    public List<StudentProfile> profiles() { return repositories.profiles().findAll(); }
    public StudentProfile saveProfile(StudentProfile value) { return repositories.profiles().save(value); }
    public List<Siswa> students() { return repositories.students().findAll(); }
    public Siswa saveStudent(Siswa value) { return repositories.students().save(value); }
    public boolean deleteStudent(String id) {
        if (sessions().stream().anyMatch(session -> session.getStudentId().equals(id)))
            throw new IllegalStateException("Siswa masih terhubung dengan jadwal belajar.");
        return repositories.students().deleteById(id);
    }
    public List<Pengajar> teachers() { return repositories.teachers().findAll(); }
    public Pengajar saveTeacher(Pengajar value) { return repositories.teachers().save(value); }
    public boolean deleteTeacher(String id) {
        if (sessions().stream().anyMatch(session -> session.getTeacherId().equals(id)))
            throw new IllegalStateException("Pengajar masih terhubung dengan jadwal belajar.");
        return repositories.teachers().deleteById(id);
    }
    public List<Subject> subjects() { return repositories.subjects().findAll(); }
    public Subject saveSubject(Subject value) { return repositories.subjects().save(value); }
    public boolean deleteSubject(String id) {
        boolean inUse = materials().stream().anyMatch(x -> x.getSubjectId().equals(id))
                || sessions().stream().anyMatch(x -> x.getSubjectId().equals(id))
                || tasks().stream().anyMatch(x -> x.getSubjectId().equals(id))
                || evaluations().stream().anyMatch(x -> x.getSubjectId().equals(id));
        if (inUse) throw new IllegalStateException("Mata pelajaran masih digunakan oleh materi, jadwal, tugas, atau evaluasi.");
        return repositories.subjects().deleteById(id);
    }
    public List<LearningMaterial> materials() { return repositories.materials().findAll(); }
    public LearningMaterial saveMaterial(LearningMaterial value) { requireSubject(value.getSubjectId()); return repositories.materials().save(value); }
    public boolean deleteMaterial(String id) { return repositories.materials().deleteById(id); }
    public List<StudySession> sessions() { return repositories.sessions().findAll(); }
    public StudySession saveSession(StudySession value) {
        requireSubject(value.getSubjectId());
        requireStudent(value.getStudentId());
        requireTeacher(value.getTeacherId());
        return repositories.sessions().save(value);
    }
    public boolean deleteSession(String id) { return repositories.sessions().deleteById(id); }
    public List<LearningTask> tasks() { return repositories.tasks().findAll(); }
    public LearningTask saveTask(LearningTask value) { requireSubject(value.getSubjectId()); return repositories.tasks().save(value); }
    public boolean deleteTask(String id) { return repositories.tasks().deleteById(id); }
    public List<LearningGoal> goals() { return repositories.goals().findAll(); }
    public LearningGoal saveGoal(LearningGoal value) { return repositories.goals().save(value); }
    public boolean deleteGoal(String id) { return repositories.goals().deleteById(id); }
    public List<Evaluation> evaluations() { return repositories.evaluations().findAll(); }
    public Evaluation saveEvaluation(Evaluation value) { requireSubject(value.getSubjectId()); return repositories.evaluations().save(value); }
    public boolean deleteEvaluation(String id) { return repositories.evaluations().deleteById(id); }

    public DashboardSummary dashboard() {
        LocalDate today = LocalDate.now();
        List<LearningTask> tasks = tasks();
        List<LearningGoal> goals = goals();
        List<Evaluation> evaluations = evaluations();
        return new DashboardSummary(subjects().size(), students().size(), teachers().size(),
                (int) sessions().stream().filter(x -> today.equals(x.getDate())).count(),
                (int) tasks.stream().filter(x -> !x.isCompleted()).count(),
                (int) tasks.stream().filter(x -> x.isOverdue(today)).count(),
                (int) goals.stream().filter(LearningGoal::isAchieved).count(), goals.size(), averagePercent(evaluations));
    }

    public ProgressSummary progress(String subjectId, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) throw new IllegalArgumentException("Tanggal awal filter tidak boleh melewati tanggal akhir.");
        Predicate<String> matchesSubject = id -> subjectId == null || subjectId.isBlank() || subjectId.equals(id);
        List<LearningTask> tasks = tasks().stream().filter(x -> matchesSubject.test(x.getSubjectId()) && inRange(x.getDueDate(), from, to)).toList();
        List<LearningGoal> goals = goals().stream().filter(x -> inRange(x.getTargetDate(), from, to)).toList();
        List<Evaluation> evaluations = evaluations().stream().filter(x -> matchesSubject.test(x.getSubjectId()) && inRange(x.getDate(), from, to)).toList();
        double averageScore = evaluations.stream().mapToDouble(Evaluation::getScore).average().orElse(0);
        double averageMaximum = evaluations.stream().mapToDouble(Evaluation::getMaximumScore).average().orElse(0);
        return new ProgressSummary((int) tasks.stream().filter(LearningTask::isCompleted).count(), tasks.size(),
                (int) goals.stream().filter(LearningGoal::isAchieved).count(), goals.size(), evaluations.size(),
                averageScore, averageMaximum, averagePercent(evaluations));
    }

    private void requireSubject(String id) {
        if (repositories.subjects().findById(id).isEmpty()) throw new IllegalArgumentException("Mata pelajaran yang dipilih tidak ditemukan.");
    }
    private void requireStudent(String id) {
        if (repositories.students().findById(id).isEmpty()) throw new IllegalArgumentException("Siswa yang dipilih tidak ditemukan.");
    }
    private void requireTeacher(String id) {
        if (repositories.teachers().findById(id).isEmpty()) throw new IllegalArgumentException("Pengajar yang dipilih tidak ditemukan.");
    }
    private static boolean inRange(LocalDate value, LocalDate from, LocalDate to) {
        return value != null && (from == null || !value.isBefore(from)) && (to == null || !value.isAfter(to));
    }
    private static double averagePercent(List<Evaluation> evaluations) {
        return evaluations.stream().mapToDouble(Evaluation::getPercentage).average().orElse(0);
    }
}
