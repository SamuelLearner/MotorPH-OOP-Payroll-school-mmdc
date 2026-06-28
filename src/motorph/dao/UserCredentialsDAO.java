package motorph.dao;

import motorph.model.UserCredentials;
import motorph.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;

// DAO for reading/writing user credentials from/to MySQL Database using JDBC.
public class UserCredentialsDAO {

    public UserCredentialsDAO() {
        // No-arg constructor
    }

    public ArrayList<UserCredentials> loadAll() {
        ArrayList<UserCredentials> creds = new ArrayList<UserCredentials>();
        String query = "SELECT ua.employee_id, ua.password_hash, r.role_name " +
                       "FROM user_account ua " +
                       "LEFT JOIN user_role ur ON ua.user_id = ur.user_id " +
                       "LEFT JOIN role r ON ur.role_id = r.role_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String employeeId = String.valueOf(rs.getInt("employee_id"));
                String passwordHash = rs.getString("password_hash");
                String roleName = rs.getString("role_name");
                if (roleName == null) roleName = "Employee";

                creds.add(new UserCredentials(employeeId, passwordHash, roleName));
            }
        } catch (SQLException e) {
            System.out.println("Error loading credentials from DB: " + e.getMessage());
        }
        return creds;
    }

    // Finds credentials by employee ID.
    public UserCredentials findByEmployeeId(String employeeId) {
        String query = "SELECT ua.employee_id, ua.password_hash, r.role_name " +
                       "FROM user_account ua " +
                       "LEFT JOIN user_role ur ON ua.user_id = ur.user_id " +
                       "LEFT JOIN role r ON ur.role_id = r.role_id " +
                       "WHERE ua.employee_id = ?";

        int idVal;
        try {
            idVal = Integer.parseInt(employeeId);
        } catch (NumberFormatException e) {
            return null; // Non-numeric IDs cannot exist in the database
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idVal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String passwordHash = rs.getString("password_hash");
                    String roleName = rs.getString("role_name");
                    if (roleName == null) roleName = "Employee";

                    return new UserCredentials(employeeId, passwordHash, roleName);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding credentials from DB: " + e.getMessage());
        }
        return null;
    }

    // Adds credentials for a new employee.
    public void addCredential(UserCredentials cred) {
        // Find role_id from role_name
        String insertUser = "INSERT INTO user_account (employee_id, username, password_hash) VALUES (?, ?, ?)";
        String insertRole = "INSERT INTO user_role (user_id, role_id) VALUES (?, (SELECT role_id FROM role WHERE role_name = ? LIMIT 1))";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int userId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, Integer.parseInt(cred.getEmployeeId()));
                ps.setString(2, cred.getEmployeeId()); // use employeeId as username
                ps.setString(3, cred.getPassword()); // assumes already hashed by service
                ps.executeUpdate();
                
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    }
                }
            }

            if (userId != -1) {
                try (PreparedStatement ps = conn.prepareStatement(insertRole)) {
                    ps.setInt(1, userId);
                    ps.setString(2, cred.getRole());
                    ps.executeUpdate();
                }
            }
            conn.commit();
        } catch (Exception e) {
            System.out.println("Error adding credentials to DB: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { }
            }
        }
    }
}
