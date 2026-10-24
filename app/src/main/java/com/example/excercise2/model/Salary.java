package com.example.excercise2.model;

// Thực thể bảng lương tháng và chi tiết làm thêm giờ
public class Salary {
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_CALCULATED = "Calculated";
    public static final String STATUS_PAID = "Paid";

    public static final double OVERTIME_RATE = 1.5;
    public static final double STANDARD_DAILY_HOURS = 8.0;

    private int id;
    private int employeeId;
    private int month;
    private int year;
    private double totalHours;
    private double overtimeHours;
    private double totalSalary;
    private String status;

    // Khởi tạo đối tượng rỗng
    public Salary() {}

    // Khởi tạo bảng lương khi tính toán mới
    public Salary(int employeeId, int month, int year, double totalHours, double overtimeHours, double totalSalary, String status) {
        this.employeeId = employeeId;
        this.month = month;
        this.year = year;
        this.totalHours = totalHours;
        this.overtimeHours = overtimeHours;
        this.totalSalary = totalSalary;
        this.status = status;
    }

    // Khởi tạo đầy đủ thông tin khi đọc từ CSDL
    public Salary(int id, int employeeId, int month, int year, double totalHours, double overtimeHours, double totalSalary, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.month = month;
        this.year = year;
        this.totalHours = totalHours;
        this.overtimeHours = overtimeHours;
        this.totalSalary = totalSalary;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double totalHours) { this.totalHours = totalHours; }

    public double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(double overtimeHours) { this.overtimeHours = overtimeHours; }

    public double getTotalSalary() { return totalSalary; }
    public void setTotalSalary(double totalSalary) { this.totalSalary = totalSalary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
