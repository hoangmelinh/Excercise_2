package com.example.excercise2.model;

// Thực thể hồ sơ nhân viên và mức lương theo giờ
public class Employee {
    private int id;
    private String code;
    private String fullname;
    private String email;
    private String phone;
    private double hourlyRate;
    private int accountId;

    // Khởi tạo đối tượng rỗng
    public Employee() {}

    // Khởi tạo nhân viên mới khi thêm vào CSDL
    public Employee(String code, String fullname, String email, String phone, double hourlyRate, int accountId) {
        this.code = code;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.hourlyRate = hourlyRate;
        this.accountId = accountId;
    }

    // Khởi tạo nhân viên đầy đủ dữ liệu đọc từ CSDL
    public Employee(int id, String code, String fullname, String email, String phone, double hourlyRate, int accountId) {
        this.id = id;
        this.code = code;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.hourlyRate = hourlyRate;
        this.accountId = accountId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
}
