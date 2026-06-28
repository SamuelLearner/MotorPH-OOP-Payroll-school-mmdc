package motorph.model;

// Represents a leave request submitted by an employee.
public class LeaveRequest implements Exportable {

    private String requestId;
    private String employeeId;
    private String leaveType;    // Sick, Vacation, Emergency
    private String startDate;
    private String endDate;
    private String reason;
    private String status;       // Pending, Approved, Rejected
    private String approvedBy;

    public LeaveRequest(String requestId, String employeeId, String leaveType,
                        String startDate, String endDate, String reason,
                        String status, String approvedBy) {
        this.requestId = requestId;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
        this.approvedBy = approvedBy;
    }

    // Getters
    public String getRequestId() { return requestId; }
    public String getEmployeeId() { return employeeId; }
    public String getLeaveType() { return leaveType; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public String getApprovedBy() { return approvedBy; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    @Override
    public String toCSVString() {
        return String.join(",", requestId, employeeId, leaveType,
                startDate, endDate, reason, status, approvedBy);
    }
}
