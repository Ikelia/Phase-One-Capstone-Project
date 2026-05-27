package org.example.com.igirepay.lab2.dao;

import org.example.com.igirepay.lab2.db.DatabaseConnection;

import java.sql.*;

public class ProcessedRequestDAO {

    public void markProcessed(String referenceId) throws SQLException {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?) ON CONFLICT (reference_id) DO NOTHING";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, referenceId);
            ps.executeUpdate();
        }
    }

    public boolean exists(String referenceId) throws SQLException {
        String sql = "SELECT 1 FROM processed_requests WHERE reference_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, referenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void delete(String referenceId) throws SQLException {
        String sql = "DELETE FROM processed_requests WHERE reference_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, referenceId);
            ps.executeUpdate();
        }
    }
}
