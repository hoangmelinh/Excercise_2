package com.example.excercise2.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.excercise2.model.Salary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Quản lý truy xuất và lưu trữ bảng lương trong SQLite
public class SalaryRepository {
    private final DatabaseHelper dbHelper;

    public SalaryRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // Thêm mới hoặc cập nhật phiếu lương nguyên tử trong Transaction
    public boolean saveOrUpdateSalary(Salary salary) {
        if (salary == null) return false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_SAL_EMP_ID, salary.getEmployeeId());
            values.put(DatabaseHelper.COL_SAL_MONTH, salary.getMonth());
            values.put(DatabaseHelper.COL_SAL_YEAR, salary.getYear());
            values.put(DatabaseHelper.COL_SAL_TOTAL_HOURS, salary.getTotalHours());
            values.put(DatabaseHelper.COL_SAL_OVERTIME_HOURS, salary.getOvertimeHours());
            values.put(DatabaseHelper.COL_SAL_TOTAL_SALARY, salary.getTotalSalary());
            values.put(DatabaseHelper.COL_SAL_STATUS, salary.getStatus());

            // Kiểm tra xem nhân viên đã có bảng lương trong tháng/năm này chưa
            String checkQuery = "SELECT " + DatabaseHelper.COL_SAL_ID + " FROM " + DatabaseHelper.TABLE_SALARY +
                    " WHERE " + DatabaseHelper.COL_SAL_EMP_ID + " = ? AND " +
                    DatabaseHelper.COL_SAL_MONTH + " = ? AND " +
                    DatabaseHelper.COL_SAL_YEAR + " = ?";

            String[] checkArgs = new String[]{
                    String.valueOf(salary.getEmployeeId()),
                    String.valueOf(salary.getMonth()),
                    String.valueOf(salary.getYear())
            };

