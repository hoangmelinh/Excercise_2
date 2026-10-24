package com.example.excercise2.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.excercise2.R;
import com.example.excercise2.controller.adapter.WorkLogAdapter;
import com.example.excercise2.model.WorkLog;
import com.example.excercise2.service.AuthService;
import com.example.excercise2.service.ServiceResult;
import com.example.excercise2.service.WorkLogService;
import com.example.excercise2.utils.DateUtils;
import com.example.excercise2.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Màn hình chính của Nhân viên (Chấm công, tính giờ làm việc và đăng xuất)
public class EmployeeMainActivity extends AppCompatActivity {

    private AuthService authService;
    private SharedPrefManager prefManager;
    private WorkLogService workLogService;
    private WorkLogAdapter adapter;
    private int currentEmployeeId = 2; // Mặc định ID mẫu Phạm Hải Long để mở lên test ngay

    // Khai báo các view giao diện
    private TextView tvEmployeeGreeting;
    private TextView tvTodayStatusBadge;
    private TextView tvTodayHoursPreview;
    private TextView tvTodayCheckIn;
    private TextView tvTodayCheckOut;
    private TextView tvMonthlyTotalHours;
    private TextView tvHistoryCount;
    private TextView tvFilterMonthLabel;
    private MaterialButton btnEmployeeLogout;
    private MaterialButton btnViewSalary;
    private MaterialButton btnCheckIn;
    private MaterialButton btnCheckOut;
    private MaterialButton btnRefresh;
    private MaterialButton btnPrevMonth;
    private MaterialButton btnNextMonth;
    private MaterialButton btnFilterAll;
    private RecyclerView rvAttendanceHistory;
    private LinearLayout layoutEmpty;

