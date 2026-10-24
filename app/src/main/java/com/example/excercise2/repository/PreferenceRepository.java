package com.example.excercise2.repository;

import android.content.Context;

import com.example.excercise2.utils.SharedPrefManager;

// Quản lý lưu trữ phiên người dùng qua SharedPreferences
public class PreferenceRepository {
    private final SharedPrefManager prefManager;

    // Khởi tạo repository lấy thể hiện của SharedPrefManager
    public PreferenceRepository(Context context) {
        this.prefManager = SharedPrefManager.getInstance(context);
    }

    // Lưu phiên làm việc sau khi đăng nhập thành công
    public void saveSession(int accountId, String username, String role, int employeeId, String fullname) {
        prefManager.saveSession(accountId, username, role, employeeId, fullname);
    }

    // Kiểm tra trạng thái đã đăng nhập
    public boolean isLoggedIn() {
        return prefManager.isLoggedIn();
    }

    // Kiểm tra vai trò Admin
    public boolean isAdmin() {
        return prefManager.isAdmin();
    }

    public int getAccountId() { return prefManager.getAccountId(); }
    public String getUsername() { return prefManager.getUsername(); }
    public String getRole() { return prefManager.getRole(); }
    public int getEmployeeId() { return prefManager.getEmployeeId(); }
    public String getFullname() { return prefManager.getFullname(); }

    // Xóa phiên làm việc khi đăng xuất
    public void clearSession() {
        prefManager.logout();
    }
}