            Cursor cursor = db.rawQuery(checkQuery, checkArgs);
            boolean isExisting = false;
            int existingId = -1;

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    isExisting = true;
                    existingId = cursor.getInt(0);
                }
            } finally {
                if (cursor != null) cursor.close(); // Đóng con trỏ kiểm tra
            }

            if (isExisting) {
                // Đã có -> Cập nhật bản ghi cũ
                db.update(DatabaseHelper.TABLE_SALARY, values,
                        DatabaseHelper.COL_SAL_ID + " = ?", new String[]{String.valueOf(existingId)});
                salary.setId(existingId);
            } else {
                // Chưa có -> Thêm bản ghi mới
                long newId = db.insert(DatabaseHelper.TABLE_SALARY, null, values);
                salary.setId((int) newId);
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction(); // Bắt buộc kết thúc Transaction
        }
    }

    // Lấy toàn bộ bảng lương công ty theo tháng dùng SQL LEFT JOIN chống N+1 Query
    public List<Salary> getSalariesByMonthYear(int month, int year) {
        List<Salary> salaryList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sqlQuery = "SELECT s." + DatabaseHelper.COL_SAL_ID + ", " +
                "s." + DatabaseHelper.COL_SAL_EMP_ID + ", " +
                "s." + DatabaseHelper.COL_SAL_MONTH + ", " +
                "s." + DatabaseHelper.COL_SAL_YEAR + ", " +
                "s." + DatabaseHelper.COL_SAL_TOTAL_HOURS + ", " +
                "s." + DatabaseHelper.COL_SAL_OVERTIME_HOURS + ", " +
                "s." + DatabaseHelper.COL_SAL_TOTAL_SALARY + ", " +
                "s." + DatabaseHelper.COL_SAL_STATUS + ", " +
                "e." + DatabaseHelper.COL_EMP_CODE + ", " +
                "e." + DatabaseHelper.COL_EMP_FULLNAME + ", " +
                "e." + DatabaseHelper.COL_EMP_HOURLY_RATE + " " +
                "FROM " + DatabaseHelper.TABLE_SALARY + " s " +
                "LEFT JOIN " + DatabaseHelper.TABLE_EMPLOYEE + " e ON s." + DatabaseHelper.COL_SAL_EMP_ID + " = e." + DatabaseHelper.COL_EMP_ID + " " +
                "WHERE s." + DatabaseHelper.COL_SAL_MONTH + " = ? AND s." + DatabaseHelper.COL_SAL_YEAR + " = ? " +
                "ORDER BY e." + DatabaseHelper.COL_EMP_CODE + " ASC";

        Cursor cursor = db.rawQuery(sqlQuery, new String[]{String.valueOf(month), String.valueOf(year)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Salary salary = new Salary(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getInt(2),
                            cursor.getInt(3),
                            cursor.getDouble(4),
                            cursor.getDouble(5),
                            cursor.getDouble(6),
                            cursor.getString(7)
                    );
                    salary.setEmployeeCode(cursor.getString(8));
                    salary.setEmployeeName(cursor.getString(9));
                    salary.setHourlyRate(cursor.getDouble(10));
                    salaryList.add(salary);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close(); // Đóng Cursor giải phóng bộ nhớ
        }
        return salaryList;
    }

    // Lấy phiếu lương chi tiết của 1 nhân viên trong tháng
    public Salary getSalaryByEmployeeAndMonth(int employeeId, int month, int year) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sqlQuery = "SELECT s." + DatabaseHelper.COL_SAL_ID + ", " +
                "s." + DatabaseHelper.COL_SAL_EMP_ID + ", " +
                "s." + DatabaseHelper.COL_SAL_MONTH + ", " +
                "s." + DatabaseHelper.COL_SAL_YEAR + ", " +
                "s." + DatabaseHelper.COL_SAL_TOTAL_HOURS + ", " +
                "s." + DatabaseHelper.COL_SAL_OVERTIME_HOURS + ", " +
                "s." + DatabaseHelper.COL_SAL_TOTAL_SALARY + ", " +
                "s." + DatabaseHelper.COL_SAL_STATUS + ", " +
                "e." + DatabaseHelper.COL_EMP_CODE + ", " +
                "e." + DatabaseHelper.COL_EMP_FULLNAME + ", " +
                "e." + DatabaseHelper.COL_EMP_HOURLY_RATE + " " +
                "FROM " + DatabaseHelper.TABLE_SALARY + " s " +
                "LEFT JOIN " + DatabaseHelper.TABLE_EMPLOYEE + " e ON s." + DatabaseHelper.COL_SAL_EMP_ID + " = e." + DatabaseHelper.COL_EMP_ID + " " +
                "WHERE s." + DatabaseHelper.COL_SAL_EMP_ID + " = ? AND " +
                "s." + DatabaseHelper.COL_SAL_MONTH + " = ? AND " +
                "s." + DatabaseHelper.COL_SAL_YEAR + " = ?";

        String[] selectionArgs = new String[]{String.valueOf(employeeId), String.valueOf(month), String.valueOf(year)};
        Cursor cursor = db.rawQuery(sqlQuery, selectionArgs);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                Salary salary = new Salary(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getDouble(4),
                        cursor.getDouble(5),
                        cursor.getDouble(6),
                        cursor.getString(7)
                );
                salary.setEmployeeCode(cursor.getString(8));
                salary.setEmployeeName(cursor.getString(9));
                salary.setHourlyRate(cursor.getDouble(10));
                return salary;
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    // Lấy toàn bộ lịch sử phiếu lương của 1 nhân viên xếp theo tháng mới nhất
    public List<Salary> getAllSalariesByEmployee(int employeeId) {
        List<Salary> salaryList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sqlQuery = "SELECT s." + DatabaseHelper.COL_SAL_ID + ", " +
                "s." + DatabaseHelper.COL_SAL_EMP_ID + ", " +
                "s." + DatabaseHelper.COL_SAL_MONTH + ", " +
                "s." + DatabaseHelper.COL_SAL_YEAR + ", " +
                "s." + DatabaseHelper.COL_SAL_TOTAL_HOURS + ", " +
                "s." + DatabaseHelper.COL_SAL_OVERTIME_HOURS + ", " +
                "s." + DatabaseHelper.COL_SAL_TOTAL_SALARY + ", " +
                "s." + DatabaseHelper.COL_SAL_STATUS + ", " +
                "e." + DatabaseHelper.COL_EMP_CODE + ", " +
                "e." + DatabaseHelper.COL_EMP_FULLNAME + ", " +
                "e." + DatabaseHelper.COL_EMP_HOURLY_RATE + " " +
                "FROM " + DatabaseHelper.TABLE_SALARY + " s " +
                "LEFT JOIN " + DatabaseHelper.TABLE_EMPLOYEE + " e ON s." + DatabaseHelper.COL_SAL_EMP_ID + " = e." + DatabaseHelper.COL_EMP_ID + " " +
                "WHERE s." + DatabaseHelper.COL_SAL_EMP_ID + " = ? " +
                "ORDER BY s." + DatabaseHelper.COL_SAL_YEAR + " DESC, s." + DatabaseHelper.COL_SAL_MONTH + " DESC";

        Cursor cursor = db.rawQuery(sqlQuery, new String[]{String.valueOf(employeeId)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Salary salary = new Salary(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getInt(2),
                            cursor.getInt(3),
                            cursor.getDouble(4),
                            cursor.getDouble(5),
                            cursor.getDouble(6),
                            cursor.getString(7)
                    );
                    salary.setEmployeeCode(cursor.getString(8));
                    salary.setEmployeeName(cursor.getString(9));
                    salary.setHourlyRate(cursor.getDouble(10));
                    salaryList.add(salary);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return salaryList;
    }

    // Cập nhật trạng thái phiếu lương (ví dụ: Chuyển sang Paid)
    public boolean updateSalaryStatus(int salaryId, String newStatus) {
        if (salaryId <= 0 || newStatus == null || newStatus.trim().isEmpty()) {
            return false;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_SAL_STATUS, newStatus.trim());

        int affectedRows = db.update(DatabaseHelper.TABLE_SALARY, values,
                DatabaseHelper.COL_SAL_ID + " = ?", new String[]{String.valueOf(salaryId)});
        return affectedRows > 0;
    }

    // Lấy danh sách các Tháng/Năm có sẵn dữ liệu bảng lương để nạp vào Spinner
    public List<String> getAvailableMonthsYears() {
        List<String> monthYearList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sqlQuery = "SELECT DISTINCT " + DatabaseHelper.COL_SAL_MONTH + ", " + DatabaseHelper.COL_SAL_YEAR + " " +
                "FROM " + DatabaseHelper.TABLE_SALARY + " " +
                "ORDER BY " + DatabaseHelper.COL_SAL_YEAR + " DESC, " + DatabaseHelper.COL_SAL_MONTH + " DESC";

        Cursor cursor = db.rawQuery(sqlQuery, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int month = cursor.getInt(0);
                    int year = cursor.getInt(1);
                    monthYearList.add(String.format(Locale.US, "%02d/%d", month, year));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return monthYearList;
    }
}
