package com.example.excercise2.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.excercise2.model.Account;

// Tầng truy xuất dữ liệu tài khoản từ SQLite
public class AccountRepository {
    private final DatabaseHelper dbHelper;

    // Khởi tạo repository với DatabaseHelper singleton
    public AccountRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // Đăng nhập: kiểm tra username và password
    public Account login(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Account account = null;
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_ACCOUNT
                + " WHERE " + DatabaseHelper.COL_ACC_USERNAME + " = ? AND "
                + DatabaseHelper.COL_ACC_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        try {
            if (cursor != null && cursor.moveToFirst()) {
                account = cursorToAccount(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return account;
    }

    // Tìm tài khoản theo ID
    public Account getAccountById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Account account = null;
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_ACCOUNT
                + " WHERE " + DatabaseHelper.COL_ACC_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

        try {
            if (cursor != null && cursor.moveToFirst()) {
                account = cursorToAccount(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return account;
    }

    // Kiểm tra tên tài khoản đã tồn tại chưa
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + DatabaseHelper.COL_ACC_ID + " FROM " + DatabaseHelper.TABLE_ACCOUNT
                + " WHERE " + DatabaseHelper.COL_ACC_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        try {
            return (cursor != null && cursor.moveToFirst());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Thêm tài khoản mới vào CSDL
    public long insertAccount(Account account) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_ACC_USERNAME, account.getUsername());
        cv.put(DatabaseHelper.COL_ACC_PASSWORD, account.getPassword());
        cv.put(DatabaseHelper.COL_ACC_ROLE, account.getRole());
        return db.insert(DatabaseHelper.TABLE_ACCOUNT, null, cv);
    }

    // Xóa tài khoản theo ID
    public boolean deleteAccount(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_ACCOUNT,
                DatabaseHelper.COL_ACC_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // Chuyển dòng dữ liệu Cursor thành đối tượng Account
    private Account cursorToAccount(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACC_ID));
        String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACC_USERNAME));
        String password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACC_PASSWORD));
        String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACC_ROLE));
        return new Account(id, username, password, role);
    }
}
