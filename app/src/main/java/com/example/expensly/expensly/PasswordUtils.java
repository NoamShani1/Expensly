package com.example.expensly.expensly;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Password hashing so plain-text passwords are never stored in the database.
 */
public final class PasswordUtils {

    private PasswordUtils() {
    }

    /** Returns the SHA-256 hash of the input as a lowercase hex string. */
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed on Android
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
