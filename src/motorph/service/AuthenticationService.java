package motorph.service;

import motorph.dao.UserCredentialsDAO;
import motorph.dao.EmployeeDAO;
import motorph.model.Employee;
import motorph.model.UserCredentials;

// Service for authentication — validates login credentials and returns the employee.
public class AuthenticationService {

    private UserCredentialsDAO credentialsDAO;
    private EmployeeDAO employeeDAO;

    public AuthenticationService(UserCredentialsDAO credentialsDAO, EmployeeDAO employeeDAO) {
        this.credentialsDAO = credentialsDAO;
        this.employeeDAO = employeeDAO;
    }

    // Authenticates a user. Returns the Employee object if valid, null otherwise.
    public Employee authenticate(String employeeId, String password) {
        UserCredentials cred = credentialsDAO.findByEmployeeId(employeeId);
        if (cred == null) {
            return null;
        }
        String hashedInput = motorph.util.DatabaseConnection.hashPassword(password);
        if (!cred.getPassword().equals(hashedInput)) {
            return null;
        }
        return employeeDAO.findById(employeeId);
    }
}
