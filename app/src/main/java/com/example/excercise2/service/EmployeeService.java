package com.example.excercise2.service;

import android.content.Context;

import com.example.excercise2.model.Account;
import com.example.excercise2.model.Employee;
import com.example.excercise2.repository.AccountRepository;
import com.example.excercise2.repository.EmployeeRepository;

import java.util.List;

// Xử lý logic nghiệp vụ quản lý nhân viên và kiểm tra tính hợp lệ dữ liệu
public class EmployeeService {
    private final EmployeeRepository employeeRepo;
    private final AccountRepository accountRepo;

    // Khởi tạo service với context ứng dụng
    public EmployeeService(Context context) {
        this.employeeRepo = new EmployeeRepository(context);
        this.accountRepo = new AccountRepository(context);
    }

    // Lấy toàn bộ danh sách nhân viên
    public ServiceResult<List<Employee>> getAllEmployees() {
        List<Employee> list = employeeRepo.getAllEmployees();
        return ServiceResult.success(list, "Lấy danh sách nhân viên thành công!");
    }

    // Tìm kiếm nhân viên theo từ khóa (mã hoặc tên)
    public ServiceResult<List<Employee>> searchEmployees(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEmployees();
        }
        List<Employee> list = employeeRepo.searchEmployees(keyword.trim());
        return ServiceResult.success(list, "Tìm kiếm thành công!");
    }

    // Thêm nhân viên mới kèm tài khoản đăng nhập
    public ServiceResult<Employee> createEmployee(String code, String fullname, String email,
                                                   String phone, double hourlyRate,
                                                   String username, String password) {
        String validationError = validateNewEmployee(code, fullname, hourlyRate, username, password);
        if (validationError != null) {
            return ServiceResult.error(validationError);
        }

        // Tạo tài khoản cho nhân viên trước
        Account newAccount = new Account(username.trim(), password.trim(), Account.ROLE_EMPLOYEE);
        long accountId = accountRepo.insertAccount(newAccount);
        if (accountId <= 0) {
            return ServiceResult.error("Không thể tạo tài khoản cho nhân viên!");
        }

        // Tạo hồ sơ nhân viên gắn với accountId vừa tạo
        Employee newEmployee = new Employee(code.trim().toUpperCase(), fullname.trim(),
                email != null ? email.trim() : "",
                phone != null ? phone.trim() : "",
                hourlyRate, (int) accountId);

        long employeeId = employeeRepo.insertEmployee(newEmployee);
        if (employeeId <= 0) {
            accountRepo.deleteAccount((int) accountId);
            return ServiceResult.error("Không thể lưu thông tin nhân viên!");
        }

        newEmployee.setId((int) employeeId);
        return ServiceResult.success(newEmployee, "Thêm nhân viên thành công!");
    }

    // Cập nhật thông tin nhân viên
    public ServiceResult<Void> updateEmployee(Employee employee) {
        if (employee == null) {
            return ServiceResult.error("Dữ liệu nhân viên không hợp lệ!");
        }
        if (employee.getCode() == null || employee.getCode().trim().isEmpty()) {
            return ServiceResult.error("Mã nhân viên không được để trống!");
        }
        if (employee.getFullname() == null || employee.getFullname().trim().isEmpty()) {
            return ServiceResult.error("Họ và tên không được để trống!");
        }
        if (employee.getHourlyRate() <= 0) {
            return ServiceResult.error("Mức lương theo giờ phải lớn hơn 0!");
        }
        if (employeeRepo.isCodeExists(employee.getCode().trim(), employee.getId())) {
            return ServiceResult.error("Mã nhân viên đã tồn tại trên hệ thống!");
        }

        employee.setCode(employee.getCode().trim().toUpperCase());
        employee.setFullname(employee.getFullname().trim());

        boolean updated = employeeRepo.updateEmployee(employee);
        if (!updated) {
            return ServiceResult.error("Cập nhật thông tin thất bại!");
        }
        return ServiceResult.success(null, "Cập nhật nhân viên thành công!");
    }

    // Xóa nhân viên và tài khoản liên quan
    public ServiceResult<Void> deleteEmployee(int employeeId) {
        Employee employee = employeeRepo.getEmployeeById(employeeId);
        if (employee == null) {
            return ServiceResult.error("Nhân viên không tồn tại!");
        }

        boolean deleted = employeeRepo.deleteEmployee(employeeId);
        if (!deleted) {
            return ServiceResult.error("Không thể xóa nhân viên!");
        }

        accountRepo.deleteAccount(employee.getAccountId());
        return ServiceResult.success(null, "Xóa nhân viên thành công!");
    }

    // Kiểm tra tính hợp lệ của dữ liệu nhân viên mới
    private String validateNewEmployee(String code, String fullname, double hourlyRate, String username, String password) {
        if (code == null || code.trim().isEmpty()) return "Mã nhân viên không được để trống!";
        if (fullname == null || fullname.trim().isEmpty()) return "Họ tên không được để trống!";
        if (hourlyRate <= 0) return "Mức lương theo giờ phải lớn hơn 0!";
        if (username == null || username.trim().isEmpty()) return "Tên đăng nhập không được để trống!";
        if (password == null || password.trim().isEmpty()) return "Mật khẩu không được để trống!";
        if (accountRepo.isUsernameExists(username.trim())) return "Tên đăng nhập đã tồn tại!";
        if (employeeRepo.isCodeExists(code.trim(), 0)) return "Mã nhân viên đã tồn tại!";
        return null;
    }
}
