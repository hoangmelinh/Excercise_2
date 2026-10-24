package com.example.excercise2.service;

import android.content.Context;

import com.example.excercise2.model.WorkLog;
import com.example.excercise2.repository.WorkLogRepository;
import com.example.excercise2.utils.DateUtils;

import java.util.List;

// Xử lý logic nghiệp vụ chấm công, kiểm soát dữ liệu và tính giờ làm
public class WorkLogService {
    private final WorkLogRepository workLogRepository;

    // Khởi tạo Service bằng Context
    public WorkLogService(Context context) {
        this.workLogRepository = new WorkLogRepository(context);
    }

    // Khởi tạo Service bằng WorkLogRepository phục vụ kiểm thử
    public WorkLogService(WorkLogRepository workLogRepository) {
        this.workLogRepository = workLogRepository;
    }

    // Thực hiện check-in vào ca với ngày giờ hiện tại
    public ServiceResult<WorkLog> checkIn(int employeeId) {
        return checkIn(employeeId, DateUtils.getCurrentDate(), DateUtils.getCurrentTime());
    }

    // Thực hiện check-in với tham số ngày giờ cụ thể (hỗ trợ test)
    public ServiceResult<WorkLog> checkIn(int employeeId, String workDate, String checkInTime) {
        // Kiểm tra hợp lệ mã nhân viên
        if (employeeId <= 0) {
            return ServiceResult.error("Mã nhân viên không hợp lệ!");
        }

        // Kiểm tra các ca làm việc chưa hoàn thành của ngày trước
        WorkLog incompleteLog = workLogRepository.getIncompleteWorkLogBeforeDate(employeeId, workDate);
        if (incompleteLog != null) {
            return ServiceResult.error("Bạn còn ca làm ngày " + incompleteLog.getWorkDate() + " chưa check-out!");
        }

        // Chặn điểm danh trùng lặp trong cùng một ngày
        WorkLog existingLog = workLogRepository.getWorkLogByDate(employeeId, workDate);
        if (existingLog != null) {
            return ServiceResult.error("Hôm nay bạn đã điểm danh rồi!");
        }

        // Thêm bản ghi check-in mới vào CSDL
        long newId = workLogRepository.insertCheckIn(employeeId, workDate, checkInTime);
        if (newId <= 0) {
            return ServiceResult.error("Không thể ghi nhận điểm danh vào CSDL!");
        }

        WorkLog createdLog = new WorkLog((int) newId, employeeId, workDate, checkInTime, null, 0.0, WorkLog.STATUS_WORKING);
        return ServiceResult.success(createdLog, "Điểm danh vào ca thành công!");
    }

    // Thực hiện check-out kết thúc ca với giờ hiện tại
    public ServiceResult<WorkLog> checkOut(int employeeId) {
        return checkOut(employeeId, DateUtils.getCurrentDate(), DateUtils.getCurrentTime());
    }

    // Thực hiện check-out với tham số ngày giờ cụ thể (hỗ trợ test)
    public ServiceResult<WorkLog> checkOut(int employeeId, String workDate, String checkOutTime) {
        // Kiểm tra hợp lệ mã nhân viên
        if (employeeId <= 0) {
            return ServiceResult.error("Mã nhân viên không hợp lệ!");
        }

        // Lấy thông tin ca làm việc hôm nay
        WorkLog currentLog = workLogRepository.getWorkLogByDate(employeeId, workDate);
        if (currentLog == null) {
            return ServiceResult.error("Bạn chưa điểm danh vào ca hôm nay!");
        }

        // Kiểm tra xem đã hoàn thành ca làm việc chưa
        if (WorkLog.STATUS_COMPLETED.equals(currentLog.getStatus())) {
            return ServiceResult.error("Bạn đã kết thúc ca làm việc hôm nay rồi!");
        }

        // Tự động tính toán tổng số giờ làm việc
        double totalHours = DateUtils.calculateHoursDifference(currentLog.getCheckInTime(), checkOutTime);
        if (totalHours <= 0.0) {
            return ServiceResult.error("Giờ ra không hợp lệ (phải sau giờ vào " + currentLog.getCheckInTime() + ")!");
        }

        // Cập nhật CSDL
        boolean updated = workLogRepository.updateCheckOut(currentLog.getId(), checkOutTime, totalHours);
        if (!updated) {
            return ServiceResult.error("Lỗi khi cập nhật giờ kết thúc ca!");
        }

        currentLog.setCheckOutTime(checkOutTime);
        currentLog.setTotalHours(totalHours);
        currentLog.setStatus(WorkLog.STATUS_COMPLETED);

        return ServiceResult.success(currentLog, "Kết thúc ca thành công! Giờ làm: " + totalHours + "h");
    }

    // Lấy thông tin trạng thái chấm công hôm nay của nhân viên
    public ServiceResult<WorkLog> getTodayWorkLog(int employeeId) {
        if (employeeId <= 0) {
            return ServiceResult.error("Mã nhân viên không hợp lệ!");
        }
        WorkLog log = workLogRepository.getWorkLogByDate(employeeId, DateUtils.getCurrentDate());
        return ServiceResult.success(log, "Lấy trạng thái hôm nay thành công");
    }

    // Lấy toàn bộ danh sách lịch sử điểm danh của nhân viên
    public ServiceResult<List<WorkLog>> getAttendanceHistory(int employeeId) {
        if (employeeId <= 0) {
            return ServiceResult.error("Mã nhân viên không hợp lệ!");
        }
        List<WorkLog> list = workLogRepository.getWorkLogsByEmployee(employeeId);
        return ServiceResult.success(list, "Lấy lịch sử chấm công thành công");
    }

    // Lấy danh sách chấm công theo tháng và năm
    public ServiceResult<List<WorkLog>> getMonthlyWorkLogs(int employeeId, int month, int year) {
        if (employeeId <= 0) {
            return ServiceResult.error("Mã nhân viên không hợp lệ!");
        }
        List<WorkLog> list = workLogRepository.getWorkLogsByMonth(employeeId, month, year);
        return ServiceResult.success(list, "Lấy danh sách chấm công tháng thành công");
    }

    // Tính tổng số giờ làm việc trong tháng phục vụ tính lương
    public double calculateMonthlyTotalHours(int employeeId, int month, int year) {
        if (employeeId <= 0) return 0.0;
        List<WorkLog> list = workLogRepository.getWorkLogsByMonth(employeeId, month, year);
        double sum = 0.0;
        for (WorkLog item : list) {
            if (WorkLog.STATUS_COMPLETED.equals(item.getStatus())) {
                sum += item.getTotalHours();
            }
        }
        return Math.round(sum * 100.0) / 100.0;
    }
}
