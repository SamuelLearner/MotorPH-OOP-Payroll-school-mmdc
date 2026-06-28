package motorph.util;

import motorph.model.Employee;
import motorph.model.GovernmentContribution;
import java.util.ArrayList;
import java.util.HashMap;

// Utility class for payroll computations.
// Demonstrates method Overloading.
public class PayrollCalculator {

    private ArrayList<GovernmentContribution> contributions;

    public PayrollCalculator(ArrayList<GovernmentContribution> contributions) {
        this.contributions = contributions;
    }

    // Computes gross pay from hours and rate — Overloaded method #1
    public double computeGrossPay(double hoursWorked, double hourlyRate) {
        if (hoursWorked < 0 || hourlyRate < 0) return 0;
        return hoursWorked * hourlyRate;
    }

    // Computes gross pay from Employee and hours — Overloaded method #2
    public double computeGrossPay(Employee employee, double hoursWorked) {
        if (employee == null || hoursWorked < 0) return 0;
        return hoursWorked * employee.getHourlyRate();
    }

    // Computes monthly gross pay from Employee (Basic Salary + Allowances) — Overloaded method #3
    // Per rubric: Gross Salary = Basic Salary + Allowances
    public double computeGrossPay(Employee employee) {
        if (employee == null) return 0;
        return employee.getBasicSalary() + employee.getRiceSubsidy()
                + employee.getPhoneAllowance() + employee.getClothingAllowance();
    }

    // Computes total deductions using polymorphic calculate() calls.
    // Iterates through List<GovernmentContribution> — Polymorphism in action.
// Returns a map of contribution name to amount.
    public HashMap<String, Double> computeDeductions(double grossPay) {
        HashMap<String, Double> deductionMap = new HashMap<String, Double>();

        // Compute SSS, PhilHealth, PagIBIG first
        double sss = 0, philhealth = 0, pagibig = 0;
        for (GovernmentContribution gc : contributions) {
            String name = gc.getContributionName();
            if (!name.equals("Withholding Tax")) {
                double amount = gc.calculate(grossPay);
                deductionMap.put(name, amount);
                if (name.equals("SSS")) sss = amount;
                else if (name.equals("PhilHealth")) philhealth = amount;
                else if (name.equals("Pag-IBIG")) pagibig = amount;
            }
        }

        // Withholding tax is computed on taxableIncome = gross - SSS - PhilHealth - PagIBIG
        double taxableIncome = grossPay - sss - philhealth - pagibig;
        for (GovernmentContribution gc : contributions) {
            if (gc.getContributionName().equals("Withholding Tax")) {
                double tax = gc.calculate(taxableIncome);
                deductionMap.put("Withholding Tax", tax);
            }
        }

        return deductionMap;
    }

    // Computes total deduction amount from deduction map.
    public double computeTotalDeductions(HashMap<String, Double> deductions) {
        double total = 0;
        for (Double val : deductions.values()) {
            total += val;
        }
        return total;
    }

    // Computes net pay.
    public double computeNetPay(double grossPay, double totalDeductions) {
        return grossPay - totalDeductions;
    }
}
