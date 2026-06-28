package motorph.ui;

import motorph.model.Employee;
import motorph.model.LeaveRequest;
import motorph.service.EmployeeService;
import motorph.service.LeaveService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

// Panel for submitting/approving/viewing leave requests.
public class LeaveRequestPanel extends JPanel {

    private LeaveService leaveService;
    private EmployeeService employeeService;
    private Employee currentUser;
    private boolean canApprove;
    private DefaultTableModel tableModel;

    public LeaveRequestPanel(LeaveService leaveService, EmployeeService employeeService,
                             Employee currentUser, boolean canApprove) {
        this.leaveService = leaveService;
        this.employeeService = employeeService;
        this.currentUser = currentUser;
        this.canApprove = canApprove;
        setLayout(new BorderLayout(5, 5));
        initComponents();
        refreshTable();
    }

    private void initComponents() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnSubmit = new JButton("Submit Leave Request");
        btnSubmit.addActionListener(e -> showSubmitDialog());
        controlPanel.add(btnSubmit);

        if (canApprove) {
            JButton btnApprove = new JButton("Approve Selected");
            btnApprove.addActionListener(e -> processSelected("Approved"));
            controlPanel.add(btnApprove);

            JButton btnReject = new JButton("Reject Selected");
            btnReject.addActionListener(e -> processSelected("Rejected"));
            controlPanel.add(btnReject);
        }

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshTable());
        controlPanel.add(btnRefresh);

        add(controlPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Request ID", "Employee ID", "Leave Type", "Start Date",
                "End Date", "Reason", "Status", "Approved By"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        ArrayList<LeaveRequest> requests;
        if (canApprove) {
            requests = leaveService.getAllLeaves();
        } else {
            requests = leaveService.getEmployeeLeaves(currentUser.getEmployeeId());
        }
        for (LeaveRequest lr : requests) {
            tableModel.addRow(new Object[]{
                    lr.getRequestId(), lr.getEmployeeId(), lr.getLeaveType(),
                    lr.getStartDate(), lr.getEndDate(), lr.getReason(),
                    lr.getStatus(), lr.getApprovedBy()
            });
        }
    }

    private void showSubmitDialog() {
        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        String[] leaveTypes = {"Sick Leave", "Vacation Leave", "Emergency Leave"};
        JComboBox<String> cboType = new JComboBox<String>(leaveTypes);
        JTextField txtStart = new JTextField();
        JTextField txtEnd = new JTextField();
        JTextField txtReason = new JTextField();

        form.add(new JLabel("Leave Type:"));
        form.add(cboType);
        form.add(new JLabel("Start Date (yyyy-MM-dd):"));
        form.add(txtStart);
        form.add(new JLabel("End Date (yyyy-MM-dd):"));
        form.add(txtEnd);
        form.add(new JLabel("Reason:"));
        form.add(txtReason);

        int result = JOptionPane.showConfirmDialog(this, form, "Submit Leave Request",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String error = leaveService.submitLeaveRequest(
                    currentUser.getEmployeeId(),
                    (String) cboType.getSelectedItem(),
                    txtStart.getText().trim(),
                    txtEnd.getText().trim(),
                    txtReason.getText().trim());
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Leave request submitted.");
                refreshTable();
            }
        }
    }

    private void processSelected(String status) {
        int row = -1;
        // Find the table in the panel
        for (Component comp : getComponents()) {
            if (comp instanceof JScrollPane) {
                JTable table = (JTable) ((JScrollPane) comp).getViewport().getView();
                row = table.getSelectedRow();
                break;
            }
        }
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a leave request.");
            return;
        }

        String requestId = (String) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 6);
        if (!currentStatus.equals("Pending")) {
            JOptionPane.showMessageDialog(this, "This request has already been processed.");
            return;
        }

        String error = leaveService.processLeaveRequest(requestId, status, currentUser.getEmployeeId());
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Leave request " + status.toLowerCase() + ".");
            refreshTable();
        }
    }
}
