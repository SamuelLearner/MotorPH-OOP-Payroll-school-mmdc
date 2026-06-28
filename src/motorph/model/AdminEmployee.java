package motorph.model;

import java.util.ArrayList;
import java.util.List;

// Admin role — has full access to all system features.
public class AdminEmployee extends Employee {

    public AdminEmployee(String employeeId, String firstName, String lastName,
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

    public AdminEmployee(String employeeId, String firstName, String lastName,
                         double basicSalary, double hourlyRate) {
        super(employeeId, firstName, lastName, basicSalary, hourlyRate);
    }

    @Override
    public String getRole() { return "Admin"; }

    @Override
    public List<String> getPermissions() {
        List<String> permissions = new ArrayList<String>();
        permissions.add("EMPLOYEE_VIEW");
        permissions.add("EMPLOYEE_ADD");
        permissions.add("EMPLOYEE_EDIT");
        permissions.add("EMPLOYEE_DELETE");
        permissions.add("PAYROLL_PROCESS");
        permissions.add("PAYSLIP_VIEW");
        permissions.add("ATTENDANCE_VIEW");
        permissions.add("ATTENDANCE_MANAGE");
        permissions.add("LEAVE_SUBMIT");
        permissions.add("LEAVE_APPROVE");
        return permissions;
    }
}
