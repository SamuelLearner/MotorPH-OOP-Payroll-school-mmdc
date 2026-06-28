package motorph.dao;

import motorph.model.*;
import motorph.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;

// DAO for reading/writing employee data from/to MySQL Database using JDBC.
public class EmployeeDAO {

    public EmployeeDAO() {
        // No-arg constructor since we use DB connection directly now
    }

    // Loads all employees using a 7-table JOIN to reconstruct the Java model objects.
    public ArrayList<Employee> loadAll() {
        ArrayList<Employee> employees = new ArrayList<Employee>();
        
        String query = "SELECT " +
            "e.employee_id, e.first_name, e.last_name, e.status, " +
            "sup.first_name AS sup_first, sup.last_name AS sup_last, " +
            "ed.dob, ed.ssn, ed.tin, ed.phic, ed.hdmf, ed.phone_number, " +
            "a.street, a.barangay, a.city, a.province, a.zipcode, " +
            "p.position_name, p.basic_salary, p.allowance, " +
            "r.role_name " +
            "FROM employee e " +
            "LEFT JOIN employee_details ed ON e.employee_id = ed.employee_id " +
            "LEFT JOIN address a ON ed.address_id = a.address_id " +
            "LEFT JOIN position p ON e.position_id = p.position_id " +
            "LEFT JOIN user_account ua ON e.employee_id = ua.employee_id " +
            "LEFT JOIN user_role ur ON ua.user_id = ur.user_id " +
            "LEFT JOIN role r ON ur.role_id = r.role_id " +
            "LEFT JOIN employee sup ON e.supervisor_id = sup.employee_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Employee emp = mapResultSetToEmployee(rs);
                if (emp != null) {
                    employees.add(emp);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading employees from DB: " + e.getMessage());
        }
        return employees;
    }

    // Maps a ResultSet row back into the appropriate Employee subclass.
    private Employee mapResultSetToEmployee(ResultSet rs) {
        try {
            String id = String.valueOf(rs.getInt("employee_id"));
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            
            // Format supervisor name
            String supFirst = rs.getString("sup_first");
            String supLast = rs.getString("sup_last");
            String supervisor = (supLast != null && supFirst != null) ? (supLast + ", " + supFirst) : "N/A";
            
            String status = rs.getString("status");
            String dob = rs.getDate("dob") != null ? rs.getDate("dob").toString() : "";
            String sss = rs.getString("ssn");
            String tin = rs.getString("tin");
            String phic = rs.getString("phic");
            String hdmf = rs.getString("hdmf");
            String phone = rs.getString("phone_number");
            
            // Reconstruct full address for compatibility with old model
            String street = rs.getString("street");
            String address = street != null ? street : "";
            
            String position = rs.getString("position_name");
            double basicSalary = rs.getDouble("basic_salary");
            double totalAllowance = rs.getDouble("allowance");
            
            // Distribute allowances (mock logic matching the original CSV 1500,2000,1000)
            double riceSubsidy = 1500.00;
            double clothingAllowance = 1000.00;
            double phoneAllow = totalAllowance > 2500 ? (totalAllowance - 2500) : 0;
            if (phoneAllow == 0 && totalAllowance > 0) {
                // If the total allowance is different, just dump it all in rice for fallback
                riceSubsidy = totalAllowance;
                clothingAllowance = 0;
            }
            
            double hourlyRate = basicSalary / 168.0; // Estimate
            
            String role = rs.getString("role_name");
            if (role == null) role = "Employee";

            // Create correct subclass
            if (role.equals("Admin")) {
                return new AdminEmployee(id, firstName, lastName, dob, address, phone,
                        sss, phic, tin, hdmf, status, position, supervisor,
                        basicSalary, riceSubsidy, phoneAllow, clothingAllowance, hourlyRate);
            } else if (role.equals("HR")) {
                return new HREmployee(id, firstName, lastName, dob, address, phone,
                        sss, phic, tin, hdmf, status, position, supervisor,
                        basicSalary, riceSubsidy, phoneAllow, clothingAllowance, hourlyRate);
            } else if (role.equals("Finance")) {
                return new FinanceEmployee(id, firstName, lastName, dob, address, phone,
                        sss, phic, tin, hdmf, status, position, supervisor,
                        basicSalary, riceSubsidy, phoneAllow, clothingAllowance, hourlyRate);
            } else if (role.equals("IT")) {
                return new ITEmployee(id, firstName, lastName, dob, address, phone,
                        sss, phic, tin, hdmf, status, position, supervisor,
                        basicSalary, riceSubsidy, phoneAllow, clothingAllowance, hourlyRate);
            } else {
                return new BasicEmployee(id, firstName, lastName, dob, address, phone,
                        sss, phic, tin, hdmf, status, position, supervisor,
                        basicSalary, riceSubsidy, phoneAllow, clothingAllowance, hourlyRate);
            }
        } catch (SQLException e) {
            System.out.println("Error mapping employee row: " + e.getMessage());
            return null;
        }
    }

