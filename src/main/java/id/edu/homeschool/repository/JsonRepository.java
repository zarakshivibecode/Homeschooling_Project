package id.edu.homeschool.repository;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/** Atomic JSON list repository. A malformed existing file is preserved and reported. */
public final class JsonRepository<T> implements Repository<T> {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Path file;
    private final JavaType listType;
    private final Function<T, String> idReader;

    public JsonRepository(Path file, Class<T> entityType, Function<T, String> idReader) {
        this.file = Objects.requireNonNull(file).toAbsolutePath().normalize();
        this.listType = MAPPER.getTypeFactory().constructCollectionType(List.class, entityType);
        this.idReader = Objects.requireNonNull(idReader);
    }

    public Path getFile() { return file; }

    @Override public synchronized List<T> findAll() {
        if (Files.notExists(file)) return new ArrayList<>();
        try {
            List<T> data = MAPPER.readValue(file.toFile(), listType);
            return data == null ? new ArrayList<>() : new ArrayList<>(data);
        } catch (IOException | RuntimeException exception) {
            throw new RepositoryException("Data tidak dapat dibaca dari " + file + ". File tidak diubah.", exception);
        }
    }

    @Override public synchronized Optional<T> findById(String id) {
        if (id == null) return Optional.empty();
        return findAll().stream().filter(item -> id.equals(idReader.apply(item))).findFirst();
    }

    @Override public synchronized T save(T entity) {
        Objects.requireNonNull(entity, "Data tidak boleh kosong.");
        String id = Objects.requireNonNull(idReader.apply(entity), "ID data tidak boleh kosong.");
        List<T> data = findAll();
        boolean replaced = false;
        for (int i = 0; i < data.size(); i++) {
            if (id.equals(idReader.apply(data.get(i)))) { data.set(i, entity); replaced = true; break; }
        }
        if (!replaced) data.add(entity);
        write(data);
        return entity;
    }

    @Override public synchronized boolean deleteById(String id) {
        List<T> data = findAll();
        boolean removed = data.removeIf(item -> Objects.equals(id, idReader.apply(item)));
        if (removed) write(data);
        return removed;
    }

    private void write(List<T> data) {
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            MAPPER.writerFor(listType).writeValue(temporary.toFile(), data);
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
            throw new RepositoryException("Data tidak dapat disimpan ke " + file + ".", exception);
        }
    }
}
