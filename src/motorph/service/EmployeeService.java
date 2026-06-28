package motorph.service;

import motorph.dao.EmployeeDAO;
import motorph.dao.UserCredentialsDAO;
import motorph.model.*;
import java.util.ArrayList;

// Service for employee CRUD operations with validation.
public class EmployeeService {

    private EmployeeDAO employeeDAO;
    private UserCredentialsDAO credentialsDAO;

    public EmployeeService(EmployeeDAO employeeDAO, UserCredentialsDAO credentialsDAO) {
        this.employeeDAO = employeeDAO;
        this.credentialsDAO = credentialsDAO;
    }

    public ArrayList<Employee> getAllEmployees() {
        return employeeDAO.loadAll();
    }

    // Search by ID — Overloaded search method #1.
    public Employee searchEmployee(String employeeId) {
        return employeeDAO.findById(employeeId);
    }

    // Search by first and last name — Overloaded search method #2.
    public Employee searchEmployee(String firstName, String lastName) {
        ArrayList<Employee> all = employeeDAO.loadAll();
        for (Employee emp : all) {
            if (emp.getFirstName().equalsIgnoreCase(firstName)
                    && emp.getLastName().equalsIgnoreCase(lastName)) {
                return emp;
            }
        }
        return null;
    }

    // Adds a new employee with validation.
    // Returns error message or null if success.
    public String addEmployee(Employee newEmp) {
        // Validate required fields
        if (newEmp.getEmployeeId() == null || newEmp.getEmployeeId().isEmpty()) {
            return "Employee ID is required.";
        }
        if (newEmp.getFirstName() == null || newEmp.getFirstName().isEmpty()) {
            return "First name is required.";
        }
        if (newEmp.getLastName() == null || newEmp.getLastName().isEmpty()) {
            return "Last name is required.";
        }
        if (newEmp.getBirthday() == null || newEmp.getBirthday().isEmpty()) {
            return "Birthday is required.";
        }
        if (newEmp.getPosition() == null || newEmp.getPosition().isEmpty()) {
            return "Position is required.";
        }
        if (newEmp.getBasicSalary() <= 0) {
            return "Basic salary must be a positive number.";
        }
        if (newEmp.getHourlyRate() <= 0) {
            return "Hourly rate must be a positive number.";
        }

        // Validate government ID formats
        String sssError = validateGovId(newEmp.getSssNumber(), "SSS", "XX-XXXXXXX-X");
        if (sssError != null) return sssError;

        String philError = validateGovId(newEmp.getPhilhealthNumber(), "PhilHealth", "XX-XXXXXXXXX-X");
        if (philError != null) return philError;

        String tinError = validateGovId(newEmp.getTinNumber(), "TIN", "XXX-XXX-XXX-XXX");
        if (tinError != null) return tinError;

        String pagibigError = validateGovId(newEmp.getPagibigNumber(), "Pag-IBIG", "XXXX-XXXX-XXXX");
        if (pagibigError != null) return pagibigError;

        // Check duplicate ID
        if (employeeDAO.findById(newEmp.getEmployeeId()) != null) {
            return "Employee ID already exists.";
        }

        employeeDAO.addEmployee(newEmp);

        // Also add default credentials
        UserCredentials cred = new UserCredentials(
                newEmp.getEmployeeId(), "password123", newEmp.getRole());
        credentialsDAO.addCredential(cred);

        return null; // success
    }

    // Updates an existing employee.
    public String updateEmployee(Employee updated) {
        if (employeeDAO.findById(updated.getEmployeeId()) == null) {
            return "Employee not found.";
        }
        employeeDAO.updateEmployee(updated);
        return null;
    }

    // Deletes an employee by ID.
    public String deleteEmployee(String employeeId) {
        if (employeeDAO.findById(employeeId) == null) {
            return "Employee not found.";
        }
        employeeDAO.deleteEmployee(employeeId);
        return null;
    }

    // Validates government ID format (must contain digits and dashes, matching expected pattern length)
    private String validateGovId(String value, String idName, String expectedFormat) {
        if (value == null || value.trim().isEmpty()) {
            return idName + " number is required (format: " + expectedFormat + ").";
        }
        // Check that it contains at least one digit and one dash
        boolean hasDigit = false;
        boolean hasDash = false;
        for (char c : value.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            if (c == '-') hasDash = true;
        }
        if (!hasDigit || !hasDash) {
            return idName + " must follow format: " + expectedFormat;
        }
        return null; // valid
    }
}
