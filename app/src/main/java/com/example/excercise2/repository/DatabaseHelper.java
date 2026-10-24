package com.example.excercise2.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.excercise2.model.Account;
import com.example.excercise2.model.Salary;
import com.example.excercise2.model.WorkLog;
import com.example.excercise2.utils.DateUtils;

// Quản lý kết nối SQLite, tạo bảng và nạp dữ liệu mẫu ban đầu
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "work_salary_management.db";
    public static final int DATABASE_VERSION = 1;

    // Tên các bảng
    public static final String TABLE_ACCOUNT = "accounts";
    public static final String TABLE_EMPLOYEE = "employees";
    public static final String TABLE_WORKLOG = "work_logs";
    public static final String TABLE_SALARY = "salaries";

    // Cột bảng Account
    public static final String COL_ACC_ID = "id";
    public static final String COL_ACC_USERNAME = "username";
    public static final String COL_ACC_PASSWORD = "password";
    public static final String COL_ACC_ROLE = "role";

    // Cột bảng Employee
    public static final String COL_EMP_ID = "id";
    public static final String COL_EMP_CODE = "code";
    public static final String COL_EMP_FULLNAME = "fullname";
    public static final String COL_EMP_EMAIL = "email";
    public static final String COL_EMP_PHONE = "phone";
    public static final String COL_EMP_HOURLY_RATE = "hourlyRate";
    public static final String COL_EMP_ACCOUNT_ID = "accountId";

    // Cột bảng WorkLog
    public static final String COL_WORK_ID = "id";
    public static final String COL_WORK_EMP_ID = "employeeId";
    public static final String COL_WORK_DATE = "workDate";
    public static final String COL_WORK_CHECK_IN = "checkInTime";
    public static final String COL_WORK_CHECK_OUT = "checkOutTime";
    public static final String COL_WORK_TOTAL_HOURS = "totalHours";
    public static final String COL_WORK_STATUS = "status";

    // Cột bảng Salary
    public static final String COL_SAL_ID = "id";
    public static final String COL_SAL_EMP_ID = "employeeId";
    public static final String COL_SAL_MONTH = "month";
    public static final String COL_SAL_YEAR = "year";
    public static final String COL_SAL_TOTAL_HOURS = "totalHours";
    public static final String COL_SAL_OVERTIME_HOURS = "overtimeHours";
    public static final String COL_SAL_TOTAL_SALARY = "totalSalary";
    public static final String COL_SAL_STATUS = "status";

    private static DatabaseHelper instance;

    // Khởi tạo Singleton dùng ApplicationContext chống rò rỉ bộ nhớ
    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Lấy đối tượng DatabaseHelper duy nhất
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    // Bật ràng buộc khóa ngoại trong SQLite
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Tạo các bảng và seed dữ liệu mẫu
    @Override
    public void onCreate(SQLiteDatabase db) {
        createAccountTable(db);
        createEmployeeTable(db);
        createWorkLogTable(db);
        createSalaryTable(db);
        seedInitialData(db);
    }

    // Tạo bảng Account
    private void createAccountTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_ACCOUNT + " ("
                + COL_ACC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ACC_USERNAME + " TEXT UNIQUE NOT NULL, "
                + COL_ACC_PASSWORD + " TEXT NOT NULL, "
                + COL_ACC_ROLE + " TEXT NOT NULL)";
        db.execSQL(sql);
    }

    // Tạo bảng Employee
    private void createEmployeeTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_EMPLOYEE + " ("
                + COL_EMP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_EMP_CODE + " TEXT UNIQUE NOT NULL, "
                + COL_EMP_FULLNAME + " TEXT NOT NULL, "
                + COL_EMP_EMAIL + " TEXT, "
                + COL_EMP_PHONE + " TEXT, "
                + COL_EMP_HOURLY_RATE + " REAL NOT NULL, "
                + COL_EMP_ACCOUNT_ID + " INTEGER NOT NULL, "
                + "FOREIGN KEY (" + COL_EMP_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNT + "(" + COL_ACC_ID + ") ON DELETE CASCADE)";
        db.execSQL(sql);
    }

    // Tạo bảng WorkLog
    private void createWorkLogTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_WORKLOG + " ("
                + COL_WORK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_WORK_EMP_ID + " INTEGER NOT NULL, "
                + COL_WORK_DATE + " TEXT NOT NULL, "
                + COL_WORK_CHECK_IN + " TEXT NOT NULL, "
                + COL_WORK_CHECK_OUT + " TEXT, "
                + COL_WORK_TOTAL_HOURS + " REAL DEFAULT 0.0, "
                + COL_WORK_STATUS + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + COL_WORK_EMP_ID + ") REFERENCES " + TABLE_EMPLOYEE + "(" + COL_EMP_ID + ") ON DELETE CASCADE)";
        db.execSQL(sql);
    }

    // Tạo bảng Salary
    private void createSalaryTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_SALARY + " ("
                + COL_SAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SAL_EMP_ID + " INTEGER NOT NULL, "
                + COL_SAL_MONTH + " INTEGER NOT NULL, "
                + COL_SAL_YEAR + " INTEGER NOT NULL, "
                + COL_SAL_TOTAL_HOURS + " REAL NOT NULL, "
                + COL_SAL_OVERTIME_HOURS + " REAL NOT NULL, "
                + COL_SAL_TOTAL_SALARY + " REAL NOT NULL, "
                + COL_SAL_STATUS + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + COL_SAL_EMP_ID + ") REFERENCES " + TABLE_EMPLOYEE + "(" + COL_EMP_ID + ") ON DELETE CASCADE)";
        db.execSQL(sql);
    }

    // Nạp sẵn dữ liệu mẫu vào CSDL để mở app lên là test được ngay
    private void seedInitialData(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            // 1. Thêm tài khoản Admin
            insertAccount(db, "admin", "admin123", Account.ROLE_ADMIN);

            // 2. Thêm tài khoản và hồ sơ 3 nhân viên
            long accHoang = insertAccount(db, "hoangnm", "123456", Account.ROLE_EMPLOYEE);
            long accLong = insertAccount(db, "longph", "123456", Account.ROLE_EMPLOYEE);
            long accVinh = insertAccount(db, "vinhnv", "123456", Account.ROLE_EMPLOYEE);

            long empHoang = insertEmployee(db, "EMP001", "Nguyễn Minh Hoàng", "hoangnm@company.com", "0901234567", 60000.0, (int) accHoang);
            long empLong = insertEmployee(db, "EMP002", "Phạm Hải Long", "longph@company.com", "0912345678", 50000.0, (int) accLong);
            long empVinh = insertEmployee(db, "EMP003", "Nguyễn Văn Hiển Vinh", "vinhnv@company.com", "0923456789", 55000.0, (int) accVinh);

            // 3. Thêm nhật ký chấm công mẫu
            insertWorkLog(db, (int) empHoang, "2026-10-22", "08:00:00", "17:30:00", 9.5, WorkLog.STATUS_COMPLETED);
            insertWorkLog(db, (int) empHoang, "2026-10-23", "08:00:00", "16:30:00", 8.5, WorkLog.STATUS_COMPLETED);
            insertWorkLog(db, (int) empHoang, DateUtils.getCurrentDate(), "08:00:00", null, 0.0, WorkLog.STATUS_WORKING);

            insertWorkLog(db, (int) empLong, "2026-10-22", "08:15:00", "16:45:00", 8.5, WorkLog.STATUS_COMPLETED);
            insertWorkLog(db, (int) empLong, "2026-10-23", "08:00:00", "17:00:00", 9.0, WorkLog.STATUS_COMPLETED);

            insertWorkLog(db, (int) empVinh, "2026-10-22", "08:30:00", "16:30:00", 8.0, WorkLog.STATUS_COMPLETED);
            insertWorkLog(db, (int) empVinh, "2026-10-23", "08:00:00", "17:00:00", 9.0, WorkLog.STATUS_COMPLETED);

            // 4. Thêm bảng lương mẫu tháng 9/2026
            insertSalary(db, (int) empHoang, 9, 2026, 176.0, 16.0, 11040000.0, Salary.STATUS_PAID);
            insertSalary(db, (int) empLong, 9, 2026, 168.0, 8.0, 8600000.0, Salary.STATUS_PAID);
            insertSalary(db, (int) empVinh, 9, 2026, 160.0, 0.0, 8800000.0, Salary.STATUS_PENDING);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Chèn tài khoản mới vào database
    private long insertAccount(SQLiteDatabase db, String username, String password, String role) {
        ContentValues cv = new ContentValues();
        cv.put(COL_ACC_USERNAME, username);
        cv.put(COL_ACC_PASSWORD, password);
        cv.put(COL_ACC_ROLE, role);
        return db.insert(TABLE_ACCOUNT, null, cv);
    }

    // Chèn nhân viên mới vào database
    private long insertEmployee(SQLiteDatabase db, String code, String fullname, String email, String phone, double hourlyRate, int accountId) {
        ContentValues cv = new ContentValues();
        cv.put(COL_EMP_CODE, code);
        cv.put(COL_EMP_FULLNAME, fullname);
        cv.put(COL_EMP_EMAIL, email);
        cv.put(COL_EMP_PHONE, phone);
        cv.put(COL_EMP_HOURLY_RATE, hourlyRate);
        cv.put(COL_EMP_ACCOUNT_ID, accountId);
        return db.insert(TABLE_EMPLOYEE, null, cv);
    }

    // Chèn nhật ký chấm công mới vào database
    private void insertWorkLog(SQLiteDatabase db, int employeeId, String date, String checkIn, String checkOut, double hours, String status) {
        ContentValues cv = new ContentValues();
        cv.put(COL_WORK_EMP_ID, employeeId);
        cv.put(COL_WORK_DATE, date);
        cv.put(COL_WORK_CHECK_IN, checkIn);
        cv.put(COL_WORK_CHECK_OUT, checkOut);
        cv.put(COL_WORK_TOTAL_HOURS, hours);
        cv.put(COL_WORK_STATUS, status);
        db.insert(TABLE_WORKLOG, null, cv);
    }

    // Chèn bảng lương mới vào database
    private void insertSalary(SQLiteDatabase db, int employeeId, int month, int year, double hours, double otHours, double totalSalary, String status) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SAL_EMP_ID, employeeId);
        cv.put(COL_SAL_MONTH, month);
        cv.put(COL_SAL_YEAR, year);
        cv.put(COL_SAL_TOTAL_HOURS, hours);
        cv.put(COL_SAL_OVERTIME_HOURS, otHours);
        cv.put(COL_SAL_TOTAL_SALARY, totalSalary);
        cv.put(COL_SAL_STATUS, status);
        db.insert(TABLE_SALARY, null, cv);
    }

    // Nâng cấp phiên bản CSDL
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALARY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKLOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
        onCreate(db);
    }
}
