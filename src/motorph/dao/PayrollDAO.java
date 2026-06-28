package motorph.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import motorph.model.Payslip;
import motorph.util.DatabaseConnection;

public class PayrollDAO {

    /**
     * Accepts the UI's runtime memory calculation object 
     * and saves it down to the database schema.
     */
    public void persistCalculation(Payslip payslip, int payPeriodId) {
        int empId = Integer.parseInt(payslip.getEmployee().getEmployeeId());
        
        // 1. Define a clear-out query to drop matching duplicate tests
        String deleteSql = "DELETE FROM payroll WHERE employee_id = ? AND pay_period_id = ?";
        String insertSql = "INSERT INTO payroll (employee_id, pay_period_id, gross_pay, basic_pay, total_allowance, total_deduction, net_pay) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Enforce transaction auto-commit configurations
            conn.setAutoCommit(false);
            
            // 2. Clear out any older records for this cycle first
            try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                delStmt.setInt(1, empId);
                delStmt.setInt(2, payPeriodId);
                delStmt.executeUpdate();
            }

            // 3. Insert the fresh calculations
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, empId);
                pstmt.setInt(2, payPeriodId);
                pstmt.setDouble(3, payslip.getGrossPay());
                pstmt.setDouble(4, payslip.getEmployee().getBasicSalary());
                
                double calculatedAllowances = payslip.getGrossPay() - payslip.getEmployee().getBasicSalary();
                pstmt.setDouble(5, Math.max(0, calculatedAllowances)); 
                
                pstmt.setDouble(6, payslip.getTotalDeductions());
                pstmt.setDouble(7, payslip.getNetPay());
                
                pstmt.executeUpdate();
            }
            
            conn.commit();
            System.out.println("Payroll record updated cleanly (old duplicate overwritten).");
            
        } catch (SQLException e) {
            System.out.println("DAO Error during execution handling: " + e.getMessage());
        }
    }
}