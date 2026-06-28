package motorph.service;

import motorph.dao.AttendanceDAO;
import motorph.model.AttendanceRecord;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// Service for attendance operations (time in, time out, view records).
public class AttendanceService {

    private AttendanceDAO attendanceDAO;

    public AttendanceService(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
    }

    // Records time-in for an employee.
    public void timeIn(String employeeId) {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        AttendanceRecord record = new AttendanceRecord(employeeId, date, time, "");
        attendanceDAO.addRecord(record);
    }

    // Records time-out for an employee (updates today's record).
    public void timeOut(String employeeId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        attendanceDAO.updateTimeOut(employeeId, today, time);
    }

    // Gets all attendance records for an employee.
    public ArrayList<AttendanceRecord> getRecords(String employeeId) {
        return attendanceDAO.findByEmployeeId(employeeId);
    }

    // Gets all attendance records.
    public ArrayList<AttendanceRecord> getAllRecords() {
        return attendanceDAO.loadAll();
    }
}
