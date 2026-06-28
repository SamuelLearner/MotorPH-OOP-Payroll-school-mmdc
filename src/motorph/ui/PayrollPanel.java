package motorph.ui;

import motorph.model.*;
import motorph.service.EmployeeService;
import motorph.service.PayrollService;
import motorph.service.AttendanceService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

// Panel for payroll processing and payslip generation
public class PayrollPanel extends JPanel {

    private EmployeeService employeeService;
    private PayrollService payrollService;
    private AttendanceService attendanceService;
    private Employee currentUser;
    private boolean canProcess;

    private JComboBox<String> cboEmployee;
    private JComboBox<String> cboCutoff;
    private JTextField txtHours;
    private JTextArea txtPayslip;
    private JTable deductionTable;
    private DefaultTableModel deductionModel;
    private JComboBox<String> cboPayType;

    public PayrollPanel(EmployeeService employeeService, PayrollService payrollService,
                        AttendanceService attendanceService, Employee currentUser, boolean canProcess) {
        this.employeeService = employeeService;
        this.payrollService = payrollService;
        this.attendanceService = attendanceService;
        this.currentUser = currentUser;
        this.canProcess = canProcess;
        setLayout(new BorderLayout(5, 5));
        initComponents();
    }

    private void initComponents() {
        // Top controls
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));

        row1.add(new JLabel("Employee:"));
        cboEmployee = new JComboBox<String>();
        loadEmployeeList();
        row1.add(cboEmployee);
        
        row1.add(new JLabel("Cut-off Period:"));
        cboCutoff = new JComboBox<String>();
        row1.add(cboCutoff);
        cboEmployee.addActionListener(e -> updateCutoffDropdown());
        
        // Trigger initial population
        if (cboEmployee.getItemCount() > 0) updateCutoffDropdown();

        row1.add(new JLabel("Pay Type:"));
        cboPayType = new JComboBox<String>(new String[]{"Monthly (Basic + Allowances)", "Hourly (Hours x Rate)"});
        row1.add(cboPayType);

        row2.add(new JLabel("Hours (if hourly):"));
        txtHours = new JTextField(6);
        row2.add(txtHours);

        JButton btnFromAttendance = new JButton("Load Hours");
        btnFromAttendance.addActionListener(e -> loadHoursFromAttendance());
        row2.add(btnFromAttendance);

        JButton btnCalculate = new JButton("Generate Payslip");
        btnCalculate.addActionListener(e -> generatePayslip());
        row2.add(btnCalculate);

        controlPanel.add(row1);
        controlPanel.add(row2);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(controlPanel, BorderLayout.NORTH);

        // Center — payslip display
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 5, 5));

        txtPayslip = new JTextArea();
        txtPayslip.setEditable(false);
        txtPayslip.setFont(new Font("Monospaced", Font.PLAIN, 12));
        centerPanel.add(new JScrollPane(txtPayslip));

        // Deductions table
        String[] cols = {"Deduction Type", "Amount (PHP)"};
        deductionModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        deductionTable = new JTable(deductionModel);
        centerPanel.add(new JScrollPane(deductionTable));

        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadEmployeeList() {
        cboEmployee.removeAllItems();
        if (canProcess) {
            ArrayList<Employee> all = employeeService.getAllEmployees();
            for (Employee emp : all) {
                cboEmployee.addItem(emp.getEmployeeId() + " - " + emp.getFullName());
            }
        } else {
            cboEmployee.addItem(currentUser.getEmployeeId() + " - " + currentUser.getFullName());
        }
    }

    private void updateCutoffDropdown() {
        cboCutoff.removeAllItems();
        cboCutoff.addItem("All Records");
        String selected = (String) cboEmployee.getSelectedItem();
        if (selected == null) return;
        String empId = selected.split(" - ")[0];

        ArrayList<AttendanceRecord> records = attendanceService.getRecords(empId);
        java.util.TreeSet<String> periods = new java.util.TreeSet<>();
        for (AttendanceRecord r : records) {
            if (r.getDate() != null && r.getDate().length() >= 7) {
                String month = r.getDate().substring(0, 7);
                try {
                    int day = Integer.parseInt(r.getDate().substring(8, 10));
                    periods.add(day <= 15 ? month + " (1-15)" : month + " (16-31)");
                } catch (Exception ex) {
                    periods.add(month);
                }
            }
        }
        for (String p : periods) {
            cboCutoff.addItem(p);
        }
    }

    private void loadHoursFromAttendance() {
        String selected = (String) cboEmployee.getSelectedItem();
        if (selected == null) return;
        String empId = selected.split(" - ")[0];

        ArrayList<AttendanceRecord> records = attendanceService.getRecords(empId);
        String selectedCutoff = (String) cboCutoff.getSelectedItem();
        
        ArrayList<AttendanceRecord> filtered = new ArrayList<>();
        if (selectedCutoff == null || selectedCutoff.equals("All Records")) {
            filtered = records;
        } else {
            for (AttendanceRecord r : records) {
                if (r.getDate() != null && r.getDate().length() >= 10) {
                    String month = r.getDate().substring(0, 7);
                    try {
                        int day = Integer.parseInt(r.getDate().substring(8, 10));
                        String period = day <= 15 ? month + " (1-15)" : month + " (16-31)";
                        if (period.equals(selectedCutoff)) filtered.add(r);
                    } catch (Exception ex) { }
                }
            }
        }

        double totalHours = payrollService.computeTotalHours(filtered);
        txtHours.setText(String.format("%.2f", totalHours));
    }

    private void generatePayslip() {
        String selected = (String) cboEmployee.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee.");
            return;
        }

        String empId = selected.split(" - ")[0];
        Employee employee = employeeService.searchEmployee(empId);
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Employee not found.");
            return;
        }

        Payslip payslip;
        int payType = cboPayType.getSelectedIndex();

        String period = (String) cboCutoff.getSelectedItem();
        if (period == null || period.trim().isEmpty()) period = "Standard";

        if (payType == 0) {
            // Monthly: Gross = Basic Salary + Allowances (per rubric)
            payslip = payrollService.generateMonthlyPayslip(employee, period);
        } else {
            // Hourly: Gross = Hours x Rate
            double hours;
            try {
                hours = Double.parseDouble(txtHours.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid hours worked.");
                return;
            }
            payslip = payrollService.generatePayslip(employee, hours, period);
        }

        // Display payslip text
        txtPayslip.setText(payslip.generatePayslip());

        // Display deductions in table
        deductionModel.setRowCount(0);
        HashMap<String, Double> deductions = payslip.getDeductions();
        for (String key : deductions.keySet()) {
            deductionModel.addRow(new Object[]{key, String.format("%.2f", deductions.get(key))});
        }
        deductionModel.addRow(new Object[]{"TOTAL DEDUCTIONS", String.format("%.2f", payslip.getTotalDeductions())});
        deductionModel.addRow(new Object[]{"", ""});
        deductionModel.addRow(new Object[]{"GROSS PAY", String.format("%.2f", payslip.getGrossPay())});
        deductionModel.addRow(new Object[]{"NET PAY", String.format("%.2f", payslip.getNetPay())});
    
        try {
            // 1. Resolve your selected text period dropdown into a numeric database Key 
            // (e.g., if "All Records" or "Standard", default to 1, otherwise map dynamically)
            int operationalPayPeriodId = 1; 
            String selectedPeriod = (String) cboCutoff.getSelectedItem();
            if (selectedPeriod != null && !selectedPeriod.equals("All Records")) {
                // If you have a period map or database query, assign the real PK ID here
                operationalPayPeriodId = 1; 
            }

            // 2. Instantiate your Data Access Object class
            motorph.dao.PayrollDAO payrollDAO = new motorph.dao.PayrollDAO();

            // 3. Save the calculated values to MySQL disk
            payrollDAO.persistCalculation(payslip, operationalPayPeriodId);
            
            System.out.println("Successfully persisted payroll to database via DAO.");
            
        } catch (Exception ex) {
            System.out.println("Error triggering payroll persistence: " + ex.getMessage());
        }
    }
}
