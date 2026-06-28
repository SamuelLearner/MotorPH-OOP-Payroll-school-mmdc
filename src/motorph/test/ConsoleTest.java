package motorph.test;

import motorph.model.*;
import motorph.service.*;
import motorph.util.PayrollCalculator;
import motorph.dao.*;
import java.util.ArrayList;
import java.util.HashMap;

// Console Test Class — verifies all backend computations before GUI integration
// Required by Coursera Task 2: "Console Testing Before Touching the GUI"
public class ConsoleTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  MotorPH Backend Console Tests");
        System.out.println("========================================\n");

        testEncapsulation();
        testInheritanceAndPolymorphism();
        testSSSContribution();
        testPhilHealthContribution();
        testPagIbigContribution();
        testWithholdingTax();
        testPayrollCalculator();
        testMonthlyGrossPay();
        testAttendanceRecord();
        testAuthenticationService();
        testEmployeeService();
        testPayrollService();

        System.out.println("\n========================================");
        System.out.println("  RESULTS: " + passed + " passed, " + failed + " failed");
        System.out.println("========================================");
    }

    // Test 1: Encapsulation — validated setters reject invalid data
    private static void testEncapsulation() {
        System.out.println("[Test] Encapsulation — Validated Setters");
        BasicEmployee emp = new BasicEmployee("T001", "Test", "User", 25000, 148.81);

        emp.setBasicSalary(-5000);
        check("Salary rejects negative", emp.getBasicSalary() == 0);

        emp.setBasicSalary(30000);
        check("Salary accepts positive", emp.getBasicSalary() == 30000);

        emp.setHourlyRate(-10);
        check("HourlyRate rejects negative", emp.getHourlyRate() == 0);

        emp.setHourlyRate(200);
        check("HourlyRate accepts positive", emp.getHourlyRate() == 200);
        System.out.println();
    }

    // Test 2: Inheritance + Polymorphism — role overrides
    private static void testInheritanceAndPolymorphism() {
        System.out.println("[Test] Inheritance & Polymorphism — Role Overrides");
        Employee admin = new AdminEmployee("A01", "Admin", "User", 90000, 535.71);
        Employee hr = new HREmployee("H01", "HR", "User", 52670, 313.51);
        Employee finance = new FinanceEmployee("F01", "Finance", "User", 52670, 313.51);
        Employee it = new ITEmployee("I01", "IT", "User", 42975, 255.80);
        Employee basic = new BasicEmployee("E01", "Basic", "User", 24000, 142.86);

        check("Admin getRole()", admin.getRole().equals("Admin"));
        check("HR getRole()", hr.getRole().equals("HR"));
        check("Finance getRole()", finance.getRole().equals("Finance"));
        check("IT getRole()", it.getRole().equals("IT"));
        check("Employee getRole()", basic.getRole().equals("Employee"));

        // RBAC permission checks
        check("Admin has PAYROLL_PROCESS", admin.getPermissions().contains("PAYROLL_PROCESS"));
        check("HR lacks PAYROLL_PROCESS", !hr.getPermissions().contains("PAYROLL_PROCESS"));
        check("Finance has PAYROLL_PROCESS", finance.getPermissions().contains("PAYROLL_PROCESS"));
        check("HR has LEAVE_APPROVE", hr.getPermissions().contains("LEAVE_APPROVE"));
        check("Basic lacks LEAVE_APPROVE", !basic.getPermissions().contains("LEAVE_APPROVE"));
        System.out.println();
    }

    // Test 3: SSS Contribution
    private static void testSSSContribution() {
        System.out.println("[Test] SSS Contribution — Bracket Lookup");
        SSSContribution sss = new SSSContribution();

        check("SSS for 24750 = 1125.00", sss.calculate(24750) == 1125.00);
        check("SSS for 22500 = 1012.50", sss.calculate(22500) == 1012.50);
        check("SSS for 15000 = 675.00", sss.calculate(15000) == 675.00);
        check("SSS for 3000 = 135.00 (lowest)", sss.calculate(3000) == 135.00);
        System.out.println();
    }

    // Test 4: PhilHealth Contribution
    private static void testPhilHealthContribution() {
        System.out.println("[Test] PhilHealth Contribution — Percentage Based");
        PhilHealthContribution phil = new PhilHealthContribution();

        // PhilHealth = (salary * 5%) / 2 — employee share
        double result = phil.calculate(30000);
        check("PhilHealth for 30000 = 750.00", Math.abs(result - 750.00) < 0.01);

        result = phil.calculate(5000); // below min, uses 10000
        check("PhilHealth for 5000 (below min) = 250.00", Math.abs(result - 250.00) < 0.01);

        result = phil.calculate(200000); // above max, uses 100000
        check("PhilHealth for 200000 (above max) = 2500.00", Math.abs(result - 2500.00) < 0.01);
        System.out.println();
    }

    // Test 5: Pag-IBIG Contribution
    private static void testPagIbigContribution() {
        System.out.println("[Test] Pag-IBIG Contribution — Tiered Rate");
        PagIbigContribution pagibig = new PagIbigContribution();

        check("PagIBIG for 1000 = 10.00 (1%)", Math.abs(pagibig.calculate(1000) - 10.00) < 0.01);
        check("PagIBIG for 5000 = 100.00 (2%)", Math.abs(pagibig.calculate(5000) - 100.00) < 0.01);
        check("PagIBIG for 50000 = 200.00 (capped)", Math.abs(pagibig.calculate(50000) - 200.00) < 0.01);
        System.out.println();
    }

    // Test 6: Withholding Tax
    private static void testWithholdingTax() {
        System.out.println("[Test] Withholding Tax — BIR Brackets");
        WithholdingTax tax = new WithholdingTax();

        check("Tax for 20000 = 0 (exempt)", Math.abs(tax.calculate(20000) - 0) < 0.01);
        check("Tax for 25000 = 625.05", Math.abs(tax.calculate(25000) - ((25000 - 20833) * 0.15)) < 0.01);
        System.out.println();
    }

    // Test 7: PayrollCalculator — Overloaded computeGrossPay
    private static void testPayrollCalculator() {
        System.out.println("[Test] PayrollCalculator — Overloaded computeGrossPay()");
        ArrayList<GovernmentContribution> contribs = new ArrayList<GovernmentContribution>();
        contribs.add(new SSSContribution());
        contribs.add(new PhilHealthContribution());
        contribs.add(new PagIbigContribution());
        contribs.add(new WithholdingTax());
        PayrollCalculator calc = new PayrollCalculator(contribs);

        // Overload 1: hours x rate
        double gross1 = calc.computeGrossPay(168.0, 142.86);
        check("Overload 1: computeGrossPay(168, 142.86) = 24000.48", Math.abs(gross1 - 24000.48) < 0.01);

        // Overload 2: Employee x hours
        BasicEmployee emp = new BasicEmployee("T01", "Test", "User", 24000, 142.86);
        double gross2 = calc.computeGrossPay(emp, 168.0);
        check("Overload 2: computeGrossPay(emp, 168) = 24000.48", Math.abs(gross2 - 24000.48) < 0.01);

        // Overload 3: Employee only (monthly)
        BasicEmployee emp2 = new BasicEmployee("T02", "Full", "Employee",
                "1990-01-01", "Address", "09170000000",
                "33-1234567-8", "12-123456789-0", "123-456-789-000", "1234-5678-9012",
                "Regular", "Staff", "Manager", 24000, 1500, 500, 1000, 142.86);
        double gross3 = calc.computeGrossPay(emp2);
        check("Overload 3: computeGrossPay(emp) = 27000 (salary+allowances)", Math.abs(gross3 - 27000) < 0.01);
        System.out.println();
    }

    // Test 8: Monthly Gross Pay formula (per rubric)
    private static void testMonthlyGrossPay() {
        System.out.println("[Test] Monthly Gross Pay — Rubric: Basic + Allowances");
        BasicEmployee emp = new BasicEmployee("M01", "Monthly", "Test",
                "1990-05-20", "456 Oak Ave", "09181234567",
                "33-2345678-9", "12-234567890-1", "234-567-890-000", "2345-6789-0123",
                "Regular", "HR Manager", "Boss", 52670, 1500, 1500, 1000, 313.51);

        PayrollService service = new PayrollService();
        Payslip payslip = service.generateMonthlyPayslip(emp, "Test Period");

        double expectedGross = 52670 + 1500 + 1500 + 1000; // = 56670
        check("Monthly gross = 56670", Math.abs(payslip.getGrossPay() - expectedGross) < 0.01);
        check("Net pay > 0", payslip.getNetPay() > 0);
        check("Net pay < gross", payslip.getNetPay() < payslip.getGrossPay());
        System.out.println();
    }

    // Test 9: Attendance Record — hours computation
    private static void testAttendanceRecord() {
        System.out.println("[Test] AttendanceRecord — Hours Computation");

        AttendanceRecord onTime = new AttendanceRecord("E01", "2024-01-15", "8:00", "17:30");
        check("On time (8:00-17:30) = 8.5 hrs", Math.abs(onTime.computeDailyHours() - 8.5) < 0.01);

        AttendanceRecord withinGrace = new AttendanceRecord("E02", "2024-01-15", "8:05", "17:30");
        check("Within grace (8:05-17:30) = 8.5 hrs", Math.abs(withinGrace.computeDailyHours() - 8.5) < 0.01);

        AttendanceRecord late = new AttendanceRecord("E03", "2024-01-15", "8:15", "17:30");
        check("Late (8:15-17:30) = 8.25 hrs", Math.abs(late.computeDailyHours() - 8.25) < 0.01);
        System.out.println();
    }

    // Test 10: Authentication Service
    private static void testAuthenticationService() {
        System.out.println("[Test] AuthenticationService — Login Validation");
        UserCredentialsDAO credDAO = new UserCredentialsDAO();
        EmployeeDAO empDAO = new EmployeeDAO();
        AuthenticationService auth = new AuthenticationService(credDAO, empDAO);

        Employee valid = auth.authenticate("10001", "password123");
        check("Valid login returns employee", valid != null);
        check("Valid login correct role", valid != null && valid.getRole().equals("Admin"));

        Employee invalid = auth.authenticate("10001", "wrongpass");
        check("Wrong password returns null", invalid == null);

        Employee noExist = auth.authenticate("99999", "abc");
        check("Non-existent ID returns null", noExist == null);
        System.out.println();
    }

    // Test 11: Employee Service — CRUD
    private static void testEmployeeService() {
        System.out.println("[Test] EmployeeService — Validation Checks");
        EmployeeDAO empDAO = new EmployeeDAO();
        UserCredentialsDAO credDAO = new UserCredentialsDAO();
        EmployeeService service = new EmployeeService(empDAO, credDAO);

        // Search by ID — overloaded method #1
        Employee found1 = service.searchEmployee("10001");
        check("searchEmployee(ID) finds 10001", found1 != null);

        // Search by name — overloaded method #2
        Employee found2 = service.searchEmployee("Manuel III", "Garcia");
        check("searchEmployee(first, last) finds Manuel", found2 != null);

        // Validation: duplicate ID (using full constructor with valid gov't IDs)
        BasicEmployee dup = new BasicEmployee("10001", "Dup", "Test",
                "1990-01-01", "Address", "09170000000",
                "33-1111111-1", "12-111111111-1", "111-111-111-111", "1111-1111-1111",
                "Regular", "Staff", "Manager", 20000, 0, 0, 0, 100);
        String err = service.addEmployee(dup);
        check("Rejects duplicate ID", err != null && err.contains("already exists"));
        System.out.println();
    }

    // Test 12: Payroll Service — full pipeline
    private static void testPayrollService() {
        System.out.println("[Test] PayrollService — Full Pipeline");
        PayrollService service = new PayrollService();

        BasicEmployee emp = new BasicEmployee("P01", "Payroll", "Test",
                "1993-07-08", "654 Maple St", "09211234567",
                "33-5678901-2", "12-567890123-4", "567-890-123-000", "5678-9012-3456",
                "Regular", "Account Manager", "Boss", 34125, 1500, 800, 1000, 203.13);

        // Test hourly payslip
        Payslip hourly = service.generatePayslip(emp, 168.0, "Test Period");
        check("Hourly gross > 0", hourly.getGrossPay() > 0);
        check("Hourly deductions > 0", hourly.getTotalDeductions() > 0);
        check("Hourly net < gross", hourly.getNetPay() < hourly.getGrossPay());

        // Test monthly payslip
        Payslip monthly = service.generateMonthlyPayslip(emp, "Test Period");
        double expectedGross = 34125 + 1500 + 800 + 1000;
        check("Monthly gross = " + expectedGross, Math.abs(monthly.getGrossPay() - expectedGross) < 0.01);
        check("Monthly deductions > 0", monthly.getTotalDeductions() > 0);

        // Display a sample payslip
        System.out.println("\n--- Sample Payslip ---");
        System.out.println(monthly.generatePayslip());
    }

    // Helper: check result and print pass/fail
    private static void check(String testName, boolean condition) {
        if (condition) {
            System.out.println("  PASS: " + testName);
            passed++;
        } else {
            System.out.println("  FAIL: " + testName);
            failed++;
        }
    }
}
