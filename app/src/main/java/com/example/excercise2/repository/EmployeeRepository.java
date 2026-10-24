package com.example.excercise2.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.excercise2.model.Employee;

import java.util.ArrayList;
import java.util.List;

// Tầng truy xuất dữ liệu nhân viên từ SQLite
public class EmployeeRepository {
    private final DatabaseHelper dbHelper;

    // Khởi tạo repository với DatabaseHelper singleton
    public EmployeeRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // Lấy toàn bộ danh sách nhân viên
    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_EMPLOYEE
                + " ORDER BY " + DatabaseHelper.COL_EMP_ID + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToEmployee(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // Lấy thông tin nhân viên theo ID
    public Employee getEmployeeById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Employee employee = null;
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_EMPLOYEE
                + " WHERE " + DatabaseHelper.COL_EMP_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

        try {
            if (cursor != null && cursor.moveToFirst()) {
                employee = cursorToEmployee(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return employee;
    }

    // Lấy thông tin nhân viên theo Account ID
    public Employee getEmployeeByAccountId(int accountId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Employee employee = null;
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_EMPLOYEE
                + " WHERE " + DatabaseHelper.COL_EMP_ACCOUNT_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(accountId)});

        try {
            if (cursor != null && cursor.moveToFirst()) {
                employee = cursorToEmployee(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return employee;
    }

    // Tìm kiếm nhân viên theo mã hoặc họ tên
    public List<Employee> searchEmployees(String keyword) {
        List<Employee> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_EMPLOYEE
                + " WHERE " + DatabaseHelper.COL_EMP_CODE + " LIKE ? OR "
                + DatabaseHelper.COL_EMP_FULLNAME + " LIKE ? ORDER BY " + DatabaseHelper.COL_EMP_ID + " DESC";
        String pattern = "%" + keyword + "%";
        Cursor cursor = db.rawQuery(query, new String[]{pattern, pattern});

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToEmployee(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // Kiểm tra mã nhân viên đã tồn tại chưa
    public boolean isCodeExists(String code, int excludeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + DatabaseHelper.COL_EMP_ID + " FROM " + DatabaseHelper.TABLE_EMPLOYEE
                + " WHERE " + DatabaseHelper.COL_EMP_CODE + " = ? AND "
                + DatabaseHelper.COL_EMP_ID + " != ?";
        Cursor cursor = db.rawQuery(query, new String[]{code, String.valueOf(excludeId)});

        try {
            return (cursor != null && cursor.moveToFirst());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Thêm nhân viên mới vào CSDL
    public long insertEmployee(Employee employee) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_EMP_CODE, employee.getCode());
        cv.put(DatabaseHelper.COL_EMP_FULLNAME, employee.getFullname());
        cv.put(DatabaseHelper.COL_EMP_EMAIL, employee.getEmail());
        cv.put(DatabaseHelper.COL_EMP_PHONE, employee.getPhone());
        cv.put(DatabaseHelper.COL_EMP_HOURLY_RATE, employee.getHourlyRate());
        cv.put(DatabaseHelper.COL_EMP_ACCOUNT_ID, employee.getAccountId());
        return db.insert(DatabaseHelper.TABLE_EMPLOYEE, null, cv);
    }

    // Cập nhật thông tin nhân viên
    public boolean updateEmployee(Employee employee) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_EMP_CODE, employee.getCode());
        cv.put(DatabaseHelper.COL_EMP_FULLNAME, employee.getFullname());
        cv.put(DatabaseHelper.COL_EMP_EMAIL, employee.getEmail());
        cv.put(DatabaseHelper.COL_EMP_PHONE, employee.getPhone());
        cv.put(DatabaseHelper.COL_EMP_HOURLY_RATE, employee.getHourlyRate());

        int rows = db.update(DatabaseHelper.TABLE_EMPLOYEE, cv,
                DatabaseHelper.COL_EMP_ID + " = ?", new String[]{String.valueOf(employee.getId())});
        return rows > 0;
    }

    // Xóa nhân viên theo ID
    public boolean deleteEmployee(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_EMPLOYEE,
                DatabaseHelper.COL_EMP_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // Chuyển dòng dữ liệu Cursor thành đối tượng Employee
    private Employee cursorToEmployee(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMP_ID));
        String code = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMP_CODE));
        String fullname = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMP_FULLNAME));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMP_EMAIL));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMP_PHONE));
        double hourlyRate = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMP_HOURLY_RATE));
        int accountId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMP_ACCOUNT_ID));
        return new Employee(id, code, fullname, email, phone, hourlyRate, accountId);
    }
}
