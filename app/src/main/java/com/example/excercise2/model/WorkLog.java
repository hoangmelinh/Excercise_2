package com.example.excercise2.model;

// Thực thể nhật ký chấm công và giờ làm việc hàng ngày
public class WorkLog {
    public static final String STATUS_WORKING = "Working";
    public static final String STATUS_COMPLETED = "Completed";

    private int id;
    private int employeeId;
    private String workDate;
    private String checkInTime;
    private String checkOutTime;
    private double totalHours;
    private String status;

    // Khởi tạo đối tượng rỗng
    public WorkLog() {}

    // Khởi tạo khi nhân viên check-in vào ca
    public WorkLog(int employeeId, String workDate, String checkInTime, String status) {
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = null;
        this.totalHours = 0.0;
        this.status = status;
    }

    // Khởi tạo đầy đủ thông tin khi đọc từ CSDL
    public WorkLog(int id, int employeeId, String workDate, String checkInTime, String checkOutTime, double totalHours, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.totalHours = totalHours;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getWorkDate() { return workDate; }
    public void setWorkDate(String workDate) { this.workDate = workDate; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }

    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double totalHours) { this.totalHours = totalHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
