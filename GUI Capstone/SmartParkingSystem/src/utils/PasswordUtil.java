package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Very small helper for hashing passwords with SHA-256 so that plain
 * text passwords are never stored in, or compared against, the
 * database. This keeps the project dependency-free (no external
 * hashing library needed) while still avoiding the #1 basic
 * vulnerability of storing raw passwords.
 *
 * For a production system you would prefer a salted, slow hash such
 * as BCrypt or Argon2, but SHA-256 is a reasonable, easy-to-understand
 * baseline for a learning project like this one.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainText.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to hash password", e);
        }
    }
}
