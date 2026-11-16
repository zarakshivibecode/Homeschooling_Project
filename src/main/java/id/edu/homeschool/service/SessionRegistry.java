package id.edu.homeschool.service;

import id.edu.homeschool.model.AccountStatus;
import id.edu.homeschool.model.UserAccount;
import id.edu.homeschool.repository.Repository;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Process-local session registry; session tokens are not persisted. */
public final class SessionRegistry {
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private final Repository<UserAccount> accounts;
    private final SecureRandom random = new SecureRandom();

    public SessionRegistry(Repository<UserAccount> accounts) { this.accounts = accounts; }

    String create(UserAccount account) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, account.getId());
        return token;
    }

    public UserAccount requireAccount(String token) {
        String accountId = token == null ? null : sessions.get(token);
        if (accountId == null) throw new SecurityException("Sesi tidak valid atau sudah berakhir. Silakan masuk kembali.");
        UserAccount account = accounts.findById(accountId).orElse(null);
        if (account == null) {
            // The account cannot be read right now, so the session is kept for the next attempt.
            throw new SecurityException("Akun tidak dapat dibaca dari penyimpanan. Silakan coba lagi.");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            sessions.remove(token);
            throw new SecurityException("Akun tidak aktif. Silakan hubungi pengelola aplikasi.");
        }
        return account;
    }

    public void invalidate(String token) { if (token != null) sessions.remove(token); }
}
