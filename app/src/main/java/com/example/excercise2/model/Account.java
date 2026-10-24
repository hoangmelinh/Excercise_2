package com.example.excercise2.model;

// Thực thể tài khoản đăng nhập và phân quyền
public class Account {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";

    private int id;
    private String username;
    private String password;
    private String role;

    // Khởi tạo đối tượng rỗng
    public Account() {}

    // Khởi tạo đối tượng khi tạo mới (chưa có ID)
    public Account(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Khởi tạo đối tượng đầy đủ dữ liệu từ CSDL
    public Account(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
