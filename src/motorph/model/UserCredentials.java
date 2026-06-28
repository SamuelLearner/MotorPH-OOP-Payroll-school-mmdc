package motorph.model;

// Stores login credentials for an employee.
public class UserCredentials {

    private String employeeId;
    private String password;
    private String role;

    public UserCredentials(String employeeId, String password, String role) {
        this.employeeId = employeeId;
        this.password = password;
        this.role = role;
    }

    public String getEmployeeId() { return employeeId; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
}
