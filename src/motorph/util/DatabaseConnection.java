package motorph.util;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton utility class for managing the JDBC connection to the MySQL database.
 * Also provides password hashing utility for authentication.
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/motorph_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // <-- SET YOUR MYSQL PASSWORD HERE

    private static Connection connection;

    // Private constructor — Singleton pattern
    private DatabaseConnection() {}

    /**
     * Returns a shared database connection. Creates one if it doesn't exist or is closed.
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found. Make sure mysql-connector-j.jar is in lib/.");
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database connection failed. Make sure MySQL is running and motorph_db exists.");
            System.out.println("Error: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Closes the database connection if open.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Hashes a plaintext password using SHA-256.
     * Used for both storing passwords and verifying login attempts.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            System.out.println("Error hashing password: " + e.getMessage());
            return password; // fallback to plaintext if hashing fails
        }
    }
}
