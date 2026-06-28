package motorph.dao;

import motorph.model.LeaveRequest;
import motorph.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;

// DAO for reading/writing leave requests from/to MySQL Database using JDBC.
public class LeaveRequestDAO {

    public LeaveRequestDAO() {
        // No-arg constructor
    }

    // Loads all leave requests with a JOIN to get the leave type name.
    public ArrayList<LeaveRequest> loadAll() {
        ArrayList<LeaveRequest> requests = new ArrayList<LeaveRequest>();
        String query = "SELECT lr.leave_id, lr.employee_id, lt.name AS leaveType, " +
                       "lr.start_date, lr.end_date, lr.reason, lr.leave_status, lr.approved_by " +
                       "FROM leave_request lr " +
                       "LEFT JOIN leave_type lt ON lr.leave_type_id = lt.leave_type_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String requestId = String.valueOf(rs.getInt("leave_id"));
                String employeeId = String.valueOf(rs.getInt("employee_id"));
                String leaveType = rs.getString("leaveType");
                String startDate = rs.getDate("start_date").toString();
                String endDate = rs.getDate("end_date").toString();
                String reason = rs.getString("reason");
                String status = rs.getString("leave_status");
                
                int appById = rs.getInt("approved_by");
                String approvedBy = appById == 0 ? "" : String.valueOf(appById); // 0 if NULL in JDBC

                requests.add(new LeaveRequest(
                        requestId, employeeId, leaveType, startDate, endDate,
                        reason, status, approvedBy));
            }
        } catch (SQLException e) {
            System.out.println("Error loading leave requests from DB: " + e.getMessage());
        }
        return requests;
    }

    public ArrayList<LeaveRequest> findByEmployeeId(String employeeId) {
        ArrayList<LeaveRequest> all = loadAll();
        ArrayList<LeaveRequest> result = new ArrayList<LeaveRequest>();
        for (LeaveRequest lr : all) {
            if (lr.getEmployeeId().equals(employeeId)) {
                result.add(lr);
            }
        }
        return result;
    }

    // Inserts a new leave request.
    public void addLeaveRequest(LeaveRequest lr) {
        String insert = "INSERT INTO leave_request (employee_id, leave_type_id, start_date, end_date, reason, leave_status) " +
                        "VALUES (?, (SELECT leave_type_id FROM leave_type WHERE name = ? LIMIT 1), ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insert)) {

            ps.setInt(1, Integer.parseInt(lr.getEmployeeId()));
            ps.setString(2, lr.getLeaveType());
            ps.setDate(3, java.sql.Date.valueOf(lr.getStartDate()));
            ps.setDate(4, java.sql.Date.valueOf(lr.getEndDate()));
            ps.setString(5, lr.getReason());
            ps.setString(6, lr.getStatus());

            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error adding leave request to DB: " + e.getMessage());
        }
    }

    // Updates the status and approver of an existing leave request.
    public void updateLeaveStatus(String requestId, String status, String approvedBy) {
        String update = "UPDATE leave_request SET leave_status = ?, approved_by = ? WHERE leave_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(update)) {

            ps.setString(1, status);
            if (approvedBy == null || approvedBy.isEmpty()) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, Integer.parseInt(approvedBy));
            }
            ps.setInt(3, Integer.parseInt(requestId));

            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error updating leave request in DB: " + e.getMessage());
        }
    }
}
