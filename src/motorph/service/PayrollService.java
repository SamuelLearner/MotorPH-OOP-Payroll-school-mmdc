package motorph.service;

import motorph.model.*;
import motorph.util.PayrollCalculator;
import java.util.ArrayList;
import java.util.HashMap;

// Service for payroll processing — uses PayrollCalculator and GovernmentContribution hierarchy.
public class PayrollService {

    private PayrollCalculator calculator;

    public PayrollService() {
        // Build the contribution list — Polymorphism: all stored as GovernmentContribution
        ArrayList<GovernmentContribution> contributions = new ArrayList<GovernmentContribution>();
        contributions.add(new SSSContribution());
        contributions.add(new PhilHealthContribution());
        contributions.add(new PagIbigContribution());
        contributions.add(new WithholdingTax());
        this.calculator = new PayrollCalculator(contributions);
    }

    // Generates a payslip for an employee given hours worked (hourly-based)
    public Payslip generatePayslip(Employee employee, double totalHoursWorked, String period) {
        double grossPay = calculator.computeGrossPay(employee, totalHoursWorked);

        HashMap<String, Double> deductions = calculator.computeDeductions(grossPay);
        double totalDeductions = calculator.computeTotalDeductions(deductions);
        double netPay = calculator.computeNetPay(grossPay, totalDeductions);

        return new Payslip(employee, period, grossPay, deductions, netPay);
    }

    // Generates a monthly payslip — Gross = Basic Salary + Allowances (per rubric)
    public Payslip generateMonthlyPayslip(Employee employee, String period) {
        double grossPay = calculator.computeGrossPay(employee);

        HashMap<String, Double> deductions = calculator.computeDeductions(grossPay);
        double totalDeductions = calculator.computeTotalDeductions(deductions);
        double netPay = calculator.computeNetPay(grossPay, totalDeductions);

        return new Payslip(employee, period, grossPay, deductions, netPay);
    }

    // Computes total hours worked from a list of attendance records
    public double computeTotalHours(ArrayList<AttendanceRecord> records) {
        double total = 0;
        for (AttendanceRecord record : records) {
            total += record.computeDailyHours();
        }
        return total;
    }
}
