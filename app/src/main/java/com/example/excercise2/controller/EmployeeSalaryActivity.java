package com.example.excercise2.controller;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.excercise2.R;
import com.example.excercise2.controller.adapter.SalaryAdapter;
import com.example.excercise2.model.Employee;
import com.example.excercise2.model.Salary;
import com.example.excercise2.repository.EmployeeRepository;
import com.example.excercise2.repository.SalaryRepository;
import com.example.excercise2.service.SalaryService;
import com.example.excercise2.utils.CurrencyUtils;
import com.example.excercise2.utils.DateUtils;
import com.example.excercise2.utils.SharedPrefManager;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Màn hình Nhân viên: Xem chi tiết phiếu lương cá nhân và lịch sử nhận lương
public class EmployeeSalaryActivity extends AppCompatActivity implements SalaryAdapter.OnSalaryActionListener {

    private ImageButton btnBack;
    private TextView tvHeaderSubtitle;
    private Spinner spinnerMonthYear;

    private CardView cardHeroSalary;
    private TextView tvHeroMonthLabel;
    private TextView tvHeroStatusBadge;
    private TextView tvHeroTotalSalary;
    private TextView tvHeroRegularHours;
    private TextView tvHeroOvertimeHours;
    private TextView tvHeroHourlyRate;
    private TextView tvEmptySelectedMonth;

    private RecyclerView rvSalaryHistory;
    private SalaryAdapter historyAdapter;

    private SalaryRepository salaryRepository;
    private EmployeeRepository employeeRepository;

    private int currentEmployeeId = 3; // Mặc định là NV003: Nguyễn Văn Hiển Vinh để test
    private int selectedMonth;
    private int selectedYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_salary);

        initViews();
        setupStatusBarInsets();
        initRepositories();

        int empIdExtra = getIntent().getIntExtra("EXTRA_EMPLOYEE_ID", -1);
        if (empIdExtra > 0) {
            currentEmployeeId = empIdExtra;
        } else {
            int prefEmpId = SharedPrefManager.getInstance(this).getEmployeeId();
            if (prefEmpId > 0) {
                currentEmployeeId = prefEmpId;
            }
        }

        setupEmployeeInfo();
        setupRecyclerView();
        setupMonthSpinner();
        setupEvents();
    }

    // Ánh xạ các thành phần giao diện
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvHeaderSubtitle = findViewById(R.id.tvHeaderSubtitle);
        spinnerMonthYear = findViewById(R.id.spinnerMonthYear);
        cardHeroSalary = findViewById(R.id.cardHeroSalary);
        tvHeroMonthLabel = findViewById(R.id.tvHeroMonthLabel);
        tvHeroStatusBadge = findViewById(R.id.tvHeroStatusBadge);
        tvHeroTotalSalary = findViewById(R.id.tvHeroTotalSalary);
        tvHeroRegularHours = findViewById(R.id.tvHeroRegularHours);
        tvHeroOvertimeHours = findViewById(R.id.tvHeroOvertimeHours);
        tvHeroHourlyRate = findViewById(R.id.tvHeroHourlyRate);
        tvEmptySelectedMonth = findViewById(R.id.tvEmptySelectedMonth);
        rvSalaryHistory = findViewById(R.id.rvSalaryHistory);
    }

    // Căn lề padding tránh tràn Status Bar theo AGENTS.md
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

    // Khởi tạo các tầng truy xuất dữ liệu
    private void initRepositories() {
        salaryRepository = new SalaryRepository(this);
        employeeRepository = new EmployeeRepository(this);
    }

    // Nạp thông tin nhân viên hiển thị lên Header
    private void setupEmployeeInfo() {
        Employee emp = employeeRepository.getEmployeeById(currentEmployeeId);
        if (emp != null) {
            tvHeaderSubtitle.setText(String.format("%s • %s", emp.getFullname(), emp.getCode()));
        }
    }

    // Cấu hình RecyclerView lịch sử các tháng
    private void setupRecyclerView() {
        historyAdapter = new SalaryAdapter(false, this);
        rvSalaryHistory.setLayoutManager(new LinearLayoutManager(this));
        rvSalaryHistory.setAdapter(historyAdapter);
    }

    // Thiết lập bộ chọn Tháng/Năm
    private void setupMonthSpinner() {
        List<String> list = salaryRepository.getAvailableMonthsYears();
        int curMonth = DateUtils.getMonthFromDate(DateUtils.getCurrentDate());
        int curYear = DateUtils.getYearFromDate(DateUtils.getCurrentDate());

        String currentTag = String.format(Locale.US, "%02d/%d", curMonth, curYear);
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
                loadHeroSalary();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Tách chuỗi MM/yyyy thành 2 số nguyên
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

    // Gán sự kiện nút bấm
    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());
        loadHistoryData();
    }

    // Hiển thị phiếu lương của tháng đang chọn lên Hero Card
    private void loadHeroSalary() {
        Salary salary = salaryRepository.getSalaryByEmployeeAndMonth(currentEmployeeId, selectedMonth, selectedYear);
        if (salary == null) {
            cardHeroSalary.setVisibility(View.GONE);
            tvEmptySelectedMonth.setVisibility(View.VISIBLE);
            return;
        }

        cardHeroSalary.setVisibility(View.VISIBLE);
        tvEmptySelectedMonth.setVisibility(View.GONE);

        tvHeroMonthLabel.setText(String.format(Locale.US, "Thu nhập Tháng %02d/%d", salary.getMonth(), salary.getYear()));
        tvHeroTotalSalary.setText(CurrencyUtils.formatCurrency(salary.getTotalSalary()));
        tvHeroRegularHours.setText(CurrencyUtils.formatHours(salary.getRegularHours()));
        tvHeroOvertimeHours.setText(CurrencyUtils.formatHours(salary.getOvertimeHours()));
        tvHeroHourlyRate.setText(CurrencyUtils.formatCurrency(salary.getHourlyRate()));

        String status = salary.getStatus();
        if (Salary.STATUS_PAID.equalsIgnoreCase(status)) {
            tvHeroStatusBadge.setText("Đã thanh toán");
            tvHeroStatusBadge.setBackgroundResource(R.drawable.bg_badge_success);
            tvHeroStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_success_text));
        } else {
            tvHeroStatusBadge.setText("Chờ duyệt");
            tvHeroStatusBadge.setBackgroundResource(R.drawable.bg_badge_warning);
            tvHeroStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_warning_text));
        }
    }

    // Tải toàn bộ lịch sử các tháng trước
    private void loadHistoryData() {
        List<Salary> historyList = salaryRepository.getAllSalariesByEmployee(currentEmployeeId);
        historyAdapter.setData(historyList);
    }

    @Override
    public void onPayClick(Salary salary) {}

    // Xem chi tiết công thức phân rã lương khi chạm vào bản ghi
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
        String code = salary.getEmployeeCode() != null ? salary.getEmployeeCode() : "EMP003";
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
