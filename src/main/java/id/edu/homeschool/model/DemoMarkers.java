package id.edu.homeschool.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Index of the records that were created by the built-in demo seeder. The file is stored next to
 * the other JSON data so demo records can be told apart from records the user created.
 */
public final class DemoMarkers {

    private List<String> accounts = new ArrayList<>();
    private Map<String, List<String>> resources = new LinkedHashMap<>();
    private List<String> seededStudents = new ArrayList<>();
    private List<String> seededTeachers = new ArrayList<>();

    public DemoMarkers() { }

    public List<String> getAccounts() { return accounts; }
    public void setAccounts(List<String> value) { accounts = value == null ? new ArrayList<>() : value; }
    public Map<String, List<String>> getResources() { return resources; }
    public void setResources(Map<String, List<String>> value) { resources = value == null ? new LinkedHashMap<>() : value; }
    public List<String> getSeededStudents() { return seededStudents; }
    public void setSeededStudents(List<String> value) { seededStudents = value == null ? new ArrayList<>() : value; }
    public List<String> getSeededTeachers() { return seededTeachers; }
    public void setSeededTeachers(List<String> value) { seededTeachers = value == null ? new ArrayList<>() : value; }

    public void markAccount(String accountId) {
        if (accountId != null && !accounts.contains(accountId)) accounts.add(accountId);
    }

    public void markResource(String type, String resourceId) {
        if (type == null || resourceId == null) return;
        resources.computeIfAbsent(type, key -> new ArrayList<>());
        if (!resources.get(type).contains(resourceId)) resources.get(type).add(resourceId);
    }

    public boolean isAccount(String accountId) { return accountId != null && accounts.contains(accountId); }

    public boolean isResource(String type, String resourceId) {
        if (type == null || resourceId == null) return false;
        List<String> ids = resources.get(type);
        return ids != null && ids.contains(resourceId);
    }

    public void markSeededStudent(String accountId) {
        if (accountId != null && !seededStudents.contains(accountId)) seededStudents.add(accountId);
    }

    public void markSeededTeacher(String accountId) {
        if (accountId != null && !seededTeachers.contains(accountId)) seededTeachers.add(accountId);
    }

    public boolean isSeededStudent(String accountId) { return accountId != null && seededStudents.contains(accountId); }
}
