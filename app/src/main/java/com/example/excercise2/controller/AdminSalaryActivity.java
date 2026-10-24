package com.example.excercise2.controller;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.excercise2.R;
import com.example.excercise2.controller.adapter.SalaryAdapter;
import com.example.excercise2.model.Salary;
import com.example.excercise2.repository.SalaryRepository;
import com.example.excercise2.service.SalaryService;
import com.example.excercise2.service.ServiceResult;
import com.example.excercise2.utils.CurrencyUtils;
import com.example.excercise2.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Màn hình Quản trị viên: Quản lý, tính lương tự động và duyệt chi trả toàn công ty
public class AdminSalaryActivity extends AppCompatActivity implements SalaryAdapter.OnSalaryActionListener {

    private Spinner spinnerMonthYear;
    private Button btnBatchCalculate;
    private RecyclerView rvSalaryList;
    private LinearLayout layoutEmptyState;
    private ImageButton btnBack;

    private SalaryRepository salaryRepository;
    private SalaryService salaryService;
    private SalaryAdapter salaryAdapter;

    private int selectedMonth;
    private int selectedYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_salary);

        initViews();
        setupStatusBarInsets();
        initServices();
        setupRecyclerView();
        setupMonthSpinner();
        setupEvents();
    }

    // Ánh xạ các view trên giao diện
    private void initViews() {
        spinnerMonthYear = findViewById(R.id.spinnerMonthYear);
        btnBatchCalculate = findViewById(R.id.btnBatchCalculate);
        rvSalaryList = findViewById(R.id.rvSalaryList);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnBack = findViewById(R.id.btnBack);
    }

    // Thiết lập padding tránh đè Status Bar theo AGENTS.md
    private void setupStatusBarInsets() {
        View topHeader = findViewById(R.id.layoutTopHeader);
        if (topHeader != null) {
            ViewCompat.setOnApplyWindowInsetsListener(topHeader, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), systemBars.top + 16, v.getPaddingRight(), 16);
                return insets;
            });
        }
    }

    // Khởi tạo các tầng Repository và Service
    private void initServices() {
        salaryRepository = new SalaryRepository(this);
        salaryService = new SalaryService(this);
    }

    // Thiết lập danh sách RecyclerView
    private void setupRecyclerView() {
        salaryAdapter = new SalaryAdapter(true, this);
        rvSalaryList.setLayoutManager(new LinearLayoutManager(this));
        rvSalaryList.setAdapter(salaryAdapter);
    }

    // Thiết lập bộ chọn Tháng & Năm
    private void setupMonthSpinner() {
        List<String> list = salaryRepository.getAvailableMonthsYears();
        int currentMonth = DateUtils.getMonthFromDate(DateUtils.getCurrentDate());
        int currentYear = DateUtils.getYearFromDate(DateUtils.getCurrentDate());

        String currentTag = String.format(Locale.US, "%02d/%d", currentMonth, currentYear);
        if (!list.contains(currentTag)) {
            list.add(0, currentTag);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonthYear.setAdapter(adapter);

        spinnerMonthYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                parseSelectedMonthYear(list.get(position));
                loadSalaryData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Phân tích chuỗi MM/yyyy từ Spinner thành 2 số nguyên
    private void parseSelectedMonthYear(String tag) {
        try {
            String[] parts = tag.split("/");
            selectedMonth = Integer.parseInt(parts[0]);
            selectedYear = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            selectedYear = Calendar.getInstance().get(Calendar.YEAR);
        }
    }

    // Gán các sự kiện click nút bấm
    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());
        btnBatchCalculate.setOnClickListener(v -> handleBatchCalculate());
    }

    // Tải và hiển thị danh sách bảng lương theo tháng đã chọn
    private void loadSalaryData() {
        List<Salary> salaries = salaryRepository.getSalariesByMonthYear(selectedMonth, selectedYear);
        salaryAdapter.setData(salaries);

        if (salaries.isEmpty()) {
            rvSalaryList.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvSalaryList.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    // Xử lý tính lương tự động hàng loạt cho toàn bộ nhân viên
    private void handleBatchCalculate() {
        ServiceResult<Integer> result = salaryService.batchCalculateAllEmployees(selectedMonth, selectedYear);
        Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
        loadSalaryData();
    }

    // Xử lý khi Admin bấm nút Chi trả trên từng dòng
    @Override
    public void onPayClick(Salary salary) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận chi trả")
                .setMessage("Xác nhận đã thanh toán lương cho nhân viên " + salary.getEmployeeName() + " (" + CurrencyUtils.formatCurrency(salary.getTotalSalary()) + ")?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    ServiceResult<Boolean> res = salaryService.markSalaryAsPaid(salary.getId());
                    Toast.makeText(this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    loadSalaryData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Hiển thị dialog xem công thức phân rã chi tiết phiếu lương
    @Override
    public void onItemClick(Salary salary) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_salary_detail);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle = dialog.findViewById(R.id.tvDetailSubtitle);
        TextView tvReg = dialog.findViewById(R.id.tvDetailRegularLabel);
        TextView tvRegAmount = dialog.findViewById(R.id.tvDetailRegularAmount);
        TextView tvOt = dialog.findViewById(R.id.tvDetailOvertimeLabel);
        TextView tvOtAmount = dialog.findViewById(R.id.tvDetailOvertimeAmount);
        TextView tvTotal = dialog.findViewById(R.id.tvDetailTotalSalary);
        Button btnClose = dialog.findViewById(R.id.btnDetailClose);

        String name = salary.getEmployeeName() != null ? salary.getEmployeeName() : "Nhân viên";
        String code = salary.getEmployeeCode() != null ? salary.getEmployeeCode() : "ID:" + salary.getEmployeeId();
        tvTitle.setText(String.format(Locale.US, "%s • %s • Tháng %02d/%d", name, code, salary.getMonth(), salary.getYear()));

        double regSalary = salary.getRegularHours() * salary.getHourlyRate();
        double otSalary = salary.getOvertimeHours() * salary.getHourlyRate() * SalaryService.OVERTIME_RATE;

        tvReg.setText(String.format(Locale.US, "Giờ thường (%s x %s):",
                CurrencyUtils.formatHours(salary.getRegularHours()),
                CurrencyUtils.formatCurrency(salary.getHourlyRate())));
        tvRegAmount.setText(CurrencyUtils.formatCurrency(regSalary));

        tvOt.setText(String.format(Locale.US, "Tăng ca OT x1.5 (%s x %s):",
                CurrencyUtils.formatHours(salary.getOvertimeHours()),
                CurrencyUtils.formatCurrency(salary.getHourlyRate() * SalaryService.OVERTIME_RATE)));
        tvOtAmount.setText(CurrencyUtils.formatCurrency(otSalary));

        tvTotal.setText(CurrencyUtils.formatCurrency(salary.getTotalSalary()));
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
