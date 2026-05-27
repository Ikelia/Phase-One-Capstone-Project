package org.example.com.igirepay.lab3.auth;

import org.example.com.igirepay.lab2.dao.CustomerDAO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles PIN creation, validation, and change.
 *
 * PINs are stored as SHA-256 hashes – never in plain text.
 *
 * Bonus: locks an account after {@value #MAX_ATTEMPTS} consecutive failures.
 */
public class PinService {

    private static final int MAX_ATTEMPTS = 3;

    private final CustomerDAO customerDAO = new CustomerDAO();

    /**
     * Tracks consecutive failed attempts per customerId.
     */
    private final Map<String, Integer> failedAttempts = new HashMap<>();

    /**
     * Locked customer IDs.
     */
    private final Map<String, Boolean> lockedAccounts = new HashMap<>();

    // ── PIN creation ──────────────────────────────────────────────────────────

    /**
     * Creates (or resets) a PIN for the given customer.
     * The PIN is hashed before storage.
     */
    public void createPin(String customerId, String pin) throws SQLException {
        validatePinFormat(pin);
        String hash = hash(pin);
        customerDAO.updatePin(customerId, hash);
        System.out.println("[PinService] PIN set for customer: " + customerId);
    }

    // ── PIN validation ────────────────────────────────────────────────────────

    /**
     * Validates a PIN attempt.
     *
     * @return true if the PIN matches and the account is not locked
     * @throws SecurityException if the account is locked
     */
    public boolean validatePin(String customerId, String pin) throws SQLException {
        if (Boolean.TRUE.equals(lockedAccounts.get(customerId))) {
            throw new SecurityException("Account " + customerId + " is locked due to too many failed PIN attempts.");
        }

        String storedHash = customerDAO.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId))
                .getPinHash();

        if (storedHash == null) {
            throw new IllegalStateException("No PIN set for customer: " + customerId);
        }

        boolean match = storedHash.equals(hash(pin));
        if (match) {
            failedAttempts.remove(customerId); // reset on success
        } else {
            int attempts = failedAttempts.getOrDefault(customerId, 0) + 1;
            failedAttempts.put(customerId, attempts);
            if (attempts >= MAX_ATTEMPTS) {
                lockedAccounts.put(customerId, true);
                System.out.println("[PinService] Account LOCKED: " + customerId);
            }
            System.out.println("[PinService] Invalid PIN. Attempt " + attempts + "/" + MAX_ATTEMPTS);
        }
        return match;
    }

    // ── PIN change ────────────────────────────────────────────────────────────

    /**
     * Changes a PIN after verifying the old one.
     */
    public void changePin(String customerId, String oldPin, String newPin) throws SQLException {
        if (!validatePin(customerId, oldPin)) {
            throw new SecurityException("Old PIN is incorrect. PIN change denied.");
        }
        validatePinFormat(newPin);
        createPin(customerId, newPin);
        System.out.println("[PinService] PIN changed successfully for: " + customerId);
    }

    // ── Account unlock (admin) ────────────────────────────────────────────────

    public void unlockAccount(String customerId) {
        lockedAccounts.remove(customerId);
        failedAttempts.remove(customerId);
        System.out.println("[PinService] Account unlocked: " + customerId);
    }

    public boolean isLocked(String customerId) {
        return Boolean.TRUE.equals(lockedAccounts.get(customerId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validatePinFormat(String pin) {
        if (pin == null || !pin.matches("\\d{4,6}")) {
            throw new IllegalArgumentException("PIN must be 4–6 digits.");
        }
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
