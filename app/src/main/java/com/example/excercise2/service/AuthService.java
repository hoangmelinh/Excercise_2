package com.example.excercise2.service;

import android.content.Context;

import com.example.excercise2.model.Account;
import com.example.excercise2.model.Employee;
import com.example.excercise2.repository.AccountRepository;
import com.example.excercise2.repository.EmployeeRepository;
import com.example.excercise2.repository.PreferenceRepository;

// Xử lý nghiệp vụ xác thực đăng nhập và điều phối phiên làm việc
public class AuthService {
    private final AccountRepository accountRepo;
    private final EmployeeRepository employeeRepo;
    private final PreferenceRepository prefRepo;

    // Khởi tạo service với các repository cần thiết
    public AuthService(Context context) {
        this.accountRepo = new AccountRepository(context);
        this.employeeRepo = new EmployeeRepository(context);
        this.prefRepo = new PreferenceRepository(context);
    }

    // Nghiệp vụ đăng nhập và lưu phiên
    public ServiceResult<Account> login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return ServiceResult.error("Vui lòng nhập tên đăng nhập!");
        }
        if (password == null || password.trim().isEmpty()) {
            return ServiceResult.error("Vui lòng nhập mật khẩu!");
        }

        Account account = accountRepo.login(username.trim(), password.trim());
        if (account == null) {
            return ServiceResult.error("Tên đăng nhập hoặc mật khẩu không chính xác!");
        }

        int employeeId = -1;
        String fullname = "Quản trị viên";

        if (Account.ROLE_EMPLOYEE.equals(account.getRole())) {
            Employee employee = employeeRepo.getEmployeeByAccountId(account.getId());
            if (employee != null) {
                employeeId = employee.getId();
                fullname = employee.getFullname();
            }
        }

        prefRepo.saveSession(account.getId(), account.getUsername(), account.getRole(), employeeId, fullname);
        return ServiceResult.success(account, "Đăng nhập thành công!");
    }

    // Đăng xuất và xóa phiên đăng nhập
    public void logout() {
        prefRepo.clearSession();
    }

    // Lấy thông tin tài khoản đang đăng nhập hiện tại
    public PreferenceRepository getCurrentSession() {
        return prefRepo;
    }
}
