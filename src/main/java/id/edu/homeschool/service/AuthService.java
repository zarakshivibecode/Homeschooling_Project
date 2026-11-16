package id.edu.homeschool.service;

import id.edu.homeschool.model.AccountRole;
import id.edu.homeschool.model.AccountStatus;
import id.edu.homeschool.model.UserAccount;
import id.edu.homeschool.repository.Repository;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Local prototype authentication. Teacher self-registration is enabled by policy. */
public final class AuthService {
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private final Repository<UserAccount> accounts;
    private final SessionRegistry sessions;
    private final PasswordHasher passwordHasher = new PasswordHasher();

    public AuthService(Repository<UserAccount> accounts, SessionRegistry sessions) {
        this.accounts = Objects.requireNonNull(accounts);
        this.sessions = Objects.requireNonNull(sessions);
    }

    public AccountSummary registerStudent(String name, String email, char[] password, String educationLevel) {
        return register(name, email, password, AccountRole.SISWA, educationLevel);
    }

    /** Self-registration is permitted for this local prototype; no administrator approval exists yet. */
    public AccountSummary registerTeacher(String name, String email, char[] password) {
        return register(name, email, password, AccountRole.PENGAJAR, "");
    }

    private AccountSummary register(String name, String email, char[] password, AccountRole role, String level) {
        Objects.requireNonNull(password, "Kata sandi wajib diisi.");
        validatePassword(password);
        String normalizedEmail = normalizeEmail(email);
        if (findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException(
                    "Email sudah terdaftar sehingga data registrasi tidak dapat digunakan. Silakan masuk atau memakai email lain.");
        }
        UserAccount account = new UserAccount(required(name, "Nama lengkap"), normalizedEmail,
                passwordHasher.hash(password), role, role == AccountRole.SISWA ? required(level, "Jenjang pendidikan") : "");
        UserAccount saved = accounts.save(account);
        return summary(saved);
    }

    public UserSession login(String email, char[] password) {
        Objects.requireNonNull(password, "Email atau kata sandi tidak valid, atau akun tidak aktif.");
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        UserAccount account = normalizedEmail.isEmpty() ? null
                : accounts.findAll().stream().filter(value -> value.getEmail().equalsIgnoreCase(normalizedEmail)).findFirst().orElse(null);
        boolean passwordMatches = passwordHasher.matches(password,
                account == null ? passwordHasher.dummyHash() : account.getPasswordHash());
        if (account == null || !passwordMatches || account.getStatus() != AccountStatus.ACTIVE) {
            throw new SecurityException("Email atau kata sandi tidak valid, atau akun tidak aktif.");
        }
        return new UserSession(sessions.create(account), account.getId(), account.getName(), account.getEmail(),
                account.getRole(), account.getEducationLevel());
    }

    public AccountSummary currentAccount(String token) { return summary(sessions.requireAccount(token)); }
    public void logout(String token) { sessions.invalidate(token); }

    /** True when the e-mail already belongs to an account; used by the UI to show an early warning. */
    public boolean emailExists(String email) { return findByEmail(email).isPresent(); }

    public Optional<UserAccount> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return accounts.findAll().stream().filter(value -> value.getEmail().equalsIgnoreCase(normalized)).findFirst();
    }

    /** Updates the editable profile fields of the signed-in account. */
    public AccountSummary updateProfile(String token, String name, String educationLevel) {
        UserAccount account = sessions.requireAccount(token);
        account.setName(required(name, "Nama lengkap"));
        if (account.getRole() == AccountRole.SISWA) {
            account.setEducationLevel(required(educationLevel, "Jenjang pendidikan"));
        }
        accounts.save(account);
        return summary(account);
    }

    /** Replaces the password of the signed-in account after verifying the current one. */
    public void changePassword(String token, char[] currentPassword, char[] newPassword) {
        UserAccount account = sessions.requireAccount(token);
        Objects.requireNonNull(currentPassword, "Kata sandi saat ini wajib diisi.");
        Objects.requireNonNull(newPassword, "Kata sandi baru wajib diisi.");
        if (!passwordHasher.matches(currentPassword, account.getPasswordHash())) {
            throw new SecurityException("Kata sandi saat ini tidak sesuai.");
        }
        validatePassword(newPassword);
        account.setPasswordHash(passwordHasher.hash(newPassword));
        accounts.save(account);
    }

    private static AccountSummary summary(UserAccount account) {
        return new AccountSummary(account.getId(), account.getName(), account.getEmail(), account.getRole(), account.getEducationLevel());
    }

    private static void validatePassword(char[] password) {
        if (password.length < MIN_PASSWORD_LENGTH || password.length > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Kata sandi harus terdiri dari 12 sampai 128 karakter.");
        }
        boolean letter = false, digit = false;
        for (char character : password) {
            letter |= Character.isLetter(character);
            digit |= Character.isDigit(character);
        }
        if (!letter || !digit) throw new IllegalArgumentException("Kata sandi harus memuat huruf dan angka.");
    }

    private static String normalizeEmail(String email) {
        String value = required(email, "Email").toLowerCase(Locale.ROOT);
        if (!value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Format email tidak valid.");
        return value;
    }

    private static String required(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " wajib diisi.");
        return value.trim();
    }
}
