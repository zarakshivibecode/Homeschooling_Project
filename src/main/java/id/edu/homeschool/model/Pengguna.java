package id.edu.homeschool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;

/** Shared identity and contact data for the people using the learning system. */
public abstract class Pengguna {
    private String id = UUID.randomUUID().toString();
    private String name;
    private String email = "";
    private String phone = "";

    protected Pengguna() { }
    protected Pengguna(String name, String email, String phone) {
        setName(name); setEmail(email); setPhone(phone);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = required(id, "ID"); }
    public String getName() { return name; }
    public void setName(String name) { this.name = required(name, "Nama"); }
    public String getEmail() { return email; }
    public void setEmail(String email) {
        String value = email == null ? "" : email.trim();
        if (!value.isEmpty() && !value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("Format email tidak valid.");
        this.email = value;
    }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone == null ? "" : phone.trim(); }
    @JsonIgnore
    public abstract String getRoleLabel();

    protected static String required(String value, String label) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(label + " wajib diisi.");
        return value.trim();
    }
    @Override public String toString() { return name == null ? "Pengguna" : name; }
}
