package motorph;

import motorph.dao.EmployeeDAO;
import motorph.dao.UserCredentialsDAO;
import motorph.service.AuthenticationService;
import motorph.ui.LoginFrame;
import javax.swing.*;

// Entry point for the MotorPH Payroll System.
public class Main {

    public static void main(String[] args) {
        // Set Swing look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }

        // Initialize DAOs and services
        EmployeeDAO employeeDAO = new EmployeeDAO();
        UserCredentialsDAO credentialsDAO = new UserCredentialsDAO();
        AuthenticationService authService = new AuthenticationService(credentialsDAO, employeeDAO);

        // Launch login screen
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame(authService);
                loginFrame.setVisible(true);
            }
        });
    }
}
