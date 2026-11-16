package id.edu.homeschool.service;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** PBKDF2-HMAC-SHA256 password KDF using per-password random salts. */
final class PasswordHasher {
    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final String dummyHash;

    PasswordHasher() {
        byte[] randomPassword = new byte[32];
        RANDOM.nextBytes(randomPassword);
        char[] temporary = Base64.getUrlEncoder().withoutPadding().encodeToString(randomPassword).toCharArray();
        dummyHash = hash(temporary);
        Arrays.fill(temporary, '\0');
        Arrays.fill(randomPassword, (byte) 0);
    }

    String hash(char[] password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        return "pbkdf2-sha256$" + ITERATIONS + "$" + encode(salt) + "$" + encode(derived);
    }

    boolean matches(char[] password, String stored) {
        if (stored == null) return false;
        try {
            String[] parts = stored.split("\\$", -1);
            if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) return false;
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 100_000 || iterations > 2_000_000) return false;
            byte[] salt = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expected = Base64.getUrlDecoder().decode(parts[3]);
            if (salt.length != SALT_BYTES || expected.length != KEY_BITS / 8) return false;
            return MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    String dummyHash() { return dummyHash; }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Fitur hashing kata sandi tidak tersedia pada runtime ini.", exception);
        } finally {
            spec.clearPassword();
        }
    }

    private static String encode(byte[] value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value); }
}
