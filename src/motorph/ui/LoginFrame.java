package motorph.ui;

import motorph.model.Employee;
import motorph.service.AuthenticationService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Login screen — validates credentials and opens DashboardFrame on success.
public class LoginFrame extends JFrame {

    private JTextField txtEmployeeId;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblMessage;
    private AuthenticationService authService;

    public LoginFrame(AuthenticationService authService) {
        this.authService = authService;
        setTitle("MotorPH Payroll System - Login");
        setSize(420, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Title
        JLabel lblTitle = new JLabel("MotorPH Payroll System", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        formPanel.add(new JLabel("Employee ID:"));
        txtEmployeeId = new JTextField();
        formPanel.add(txtEmployeeId);

        formPanel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        formPanel.add(txtPassword);

        formPanel.add(new JLabel("")); // spacer
        btnLogin = new JButton("Login");
        formPanel.add(btnLogin);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Message label
        lblMessage = new JLabel(" ", SwingConstants.CENTER);
        lblMessage.setForeground(Color.RED);
        mainPanel.add(lblMessage, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Login action
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // Allow Enter key to submit
        txtPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
    }

    private void handleLogin() {
        String id = txtEmployeeId.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (id.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Please enter both Employee ID and Password.");
            return;
        }

        Employee employee = authService.authenticate(id, password);

        if (employee != null) {
            lblMessage.setText("Login successful!");
            dispose(); // close login window
            new DashboardFrame(employee).setVisible(true);
        } else {
            lblMessage.setText("Invalid Employee ID or Password.");
            txtPassword.setText("");
        }
    }
}
