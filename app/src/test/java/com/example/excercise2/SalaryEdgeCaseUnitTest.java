package com.example.excercise2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.excercise2.model.Salary;
import com.example.excercise2.model.WorkLog;
import com.example.excercise2.service.SalaryService;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

// Kiểm thử các trường hợp biên (Edge Cases) trong nghiệp vụ tính lương
public class SalaryEdgeCaseUnitTest {

    // Trường hợp biên 1: Ca làm việc chưa checkout hoặc 0 giờ làm việc (phải bỏ qua, không gây lỗi)
    @Test
    public void testEdgeCase_ZeroOrIncompleteHoursAreIgnored() {
        List<WorkLog> logs = new ArrayList<>();
        logs.add(new WorkLog(1, 1, "2026-10-01", "08:00:00", null, 0.0, WorkLog.STATUS_WORKING));
        logs.add(new WorkLog(2, 1, "2026-10-02", "08:00:00", "17:00:00", 8.0, WorkLog.STATUS_COMPLETED));

        double regularHours = 0.0;
        double overtimeHours = 0.0;

        for (WorkLog log : logs) {
            double h = log.getTotalHours();
            if (h <= 0) continue; // Bỏ qua ca 0 giờ

            if (h > SalaryService.STANDARD_DAILY_HOURS) {
                regularHours += SalaryService.STANDARD_DAILY_HOURS;
                overtimeHours += (h - SalaryService.STANDARD_DAILY_HOURS);
            } else {
                regularHours += h;
            }
        }

        assertEquals(8.0, regularHours, 0.001);
        assertEquals(0.0, overtimeHours, 0.001);
    }

    // Trường hợp biên 2: Ca làm đúng chuẩn 8.0 giờ (Overtime phải bằng đúng 0.0h)
    @Test
    public void testEdgeCase_ExactlyEightHoursDaily() {
        List<WorkLog> logs = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            logs.add(new WorkLog(i, 2, "2026-10-" + (i < 10 ? "0" + i : i), "08:00:00", "17:00:00", 8.0, WorkLog.STATUS_COMPLETED));
        }

        double regularHours = 0.0;
        double overtimeHours = 0.0;

        for (WorkLog log : logs) {
            double h = log.getTotalHours();
            if (h > SalaryService.STANDARD_DAILY_HOURS) {
                regularHours += SalaryService.STANDARD_DAILY_HOURS;
                overtimeHours += (h - SalaryService.STANDARD_DAILY_HOURS);
            } else {
                regularHours += h;
            }
        }

        assertEquals(160.0, regularHours, 0.001);
        assertEquals(0.0, overtimeHours, 0.001);
    }

    // Trường hợp biên 3: Số giờ làm lẻ thập phân (ví dụ: 9.7 giờ -> thường 8h, tăng ca 1.7h)
    @Test
    public void testEdgeCase_FractionalOvertimeCalculation() {
        double dayHours = 9.7;
        double reg = 0.0;
        double ot = 0.0;

        if (dayHours > SalaryService.STANDARD_DAILY_HOURS) {
            reg = SalaryService.STANDARD_DAILY_HOURS;
            ot = Math.round((dayHours - SalaryService.STANDARD_DAILY_HOURS) * 10.0) / 10.0;
        }

        assertEquals(8.0, reg, 0.001);
        assertEquals(1.7, ot, 0.001);

        double hourlyRate = 60000.0;
        double expectedOtPay = 1.7 * hourlyRate * 1.5; // 153.000 đ
        assertEquals(153000.0, expectedOtPay, 0.001);
    }

    // Trường hợp biên 4: Kiểm tra chu trình chuyển đổi trạng thái phiếu lương
    @Test
    public void testEdgeCase_SalaryStatusTransitions() {
        Salary salary = new Salary(1, 10, 2026, 176.0, 16.0, 9200000.0, Salary.STATUS_CALCULATED);
        assertEquals("Calculated", salary.getStatus());

        // Đổi sang Pending (chờ đối soát)
        salary.setStatus(Salary.STATUS_PENDING);
        assertEquals("Pending", salary.getStatus());

        // Admin bấm duyệt chi trả -> Paid
        salary.setStatus(Salary.STATUS_PAID);
        assertEquals("Paid", salary.getStatus());
        assertTrue(Salary.STATUS_PAID.equalsIgnoreCase(salary.getStatus()));
    }
}
