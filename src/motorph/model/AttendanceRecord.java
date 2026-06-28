package motorph.model;

// Represents a single attendance entry (one day) for an employee.
// Handles time-in/time-out and computes daily hours worked.
public class AttendanceRecord implements Exportable {

    private String employeeId;
    private String date;
    private String timeIn;   // format: HH:mm
    private String timeOut;  // format: HH:mm

    public AttendanceRecord(String employeeId, String date, String timeIn, String timeOut) {
        this.employeeId = employeeId;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    // Getters
    public String getEmployeeId() { return employeeId; }
    public String getDate() { return date; }
    public String getTimeIn() { return timeIn; }
    public String getTimeOut() { return timeOut; }

    // Setters
    public void setTimeIn(String timeIn) { this.timeIn = timeIn; }
    public void setTimeOut(String timeOut) { this.timeOut = timeOut; }

    // Checks if the employee logged in after the grace period (8:10 AM).
    private boolean isLate() {
        int inMinutes = parseTimeToMinutes(timeIn);
        int graceMinutes = 8 * 60 + 10; // 8:10 AM = 490 minutes
        return inMinutes > graceMinutes;
    }

    // Calculates hours worked for this day.
    // Grace period: login at or before 8:10 = starts at 8:00.
// Login after 8:10 = starts at actual time in.
// Deducts 1 hour for lunch.
    public double computeDailyHours() {
        if (timeIn == null || timeOut == null || timeIn.isEmpty() || timeOut.isEmpty()) {
            return 0;
        }

        int inMinutes = parseTimeToMinutes(timeIn);
        int outMinutes = parseTimeToMinutes(timeOut);

        // If on time (before or at 8:10), count from 8:00
        int startMinutes = 8 * 60; // 8:00 AM
        if (isLate()) {
            startMinutes = inMinutes; // count from actual login
        }

        int workedMinutes = outMinutes - startMinutes;
        if (workedMinutes < 0) {
            workedMinutes = 0;
        }

        // Deduct 1 hour lunch (60 min) if worked more than 5 hours
        if (workedMinutes > 300) {
            workedMinutes -= 60;
        }

        return workedMinutes / 60.0;
    }

    // Parses time string "HH:mm" or "H:mm" to total minutes from midnight.
    private int parseTimeToMinutes(String time) {
        try {
            String[] parts = time.split(":");
            int hours = Integer.parseInt(parts[0].trim());
            int minutes = Integer.parseInt(parts[1].trim());
            return hours * 60 + minutes;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String toCSVString() {
        return employeeId + "," + date + "," + timeIn + "," + timeOut;
    }
}
