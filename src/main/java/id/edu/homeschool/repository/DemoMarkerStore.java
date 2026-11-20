package id.edu.homeschool.repository;

import id.edu.homeschool.model.DemoMarkers;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Stores demo marker metadata in SQLite instead of a separate JSON marker file. */
public final class DemoMarkerStore {
    private final DatabaseManager database;

    public DemoMarkerStore(DatabaseManager database) {
        this.database = database;
    }

    public synchronized DemoMarkers load() {
        DemoMarkers markers = new DemoMarkers();
        try (Connection connection = database.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT account_id FROM demo_accounts");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) markers.markAccount(result.getString(1));
            }
            try (PreparedStatement statement = connection.prepareStatement("SELECT resource_type,resource_id FROM demo_resources");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) markers.markResource(result.getString(1), result.getString(2));
            }
            try (PreparedStatement statement = connection.prepareStatement("SELECT account_id FROM seeded_students");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) markers.markSeededStudent(result.getString(1));
            }
            try (PreparedStatement statement = connection.prepareStatement("SELECT account_id FROM seeded_teachers");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) markers.markSeededTeacher(result.getString(1));
            }
            return markers;
        } catch (SQLException exception) {
            throw new RepositoryException("Penanda data demo tidak dapat dibaca dari SQLite.", exception);
        }
    }

    public synchronized void save(DemoMarkers markers) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement clearAccounts = connection.prepareStatement("DELETE FROM demo_accounts");
                 PreparedStatement clearResources = connection.prepareStatement("DELETE FROM demo_resources");
                 PreparedStatement clearStudents = connection.prepareStatement("DELETE FROM seeded_students");
                 PreparedStatement clearTeachers = connection.prepareStatement("DELETE FROM seeded_teachers")) {
                clearAccounts.executeUpdate();
                clearResources.executeUpdate();
                clearStudents.executeUpdate();
                clearTeachers.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT OR IGNORE INTO demo_accounts(account_id) VALUES(?)")) {
                for (String id : markers.getAccounts()) {
                    statement.setString(1, id);
                    statement.executeUpdate();
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT OR IGNORE INTO demo_resources(resource_type,resource_id) VALUES(?,?)")) {
                for (var entry : markers.getResources().entrySet()) {
                    for (String id : entry.getValue()) {
                        statement.setString(1, entry.getKey());
                        statement.setString(2, id);
                        statement.executeUpdate();
                    }
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT OR IGNORE INTO seeded_students(account_id) VALUES(?)")) {
                for (String id : markers.getSeededStudents()) {
                    statement.setString(1, id);
                    statement.executeUpdate();
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT OR IGNORE INTO seeded_teachers(account_id) VALUES(?)")) {
                for (String id : markers.getSeededTeachers()) {
                    statement.setString(1, id);
                    statement.executeUpdate();
                }
            }
            connection.commit();
        } catch (SQLException exception) {
            throw new RepositoryException("Penanda data demo tidak dapat disimpan ke SQLite.", exception);
        }
    }
}
