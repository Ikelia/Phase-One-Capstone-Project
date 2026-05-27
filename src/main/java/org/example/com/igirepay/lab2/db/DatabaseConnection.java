package org.example.com.igirepay.lab2.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton database connection manager.
 *
 * Configure the four constants below to match your PostgreSQL installation,
 * or set the environment variables:
 *   DB_URL, DB_USER, DB_PASSWORD
 */
public class DatabaseConnection {

    // ── Configuration – override via environment variables in production ───────
    private static final String DEFAULT_URL      = "jdbc:postgresql://localhost:5432/igirepay";
    private static final String DEFAULT_USER     = "postgres";
    private static final String DEFAULT_PASSWORD = "Admin123";

    private static Connection instance;

    private DatabaseConnection() { /* utility class */ }

    /**
     * Returns a shared Connection, creating it on first call.
     * Reconnects automatically if the connection has been closed.
     */
    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            String url      = System.getenv("DB_URL")      != null ? System.getenv("DB_URL")      : DEFAULT_URL;
            String user     = System.getenv("DB_USER")     != null ? System.getenv("DB_USER")     : DEFAULT_USER;
            String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : DEFAULT_PASSWORD;

            instance = DriverManager.getConnection(url, user, password);
            System.out.println("[DB] Connected to PostgreSQL.");
        }
        return instance;
    }

    /** Closes the shared connection (call on application shutdown). */
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