    // Biến lưu trạng thái bộ lọc tháng/năm
    private int filterMonth;
    private int filterYear;
    private boolean isFilterAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_main);

        // Bắt sự kiện System Bar để tránh tràn thanh trạng thái
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initDependencies();
        initViews();
        setupRecyclerView();
        setupListeners();
        updateFilterLabel();
        refreshData();
    }

    // Khởi tạo các tầng nghiệp vụ và nạp thông tin phiên đăng nhập
    private void initDependencies() {
        authService = new AuthService(this);
        prefManager = SharedPrefManager.getInstance(this);
        workLogService = new WorkLogService(this);

        // Khởi tạo tháng/năm hiện tại làm bộ lọc mặc định
        Calendar cal = Calendar.getInstance();
        filterMonth = cal.get(Calendar.MONTH) + 1;
        filterYear = cal.get(Calendar.YEAR);

        // Lấy mã nhân viên từ phiên đăng nhập (nếu chưa đăng nhập thì giữ ID mẫu để test)
        int sessionEmpId = prefManager.getEmployeeId();
        if (sessionEmpId > 0) {
            currentEmployeeId = sessionEmpId;
        } else if (authService.getCurrentSession() != null && authService.getCurrentSession().getEmployeeId() > 0) {
            currentEmployeeId = authService.getCurrentSession().getEmployeeId();
        }
    }

    // Ánh xạ các thành phần giao diện từ layout XML
    private void initViews() {
        tvEmployeeGreeting = findViewById(R.id.tvEmployeeGreeting);
        tvTodayStatusBadge = findViewById(R.id.tvTodayStatusBadge);
        tvTodayHoursPreview = findViewById(R.id.tvTodayHoursPreview);
        tvTodayCheckIn = findViewById(R.id.tvTodayCheckIn);
        tvTodayCheckOut = findViewById(R.id.tvTodayCheckOut);
        tvMonthlyTotalHours = findViewById(R.id.tvMonthlyTotalHours);
        tvHistoryCount = findViewById(R.id.tvHistoryCount);
        tvFilterMonthLabel = findViewById(R.id.tvFilterMonthLabel);
        btnEmployeeLogout = findViewById(R.id.btnEmployeeLogout);
        btnViewSalary = findViewById(R.id.btnViewSalary);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        rvAttendanceHistory = findViewById(R.id.rvAttendanceHistory);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    // Cấu hình danh sách RecyclerView tối ưu cuộn
    private void setupRecyclerView() {
        adapter = new WorkLogAdapter(this);
        rvAttendanceHistory.setLayoutManager(new LinearLayoutManager(this));
        rvAttendanceHistory.setAdapter(adapter);
    }

    // Gán sự kiện click cho các nút bấm chức năng
    private void setupListeners() {
        if (btnEmployeeLogout != null) {
            btnEmployeeLogout.setOnClickListener(v -> performLogout());
        }
        if (btnViewSalary != null) {
            btnViewSalary.setOnClickListener(v -> {
                Intent intent = new Intent(EmployeeMainActivity.this, EmployeeSalaryActivity.class);
                intent.putExtra("EXTRA_EMPLOYEE_ID", currentEmployeeId);
                startActivity(intent);
            });
        }
        btnCheckIn.setOnClickListener(v -> handleCheckIn());
        btnCheckOut.setOnClickListener(v -> showConfirmCheckOutDialog());
        btnRefresh.setOnClickListener(v -> refreshData());
        btnPrevMonth.setOnClickListener(v -> navigateMonth(-1));
        btnNextMonth.setOnClickListener(v -> navigateMonth(1));
        btnFilterAll.setOnClickListener(v -> toggleFilterAll());
    }

    // Làm mới toàn bộ dữ liệu trên màn hình
    private void refreshData() {
        updateHeaderInfo();
        loadTodayStatus();
        loadAttendanceHistory();
        loadMonthlySummary();
    }

    // Cập nhật thông tin chào mừng và ngày hiện tại
    private void updateHeaderInfo() {
        String fullname = "";
        String username = "";

        if (authService != null && authService.getCurrentSession() != null) {
            fullname = authService.getCurrentSession().getFullname();
            username = authService.getCurrentSession().getUsername();
        }

        if (fullname == null || fullname.isEmpty()) {
            fullname = prefManager.getFullname();
        }

        if (fullname == null || fullname.isEmpty()) {
            fullname = "Phạm Hải Long"; // Tên mặc định khi test trực tiếp
        }

        if (tvEmployeeGreeting != null) {
            if (username != null && !username.isEmpty()) {
                tvEmployeeGreeting.setText("Xin chào: " + fullname + " (" + username + ")");
            } else {
                tvEmployeeGreeting.setText("Xin chào: " + fullname);
            }
        }
    }

    // Nạp trạng thái chấm công của ngày hôm nay và cập nhật nút bấm
    private void loadTodayStatus() {
        ServiceResult<WorkLog> result = workLogService.getTodayWorkLog(currentEmployeeId);
        WorkLog todayLog = result.getData();

        if (todayLog == null) {
            // Chưa điểm danh vào ca
            tvTodayStatusBadge.setText("Chưa vào ca");
            tvTodayStatusBadge.setBackgroundResource(R.drawable.bg_badge_muted);
            tvTodayStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            tvTodayCheckIn.setText("--:--:--");
            tvTodayCheckOut.setText("--:--:--");
            tvTodayHoursPreview.setText("Chưa có giờ tích lũy");

            btnCheckIn.setEnabled(true);
            btnCheckIn.setAlpha(1.0f);
            btnCheckOut.setEnabled(false);
            btnCheckOut.setAlpha(0.4f);
            return;
        }

        if (WorkLog.STATUS_WORKING.equalsIgnoreCase(todayLog.getStatus())) {
            // Đang trong ca làm việc
            tvTodayStatusBadge.setText("Đang làm việc");
            tvTodayStatusBadge.setBackgroundResource(R.drawable.bg_badge_working);
            tvTodayStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_warning_text));
            tvTodayCheckIn.setText(todayLog.getCheckInTime());
            tvTodayCheckOut.setText("--:--:--");
            tvTodayHoursPreview.setText("Đang trong ca làm việc...");

            btnCheckIn.setEnabled(false);
            btnCheckIn.setAlpha(0.4f);
            btnCheckOut.setEnabled(true);
            btnCheckOut.setAlpha(1.0f);
            return;
        }

        // Đã kết thúc ca làm việc hôm nay
        tvTodayStatusBadge.setText("Đã hoàn thành");
        tvTodayStatusBadge.setBackgroundResource(R.drawable.bg_badge_completed);
        tvTodayStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_success_text));
        tvTodayCheckIn.setText(todayLog.getCheckInTime());
        tvTodayCheckOut.setText(todayLog.getCheckOutTime());
        tvTodayHoursPreview.setText(String.format(Locale.US, "Tổng giờ hôm nay: %.1fh", todayLog.getTotalHours()));

        btnCheckIn.setEnabled(false);
        btnCheckIn.setAlpha(0.4f);
        btnCheckOut.setEnabled(false);
        btnCheckOut.setAlpha(0.4f);
    }

    // Xử lý sự kiện bấm nút Check-in
    private void handleCheckIn() {
        ServiceResult<WorkLog> result = workLogService.checkIn(currentEmployeeId);
        if (result.isSuccess()) {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            refreshData();
        } else {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Hiển thị hộp thoại xác nhận trước khi Check-out để tránh bấm nhầm
    private void showConfirmCheckOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận kết thúc ca")
                .setMessage("Bạn có chắc chắn muốn điểm danh tan ca tại thời điểm hiện tại không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> handleCheckOut())
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Xử lý sự kiện bấm nút Check-out
    private void handleCheckOut() {
        ServiceResult<WorkLog> result = workLogService.checkOut(currentEmployeeId);
        if (result.isSuccess()) {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            refreshData();
        } else {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Điều hướng sang tháng trước hoặc tháng kế tiếp
    private void navigateMonth(int offset) {
        isFilterAll = false;
        filterMonth += offset;
        if (filterMonth < 1) {
            filterMonth = 12;
            filterYear--;
        } else if (filterMonth > 12) {
            filterMonth = 1;
            filterYear++;
        }
        updateFilterLabel();
        loadAttendanceHistory();
        loadMonthlySummary();
    }

    // Chuyển đổi giữa chế độ xem Tất cả và xem Theo tháng
    private void toggleFilterAll() {
        isFilterAll = !isFilterAll;
        updateFilterLabel();
        loadAttendanceHistory();
        loadMonthlySummary();
    }

    // Cập nhật nhãn hiển thị tháng đang lọc
    private void updateFilterLabel() {
        if (isFilterAll) {
            tvFilterMonthLabel.setText("Tất cả lịch sử");
            btnFilterAll.setText("Theo tháng");
            btnPrevMonth.setEnabled(false);
            btnNextMonth.setEnabled(false);
            btnPrevMonth.setAlpha(0.3f);
            btnNextMonth.setAlpha(0.3f);
        } else {
            tvFilterMonthLabel.setText(String.format(Locale.US, "Tháng %02d/%04d", filterMonth, filterYear));
            btnFilterAll.setText("Tất cả");
            btnPrevMonth.setEnabled(true);
            btnNextMonth.setEnabled(true);
            btnPrevMonth.setAlpha(1.0f);
            btnNextMonth.setAlpha(1.0f);
        }
    }

    // Nạp danh sách lịch sử chấm công theo bộ lọc đang chọn
    private void loadAttendanceHistory() {
        ServiceResult<List<WorkLog>> result;
        if (isFilterAll) {
            result = workLogService.getAttendanceHistory(currentEmployeeId);
        } else {
            result = workLogService.getMonthlyWorkLogs(currentEmployeeId, filterMonth, filterYear);
        }
        List<WorkLog> list = result.getData();

        if (list == null || list.isEmpty()) {
            rvAttendanceHistory.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            tvHistoryCount.setText("(0 ca)");
        } else {
            rvAttendanceHistory.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            adapter.submitList(list);
            tvHistoryCount.setText("(" + list.size() + " ca)");
        }
    }

    // Nạp tổng số giờ làm việc theo tháng đang chọn
    private void loadMonthlySummary() {
        double totalHours;
        if (isFilterAll) {
            Calendar cal = Calendar.getInstance();
            totalHours = workLogService.calculateMonthlyTotalHours(currentEmployeeId, cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
        } else {
            totalHours = workLogService.calculateMonthlyTotalHours(currentEmployeeId, filterMonth, filterYear);
        }
        tvMonthlyTotalHours.setText(String.format(Locale.US, "%.1f giờ", totalHours));
    }

    // Đăng xuất và điều hướng về LoginActivity
    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    if (authService != null) authService.logout();
                    prefManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
