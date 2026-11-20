package id.edu.homeschool.repository;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import id.edu.homeschool.model.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** One-time, idempotent migration from the original JSON files to SQLite. */
public final class JsonToSqliteMigrator {
    public static final String MIGRATION_NAME = "json-v1-to-sqlite";
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final DatabaseManager database;
    private final Path dataDirectory;

    public JsonToSqliteMigrator(DatabaseManager database, Path dataDirectory) {
        this.database = database;
        this.dataDirectory = dataDirectory.toAbsolutePath().normalize();
    }

    public synchronized void migrate(ApplicationRepositories repositories) {
        if (database.migrationComplete(MIGRATION_NAME)) return;
        boolean hasJson = false;
        for (String name : JSON_FILES) if (Files.exists(dataDirectory.resolve(name))) hasJson = true;
        if (!hasJson) {
            database.markMigrationComplete(MIGRATION_NAME);
            return;
        }

        Path backup = createBackup();
        System.out.println("Backup data JSON dibuat di " + backup);
        try {
            migrateFile(repositories.profiles(), "profile.json", StudentProfile.class);
            migrateFile(repositories.students(), "students.json", Siswa.class);
            migrateFile(repositories.teachers(), "teachers.json", Pengajar.class);
            migrateFile(repositories.accounts(), "accounts.json", UserAccount.class);
            migrateFile(repositories.subjects(), "subjects.json", Subject.class);
            migrateFile(repositories.materials(), "materials.json", LearningMaterial.class);
            migrateFile(repositories.tasks(), "tasks.json", LearningTask.class);
            migrateFile(repositories.sessions(), "study-sessions.json", StudySession.class);
            migrateFile(repositories.evaluations(), "evaluations.json", Evaluation.class);
            migrateFile(repositories.goals(), "goals.json", LearningGoal.class);
            migrateFile(repositories.invitations(), "teacher-invitations.json", TeacherInvitation.class);
            migrateFile(repositories.connectionRequests(), "teacher-connection-requests.json", TeacherConnectionRequest.class);
            migrateFile(repositories.connections(), "teacher-student-connections.json", TeacherStudentConnection.class);
            migrateFile(repositories.learningAccessGrants(), "learning-access-grants.json", LearningAccessGrant.class);
            migrateDemoMarkers(repositories);
            database.markMigrationComplete(MIGRATION_NAME);
            System.out.println("Migrasi JSON ke SQLite selesai.");
        } catch (RuntimeException exception) {
            throw new RepositoryException(
                    "Migrasi JSON ke SQLite gagal. Berkas asli dan backup tetap utuh. "
                    + "Pulihkan atau perbaiki data sebelum memulai ulang. Backup: " + backup,
                    exception);
        }
    }

    private <T> void migrateFile(Repository<T> repository, String fileName, Class<T> type) {
        Path file = dataDirectory.resolve(fileName);
        if (!Files.exists(file)) return;
        try {
            JavaType listType = MAPPER.getTypeFactory().constructCollectionType(List.class, type);
            List<T> values = MAPPER.readValue(file.toFile(), listType);
            if (values == null) return;
            for (T value : values) repository.save(value);
        } catch (IOException | RuntimeException exception) {
            throw new RepositoryException("Berkas JSON " + fileName + " tidak dapat dipindahkan: "
                    + exception.getMessage(), exception);
        }
    }

    private void migrateDemoMarkers(ApplicationRepositories repositories) {
        Path file = dataDirectory.resolve("demo-data.json");
        if (!Files.exists(file)) return;
        try {
            DemoMarkers markers = MAPPER.readValue(file.toFile(), DemoMarkers.class);
            if (markers != null) repositories.demoMarkers().save(markers);
        } catch (IOException | RuntimeException exception) {
            throw new RepositoryException("Berkas demo-data.json tidak dapat dipindahkan: "
                    + exception.getMessage(), exception);
        }
    }

    private Path createBackup() {
        String stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
        Path backup = dataDirectory.resolve("json-backup-" + stamp);
        int suffix = 1;
        while (Files.exists(backup)) {
            backup = dataDirectory.resolve("json-backup-" + stamp + "-" + suffix++);
        }
        try {
            Files.createDirectories(backup);
            for (String name : JSON_FILES) {
                Path source = dataDirectory.resolve(name);
                if (Files.exists(source)) Files.copy(source, backup.resolve(name),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return backup;
        } catch (IOException exception) {
            throw new RepositoryException("Backup data JSON tidak dapat dibuat.", exception);
        }
    }

    private static final String[] JSON_FILES = {
        "profile.json", "students.json", "teachers.json", "subjects.json", "materials.json",
        "study-sessions.json", "tasks.json", "goals.json", "evaluations.json", "accounts.json",
        "teacher-invitations.json", "teacher-connection-requests.json",
        "teacher-student-connections.json", "learning-access-grants.json", "demo-data.json"
    };
}
