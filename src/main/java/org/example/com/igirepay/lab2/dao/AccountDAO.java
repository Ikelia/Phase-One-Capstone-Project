package org.example.com.igirepay.lab2.dao;

import org.example.com.igirepay.lab1.model.Account;
import org.example.com.igirepay.lab1.model.SavingsAccount;
import org.example.com.igirepay.lab1.model.WalletAccount;
import org.example.com.igirepay.lab2.db.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDAO {

    public void create(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (id, customer_id, account_type, balance) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, account.getAccountId());
            ps.setString(2, account.getCustomerId());
            ps.setString(3, account.getAccountType());
            ps.setBigDecimal(4, account.getBalance());
            ps.executeUpdate();
            System.out.println("[AccountDAO] Created account: " + account.getAccountId());
        }
    }

    public Optional<Account> findById(String id) throws SQLException {
        String sql = "SELECT id, customer_id, account_type, balance FROM accounts WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public List<Account> findByCustomerId(String customerId) throws SQLException {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT id, customer_id, account_type, balance FROM accounts WHERE customer_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Account> findAll() throws SQLException {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT id, customer_id, account_type, balance FROM accounts";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM accounts";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public void updateBalance(String accountId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setString(2, accountId);
            ps.executeUpdate();
        }
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
            System.out.println("[AccountDAO] Deleted account: " + id);
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        String type    = rs.getString("account_type");
        String id      = rs.getString("id");
        String custId  = rs.getString("customer_id");
        BigDecimal bal = rs.getBigDecimal("balance");

        if ("SAVINGS".equalsIgnoreCase(type)) {
            return new SavingsAccount(id, custId, bal,
                    new BigDecimal("0.02"), new BigDecimal("500.00"));
        } else {
            return new WalletAccount(id, custId, bal, new BigDecimal("5000000.00"));
        }
    }
}
