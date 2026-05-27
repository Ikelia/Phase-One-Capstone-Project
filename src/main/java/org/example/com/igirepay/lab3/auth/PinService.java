package org.example.com.igirepay.lab3.auth;

import org.example.com.igirepay.lab2.dao.CustomerDAO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PinService {

    private static final int MAX_ATTEMPTS = 3;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final Map<String, Integer> failedAttempts = new HashMap<>();
    private final Map<String, Boolean> lockedAccounts = new HashMap<>();

    public void createPin(String customerId, String pin) throws SQLException {
        validatePinFormat(pin);
        String hash = hash(pin);
        customerDAO.updatePin(customerId, hash);
        System.out.println("[PinService] PIN set for customer: " + customerId);
    }

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
            failedAttempts.remove(customerId);
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

    public void changePin(String customerId, String oldPin, String newPin) throws SQLException {
        if (!validatePin(customerId, oldPin)) {
            throw new SecurityException("Old PIN is incorrect. PIN change denied.");
        }
        validatePinFormat(newPin);
        createPin(customerId, newPin);
        System.out.println("[PinService] PIN changed successfully for: " + customerId);
    }

    public void unlockAccount(String customerId) {
        lockedAccounts.remove(customerId);
        failedAttempts.remove(customerId);
        System.out.println("[PinService] Account unlocked: " + customerId);
    }

    public void lockAccount(String customerId) {
        lockedAccounts.put(customerId, true);
        failedAttempts.put(customerId, MAX_ATTEMPTS);
        System.out.println("[PinService] Account LOCKED by admin: " + customerId);
    }

    public boolean isLocked(String customerId) {
        return Boolean.TRUE.equals(lockedAccounts.get(customerId));
    }

    private void validatePinFormat(String pin) {
        if (pin == null || !pin.matches("\\d{4,6}")) {
            throw new IllegalArgumentException("PIN must be 4-6 digits.");
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
