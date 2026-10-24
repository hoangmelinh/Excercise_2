package com.example.excercise2.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.excercise2.model.WorkLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Quản lý truy xuất dữ liệu bảng WorkLog trong SQLite
public class WorkLogRepository {
    private final DatabaseHelper dbHelper;

    // Khởi tạo Repository bằng Context
    public WorkLogRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // Khởi tạo Repository bằng DatabaseHelper phục vụ kiểm thử
    public WorkLogRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Thêm bản ghi check-in mới khi nhân viên bắt đầu ca làm việc
    public long insertCheckIn(int employeeId, String workDate, String checkInTime) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_WORK_EMP_ID, employeeId);
        cv.put(DatabaseHelper.COL_WORK_DATE, workDate);
        cv.put(DatabaseHelper.COL_WORK_CHECK_IN, checkInTime);
        cv.put(DatabaseHelper.COL_WORK_CHECK_OUT, (String) null);
        cv.put(DatabaseHelper.COL_WORK_TOTAL_HOURS, 0.0);
        cv.put(DatabaseHelper.COL_WORK_STATUS, WorkLog.STATUS_WORKING);
        return db.insert(DatabaseHelper.TABLE_WORKLOG, null, cv);
    }

    // Cập nhật giờ check-out và tổng giờ làm khi kết thúc ca
    public boolean updateCheckOut(int workLogId, String checkOutTime, double totalHours) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_WORK_CHECK_OUT, checkOutTime);
        cv.put(DatabaseHelper.COL_WORK_TOTAL_HOURS, totalHours);
        cv.put(DatabaseHelper.COL_WORK_STATUS, WorkLog.STATUS_COMPLETED);

        int rows = db.update(DatabaseHelper.TABLE_WORKLOG, cv,
                DatabaseHelper.COL_WORK_ID + " = ?",
                new String[]{String.valueOf(workLogId)});
        return rows > 0;
    }

    // Lấy bản ghi chấm công của nhân viên trong ngày cụ thể
    public WorkLog getWorkLogByDate(int employeeId, String workDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_WORKLOG, null,
                DatabaseHelper.COL_WORK_EMP_ID + " = ? AND " + DatabaseHelper.COL_WORK_DATE + " = ?",
                new String[]{String.valueOf(employeeId), workDate},
                null, null, DatabaseHelper.COL_WORK_ID + " DESC", "1");
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToWorkLog(cursor);
            }
        } finally {
            if (cursor != null) cursor.close(); 
        }
        return null;
    }

    // Kiểm tra có ca làm việc nào trước hôm nay chưa check-out không
    public WorkLog getIncompleteWorkLogBeforeDate(int employeeId, String todayDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_WORKLOG, null,
                DatabaseHelper.COL_WORK_EMP_ID + " = ? AND " + DatabaseHelper.COL_WORK_DATE + " < ? AND " + DatabaseHelper.COL_WORK_STATUS + " = ?",
                new String[]{String.valueOf(employeeId), todayDate, WorkLog.STATUS_WORKING},
                null, null, DatabaseHelper.COL_WORK_DATE + " DESC", "1");
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToWorkLog(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    // Lấy toàn bộ lịch sử chấm công của một nhân viên
    public List<WorkLog> getWorkLogsByEmployee(int employeeId) {
        List<WorkLog> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_WORKLOG, null,
                DatabaseHelper.COL_WORK_EMP_ID + " = ?",
                new String[]{String.valueOf(employeeId)},
                null, null, DatabaseHelper.COL_WORK_DATE + " DESC, " + DatabaseHelper.COL_WORK_ID + " DESC");
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToWorkLog(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close(); 
        }
        return list;
    }

    // Lấy danh sách chấm công của nhân viên theo tháng và năm
    public List<WorkLog> getWorkLogsByMonth(int employeeId, int month, int year) {
        List<WorkLog> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String pattern = String.format(Locale.US, "%04d-%02d-%%", year, month);

        Cursor cursor = db.query(DatabaseHelper.TABLE_WORKLOG, null,
                DatabaseHelper.COL_WORK_EMP_ID + " = ? AND " + DatabaseHelper.COL_WORK_DATE + " LIKE ?",
                new String[]{String.valueOf(employeeId), pattern},
                null, null, DatabaseHelper.COL_WORK_DATE + " DESC, " + DatabaseHelper.COL_WORK_ID + " DESC");
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToWorkLog(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close(); 
        }
        return list;
    }

    // Alias tương thích cho getWorkLogsByMonth
    public List<WorkLog> getWorkLogsByEmployeeAndMonth(int employeeId, int month, int year) {
        return getWorkLogsByMonth(employeeId, month, year);
    }

    // Chuyển dòng dữ liệu Cursor thành đối tượng WorkLog
    private WorkLog cursorToWorkLog(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WORK_ID));
        int empId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WORK_EMP_ID));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WORK_DATE));
        String inTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WORK_CHECK_IN));
        String outTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WORK_CHECK_OUT));
        double hours = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WORK_TOTAL_HOURS));
        String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WORK_STATUS));
        return new WorkLog(id, empId, date, inTime, outTime, hours, status);
    }
}
