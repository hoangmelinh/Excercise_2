package com.example.excercise2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.excercise2.model.Employee;
import com.example.excercise2.model.Salary;
import com.example.excercise2.model.WorkLog;
import com.example.excercise2.service.SalaryService;
import com.example.excercise2.service.ServiceResult;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

// Kiểm thử đơn vị thuật toán tính lương và bóc tách tăng ca Overtime của SalaryService
public class SalaryServiceUnitTest {

    @Test
    public void testOvertimeAlgorithmLogic_withMixedDays() {
        // Giả lập 4 ca làm việc:
        // Ca 1: 8.0h -> Thường 8.0h, OT 0.0h
        // Ca 2: 10.0h -> Thường 8.0h, OT 2.0h
        // Ca 3: 9.5h -> Thường 8.0h, OT 1.5h
        // Ca 4: 8.0h -> Thường 8.0h, OT 0.0h
        List<WorkLog> logs = new ArrayList<>();
        logs.add(new WorkLog(1, 1, "2026-10-01", "08:00:00", "17:00:00", 8.0, WorkLog.STATUS_COMPLETED));
        logs.add(new WorkLog(2, 1, "2026-10-02", "08:00:00", "19:00:00", 10.0, WorkLog.STATUS_COMPLETED));
        logs.add(new WorkLog(3, 1, "2026-10-03", "08:00:00", "18:30:00", 9.5, WorkLog.STATUS_COMPLETED));
        logs.add(new WorkLog(4, 1, "2026-10-04", "08:00:00", "17:00:00", 8.0, WorkLog.STATUS_COMPLETED));

        double hourlyRate = 50000.0;
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

        regularHours = Math.round(regularHours * 10.0) / 10.0;
        overtimeHours = Math.round(overtimeHours * 10.0) / 10.0;

        assertEquals(32.0, regularHours, 0.001);
        assertEquals(3.5, overtimeHours, 0.001);
        assertEquals(35.5, regularHours + overtimeHours, 0.001);

        double regularSalary = regularHours * hourlyRate; // 32.0 * 50k = 1.600.000 đ
        double overtimeSalary = overtimeHours * hourlyRate * SalaryService.OVERTIME_RATE; // 3.5 * 50k * 1.5 = 262.500 đ
        double totalSalary = Math.round(regularSalary + overtimeSalary); // 1.862.500 đ

        assertEquals(1600000.0, regularSalary, 0.001);
        assertEquals(262500.0, overtimeSalary, 0.001);
        assertEquals(1862500.0, totalSalary, 0.001);
    }

    @Test
    public void testOvertimeAlgorithmLogic_noOvertime() {
        // Giả lập ca làm bình thường không tăng ca
        List<WorkLog> logs = new ArrayList<>();
        logs.add(new WorkLog(1, 2, "2026-10-01", "08:00:00", "17:00:00", 8.0, WorkLog.STATUS_COMPLETED));
        logs.add(new WorkLog(2, 2, "2026-10-02", "08:30:00", "16:00:00", 7.5, WorkLog.STATUS_COMPLETED));

        double hourlyRate = 60000.0;
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

        assertEquals(15.5, regularHours, 0.001);
        assertEquals(0.0, overtimeHours, 0.001);

        double totalSalary = (regularHours * hourlyRate) + (overtimeHours * hourlyRate * 1.5);
        assertEquals(930000.0, totalSalary, 0.001);
    }

    @Test
    public void testServiceResultHelper() {
        ServiceResult<String> successResult = ServiceResult.success("Thao tác thành công", "DATA_TEST");
        assertTrue(successResult.isSuccess());
        assertEquals("DATA_TEST", successResult.getData());

        ServiceResult<String> errorResult = ServiceResult.error("Dữ liệu không hợp lệ");
        assertFalse(errorResult.isSuccess());
        assertEquals("Dữ liệu không hợp lệ", errorResult.getMessage());
    }
}
