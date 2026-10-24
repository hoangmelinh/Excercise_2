package com.example.excercise2.utils;

import android.content.Context;
import android.content.SharedPreferences;

// Quản lý phiên đăng nhập người dùng bằng SharedPreferences
public class SharedPrefManager {
    private static final String PREF_NAME = "employee_salary_pref";
    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_EMPLOYEE_ID = "employee_id";
    private static final String KEY_FULLNAME = "fullname";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static SharedPrefManager instance;
    private final SharedPreferences sharedPreferences;

    // Khởi tạo Singleton dùng ApplicationContext chống Memory Leak
    private SharedPrefManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Lấy thể hiện duy nhất của SharedPrefManager
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    // Lưu thông tin phiên đăng nhập
    public void saveSession(int accountId, String username, String role, int employeeId, String fullname) {
        sharedPreferences.edit()
                .putInt(KEY_ACCOUNT_ID, accountId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_ROLE, role)
                .putInt(KEY_EMPLOYEE_ID, employeeId)
                .putString(KEY_FULLNAME, fullname)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    // Kiểm tra người dùng đã đăng nhập chưa
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Kiểm tra người dùng hiện tại có phải Admin không
    public boolean isAdmin() {
        return "ADMIN".equals(sharedPreferences.getString(KEY_ROLE, ""));
    }

    public int getAccountId() { return sharedPreferences.getInt(KEY_ACCOUNT_ID, -1); }
    public String getUsername() { return sharedPreferences.getString(KEY_USERNAME, ""); }
    public String getRole() { return sharedPreferences.getString(KEY_ROLE, ""); }
    public int getEmployeeId() { return sharedPreferences.getInt(KEY_EMPLOYEE_ID, -1); }
    public String getFullname() { return sharedPreferences.getString(KEY_FULLNAME, ""); }

    // Xóa phiên đăng nhập khi đăng xuất
    public void logout() {
        sharedPreferences.edit().clear().apply();
    }
}
