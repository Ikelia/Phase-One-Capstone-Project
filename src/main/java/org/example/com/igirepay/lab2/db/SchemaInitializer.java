package org.example.com.igirepay.lab2.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// Exercise 2.1: Creates all 4 required tables if they do not already exist
// Safe to run on every startup — CREATE TABLE IF NOT EXISTS prevents duplicates
public class SchemaInitializer {

    private SchemaInitializer() {}

    public static void initialize() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (Statement stmt = conn.createStatement()) {

            // Table 1: customers — stores customer profiles and PIN hash
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS customers (" +
                "  id           VARCHAR(36)  PRIMARY KEY," +
                "  full_name    VARCHAR(150) NOT NULL," +
                "  email        VARCHAR(150) UNIQUE NOT NULL," +  // UNIQUE prevents duplicate emails
                "  phone_number VARCHAR(20)  UNIQUE NOT NULL," +  // UNIQUE prevents duplicate phones
                "  pin_hash     VARCHAR(255)," +
                "  created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Table 2: accounts — linked to customers via foreign key
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS accounts (" +
                "  id           VARCHAR(36)    PRIMARY KEY," +
                "  customer_id  VARCHAR(36)    NOT NULL REFERENCES customers(id) ON DELETE CASCADE," +
                "  account_type VARCHAR(20)    NOT NULL CHECK (account_type IN ('WALLET','SAVINGS'))," +
                "  balance      NUMERIC(18,2)  NOT NULL DEFAULT 0.00," + // NUMERIC for precision
                "  created_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Table 3: transactions — records every deposit, withdrawal, and transfer
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "  id               VARCHAR(36)   PRIMARY KEY," +
                "  account_id       VARCHAR(36)   NOT NULL REFERENCES accounts(id)," +
                "  reference_id     VARCHAR(100)  NOT NULL," +
                "  transaction_type VARCHAR(20)   NOT NULL," +
                "  amount           NUMERIC(18,2) NOT NULL," +
                "  status           VARCHAR(20)   NOT NULL DEFAULT 'SUCCESS'," +
                "  created_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Table 4: processed_requests — idempotency guard (Exercise 2.5)
            // UNIQUE on reference_id ensures the database itself prevents duplicates
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS processed_requests (" +
                "  id           SERIAL       PRIMARY KEY," +
                "  reference_id VARCHAR(100) UNIQUE NOT NULL," +
                "  processed_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            System.out.println("[Schema] All tables verified / created.");
        }
    }
}
