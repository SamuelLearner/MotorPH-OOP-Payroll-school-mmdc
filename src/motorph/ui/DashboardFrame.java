package motorph.ui;

import motorph.model.Employee;
import motorph.dao.*;
import motorph.service.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

// Main dashboard — shows tabs based on role permissions (RBAC).
public class DashboardFrame extends JFrame {

    private Employee currentUser;
    private JTabbedPane tabbedPane;

    // Services
    private EmployeeService employeeService;
    private PayrollService payrollService;
    private AttendanceService attendanceService;
    private LeaveService leaveService;

    public DashboardFrame(Employee currentUser) {
        this.currentUser = currentUser;
        initServices();
        setTitle("MotorPH Payroll System - " + currentUser.getFullName() + " [" + currentUser.getRole() + "]");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initServices() {
        EmployeeDAO empDAO = new EmployeeDAO();
        UserCredentialsDAO credDAO = new UserCredentialsDAO();
        AttendanceDAO attDAO = new AttendanceDAO();
        LeaveRequestDAO leaveDAO = new LeaveRequestDAO();

        employeeService = new EmployeeService(empDAO, credDAO);
        payrollService = new PayrollService();
        attendanceService = new AttendanceService(attDAO);
        leaveService = new LeaveService(leaveDAO);
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(41, 128, 185));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblWelcome = new JLabel("Welcome, " + currentUser.getFullName()
                + "  |  Role: " + currentUser.getRole()
                + "  |  Status: " + currentUser.getEmploymentStatus());
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblWelcome.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setOpaque(true);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        btnLogout.addActionListener(e -> {
            dispose();
            AuthenticationService authService = new AuthenticationService(
                    new UserCredentialsDAO(),
                    new EmployeeDAO());
            new LoginFrame(authService).setVisible(true);
        });

        topPanel.add(lblWelcome, BorderLayout.WEST);
        topPanel.add(btnLogout, BorderLayout.EAST);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);
        List<String> permissions = currentUser.getPermissions();

        // Add tabs based on RBAC permissions
        if (permissions.contains("EMPLOYEE_VIEW")) {
            boolean canAdd = permissions.contains("EMPLOYEE_ADD");
            boolean canEdit = permissions.contains("EMPLOYEE_EDIT");
            boolean canDelete = permissions.contains("EMPLOYEE_DELETE");
            tabbedPane.addTab("Employees", new EmployeeManagementPanel(
                    employeeService, canAdd, canEdit, canDelete));
        }

        if (permissions.contains("PAYROLL_PROCESS") || permissions.contains("PAYSLIP_VIEW")) {
            boolean canProcess = permissions.contains("PAYROLL_PROCESS");
            tabbedPane.addTab("Payroll / Payslip", new PayrollPanel(
                    employeeService, payrollService, attendanceService, currentUser, canProcess));
        }

        if (permissions.contains("ATTENDANCE_VIEW")) {
            boolean canManage = permissions.contains("ATTENDANCE_MANAGE");
            tabbedPane.addTab("Attendance", new AttendancePanel(
                    attendanceService, employeeService, currentUser, canManage));
        }

        if (permissions.contains("LEAVE_SUBMIT") || permissions.contains("LEAVE_APPROVE")) {
            boolean canApprove = permissions.contains("LEAVE_APPROVE");
            tabbedPane.addTab("Leave Requests", new LeaveRequestPanel(
                    leaveService, employeeService, currentUser, canApprove));
        }

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }
}
