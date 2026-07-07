package org.example.com.igirepay.lab2.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Singleton pattern: one shared database connection for the entire application
public class DatabaseConnection {

    // Connection details — configure these to match your PostgreSQL installation
    private static final String DEFAULT_URL      = "jdbc:postgresql://localhost:5432/igirepay";
    private static final String DEFAULT_USER     = "postgres";
    private static final String DEFAULT_PASSWORD = "Admi123";

    // Static instance — only one connection exists at a time
    private static Connection instance;

    private DatabaseConnection() {}

    // Returns the shared connection, creates it if it does not exist or was closed
    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            // Environment variables take priority over hardcoded defaults (production safety)
            String url      = System.getenv("DB_URL")      != null ? System.getenv("DB_URL")      : DEFAULT_URL;
            String user     = System.getenv("DB_USER")     != null ? System.getenv("DB_USER")     : DEFAULT_USER;
            String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : DEFAULT_PASSWORD;

            instance = DriverManager.getConnection(url, user, password);
            System.out.println("[DB] Connected to PostgreSQL.");
        }
        return instance;
    }

    // Call on application shutdown to release the database connection
    public static void close() {
        if (instance != null) {
            try {
                instance.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }
}
