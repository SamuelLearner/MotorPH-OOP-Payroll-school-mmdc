package motorph.model;

import java.util.ArrayList;
import java.util.List;

// Finance role — can process payroll and generate payslips.
public class FinanceEmployee extends Employee {

    public FinanceEmployee(String employeeId, String firstName, String lastName,
                           String birthday, String address, String phoneNumber,
                           String sssNumber, String philhealthNumber, String tinNumber,
                           String pagibigNumber, String employmentStatus, String position,
                           String supervisor, double basicSalary, double riceSubsidy,
                           double phoneAllowance, double clothingAllowance, double hourlyRate) {
        super(employeeId, firstName, lastName, birthday, address, phoneNumber,
              sssNumber, philhealthNumber, tinNumber, pagibigNumber,
              employmentStatus, position, supervisor, basicSalary, riceSubsidy,
              phoneAllowance, clothingAllowance, hourlyRate);
    }

    public FinanceEmployee(String employeeId, String firstName, String lastName,
                           double basicSalary, double hourlyRate) {
        super(employeeId, firstName, lastName, basicSalary, hourlyRate);
    }

    @Override
    public String getRole() { return "Finance"; }

    @Override
    public List<String> getPermissions() {
        List<String> permissions = new ArrayList<String>();
        permissions.add("EMPLOYEE_VIEW");
        permissions.add("PAYROLL_PROCESS");
        permissions.add("PAYSLIP_VIEW");
        permissions.add("ATTENDANCE_VIEW");
        permissions.add("LEAVE_SUBMIT");
        return permissions;
    }
}
