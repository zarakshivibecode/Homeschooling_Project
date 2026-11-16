package id.edu.homeschool.model;

import java.util.UUID;

/** Account ownership for newly-created learning records; legacy records without a grant remain private. */
public final class LearningAccessGrant {
    private String id = UUID.randomUUID().toString();
    private String resourceType;
    private String resourceId;
    private String teacherAccountId;
    private String studentAccountId;

    public LearningAccessGrant() { }
    public LearningAccessGrant(String type, String resourceId, String teacherId, String studentId) {
        setResourceType(type); setResourceId(resourceId); setTeacherAccountId(teacherId); setStudentAccountId(studentId);
    }
    public String getId() { return id; }
    public void setId(String value) { id = required(value); }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String value) { resourceType = required(value); }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String value) { resourceId = required(value); }
    public String getTeacherAccountId() { return teacherAccountId; }
    public void setTeacherAccountId(String value) { teacherAccountId = required(value); }
    public String getStudentAccountId() { return studentAccountId; }
    public void setStudentAccountId(String value) { studentAccountId = required(value); }
    private static String required(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Relasi kepemilikan tidak lengkap.");
        return value.trim();
    }
}