    // Finds an employee by ID.
    public Employee findById(String employeeId) {
        ArrayList<Employee> employees = loadAll();
        for (Employee emp : employees) {
            if (emp.getEmployeeId().equals(employeeId)) {
                return emp;
            }
        }
        return null;
    }

    // New JDBC method: adds an employee directly via INSERT.
    public void addEmployee(Employee emp) {
        // Simplified for now: just adds to core `employee` and `employee_details`.
        // A complete implementation would insert into address, position, and user_account as well.
        String insertEmp = "INSERT INTO employee (employee_id, last_name, first_name, status) VALUES (?, ?, ?, ?)";
        String insertDetails = "INSERT INTO employee_details (employee_id, dob, ssn, tin, phic, hdmf, phone_number) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Transaction

            try (PreparedStatement ps = conn.prepareStatement(insertEmp)) {
                ps.setInt(1, Integer.parseInt(emp.getEmployeeId()));
                ps.setString(2, emp.getLastName());
                ps.setString(3, emp.getFirstName());
                ps.setString(4, emp.getEmploymentStatus());
                ps.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(insertDetails)) {
                ps2.setInt(1, Integer.parseInt(emp.getEmployeeId()));
                ps2.setString(2, emp.getBirthday());
                ps2.setString(3, emp.getSssNumber());
                ps2.setString(4, emp.getTinNumber());
                ps2.setString(5, emp.getPhilhealthNumber());
                ps2.setString(6, emp.getPagibigNumber());
                ps2.setString(7, emp.getPhoneNumber());
                ps2.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            System.out.println("Error adding employee to DB: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { }
            }
        }
    }

    // New JDBC method: updates an employee via UPDATE.
    public void updateEmployee(Employee emp) {
        String updateEmp = "UPDATE employee SET last_name = ?, first_name = ?, status = ? WHERE employee_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateEmp)) {
            
            ps.setString(1, emp.getLastName());
            ps.setString(2, emp.getFirstName());
            ps.setString(3, emp.getEmploymentStatus());
            ps.setInt(4, Integer.parseInt(emp.getEmployeeId()));
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println("Error updating employee in DB: " + e.getMessage());
        }
    }

    // New JDBC method: deletes an employee via DELETE.
    public void deleteEmployee(String employeeId) {
        // Must delete child records first due to FK constraints
        String delDetails = "DELETE FROM employee_details WHERE employee_id = ?";
        String delUserRole = "DELETE FROM user_role WHERE user_id IN (SELECT user_id FROM user_account WHERE employee_id = ?)";
        String delUser = "DELETE FROM user_account WHERE employee_id = ?";
        String delEmp = "DELETE FROM employee WHERE employee_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(delDetails)) {
                ps1.setInt(1, Integer.parseInt(employeeId));
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(delUserRole)) {
                ps2.setInt(1, Integer.parseInt(employeeId));
                ps2.executeUpdate();
            }
            try (PreparedStatement ps3 = conn.prepareStatement(delUser)) {
                ps3.setInt(1, Integer.parseInt(employeeId));
                ps3.executeUpdate();
            }
            try (PreparedStatement ps4 = conn.prepareStatement(delEmp)) {
                ps4.setInt(1, Integer.parseInt(employeeId));
                ps4.executeUpdate();
            }
            conn.commit();
        } catch (Exception e) {
            System.out.println("Error deleting employee from DB: " + e.getMessage());
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
