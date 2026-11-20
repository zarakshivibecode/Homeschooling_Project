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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/** Creates repositories and keeps every application's data file in one directory. */
public final class ApplicationRepositories {
    private final Path dataDirectory;
    private final DatabaseManager database;
    private final DemoMarkerStore demoMarkers;
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
        database = new DatabaseManager(this.dataDirectory);
        demoMarkers = new DemoMarkerStore(database);
        profile = profileRepository();
        students = studentRepository();
        teachers = teacherRepository();
        subjects = subjectRepository();
        materials = materialRepository();
        sessions = sessionRepository();
        tasks = taskRepository();
        goals = goalRepository();
        evaluations = evaluationRepository();
        accounts = accountRepository();
        invitations = invitationRepository();
        connectionRequests = connectionRequestRepository();
        connections = connectionRepository();
        learningAccessGrants = grantRepository();
        new JsonToSqliteMigrator(database, this.dataDirectory).migrate(this);
    }

    public static ApplicationRepositories inDefaultLocation() {
        return new ApplicationRepositories(Path.of(System.getProperty("user.home"), ".homeschooling-learning-system", "data"));
    }
    public Path getDataDirectory() { return dataDirectory; }
    public DatabaseManager database() { return database; }
    public DemoMarkerStore demoMarkers() { return demoMarkers; }
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

    private Repository<StudentProfile> profileRepository() {
        return new SqliteRepository<>(database, "profile",
                r -> readProfile(r),
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getName()); s.setString(3,v.getEmail()); s.setString(4,v.getPhone());
                    s.setString(5,v.getBirthDate()==null?null:v.getBirthDate().toString()); s.setString(6,v.getEducationLevel()); s.setString(7,v.getLearningPreferences()); },
                StudentProfile::getId,
                "id,name,email,phone,birth_date,education_level,learning_preferences",
                "name=excluded.name,email=excluded.email,phone=excluded.phone,birth_date=excluded.birth_date,"
                + "education_level=excluded.education_level,learning_preferences=excluded.learning_preferences");
    }

    private Repository<Siswa> studentRepository() {
        return new SqliteRepository<>(database, "students",
                r -> readStudent(r),
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getName()); s.setString(3,v.getEmail()); s.setString(4,v.getPhone());
                    s.setString(5,v.getBirthDate()==null?null:v.getBirthDate().toString()); s.setString(6,v.getEducationLevel()); s.setString(7,v.getLearningPreferences()); },
                Siswa::getId,
                "id,name,email,phone,birth_date,education_level,learning_preferences",
                "name=excluded.name,email=excluded.email,phone=excluded.phone,birth_date=excluded.birth_date,"
                + "education_level=excluded.education_level,learning_preferences=excluded.learning_preferences");
    }

    private Repository<Pengajar> teacherRepository() {
        return new SqliteRepository<>(database, "teachers",
                r -> readTeacher(r),
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getName()); s.setString(3,v.getEmail()); s.setString(4,v.getPhone());
                    s.setString(5,v.getExpertise()); s.setString(6,v.getQualifications()); },
                Pengajar::getId,
                "id,name,email,phone,expertise,qualifications",
                "name=excluded.name,email=excluded.email,phone=excluded.phone,expertise=excluded.expertise,"
                + "qualifications=excluded.qualifications");
    }

    private Repository<Subject> subjectRepository() {
        return new SqliteRepository<>(database, "subjects",
                r -> readSubject(r),
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getName()); s.setString(3,v.getDescription()); },
                Subject::getId,
                "id,name,description",
                "name=excluded.name,description=excluded.description");
    }

    private Repository<LearningMaterial> materialRepository() {
        return new SqliteRepository<>(database, "materials",
                r -> {
                    LearningMaterial v = new LearningMaterial(r.getString("subject_id"),r.getString("title"),
                            r.getString("type"),r.getString("location"),r.getString("notes"),
                            r.getString("objectives"),r.getString("content"));
                    v.setId(r.getString("id")); return v;
                },
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getSubjectId()); s.setString(3,v.getTitle());
                    s.setString(4,v.getType()); s.setString(5,v.getLocation()); s.setString(6,v.getNotes());
                    s.setString(7,v.getObjectives()); s.setString(8,v.getContent()); },
                LearningMaterial::getId,
                "id,subject_id,title,type,location,notes,objectives,content",
                "subject_id=excluded.subject_id,title=excluded.title,type=excluded.type,location=excluded.location,"
                + "notes=excluded.notes,objectives=excluded.objectives,content=excluded.content");
    }

    private Repository<StudySession> sessionRepository() {
        return new SqliteRepository<>(database, "study_sessions",
                r -> {
                    StudySession v = new StudySession(r.getString("student_id"),r.getString("teacher_id"),
                            r.getString("subject_id"),LocalDate.parse(r.getString("date")),
                            LocalTime.parse(r.getString("start_time")),r.getInt("duration_minutes"),r.getString("notes"));
                    v.setId(r.getString("id")); return v;
                },
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getStudentId()); s.setString(3,v.getTeacherId());
                    s.setString(4,v.getSubjectId()); s.setString(5,v.getDate().toString()); s.setString(6,v.getStartTime().toString());
                    s.setInt(7,v.getDurationMinutes()); s.setString(8,v.getNotes()); },
                StudySession::getId,
                "id,student_id,teacher_id,subject_id,date,start_time,duration_minutes,notes",
                "student_id=excluded.student_id,teacher_id=excluded.teacher_id,subject_id=excluded.subject_id,"
                + "date=excluded.date,start_time=excluded.start_time,duration_minutes=excluded.duration_minutes,notes=excluded.notes");
    }

    private Repository<LearningTask> taskRepository() {
        return new SqliteRepository<>(database, "tasks",
                r -> {
                    LearningTask v = new LearningTask(r.getString("title"),r.getString("subject_id"),
                            LocalDate.parse(r.getString("due_date")),r.getString("instructions"));
                    v.setId(r.getString("id")); v.setCompleted(r.getInt("completed") != 0); return v;
                },
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getSubjectId()); s.setString(3,v.getTitle());
                    s.setString(4,v.getDueDate().toString()); s.setInt(5,v.isCompleted()?1:0); s.setString(6,v.getInstructions()); },
                LearningTask::getId,
                "id,subject_id,title,due_date,completed,instructions",
                "subject_id=excluded.subject_id,title=excluded.title,due_date=excluded.due_date,"
                + "completed=excluded.completed,instructions=excluded.instructions");
    }

    private Repository<LearningGoal> goalRepository() {
        return new SqliteRepository<>(database, "goals",
                r -> {
                    LearningGoal v = new LearningGoal(r.getString("title"),LocalDate.parse(r.getString("target_date")));
                    v.setId(r.getString("id")); v.setAchieved(r.getInt("achieved") != 0); return v;
                },
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getTitle()); s.setString(3,v.getTargetDate().toString());
                    s.setInt(4,v.isAchieved()?1:0); },
                LearningGoal::getId,
                "id,title,target_date,achieved",
                "title=excluded.title,target_date=excluded.target_date,achieved=excluded.achieved");
    }

    private Repository<Evaluation> evaluationRepository() {
        return new SqliteRepository<>(database, "evaluations",
                r -> new Evaluation(r.getString("id"),r.getString("subject_id"),LocalDate.parse(r.getString("date")),
                        r.getDouble("score"),r.getDouble("maximum_score"),r.getString("notes")),
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getSubjectId()); s.setString(3,v.getDate().toString());
                    s.setDouble(4,v.getScore()); s.setDouble(5,v.getMaximumScore()); s.setString(6,v.getNotes()); },
                Evaluation::getId,
                "id,subject_id,date,score,maximum_score,notes",
                "subject_id=excluded.subject_id,date=excluded.date,score=excluded.score,"
                + "maximum_score=excluded.maximum_score,notes=excluded.notes");
    }

    private Repository<UserAccount> accountRepository() {
        return new SqliteRepository<>(database, "accounts",
                r -> readAccount(r),
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getName()); s.setString(3,v.getEmail());
                    s.setString(4,v.getPasswordHash()); s.setString(5,v.getRole().name()); s.setString(6,v.getStatus().name());
                    s.setString(7,v.getEducationLevel()); },
                UserAccount::getId,
                "id,name,email,password_hash,role,status,education_level",
                "name=excluded.name,email=excluded.email,password_hash=excluded.password_hash,role=excluded.role,"
                + "status=excluded.status,education_level=excluded.education_level");
    }

    private Repository<TeacherInvitation> invitationRepository() {
        return new SqliteRepository<>(database, "teacher_invitations",
                r -> readInvitation(r),
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getTeacherAccountId()); s.setString(3,v.getCodeHash());
                    s.setString(4,v.getCreatedAt().toString()); s.setString(5,v.getExpiresAt().toString());
                    s.setString(6,v.getUsedAt()==null?null:v.getUsedAt().toString()); s.setString(7,v.getStatus().name()); },
                TeacherInvitation::getId,
                "id,teacher_account_id,code_hash,created_at,expires_at,used_at,status",
                "teacher_account_id=excluded.teacher_account_id,code_hash=excluded.code_hash,"
                + "created_at=excluded.created_at,expires_at=excluded.expires_at,used_at=excluded.used_at,status=excluded.status");
    }

    private Repository<TeacherConnectionRequest> connectionRequestRepository() {
        return new SqliteRepository<>(database, "teacher_connection_requests",
                r -> {
                    TeacherConnectionRequest v = new TeacherConnectionRequest(r.getString("student_account_id"),
                            r.getString("teacher_account_id"),r.getString("invitation_id"),Instant.parse(r.getString("created_at")));
                    v.setId(r.getString("id")); v.setStatus(id.edu.homeschool.model.ConnectionRequestStatus.valueOf(r.getString("status")));
                    v.setUpdatedAt(Instant.parse(r.getString("updated_at"))); return v;
                },
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getStudentAccountId()); s.setString(3,v.getTeacherAccountId());
                    s.setString(4,v.getInvitationId()); s.setString(5,v.getStatus().name()); s.setString(6,v.getCreatedAt().toString());
                    s.setString(7,v.getUpdatedAt().toString()); },
                TeacherConnectionRequest::getId,
                "id,student_account_id,teacher_account_id,invitation_id,status,created_at,updated_at",
                "student_account_id=excluded.student_account_id,teacher_account_id=excluded.teacher_account_id,"
                + "invitation_id=excluded.invitation_id,status=excluded.status,created_at=excluded.created_at,updated_at=excluded.updated_at");
    }

    private Repository<TeacherStudentConnection> connectionRepository() {
        return new SqliteRepository<>(database, "teacher_student_connections",
                r -> {
                    TeacherStudentConnection v = new TeacherStudentConnection(r.getString("student_account_id"),
                            r.getString("teacher_account_id"),Instant.parse(r.getString("created_at")));
                    v.setId(r.getString("id")); v.setStatus(id.edu.homeschool.model.ConnectionStatus.valueOf(r.getString("status")));
                    String updated=r.getString("updated_at"); if(updated!=null) v.setUpdatedAt(Instant.parse(updated)); return v;
                },
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getStudentAccountId()); s.setString(3,v.getTeacherAccountId());
                    s.setString(4,v.getStatus().name()); s.setString(5,v.getCreatedAt().toString());
                    s.setString(6,v.getUpdatedAt()==null?null:v.getUpdatedAt().toString()); },
                TeacherStudentConnection::getId,
                "id,student_account_id,teacher_account_id,status,created_at,updated_at",
                "student_account_id=excluded.student_account_id,teacher_account_id=excluded.teacher_account_id,"
                + "status=excluded.status,created_at=excluded.created_at,updated_at=excluded.updated_at");
    }

    private Repository<LearningAccessGrant> grantRepository() {
        return new SqliteRepository<>(database, "learning_access_grants",
                r -> {
                    LearningAccessGrant v = new LearningAccessGrant(r.getString("resource_type"),r.getString("resource_id"),
                            r.getString("teacher_account_id"),r.getString("student_account_id")); v.setId(r.getString("id")); return v;
                },
                (s, v) -> { s.setString(1,v.getId()); s.setString(2,v.getResourceType()); s.setString(3,v.getResourceId());
                    s.setString(4,v.getTeacherAccountId()); s.setString(5,v.getStudentAccountId()); },
                LearningAccessGrant::getId,
                "id,resource_type,resource_id,teacher_account_id,student_account_id",
                "resource_type=excluded.resource_type,resource_id=excluded.resource_id,"
                + "teacher_account_id=excluded.teacher_account_id,student_account_id=excluded.student_account_id");
    }

    private StudentProfile readProfile(ResultSet r) throws java.sql.SQLException {
        String birth=r.getString("birth_date");
        StudentProfile v=new StudentProfile();
        v.setName(r.getString("name"));
        v.setEmail(r.getString("email"));
        v.setPhone(r.getString("phone"));
        if (birth != null) v.setBirthDate(LocalDate.parse(birth));
        v.setEducationLevel(r.getString("education_level"));
        v.setLearningPreferences(r.getString("learning_preferences"));
        v.setId(r.getString("id")); v.setEmail(r.getString("email")); v.setPhone(r.getString("phone")); return v;
    }
    private Siswa readStudent(ResultSet r) throws java.sql.SQLException {
        String birth=r.getString("birth_date");
        Siswa v=new Siswa();
        v.setName(r.getString("name")); v.setEmail(r.getString("email")); v.setPhone(r.getString("phone"));
        if (birth != null) v.setBirthDate(LocalDate.parse(birth));
        v.setEducationLevel(r.getString("education_level"));
        v.setLearningPreferences(r.getString("learning_preferences"));
        v.setId(r.getString("id")); return v;
    }
    private Subject readSubject(ResultSet r) throws java.sql.SQLException {
        Subject v=new Subject(r.getString("name"),r.getString("description"));
        v.setId(r.getString("id")); return v;
    }
    private Pengajar readTeacher(ResultSet r) throws java.sql.SQLException {
        Pengajar v=new Pengajar(r.getString("name"),r.getString("email"),r.getString("phone"),
                r.getString("expertise"),r.getString("qualifications")); v.setId(r.getString("id")); return v;
    }
    private UserAccount readAccount(ResultSet r) throws java.sql.SQLException {
        UserAccount v=new UserAccount(r.getString("name"),r.getString("email"),r.getString("password_hash"),
                id.edu.homeschool.model.AccountRole.valueOf(r.getString("role")),r.getString("education_level"));
        v.setId(r.getString("id")); v.setStatus(id.edu.homeschool.model.AccountStatus.valueOf(r.getString("status"))); return v;
    }
    private TeacherInvitation readInvitation(ResultSet r) throws java.sql.SQLException {
        TeacherInvitation v=new TeacherInvitation(r.getString("teacher_account_id"),r.getString("code_hash"),
                Instant.parse(r.getString("created_at")),Instant.parse(r.getString("expires_at")));
        v.setId(r.getString("id")); String used=r.getString("used_at"); if(used!=null)v.setUsedAt(Instant.parse(used));
        v.setStatus(id.edu.homeschool.model.InvitationStatus.valueOf(r.getString("status"))); return v;
    }
}
