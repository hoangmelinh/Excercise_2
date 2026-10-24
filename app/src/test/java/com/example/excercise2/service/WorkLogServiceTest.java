package com.example.excercise2.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.excercise2.model.WorkLog;
import com.example.excercise2.repository.WorkLogRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Kiểm thử đơn vị các logic nghiệp vụ chấm công và tính giờ làm
public class WorkLogServiceTest {

    // Lớp giả lập Repository trong bộ nhớ để chạy Unit Test nhanh không cần máy ảo
    private static class FakeWorkLogRepository extends WorkLogRepository {
        private final Map<Integer, WorkLog> storage = new HashMap<>();
        private int autoId = 1;

        public FakeWorkLogRepository() {
            super((com.example.excercise2.repository.DatabaseHelper) null);
        }

        @Override
        public long insertCheckIn(int employeeId, String workDate, String checkInTime) {
            int id = autoId++;
            WorkLog log = new WorkLog(id, employeeId, workDate, checkInTime, null, 0.0, WorkLog.STATUS_WORKING);
            storage.put(id, log);
            return id;
        }

        @Override
        public boolean updateCheckOut(int workLogId, String checkOutTime, double totalHours) {
            WorkLog log = storage.get(workLogId);
            if (log == null) return false;
            log.setCheckOutTime(checkOutTime);
            log.setTotalHours(totalHours);
            log.setStatus(WorkLog.STATUS_COMPLETED);
            return true;
        }

        @Override
        public WorkLog getWorkLogByDate(int employeeId, String workDate) {
            for (WorkLog log : storage.values()) {
                if (log.getEmployeeId() == employeeId && log.getWorkDate().equals(workDate)) {
                    return log;
                }
            }
            return null;
        }

        @Override
        public WorkLog getIncompleteWorkLogBeforeDate(int employeeId, String todayDate) {
            for (WorkLog log : storage.values()) {
                if (log.getEmployeeId() == employeeId && log.getWorkDate().compareTo(todayDate) < 0
                        && WorkLog.STATUS_WORKING.equals(log.getStatus())) {
                    return log;
                }
            }
            return null;
        }

        @Override
        public List<WorkLog> getWorkLogsByEmployee(int employeeId) {
            List<WorkLog> result = new ArrayList<>();
            for (WorkLog log : storage.values()) {
                if (log.getEmployeeId() == employeeId) {
                    result.add(log);
                }
            }
            return result;
        }

        @Override
        public List<WorkLog> getWorkLogsByMonth(int employeeId, int month, int year) {
            List<WorkLog> result = new ArrayList<>();
            String prefix = String.format("%04d-%02d", year, month);
            for (WorkLog log : storage.values()) {
                if (log.getEmployeeId() == employeeId && log.getWorkDate().startsWith(prefix)) {
                    result.add(log);
                }
            }
            return result;
        }
    }

    private FakeWorkLogRepository fakeRepository;
    private WorkLogService workLogService;

    @Before
    public void setUp() {
        fakeRepository = new FakeWorkLogRepository();
        workLogService = new WorkLogService(fakeRepository);
    }

    @Test
    public void testCheckInSuccess() {
        ServiceResult<WorkLog> result = workLogService.checkIn(101, "2026-10-24", "08:00:00");
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("08:00:00", result.getData().getCheckInTime());
        assertEquals(WorkLog.STATUS_WORKING, result.getData().getStatus());
    }

    @Test
    public void testPreventDuplicateCheckIn() {
        // Lần 1: Thành công
        ServiceResult<WorkLog> first = workLogService.checkIn(101, "2026-10-24", "08:00:00");
        assertTrue(first.isSuccess());

        // Lần 2 trong cùng ngày: Bị chặn
        ServiceResult<WorkLog> second = workLogService.checkIn(101, "2026-10-24", "08:30:00");
        assertFalse(second.isSuccess());
        assertTrue(second.getMessage().contains("đã điểm danh"));
    }

    @Test
    public void testPreventIncompleteShiftFromPreviousDay() {
        // Ngày hôm trước check-in nhưng chưa check-out
        fakeRepository.insertCheckIn(101, "2026-10-23", "08:00:00");

        // Hôm nay thử check-in -> Bị chặn
        ServiceResult<WorkLog> result = workLogService.checkIn(101, "2026-10-24", "08:00:00");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("chưa check-out"));
    }

    @Test
    public void testCheckOutSuccessAndCalculatesHours() {
        // Check-in lúc 08:00:00
        workLogService.checkIn(101, "2026-10-24", "08:00:00");

        // Check-out lúc 17:30:00 (9.5 giờ)
        ServiceResult<WorkLog> outResult = workLogService.checkOut(101, "2026-10-24", "17:30:00");
        assertTrue(outResult.isSuccess());
        assertNotNull(outResult.getData());
        assertEquals(9.5, outResult.getData().getTotalHours(), 0.01);
        assertEquals(WorkLog.STATUS_COMPLETED, outResult.getData().getStatus());
    }

    @Test
    public void testCheckOutWithoutCheckInFails() {
        ServiceResult<WorkLog> result = workLogService.checkOut(999, "2026-10-24", "17:00:00");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("chưa điểm danh"));
    }

    @Test
    public void testCheckOutInvalidTimeFails() {
        // Vào lúc 17:00:00 nhưng lại ra lúc 08:00:00 (giờ ra trước giờ vào)
        workLogService.checkIn(101, "2026-10-24", "17:00:00");
        ServiceResult<WorkLog> result = workLogService.checkOut(101, "2026-10-24", "08:00:00");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("không hợp lệ"));
    }

    @Test
    public void testCalculateMonthlyTotalHours() {
        workLogService.checkIn(101, "2026-10-20", "08:00:00");
        workLogService.checkOut(101, "2026-10-20", "16:30:00"); // 8.5 giờ

        workLogService.checkIn(101, "2026-10-21", "08:00:00");
        workLogService.checkOut(101, "2026-10-21", "17:00:00"); // 9.0 giờ

        double total = workLogService.calculateMonthlyTotalHours(101, 10, 2026);
        assertEquals(17.5, total, 0.01);
    }
}
