package org.example.com.igirepay.lab2.dao;

import org.example.com.igirepay.lab1.model.Customer;
import org.example.com.igirepay.lab2.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Exercise 2.4: DAO for the customers table — all queries use PreparedStatements (Exercise 2.3)
public class CustomerDAO {

    // CREATE — adds a new customer row to the database
    public void create(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (id, full_name, email, phone_number, pin_hash) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, customer.getCustomerId());
            ps.setString(2, customer.getFullName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhoneNumber());
            ps.setString(5, customer.getPinHash()); // stores hashed PIN, not plain text
            ps.executeUpdate();
            System.out.println("[CustomerDAO] Created customer: " + customer.getFullName());
        }
    }

    // READ — find a single customer by primary key
    public Optional<Customer> findById(String id) throws SQLException {
        String sql = "SELECT id, full_name, email, phone_number, pin_hash FROM customers WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty(); // Optional avoids null — caller checks with .isPresent()
    }

    // Custom query: find by email — used for duplicate email validation during registration
    public Optional<Customer> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, full_name, email, phone_number, pin_hash FROM customers WHERE email = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // Custom query: find by phone — used during login to locate the customer
    public Optional<Customer> findByPhone(String phone) throws SQLException {
        String sql = "SELECT id, full_name, email, phone_number, pin_hash FROM customers WHERE phone_number = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // READ ALL — returns all customers ordered alphabetically
    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT id, full_name, email, phone_number, pin_hash FROM customers ORDER BY full_name";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // Custom query: count rows — used to generate sequential CUS-001, CUS-002... IDs
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // UPDATE — changes name, email, phone of an existing customer
    public void update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET full_name = ?, email = ?, phone_number = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhoneNumber());
            ps.setString(4, customer.getCustomerId());
            ps.executeUpdate();
            System.out.println("[CustomerDAO] Updated customer: " + customer.getCustomerId());
        }
    }

    // Custom update: only updates the PIN hash — called by PinService after hashing
    public void updatePin(String customerId, String newPinHash) throws SQLException {
        String sql = "UPDATE customers SET pin_hash = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, newPinHash);
            ps.setString(2, customerId);
            ps.executeUpdate();
        }
    }

    // DELETE — removes a customer (cascade deletes their accounts too)
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
            System.out.println("[CustomerDAO] Deleted customer: " + id);
        }
    }

    // Converts a ResultSet row into a Customer Java object
    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer(
                rs.getString("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone_number")
        );
        c.setPinHash(rs.getString("pin_hash"));
        return c;
    }
}
