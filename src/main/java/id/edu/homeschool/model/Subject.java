package id.edu.homeschool.model;

import java.util.UUID;

public final class Subject {
    private String id = UUID.randomUUID().toString();
    private String name;
    private String description = "";

    public Subject() { }
    public Subject(String name, String description) { setName(name); setDescription(description); }
    public String getId() { return id; }
    public void setId(String id) { this.id = LearningItem.requireText(id, "ID mata pelajaran"); }
    public String getName() { return name; }
    public void setName(String name) { this.name = LearningItem.requireText(name, "Nama mata pelajaran"); }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description == null ? "" : description.trim(); }
    @Override public String toString() { return name == null ? "Pilih mata pelajaran" : name; }
}
