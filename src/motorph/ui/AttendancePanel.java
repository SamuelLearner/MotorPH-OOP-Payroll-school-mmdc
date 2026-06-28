package motorph.ui;

import motorph.model.AttendanceRecord;
import motorph.model.Employee;
import motorph.service.AttendanceService;
import motorph.service.EmployeeService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

// Panel for attendance management (Time In / Time Out + Records View).
public class AttendancePanel extends JPanel {

    private AttendanceService attendanceService;
    private EmployeeService employeeService;
    private Employee currentUser;
    private boolean canManage;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboEmployee;

    public AttendancePanel(AttendanceService attendanceService, EmployeeService employeeService,
                           Employee currentUser, boolean canManage) {
        this.attendanceService = attendanceService;
        this.employeeService = employeeService;
        this.currentUser = currentUser;
        this.canManage = canManage;
        setLayout(new BorderLayout(5, 5));
        initComponents();
    }

    private void initComponents() {
        // Controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnTimeIn = new JButton("Time In");
        btnTimeIn.addActionListener(e -> {
            attendanceService.timeIn(currentUser.getEmployeeId());
            JOptionPane.showMessageDialog(this, "Time In recorded.");
            refreshTable();
        });
        controlPanel.add(btnTimeIn);

        JButton btnTimeOut = new JButton("Time Out");
        btnTimeOut.addActionListener(e -> {
            attendanceService.timeOut(currentUser.getEmployeeId());
            JOptionPane.showMessageDialog(this, "Time Out recorded.");
            refreshTable();
        });
        controlPanel.add(btnTimeOut);

        controlPanel.add(new JLabel("  |  View records for:"));
        cboEmployee = new JComboBox<String>();
        if (canManage) {
            cboEmployee.addItem("All Employees");
            ArrayList<Employee> all = employeeService.getAllEmployees();
            for (Employee emp : all) {
                cboEmployee.addItem(emp.getEmployeeId() + " - " + emp.getFullName());
            }
        } else {
            cboEmployee.addItem(currentUser.getEmployeeId() + " - " + currentUser.getFullName());
        }
        controlPanel.add(cboEmployee);

        JButton btnView = new JButton("View");
        btnView.addActionListener(e -> refreshTable());
        controlPanel.add(btnView);

        add(controlPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Employee ID", "Date", "Time In", "Time Out", "Hours Worked"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String selected = (String) cboEmployee.getSelectedItem();
        ArrayList<AttendanceRecord> records;

        if (selected != null && selected.equals("All Employees")) {
            records = attendanceService.getAllRecords();
        } else if (selected != null) {
            String empId = selected.split(" - ")[0];
            records = attendanceService.getRecords(empId);
        } else {
            records = attendanceService.getRecords(currentUser.getEmployeeId());
        }

        for (AttendanceRecord r : records) {
            tableModel.addRow(new Object[]{
                    r.getEmployeeId(), r.getDate(), r.getTimeIn(), r.getTimeOut(),
                    String.format("%.2f", r.computeDailyHours())
            });
        }
    }
}
