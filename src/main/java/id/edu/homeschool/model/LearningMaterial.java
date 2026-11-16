package id.edu.homeschool.model;

import java.util.UUID;

/**
 * A learning material. Besides the catalogue fields it carries the learning objectives and the
 * full teaching content so the material stays useful inside the application without an internet
 * connection.
 */
public final class LearningMaterial {
    private String id = UUID.randomUUID().toString();
    private String subjectId;
    private String title;
    private String type;
    private String location;
    private String notes = "";
    private String objectives = "";
    private String content = "";

    public LearningMaterial() { }

    public LearningMaterial(String subjectId, String title, String type, String location, String notes) {
        setSubjectId(subjectId); setTitle(title); setType(type); setLocation(location); setNotes(notes);
    }

    public LearningMaterial(String subjectId, String title, String type, String location, String notes,
            String objectives, String content) {
        this(subjectId, title, type, location, notes);
        setObjectives(objectives);
        setContent(content);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = LearningItem.requireText(id, "ID materi"); }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = LearningItem.requireText(subjectId, "Mata pelajaran"); }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = LearningItem.requireText(title, "Judul materi"); }
    public String getType() { return type; }
    public void setType(String type) { this.type = LearningItem.requireText(type, "Jenis materi"); }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = LearningItem.requireText(location, "Lokasi atau URL"); }
    public String getNotes() { return notes; }
    public void setNotes(String value) { this.notes = value == null ? "" : value.trim(); }

    /** Learning objectives of the material, usually written as a short list. */
    public String getObjectives() { return objectives; }
    public void setObjectives(String value) { this.objectives = value == null ? "" : value.trim(); }

    /** The full teaching content: explanation, examples and exercises. */
    public String getContent() { return content; }
    public void setContent(String value) { this.content = value == null ? "" : value.trim(); }
}
