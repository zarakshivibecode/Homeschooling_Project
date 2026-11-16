package id.edu.homeschool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Locale;
import java.util.UUID;

/** Authentication identity. It deliberately has no plaintext-password field. */
public final class UserAccount {
    private String id = UUID.randomUUID().toString();
    private String name;
    private String email;
    private String passwordHash;
    private AccountRole role;
    private AccountStatus status = AccountStatus.ACTIVE;
    private String educationLevel = "";

    public UserAccount() { }

    public UserAccount(String name, String email, String passwordHash, AccountRole role, String educationLevel) {
        setName(name);
        setEmail(email);
        setPasswordHash(passwordHash);
        setRole(role);
        setStatus(AccountStatus.ACTIVE);
        setEducationLevel(educationLevel);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = required(id, "ID"); }
    public String getName() { return name; }
    public void setName(String name) { this.name = required(name, "Nama"); }
    public String getEmail() { return email; }
    public void setEmail(String email) {
        String normalized = required(email, "Email").toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Format email tidak valid.");
        this.email = normalized;
    }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) {
        String value = required(passwordHash, "Hash kata sandi");
        if (!value.matches("pbkdf2-sha256\\$[0-9]{6}\\$[A-Za-z0-9_-]{22}\\$[A-Za-z0-9_-]{43}"))
            throw new IllegalArgumentException("Format hash kata sandi tidak valid.");
        this.passwordHash = value;
    }
    public AccountRole getRole() { return role; }
    public void setRole(AccountRole role) {
        if (role == null) throw new IllegalArgumentException("Peran akun wajib dipilih.");
        this.role = role;
    }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) {
        if (status == null) throw new IllegalArgumentException("Status akun wajib diisi.");
        this.status = status;
    }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel == null ? "" : educationLevel.trim(); }

    @JsonIgnore
    public boolean isActive() { return status == AccountStatus.ACTIVE; }

    private static String required(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " wajib diisi.");
        return value.trim();
    }
}
