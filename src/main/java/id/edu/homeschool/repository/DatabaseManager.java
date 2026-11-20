package id.edu.homeschool.repository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/** Creates and owns the application's local SQLite database. */
public final class DatabaseManager implements AutoCloseable {
    public static final String DATABASE_FILE = "homeschooling.db";
    private final Path databaseFile;
    private final String jdbcUrl;

    public DatabaseManager(Path dataDirectory) {
        Path directory = Objects.requireNonNull(dataDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
        } catch (java.io.IOException exception) {
            throw new RepositoryException("Direktori basis data tidak dapat dibuat.", exception);
        }
        databaseFile = directory.resolve(DATABASE_FILE);
        jdbcUrl = "jdbc:sqlite:" + databaseFile;
        initializeSchema();
    }

    public Path getDatabaseFile() {
        return databaseFile;
    }

    public Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                statement.execute("PRAGMA busy_timeout = 5000");
                statement.execute("PRAGMA journal_mode = WAL");
            }
            connection.setAutoCommit(true);
            return connection;
        } catch (SQLException exception) {
            throw new RepositoryException("Koneksi SQLite tidak dapat dibuka.", exception);
        }
    }

    public boolean migrationComplete(String name) {
        try (Connection connection = getConnection();
             java.sql.PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM app_migrations WHERE name = ? AND status = 'complete'")) {
            statement.setString(1, name);
            try (java.sql.ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Status migrasi basis data tidak dapat dibaca.", exception);
        }
    }

    public void markMigrationComplete(String name) {
        try (Connection connection = getConnection();
             java.sql.PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO app_migrations(name,status,completed_at,error) VALUES(?, 'complete', ?, NULL) "
                     + "ON CONFLICT(name) DO UPDATE SET status='complete', completed_at=excluded.completed_at, error=NULL")) {
            statement.setString(1, name);
            statement.setString(2, java.time.Instant.now().toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RepositoryException("Status migrasi basis data tidak dapat disimpan.", exception);
        }
    }

    private void initializeSchema() {
        String sql = """
                CREATE TABLE IF NOT EXISTS app_migrations (
                  name TEXT PRIMARY KEY,
                  status TEXT NOT NULL,
                  completed_at TEXT,
                  error TEXT
                );
                CREATE TABLE IF NOT EXISTS accounts (
                  id TEXT PRIMARY KEY,
                  name TEXT NOT NULL,
                  email TEXT NOT NULL UNIQUE,
                  password_hash TEXT NOT NULL,
                  role TEXT NOT NULL CHECK(role IN ('PENGAJAR','SISWA')),
                  status TEXT NOT NULL CHECK(status IN ('ACTIVE','DISABLED')),
                  education_level TEXT NOT NULL DEFAULT ''
                );
                CREATE TABLE IF NOT EXISTS profile (
                  id TEXT PRIMARY KEY,
                  name TEXT NOT NULL,
                  email TEXT NOT NULL DEFAULT '',
                  phone TEXT NOT NULL DEFAULT '',
                  birth_date TEXT,
                  education_level TEXT NOT NULL DEFAULT '',
                  learning_preferences TEXT NOT NULL DEFAULT ''
                );
                CREATE TABLE IF NOT EXISTS students (
                  id TEXT PRIMARY KEY,
                  name TEXT NOT NULL,
                  email TEXT NOT NULL DEFAULT '',
                  phone TEXT NOT NULL DEFAULT '',
                  birth_date TEXT,
                  education_level TEXT NOT NULL DEFAULT '',
                  learning_preferences TEXT NOT NULL DEFAULT ''
                );
                CREATE TABLE IF NOT EXISTS teachers (
                  id TEXT PRIMARY KEY,
                  name TEXT NOT NULL,
                  email TEXT NOT NULL DEFAULT '',
                  phone TEXT NOT NULL DEFAULT '',
                  expertise TEXT NOT NULL,
                  qualifications TEXT NOT NULL DEFAULT ''
                );
                CREATE TABLE IF NOT EXISTS subjects (
                  id TEXT PRIMARY KEY,
                  name TEXT NOT NULL,
                  description TEXT NOT NULL DEFAULT ''
                );
                CREATE TABLE IF NOT EXISTS materials (
                  id TEXT PRIMARY KEY,
                  subject_id TEXT NOT NULL,
                  title TEXT NOT NULL,
                  type TEXT NOT NULL,
                  location TEXT NOT NULL,
                  notes TEXT NOT NULL DEFAULT '',
                  objectives TEXT NOT NULL DEFAULT '',
                  content TEXT NOT NULL DEFAULT ''
                );
                CREATE TABLE IF NOT EXISTS study_sessions (
                  id TEXT PRIMARY KEY,
                  student_id TEXT NOT NULL,
                  teacher_id TEXT NOT NULL,
                  subject_id TEXT NOT NULL,
                  date TEXT NOT NULL,
                  start_time TEXT NOT NULL,
                  duration_minutes INTEGER NOT NULL,
                  notes TEXT NOT NULL DEFAULT ''
                );
                CREATE TABLE IF NOT EXISTS tasks (
                  id TEXT PRIMARY KEY,
                  subject_id TEXT NOT NULL,
                  title TEXT NOT NULL,
                  due_date TEXT NOT NULL,
                  completed INTEGER NOT NULL DEFAULT 0,
                  instructions TEXT NOT NULL DEFAULT ''
                );
                CREATE TABLE IF NOT EXISTS goals (
                  id TEXT PRIMARY KEY,
                  title TEXT NOT NULL,
                  target_date TEXT NOT NULL,
                  achieved INTEGER NOT NULL DEFAULT 0
                );
                CREATE TABLE IF NOT EXISTS evaluations (
                  id TEXT PRIMARY KEY,
                  subject_id TEXT NOT NULL,
                  date TEXT NOT NULL,
                  score REAL NOT NULL,
                  maximum_score REAL NOT NULL,
                  notes TEXT NOT NULL DEFAULT ''
                );
                CREATE TABLE IF NOT EXISTS teacher_invitations (
                  id TEXT PRIMARY KEY,
                  teacher_account_id TEXT NOT NULL,
                  code_hash TEXT NOT NULL UNIQUE,
                  created_at TEXT NOT NULL,
                  expires_at TEXT NOT NULL,
                  used_at TEXT,
                  status TEXT NOT NULL CHECK(status IN ('ACTIVE','USED','REVOKED','EXPIRED')),
                  FOREIGN KEY(teacher_account_id) REFERENCES accounts(id) ON DELETE RESTRICT
                );
                CREATE TABLE IF NOT EXISTS teacher_connection_requests (
                  id TEXT PRIMARY KEY,
                  student_account_id TEXT NOT NULL,
                  teacher_account_id TEXT NOT NULL,
                  invitation_id TEXT NOT NULL,
                  status TEXT NOT NULL CHECK(status IN ('PENDING','APPROVED','REJECTED')),
                  created_at TEXT NOT NULL,
                  updated_at TEXT NOT NULL,
                  FOREIGN KEY(student_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
                  FOREIGN KEY(teacher_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
                  FOREIGN KEY(invitation_id) REFERENCES teacher_invitations(id) ON DELETE RESTRICT
                );
                CREATE TABLE IF NOT EXISTS teacher_student_connections (
                  id TEXT PRIMARY KEY,
                  student_account_id TEXT NOT NULL,
                  teacher_account_id TEXT NOT NULL,
                  status TEXT NOT NULL CHECK(status IN ('ACTIVE','REVOKED','ENDED')),
                  created_at TEXT NOT NULL,
                  updated_at TEXT,
                  FOREIGN KEY(student_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
                  FOREIGN KEY(teacher_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
                  UNIQUE(student_account_id,teacher_account_id)
                );
                CREATE TABLE IF NOT EXISTS learning_access_grants (
                  id TEXT PRIMARY KEY,
                  resource_type TEXT NOT NULL,
                  resource_id TEXT NOT NULL,
                  teacher_account_id TEXT NOT NULL,
                  student_account_id TEXT NOT NULL,
                  FOREIGN KEY(teacher_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
                  FOREIGN KEY(student_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
                  UNIQUE(resource_type,resource_id)
                );
                CREATE TABLE IF NOT EXISTS demo_accounts (
                  account_id TEXT PRIMARY KEY,
                  FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE
                );
                CREATE TABLE IF NOT EXISTS demo_resources (
                  resource_type TEXT NOT NULL,
                  resource_id TEXT NOT NULL,
                  PRIMARY KEY(resource_type,resource_id)
                );
                CREATE TABLE IF NOT EXISTS seeded_students (
                  account_id TEXT PRIMARY KEY,
                  FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE
                );
                CREATE TABLE IF NOT EXISTS seeded_teachers (
                  account_id TEXT PRIMARY KEY,
                  FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE
                );
                CREATE INDEX IF NOT EXISTS idx_materials_subject ON materials(subject_id);
                CREATE INDEX IF NOT EXISTS idx_sessions_student ON study_sessions(student_id);
                CREATE INDEX IF NOT EXISTS idx_sessions_subject ON study_sessions(subject_id);
                CREATE INDEX IF NOT EXISTS idx_tasks_subject ON tasks(subject_id);
                CREATE INDEX IF NOT EXISTS idx_evaluations_subject ON evaluations(subject_id);
                CREATE INDEX IF NOT EXISTS idx_requests_teacher_status ON teacher_connection_requests(teacher_account_id,status);
                CREATE INDEX IF NOT EXISTS idx_requests_student ON teacher_connection_requests(student_account_id);
                CREATE INDEX IF NOT EXISTS idx_connections_student_status ON teacher_student_connections(student_account_id,status);
                CREATE INDEX IF NOT EXISTS idx_connections_teacher_status ON teacher_student_connections(teacher_account_id,status);
                CREATE INDEX IF NOT EXISTS idx_grants_resource ON learning_access_grants(resource_type,resource_id);
                CREATE INDEX IF NOT EXISTS idx_grants_student ON learning_access_grants(student_account_id);
                """;
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            for (String ddl : sql.split(";")) {
                if (!ddl.strip().isEmpty()) statement.execute(ddl);
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Skema SQLite tidak dapat diinisialisasi.", exception);
        }
    }

    @Override
    public void close() {
        // Connections are short-lived and closed by their callers.
    }
}
