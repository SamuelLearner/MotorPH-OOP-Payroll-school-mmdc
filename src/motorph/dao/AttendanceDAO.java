package motorph.dao;

import motorph.model.AttendanceRecord;
import motorph.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// DAO for reading/writing attendance records from/to MySQL Database using JDBC.
public class AttendanceDAO {

    public AttendanceDAO() {
        // No-arg constructor
    }

    // Loads all attendance records.
    public ArrayList<AttendanceRecord> loadAll() {
        ArrayList<AttendanceRecord> records = new ArrayList<AttendanceRecord>();
        String query = "SELECT employee_id, time_in, time_out FROM attendance";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String employeeId = String.valueOf(rs.getInt("employee_id"));
                Timestamp timeInTs = rs.getTimestamp("time_in");
                Timestamp timeOutTs = rs.getTimestamp("time_out");

                String date = "";
                String timeIn = "";
                String timeOut = "";

                if (timeInTs != null) {
                    LocalDateTime dtIn = timeInTs.toLocalDateTime();
                    date = dtIn.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    timeIn = dtIn.format(DateTimeFormatter.ofPattern("HH:mm"));
                }
                if (timeOutTs != null) {
                    LocalDateTime dtOut = timeOutTs.toLocalDateTime();
                    timeOut = dtOut.format(DateTimeFormatter.ofPattern("HH:mm"));
                }

                records.add(new AttendanceRecord(employeeId, date, timeIn, timeOut));
            }
        } catch (SQLException e) {
            System.out.println("Error loading attendance from DB: " + e.getMessage());
        }
        return records;
    }

    // Returns attendance records for a specific employee.
    public ArrayList<AttendanceRecord> findByEmployeeId(String employeeId) {
        ArrayList<AttendanceRecord> result = new ArrayList<AttendanceRecord>();
        String query = "SELECT time_in, time_out FROM attendance WHERE employee_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, Integer.parseInt(employeeId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp timeInTs = rs.getTimestamp("time_in");
                    Timestamp timeOutTs = rs.getTimestamp("time_out");

                    String date = "";
                    String timeIn = "";
                    String timeOut = "";

                    if (timeInTs != null) {
                        LocalDateTime dtIn = timeInTs.toLocalDateTime();
                        date = dtIn.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        timeIn = dtIn.format(DateTimeFormatter.ofPattern("HH:mm"));
                    }
                    if (timeOutTs != null) {
                        LocalDateTime dtOut = timeOutTs.toLocalDateTime();
                        timeOut = dtOut.format(DateTimeFormatter.ofPattern("HH:mm"));
                    }

                    result.add(new AttendanceRecord(employeeId, date, timeIn, timeOut));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding attendance: " + e.getMessage());
        }
        return result;
    }

    // Adds a new attendance record (time-in).
    public void addRecord(AttendanceRecord record) {
        String insert = "INSERT INTO attendance (employee_id, time_in) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insert)) {

            ps.setInt(1, Integer.parseInt(record.getEmployeeId()));
            String dtIn = record.getDate() + " " + record.getTimeIn() + ":00";
            ps.setTimestamp(2, Timestamp.valueOf(dtIn));
            
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error saving attendance to DB: " + e.getMessage());
        }
    }

    // Updates an existing attendance record (time-out).
    public void updateTimeOut(String employeeId, String date, String timeOut) {
        String update = "UPDATE attendance SET time_out = ? WHERE employee_id = ? AND DATE(time_in) = ? AND time_out IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(update)) {

            String dtOut = date + " " + timeOut + ":00";
            ps.setTimestamp(1, Timestamp.valueOf(dtOut));
            ps.setInt(2, Integer.parseInt(employeeId));
            ps.setDate(3, java.sql.Date.valueOf(date));
            
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error updating time-out in DB: " + e.getMessage());
        }
    }
}
