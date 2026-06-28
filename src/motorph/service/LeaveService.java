package motorph.service;

import motorph.dao.LeaveRequestDAO;
import motorph.model.LeaveRequest;
import java.util.ArrayList;

// Service for leave request operations.
public class LeaveService {

    private LeaveRequestDAO leaveDAO;

    public LeaveService(LeaveRequestDAO leaveDAO) {
        this.leaveDAO = leaveDAO;
    }

    // Submits a new leave request.
    public String submitLeaveRequest(String employeeId, String leaveType,
                                     String startDate, String endDate, String reason) {
        if (leaveType == null || leaveType.isEmpty()) return "Leave type is required.";
        if (startDate == null || startDate.isEmpty()) return "Start date is required.";
        if (endDate == null || endDate.isEmpty()) return "End date is required.";

        LeaveRequest request = new LeaveRequest(
                "0", employeeId, leaveType, startDate, endDate,
                reason, "Pending", "");

        leaveDAO.addLeaveRequest(request);
        return null; // success
    }

    // Approves or rejects a leave request.
    public String processLeaveRequest(String requestId, String status, String approvedBy) {
        leaveDAO.updateLeaveStatus(requestId, status, approvedBy);
        return null;
    }

    // Gets leave requests for a specific employee.
    public ArrayList<LeaveRequest> getEmployeeLeaves(String employeeId) {
        return leaveDAO.findByEmployeeId(employeeId);
    }

    // Gets all pending leave requests (for HR/Admin approval).
    public ArrayList<LeaveRequest> getPendingRequests() {
        ArrayList<LeaveRequest> all = leaveDAO.loadAll();
        ArrayList<LeaveRequest> pending = new ArrayList<LeaveRequest>();
        for (LeaveRequest lr : all) {
            if (lr.getStatus().equals("Pending")) {
                pending.add(lr);
            }
        }
        return pending;
    }

    public ArrayList<LeaveRequest> getAllLeaves() {
        return leaveDAO.loadAll();
    }
}
