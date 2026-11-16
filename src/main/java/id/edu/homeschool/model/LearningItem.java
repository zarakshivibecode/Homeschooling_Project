package id.edu.homeschool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;

/** Common identity and title for dated tasks and learning goals. */
public abstract class LearningItem {
    private String id;
    private String title;

    protected LearningItem() {
        this.id = UUID.randomUUID().toString();
    }

    protected LearningItem(String title) {
        this();
        setTitle(title);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = requireText(id, "ID"); }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = requireText(title, "Judul"); }

    /** Concrete learning items provide their own user-facing status. */
    @JsonIgnore
    public abstract String getStatusLabel();

    /** Validates that a value is present. A missing value is a user input error, not a null-pointer bug. */
    protected static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(label + " wajib diisi.");
        return value.trim();
    }
}
