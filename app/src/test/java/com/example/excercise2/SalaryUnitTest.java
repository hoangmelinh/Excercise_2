package com.example.excercise2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.example.excercise2.model.Salary;

import org.junit.Test;

// Kiểm thử đơn vị các phương thức tính toán của Salary Model
public class SalaryUnitTest {

    @Test
    public void testRegularHoursCalculation_withOvertime() {
        // Tổng 176h, tăng ca 16h -> giờ thường phải là 160.0h
        Salary salary = new Salary(1, 10, 2026, 176.0, 16.0, 9200000.0, Salary.STATUS_PENDING);
        assertEquals(160.0, salary.getRegularHours(), 0.001);
    }

    @Test
    public void testRegularHoursCalculation_noOvertime() {
        // Tổng 160h, tăng ca 0h -> giờ thường là 160.0h
        Salary salary = new Salary(2, 10, 2026, 160.0, 0.0, 8000000.0, Salary.STATUS_CALCULATED);
        assertEquals(160.0, salary.getRegularHours(), 0.001);
    }

    @Test
    public void testRegularHoursCalculation_fractionalHours() {
        // Tổng 165.5h, tăng ca 5.5h -> giờ thường là 160.0h
        Salary salary = new Salary(3, 10, 2026, 165.5, 5.5, 9500000.0, Salary.STATUS_PAID);
        assertEquals(160.0, salary.getRegularHours(), 0.001);
    }

    @Test
    public void testSalaryHelperFields() {
        Salary salary = new Salary();
        salary.setEmployeeCode("EMP003");
        salary.setEmployeeName("Nguyễn Văn Hiển Vinh");
        salary.setHourlyRate(55000.0);

        assertNotNull(salary.getEmployeeCode());
        assertEquals("EMP003", salary.getEmployeeCode());
        assertEquals("Nguyễn Văn Hiển Vinh", salary.getEmployeeName());
        assertEquals(55000.0, salary.getHourlyRate(), 0.001);
    }
}
