package motorph.model;

import java.util.HashMap;

// Represents a payslip for an employee.
// Compiles gross pay, deductions, and net pay into a displayable format.
public class Payslip implements Displayable {

    private Employee employee;
    private String period;
    private double grossPay;
    private HashMap<String, Double> deductions;
    private double totalDeductions;
    private double netPay;

    public Payslip(Employee employee, String period, double grossPay,
                   HashMap<String, Double> deductions, double netPay) {
        this.employee = employee;
        this.period = period;
        this.grossPay = grossPay;
        this.deductions = deductions;
        this.netPay = netPay;
        this.totalDeductions = 0;
        for (Double val : deductions.values()) {
            this.totalDeductions += val;
        }
    }

    // Getters
    public Employee getEmployee() { return employee; }
    public String getPeriod() { return period; }
    public double getGrossPay() { return grossPay; }
    public HashMap<String, Double> getDeductions() { return deductions; }
    public double getTotalDeductions() { return totalDeductions; }
    public double getNetPay() { return netPay; }

    // Generates a formatted payslip string for display.
    public String generatePayslip() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("         MOTORPH PAYSLIP\n");
        sb.append("========================================\n");
        sb.append("Period      : " + period + "\n");
        sb.append("Employee ID : " + employee.getEmployeeId() + "\n");
        sb.append("Name        : " + employee.getFullName() + "\n");
        sb.append("Position    : " + employee.getPosition() + "\n");
        sb.append("Status      : " + employee.getEmploymentStatus() + "\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("Gross Pay       : PHP %,.2f\n", grossPay));
        sb.append("----------------------------------------\n");
        sb.append("Deductions:\n");
        for (String key : deductions.keySet()) {
            sb.append(String.format("  %-16s: PHP %,.2f\n", key, deductions.get(key)));
        }
        sb.append(String.format("  %-16s: PHP %,.2f\n", "TOTAL", totalDeductions));
        sb.append("----------------------------------------\n");
        sb.append(String.format("Net Pay         : PHP %,.2f\n", netPay));
        sb.append("========================================\n");
        return sb.toString();
    }

    @Override
    public String displayInfo() {
        return generatePayslip();
    }
}
