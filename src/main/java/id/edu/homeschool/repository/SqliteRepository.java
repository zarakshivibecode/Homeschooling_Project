package id.edu.homeschool.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/** Small prepared-statement repository implementation backed by SQLite. */
public final class SqliteRepository<T> implements Repository<T> {
    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet result) throws SQLException;
    }
    @FunctionalInterface
    public interface EntityBinder<T> {
        void bind(PreparedStatement statement, T entity) throws SQLException;
    }

    private final DatabaseManager database;
    private final String table;
    private final RowMapper<T> mapper;
    private final EntityBinder<T> binder;
    private final Function<T, String> idReader;
    private final String insertColumns;
    private final String insertPlaceholders;
    private final String updateColumns;

    public SqliteRepository(DatabaseManager database, String table, RowMapper<T> mapper,
            EntityBinder<T> binder, Function<T, String> idReader,
            String insertColumns, String updateColumns) {
        this.database = Objects.requireNonNull(database);
        this.table = requireIdentifier(table);
        this.mapper = Objects.requireNonNull(mapper);
        this.binder = Objects.requireNonNull(binder);
        this.idReader = Objects.requireNonNull(idReader);
        this.insertColumns = insertColumns;
        this.insertPlaceholders = insertPlaceholders(insertColumns);
        this.updateColumns = updateColumns;
    }

    @Override
    public synchronized List<T> findAll() {
        String sql = "SELECT * FROM " + table;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<T> values = new ArrayList<>();
            while (result.next()) values.add(mapper.map(result));
            return values;
        } catch (SQLException | RuntimeException exception) {
            throw new RepositoryException("Data tidak dapat dibaca dari tabel SQLite " + table + ".", exception);
        }
    }

    @Override
    public synchronized Optional<T> findById(String id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM " + table + " WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapper.map(result)) : Optional.empty();
            }
        } catch (SQLException | RuntimeException exception) {
            throw new RepositoryException("Data tidak dapat dibaca dari tabel SQLite " + table + ".", exception);
        }
    }

    @Override
    public synchronized T save(T entity) {
        Objects.requireNonNull(entity, "Data tidak boleh kosong.");
        String id = Objects.requireNonNull(idReader.apply(entity), "ID data tidak boleh kosong.");
        String sql = "INSERT INTO " + table + " (" + insertColumns + ") VALUES (" + insertPlaceholders + ") "
                + "ON CONFLICT(id) DO UPDATE SET " + updateColumns;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement, entity);
            statement.executeUpdate();
            return entity;
        } catch (SQLException | RuntimeException exception) {
            throw new RepositoryException("Data tidak dapat disimpan ke tabel SQLite " + table + ".", exception);
        }
    }

    @Override
    public synchronized boolean deleteById(String id) {
        String sql = "DELETE FROM " + table + " WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new RepositoryException("Data tidak dapat dihapus dari tabel SQLite " + table + ".", exception);
        }
    }

    private static String insertPlaceholders(String columns) {
        int count = columns.split(",").length;
        return java.util.Collections.nCopies(count, "?").stream().reduce((a, b) -> a + "," + b).orElse("");
    }

    private static String requireIdentifier(String value) {
        if (!value.matches("[a-zA-Z_][a-zA-Z0-9_]*")) throw new IllegalArgumentException("Nama tabel tidak valid.");
        return value;
    }
}
