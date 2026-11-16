package id.edu.homeschool.repository;

import id.edu.homeschool.model.Evaluation;
import id.edu.homeschool.model.UserAccount;
import id.edu.homeschool.model.LearningAccessGrant;
import id.edu.homeschool.model.TeacherInvitation;
import id.edu.homeschool.model.TeacherConnectionRequest;
import id.edu.homeschool.model.TeacherStudentConnection;
import id.edu.homeschool.model.LearningGoal;
import id.edu.homeschool.model.LearningMaterial;
import id.edu.homeschool.model.LearningTask;
import id.edu.homeschool.model.Pengajar;
import id.edu.homeschool.model.Siswa;
import id.edu.homeschool.model.StudentProfile;
import id.edu.homeschool.model.StudySession;
import id.edu.homeschool.model.Subject;
import java.nio.file.Path;

/** Creates repositories and keeps every application's data file in one directory. */
public final class ApplicationRepositories {
    private final Path dataDirectory;
    private final Repository<StudentProfile> profile;
    private final Repository<Siswa> students;
    private final Repository<Pengajar> teachers;
    private final Repository<Subject> subjects;
    private final Repository<LearningMaterial> materials;
    private final Repository<StudySession> sessions;
    private final Repository<LearningTask> tasks;
    private final Repository<LearningGoal> goals;
    private final Repository<Evaluation> evaluations;
    private final Repository<UserAccount> accounts;
    private final Repository<TeacherInvitation> invitations;
    private final Repository<TeacherConnectionRequest> connectionRequests;
    private final Repository<TeacherStudentConnection> connections;
    private final Repository<LearningAccessGrant> learningAccessGrants;

    public ApplicationRepositories(Path dataDirectory) {
        this.dataDirectory = dataDirectory.toAbsolutePath().normalize();
        profile = repo("profile.json", StudentProfile.class, StudentProfile::getId);
        students = repo("students.json", Siswa.class, Siswa::getId);
        teachers = repo("teachers.json", Pengajar.class, Pengajar::getId);
        subjects = repo("subjects.json", Subject.class, Subject::getId);
        materials = repo("materials.json", LearningMaterial.class, LearningMaterial::getId);
        sessions = repo("study-sessions.json", StudySession.class, StudySession::getId);
        tasks = repo("tasks.json", LearningTask.class, LearningTask::getId);
        goals = repo("goals.json", LearningGoal.class, LearningGoal::getId);
        evaluations = repo("evaluations.json", Evaluation.class, Evaluation::getId);
        accounts = repo("accounts.json", UserAccount.class, UserAccount::getId);
        invitations = repo("teacher-invitations.json", TeacherInvitation.class, TeacherInvitation::getId);
        connectionRequests = repo("teacher-connection-requests.json", TeacherConnectionRequest.class, TeacherConnectionRequest::getId);
        connections = repo("teacher-student-connections.json", TeacherStudentConnection.class, TeacherStudentConnection::getId);
        learningAccessGrants = repo("learning-access-grants.json", LearningAccessGrant.class, LearningAccessGrant::getId);
    }

    public static ApplicationRepositories inDefaultLocation() {
        return new ApplicationRepositories(Path.of(System.getProperty("user.home"), ".homeschooling-learning-system", "data"));
    }
    private <T> Repository<T> repo(String name, Class<T> type, java.util.function.Function<T, String> id) {
        return new JsonRepository<>(dataDirectory.resolve(name), type, id);
    }
    public Path getDataDirectory() { return dataDirectory; }
    public Repository<StudentProfile> profiles() { return profile; }
    public Repository<Siswa> students() { return students; }
    public Repository<Pengajar> teachers() { return teachers; }
    public Repository<Subject> subjects() { return subjects; }
    public Repository<LearningMaterial> materials() { return materials; }
    public Repository<StudySession> sessions() { return sessions; }
    public Repository<LearningTask> tasks() { return tasks; }
    public Repository<LearningGoal> goals() { return goals; }
    public Repository<Evaluation> evaluations() { return evaluations; }
    public Repository<UserAccount> accounts() { return accounts; }
    public Repository<TeacherInvitation> invitations() { return invitations; }
    public Repository<TeacherConnectionRequest> connectionRequests() { return connectionRequests; }
    public Repository<TeacherStudentConnection> connections() { return connections; }
    public Repository<LearningAccessGrant> learningAccessGrants() { return learningAccessGrants; }
}
