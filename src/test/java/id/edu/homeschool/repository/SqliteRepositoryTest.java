package id.edu.homeschool.repository;

import id.edu.homeschool.model.Subject;
import id.edu.homeschool.model.UserAccount;
import id.edu.homeschool.model.AccountRole;
import id.edu.homeschool.model.AccountStatus;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class SqliteRepositoryTest {
    @TempDir Path temp;

    @Test void databasePersistsAndForeignKeysWorkAfterReopen() {
        ApplicationRepositories first = new ApplicationRepositories(temp);
        Subject subject = first.subjects().save(new Subject("Matematika", "Aljabar"));
        UserAccount account = first.accounts().save(new UserAccount(
                "Siswa", "siswa@sqlite.test", "pbkdf2-sha256$210000$"
                + "123456789012345678901234".substring(0,22) + "$"
                + "1234567890123456789012345678901234567890123",
                AccountRole.SISWA, "SMA"));

        ApplicationRepositories reopened = new ApplicationRepositories(temp);
        assertEquals(subject.getName(), reopened.subjects().findById(subject.getId()).orElseThrow().getName());
        assertEquals(account.getEmail(), reopened.accounts().findById(account.getId()).orElseThrow().getEmail());
        assertTrue(reopened.database().getDatabaseFile().toFile().isFile());
    }

    @Test void migrationMarkerIsIdempotentAndDatabaseCreatesSchema() {
        ApplicationRepositories repositories = new ApplicationRepositories(temp);
        JsonToSqliteMigrator migrator = new JsonToSqliteMigrator(repositories.database(), temp);
        migrator.migrate(repositories);
        migrator.migrate(repositories);
        assertTrue(repositories.database().migrationComplete(JsonToSqliteMigrator.MIGRATION_NAME));
    }

    @Test void malformedJsonMigrationDoesNotDeleteSourceFile() throws Exception {
        java.nio.file.Files.writeString(temp.resolve("accounts.json"), "{ broken");
        ApplicationRepositories repositories;
        try {
            repositories = new ApplicationRepositories(temp);
            fail("Migrasi rusak seharusnya dilaporkan");
        } catch (RepositoryException exception) {
            assertTrue(exception.getMessage().contains("tetap utuh"));
        }
        assertTrue(java.nio.file.Files.exists(temp.resolve("accounts.json")));
    }

    @Test void existingJsonAccountKeepsIdAndPasswordHash() throws Exception {
        String id = "preserved-account-id";
        String hash = "pbkdf2-sha256$210000$"
                + "1234567890123456789012$"
                + "1234567890123456789012345678901234567890123";
        String json = "[{\"id\":\"" + id + "\",\"name\":\"Preserved\",\"email\":\"preserved@sqlite.test\","
                + "\"passwordHash\":\"" + hash + "\",\"role\":\"SISWA\",\"status\":\"ACTIVE\","
                + "\"educationLevel\":\"SMA\"}]";
        java.nio.file.Files.writeString(temp.resolve("accounts.json"), json);

        ApplicationRepositories repositories = new ApplicationRepositories(temp);
        assertTrue(repositories.accounts().findById(id).isPresent());
        assertEquals(hash, repositories.accounts().findById(id).orElseThrow().getPasswordHash());
        assertTrue(java.nio.file.Files.exists(temp.resolve("accounts.json")));
        try (var paths = java.nio.file.Files.list(temp)) {
            assertTrue(paths.anyMatch(path -> path.getFileName().toString().startsWith("json-backup-")));
        }
    }
}
