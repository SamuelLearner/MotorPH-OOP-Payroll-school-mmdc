package motorph.model;

import java.util.ArrayList;
import java.util.List;

// Regular employee role — limited access (view own info, submit leave, view payslip).
public class BasicEmployee extends Employee {

    public BasicEmployee(String employeeId, String firstName, String lastName,
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

    public BasicEmployee(String employeeId, String firstName, String lastName,
                         double basicSalary, double hourlyRate) {
        super(employeeId, firstName, lastName, basicSalary, hourlyRate);
    }

    @Override
    public String getRole() { return "Employee"; }

    @Override
    public List<String> getPermissions() {
        List<String> permissions = new ArrayList<String>();
        permissions.add("EMPLOYEE_VIEW");
        permissions.add("ATTENDANCE_VIEW");
        permissions.add("LEAVE_SUBMIT");
        permissions.add("PAYSLIP_VIEW");
        return permissions;
    }
}
