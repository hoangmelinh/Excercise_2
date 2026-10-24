package com.example.excercise2.service;
import android.content.Context;
import com.example.excercise2.model.Employee;
import com.example.excercise2.model.Salary;
import com.example.excercise2.model.WorkLog;
import com.example.excercise2.repository.EmployeeRepository;
import com.example.excercise2.repository.SalaryRepository;
import com.example.excercise2.repository.WorkLogRepository;
import java.util.List;
// Xử lý toàn bộ nghiệp vụ tính lương tháng và làm thêm giờ (Overtime)
public class SalaryService {
    // Giờ làm việc tiêu chuẩn của 1 ngày công
    public static final double STANDARD_DAILY_HOURS = 8.0;
    // Hệ số lương tăng ca x1.5 (150% đơn giá cơ bản)
    public static final double OVERTIME_RATE = 1.5;
    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkLogRepository workLogRepository;
    public SalaryService(Context context) {
        this.salaryRepository = new SalaryRepository(context);
        this.employeeRepository = new EmployeeRepository(context);
        this.workLogRepository = new WorkLogRepository(context);
    }
    // Constructor hỗ trợ Dependency Injection và Unit Testing
    public SalaryService(SalaryRepository salaryRepo, EmployeeRepository employeeRepo, WorkLogRepository workLogRepo) {
        this.salaryRepository = salaryRepo;
        this.employeeRepository = employeeRepo;
        this.workLogRepository = workLogRepo;
    }
    // Tính toán lương tháng tự động cho 1 nhân viên dựa trên nhật ký chấm công
    public ServiceResult<Salary> calculateMonthlySalary(int employeeId, int month, int year) {
        // 1. Kiểm tra tính hợp lệ của tham số đầu vào
        if (employeeId <= 0 || month < 1 || month > 12 || year < 2000) {
            return ServiceResult.error("Thông tin tháng năm hoặc mã nhân viên không hợp lệ!");
        }
        // 2. Kiểm tra tồn tại của hồ sơ nhân viên
        Employee employee = employeeRepository.getEmployeeById(employeeId);
        if (employee == null) {
            return ServiceResult.error("Nhân viên không tồn tại trong hệ thống!");
        }
        if (employee.getHourlyRate() <= 0) {
            return ServiceResult.error("Nhân viên " + employee.getFullname() + " chưa được cấu hình đơn giá lương theo giờ!");
        }
        // 3. Lấy dữ liệu nhật ký chấm công trong tháng
        List<WorkLog> logs = workLogRepository.getWorkLogsByMonth(employeeId, month, year);
        if (logs.isEmpty()) {
            return ServiceResult.error("Nhân viên " + employee.getFullname() + " chưa có dữ liệu chấm công tháng " + month + "/" + year + "!");
        }
        // 4. Quét từng ca làm để bóc tách giờ thường và giờ tăng ca (> 8h/ngày)
        double totalRegularHours = 0.0;
        double totalOvertimeHours = 0.0;
        for (WorkLog log : logs) {
            double dayHours = log.getTotalHours();
            if (dayHours <= 0) continue; // Bỏ qua ca làm chưa hoàn thành
            if (dayHours > STANDARD_DAILY_HOURS) {
                totalRegularHours += STANDARD_DAILY_HOURS;
                totalOvertimeHours += (dayHours - STANDARD_DAILY_HOURS);
            } else {
                totalRegularHours += dayHours;
            }
        }
        // Làm tròn số giờ đến 1 chữ số thập phân
        totalRegularHours = Math.round(totalRegularHours * 10.0) / 10.0;
        totalOvertimeHours = Math.round(totalOvertimeHours * 10.0) / 10.0;
        double totalHours = Math.round((totalRegularHours + totalOvertimeHours) * 10.0) / 10.0;
        // 5. Áp dụng công thức tính tiền lương
        // Lương giờ thường = regularHours * hourlyRate
        // Lương tăng ca = overtimeHours * hourlyRate * 1.5
        double regularSalary = totalRegularHours * employee.getHourlyRate();
        double overtimeSalary = totalOvertimeHours * employee.getHourlyRate() * OVERTIME_RATE;
        double totalSalary = Math.round(regularSalary + overtimeSalary);

        // 6. Đóng gói đối tượng Salary và lưu vào CSDL qua Repository
        Salary salary = new Salary(
                employeeId,
                month,
                year,
                totalHours,
                totalOvertimeHours,
                totalSalary,
                Salary.STATUS_CALCULATED
        );
        salary.setEmployeeCode(employee.getCode());
        salary.setEmployeeName(employee.getFullname());
        salary.setHourlyRate(employee.getHourlyRate());

        boolean isSaved = salaryRepository.saveOrUpdateSalary(salary);
        if (!isSaved) {
            return ServiceResult.error("Lỗi khi lưu bảng lương vào cơ sở dữ liệu!");
        }

        return ServiceResult.success(salary, "Đã tính lương tháng " + month + "/" + year + " thành công!");
    }

    // Tính toán lương tự động hàng loạt cho toàn bộ nhân viên công ty (Batch Calculation 1 chạm)
    public ServiceResult<Integer> batchCalculateAllEmployees(int month, int year) {
        if (month < 1 || month > 12 || year < 2000) {
            return ServiceResult.error("Tháng hoặc năm tính lương không hợp lệ!");
        }

        List<Employee> allEmployees = employeeRepository.getAllEmployees();
        if (allEmployees.isEmpty()) {
            return ServiceResult.error("Chưa có nhân viên nào trong hệ thống!");
        }

        int successCount = 0;
        for (Employee emp : allEmployees) {
            ServiceResult<Salary> result = calculateMonthlySalary(emp.getId(), month, year);
            if (result.isSuccess()) {
                successCount++;
            }
        }

        if (successCount == 0) {
            return ServiceResult.error("Không tìm thấy dữ liệu chấm công nào của các nhân viên trong tháng " + month + "/" + year + "!");
        }

        return ServiceResult.success(successCount, "Đã tự động tính lương thành công cho " + successCount + " nhân viên!");
    }

    // Xác nhận chi trả lương cho nhân viên (chuyển trạng thái sang Paid)
    public ServiceResult<Boolean> markSalaryAsPaid(int salaryId) {
        if (salaryId <= 0) {
            return ServiceResult.error("Mã phiếu lương không hợp lệ!");
        }

        boolean ok = salaryRepository.updateSalaryStatus(salaryId, Salary.STATUS_PAID);
        if (ok) {
            return ServiceResult.success(true, "Đã xác nhận chi trả lương thành công!");
        }
        return ServiceResult.error("Không thể cập nhật trạng thái phiếu lương!");
    }
}
