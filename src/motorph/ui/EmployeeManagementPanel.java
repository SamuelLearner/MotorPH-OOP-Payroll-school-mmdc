package motorph.ui;

import motorph.model.*;
import motorph.service.EmployeeService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

// Panel for employee CRUD operations.
public class EmployeeManagementPanel extends JPanel {

    private EmployeeService employeeService;
    private boolean canAdd, canEdit, canDelete;
    private JTable table;
    private DefaultTableModel tableModel;

    public EmployeeManagementPanel(EmployeeService employeeService,
                                   boolean canAdd, boolean canEdit, boolean canDelete) {
        this.employeeService = employeeService;
        this.canAdd = canAdd;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
        setLayout(new BorderLayout(5, 5));
        initComponents();
        refreshTable();
    }

    private void initComponents() {
        // Table
        String[] columns = {"ID", "First Name", "Last Name", "Position", "Status", "Role", "Basic Salary"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshTable());
        buttonPanel.add(btnRefresh);

        if (canAdd) {
            JButton btnAdd = new JButton("Add Employee");
            btnAdd.addActionListener(e -> showAddDialog());
            buttonPanel.add(btnAdd);
        }

        if (canEdit) {
            JButton btnEdit = new JButton("Edit Employee");
            btnEdit.addActionListener(e -> showEditDialog());
            buttonPanel.add(btnEdit);
        }

        if (canDelete) {
            JButton btnDelete = new JButton("Delete Employee");
            btnDelete.addActionListener(e -> deleteSelected());
            buttonPanel.add(btnDelete);
        }

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        ArrayList<Employee> employees = employeeService.getAllEmployees();
        for (Employee emp : employees) {
            tableModel.addRow(new Object[]{
                    emp.getEmployeeId(), emp.getFirstName(), emp.getLastName(),
                    emp.getPosition(), emp.getEmploymentStatus(), emp.getRole(),
                    String.format("%.2f", emp.getBasicSalary())
            });
        }
    }

    private void showAddDialog() {
        JPanel form = createEmployeeForm(null);
        int result = JOptionPane.showConfirmDialog(this, form, "Add New Employee",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Employee emp = extractEmployeeFromForm(form);
            if (emp != null) {
                String error = employeeService.addEmployee(emp);
                if (error != null) {
                    JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    refreshTable();
                    JOptionPane.showMessageDialog(this, "Employee added successfully.");
                }
            }
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an employee to edit.");
            return;
        }
        String empId = (String) tableModel.getValueAt(row, 0);
        Employee emp = employeeService.searchEmployee(empId);
        if (emp == null) return;

        JPanel form = createEmployeeForm(emp);
        int result = JOptionPane.showConfirmDialog(this, form, "Edit Employee",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Employee updated = extractEmployeeFromForm(form);
            if (updated != null) {
                String error = employeeService.updateEmployee(updated);
                if (error != null) {
                    JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    refreshTable();
                    JOptionPane.showMessageDialog(this, "Employee updated successfully.");
                }
            }
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.");
            return;
        }
        String empId = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete employee " + empId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String error = employeeService.deleteEmployee(empId);
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                refreshTable();
                JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
            }
        }
    }

    private JPanel createEmployeeForm(Employee emp) {
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        String[] labels = {"Employee ID:", "First Name:", "Last Name:", "Birthday:", "Address:",
                "Phone:", "SSS Number:", "PhilHealth Number:", "TIN:", "Pag-IBIG Number:",
                "Employment Status:", "Position:", "Supervisor:", "Basic Salary:",
                "Rice Subsidy:", "Phone Allowance:", "Clothing Allowance:", "Hourly Rate:", "Role:"};
        String[] defaults = emp != null ? new String[]{
                emp.getEmployeeId(), emp.getFirstName(), emp.getLastName(), emp.getBirthday(),
                emp.getAddress(), emp.getPhoneNumber(), emp.getSssNumber(), emp.getPhilhealthNumber(),
                emp.getTinNumber(), emp.getPagibigNumber(), emp.getEmploymentStatus(), emp.getPosition(),
                emp.getSupervisor(), String.valueOf(emp.getBasicSalary()), String.valueOf(emp.getRiceSubsidy()),
                String.valueOf(emp.getPhoneAllowance()), String.valueOf(emp.getClothingAllowance()),
                String.valueOf(emp.getHourlyRate()), emp.getRole()
        } : new String[19];

        for (int i = 0; i < labels.length; i++) {
            form.add(new JLabel(labels[i]));
            JTextField field = new JTextField(defaults != null && defaults[i] != null ? defaults[i] : "");
            field.setName("field_" + i);
            if (emp != null && i == 0) field.setEditable(false); // can't change ID on edit
            form.add(field);
        }
        return form;
    }

    private Employee extractEmployeeFromForm(JPanel form) {
        try {
            Component[] components = form.getComponents();
            String[] values = new String[19];
            int idx = 0;
            for (Component comp : components) {
                if (comp instanceof JTextField) {
                    values[idx++] = ((JTextField) comp).getText().trim();
                }
            }
            String role = values[18];
            double salary = Double.parseDouble(values[13]);
            double rice = Double.parseDouble(values[14]);
            double phone = Double.parseDouble(values[15]);
            double cloth = Double.parseDouble(values[16]);
            double rate = Double.parseDouble(values[17]);

            if (role.equals("Admin")) {
                return new AdminEmployee(values[0], values[1], values[2], values[3], values[4],
                        values[5], values[6], values[7], values[8], values[9], values[10],
                        values[11], values[12], salary, rice, phone, cloth, rate);
            } else if (role.equals("HR")) {
                return new HREmployee(values[0], values[1], values[2], values[3], values[4],
                        values[5], values[6], values[7], values[8], values[9], values[10],
                        values[11], values[12], salary, rice, phone, cloth, rate);
            } else if (role.equals("Finance")) {
                return new FinanceEmployee(values[0], values[1], values[2], values[3], values[4],
                        values[5], values[6], values[7], values[8], values[9], values[10],
                        values[11], values[12], salary, rice, phone, cloth, rate);
            } else if (role.equals("IT")) {
                return new ITEmployee(values[0], values[1], values[2], values[3], values[4],
                        values[5], values[6], values[7], values[8], values[9], values[10],
                        values[11], values[12], salary, rice, phone, cloth, rate);
            } else {
                return new BasicEmployee(values[0], values[1], values[2], values[3], values[4],
                        values[5], values[6], values[7], values[8], values[9], values[10],
                        values[11], values[12], salary, rice, phone, cloth, rate);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Salary and rate fields must be numeric.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
