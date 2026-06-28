package motorph.model;

import java.util.List;

// Abstract parent class for all employee types.
// Demonstrates Encapsulation (private fields, validated setters)
// and Abstraction (abstract methods for role-specific behavior).
public abstract class Employee implements Displayable, Exportable {

    private String employeeId;
    private String firstName;
    private String lastName;
    private String birthday;
    private String address;
    private String phoneNumber;
    private String sssNumber;
    private String philhealthNumber;
    private String tinNumber;
    private String pagibigNumber;
    private String employmentStatus;
    private String position;
    private String supervisor;
    private double basicSalary;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    private double hourlyRate;

    // Full constructor
    public Employee(String employeeId, String firstName, String lastName,
                    String birthday, String address, String phoneNumber,
                    String sssNumber, String philhealthNumber, String tinNumber,
                    String pagibigNumber, String employmentStatus, String position,
                    String supervisor, double basicSalary, double riceSubsidy,
                    double phoneAllowance, double clothingAllowance, double hourlyRate) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.sssNumber = sssNumber;
        this.philhealthNumber = philhealthNumber;
        this.tinNumber = tinNumber;
        this.pagibigNumber = pagibigNumber;
        this.employmentStatus = employmentStatus;
        this.position = position;
        this.supervisor = supervisor;
        setBasicSalary(basicSalary);
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
        setHourlyRate(hourlyRate);
    }

    // Overloaded minimal constructor (Polymorphism - Overloading)
    public Employee(String employeeId, String firstName, String lastName,
                    double basicSalary, double hourlyRate) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        setBasicSalary(basicSalary);
        setHourlyRate(hourlyRate);
        this.birthday = "";
        this.address = "";
        this.phoneNumber = "";
        this.sssNumber = "";
        this.philhealthNumber = "";
        this.tinNumber = "";
        this.pagibigNumber = "";
        this.employmentStatus = "Regular";
        this.position = "";
        this.supervisor = "";
        this.riceSubsidy = 0;
        this.phoneAllowance = 0;
        this.clothingAllowance = 0;
    }

    // Abstract methods — subclasses MUST override (Polymorphism - Overriding)
    public abstract String getRole();
    public abstract List<String> getPermissions();

    // Getters — Encapsulation
    public String getEmployeeId() { return employeeId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getBirthday() { return birthday; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getSssNumber() { return sssNumber; }
    public String getPhilhealthNumber() { return philhealthNumber; }
    public String getTinNumber() { return tinNumber; }
    public String getPagibigNumber() { return pagibigNumber; }
    public String getEmploymentStatus() { return employmentStatus; }
    public String getPosition() { return position; }
    public String getSupervisor() { return supervisor; }
    public double getBasicSalary() { return basicSalary; }
    public double getRiceSubsidy() { return riceSubsidy; }
    public double getPhoneAllowance() { return phoneAllowance; }
    public double getClothingAllowance() { return clothingAllowance; }
    public double getHourlyRate() { return hourlyRate; }
    public String getFullName() { return firstName + " " + lastName; }

    // Setters — Encapsulation with validation
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public void setAddress(String address) { this.address = address; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setSssNumber(String sssNumber) { this.sssNumber = sssNumber; }
    public void setPhilhealthNumber(String philhealthNumber) { this.philhealthNumber = philhealthNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }
    public void setPagibigNumber(String pagibigNumber) { this.pagibigNumber = pagibigNumber; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
    public void setPosition(String position) { this.position = position; }
    public void setSupervisor(String supervisor) { this.supervisor = supervisor; }
    public void setRiceSubsidy(double riceSubsidy) { this.riceSubsidy = riceSubsidy; }
    public void setPhoneAllowance(double phoneAllowance) { this.phoneAllowance = phoneAllowance; }
    public void setClothingAllowance(double clothingAllowance) { this.clothingAllowance = clothingAllowance; }

    // Validated setters
    public void setBasicSalary(double salary) {
        if (salary > 0) {
            this.basicSalary = salary;
        } else {
            this.basicSalary = 0;
        }
    }

    public void setHourlyRate(double rate) {
        if (rate > 0) {
            this.hourlyRate = rate;
        } else {
            this.hourlyRate = 0;
        }
    }

    // Displayable interface
    @Override
    public String displayInfo() {
        return "Employee #" + employeeId + " - " + lastName + ", " + firstName
                + " | " + position + " | " + employmentStatus;
    }

    // Exportable interface
    @Override
    public String toCSVString() {
        return String.join(",",
                employeeId, firstName, lastName, birthday, address, phoneNumber,
                sssNumber, philhealthNumber, tinNumber, pagibigNumber,
                employmentStatus, position, supervisor,
                String.valueOf(basicSalary), String.valueOf(riceSubsidy),
                String.valueOf(phoneAllowance), String.valueOf(clothingAllowance),
                String.valueOf(hourlyRate), getRole());
    }
}
