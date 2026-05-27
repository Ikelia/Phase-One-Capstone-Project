package org.example.com.igirepay.lab2.dao;

import org.example.com.igirepay.lab2.db.DatabaseConnection;

import java.sql.*;

/**
 * Data Access Object for the {@code processed_requests} table.
 *
 * This table is the database-level idempotency guard (Exercise 2.5).
 * Before processing any transaction, call {@link #exists(String)}.
 * After a successful transaction, call {@link #markProcessed(String)}.
 */
public class ProcessedRequestDAO {

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Records a reference ID as processed.
     * Silently ignores duplicate inserts (ON CONFLICT DO NOTHING).
     */
    public void markProcessed(String referenceId) throws SQLException {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?) ON CONFLICT (reference_id) DO NOTHING";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, referenceId);
            ps.executeUpdate();
        }
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /**
     * Returns true if the reference ID has already been processed.
     */
    public boolean exists(String referenceId) throws SQLException {
        String sql = "SELECT 1 FROM processed_requests WHERE reference_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, referenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /** Remove a processed request record (admin / testing use only). */
    public void delete(String referenceId) throws SQLException {
        String sql = "DELETE FROM processed_requests WHERE reference_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, referenceId);
            ps.executeUpdate();
        }
    }
}
