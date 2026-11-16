package id.edu.homeschool.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import id.edu.homeschool.model.Subject;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonRepositoryTest {
    @TempDir Path directory;

    @Test void savesUpdatesDeletesAndReloadsJson() {
        Path file = directory.resolve("subjects.json");
        Repository<Subject> repository = new JsonRepository<>(file, Subject.class, Subject::getId);
        Subject first = new Subject("Matematika", "Aljabar");
        repository.save(first);
        assertEquals("Matematika", new JsonRepository<>(file, Subject.class, Subject::getId).findById(first.getId()).orElseThrow().getName());
        first.setName("Matematika Dasar"); repository.save(first);
        assertEquals(1, repository.findAll().size());
        assertEquals(true, repository.deleteById(first.getId()));
        assertEquals(0, repository.findAll().size());
    }

    @Test void preservesMalformedFileAndReportsReadFailure() throws Exception {
        Path file = directory.resolve("broken.json");
        Files.writeString(file, "{ not valid json }");
        Repository<Subject> repository = new JsonRepository<>(file, Subject.class, Subject::getId);
        assertThrows(RepositoryException.class, repository::findAll);
        assertEquals("{ not valid json }", Files.readString(file));
    }
}
