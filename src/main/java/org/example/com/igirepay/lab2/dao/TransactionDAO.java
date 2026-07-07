package org.example.com.igirepay.lab2.dao;

import org.example.com.igirepay.lab1.model.Transaction;
import org.example.com.igirepay.lab2.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Exercise 2.4: DAO for the transactions table — records every financial movement
public class TransactionDAO {

    // CREATE — saves a new transaction record after deposit, withdrawal, or transfer
    public void create(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (id, account_id, reference_id, transaction_type, amount, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, transaction.getTransactionId());
            ps.setString(2, transaction.getAccountId());
            ps.setString(3, transaction.getReferenceId());
            ps.setString(4, transaction.getTransactionType());
            ps.setBigDecimal(5, transaction.getAmount()); // setBigDecimal preserves financial precision
            ps.setString(6, transaction.getStatus());
            ps.executeUpdate();
        }
    }

    // READ — find a single transaction by its primary key
    public Optional<Transaction> findById(String id) throws SQLException {
        String sql = "SELECT id, account_id, reference_id, transaction_type, amount, status, created_at " +
                     "FROM transactions WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // READ — returns all transactions for an account, newest first (ORDER BY created_at DESC)
    public List<Transaction> findByAccountId(String accountId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT id, account_id, reference_id, transaction_type, amount, status, created_at " +
                     "FROM transactions WHERE account_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // READ ALL — used by admin to see every transaction in the system
    public List<Transaction> findAll() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT id, account_id, reference_id, transaction_type, amount, status, created_at " +
                     "FROM transactions ORDER BY created_at DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // Custom query: filter by date — used by ReportService for daily summaries (Exercise 3.3)
    public List<Transaction> findByAccountAndDate(String accountId, String date) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT id, account_id, reference_id, transaction_type, amount, status, created_at " +
                     "FROM transactions WHERE account_id = ? AND DATE(created_at) = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setDate(2, Date.valueOf(date)); // converts String "2026-05-27" to SQL Date
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // UPDATE — changes the status of a transaction (e.g. PENDING to SUCCESS)
    public void updateStatus(String transactionId, String status) throws SQLException {
        String sql = "UPDATE transactions SET status = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, transactionId);
            ps.executeUpdate();
        }
    }

    // DELETE — removes a transaction record
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    // Custom query: count all rows — used by AccountService to generate TRN-001, TRN-002... IDs
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // Converts a ResultSet row back into a Transaction Java object
    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction(
                rs.getString("id"),
                rs.getString("reference_id"),
                rs.getString("account_id"),
                rs.getBigDecimal("amount"),
                rs.getString("transaction_type")
        );
        t.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) t.setTimestamp(ts.toLocalDateTime()); // convert SQL Timestamp to Java LocalDateTime
        return t;
    }
}
