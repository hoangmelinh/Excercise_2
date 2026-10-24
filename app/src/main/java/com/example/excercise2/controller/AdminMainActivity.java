package com.example.excercise2.controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.example.excercise2.controller.adapter.EmployeeAdapter;
import com.example.excercise2.model.Employee;
import com.example.excercise2.service.AuthService;
import com.example.excercise2.service.EmployeeService;
import com.example.excercise2.service.ServiceResult;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

// Màn hình Quản trị viên: Quản lý nhân sự, CRUD, Live Search và điều hướng
public class AdminMainActivity extends AppCompatActivity implements EmployeeAdapter.OnEmployeeActionListener {
    private EmployeeService employeeService;
    private AuthService authService;
    private EmployeeAdapter employeeAdapter;

    private EditText etSearch;
    private RecyclerView rvEmployees;
    private TextView tvTotalEmployees;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddEmployee;
    private MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        employeeService = new EmployeeService(this);
        authService = new AuthService(this);

        initViews();
        setupStatusBarInsets();
        setupRecyclerView();
        setupLiveSearch();
        setupActionButtons();
        loadEmployees();
    }

    // Ánh xạ các thành phần giao diện
    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        rvEmployees = findViewById(R.id.rvEmployees);
        tvTotalEmployees = findViewById(R.id.tvTotalEmployees);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddEmployee = findViewById(R.id.fabAddEmployee);
        btnLogout = findViewById(R.id.btnLogout);
    }

    // Cài đặt đệm chống tràn Status Bar theo Rule V.4
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

    // Cài đặt danh sách cuộn RecyclerView
    private void setupRecyclerView() {
        employeeAdapter = new EmployeeAdapter(this);
        rvEmployees.setLayoutManager(new LinearLayoutManager(this));
        rvEmployees.setAdapter(employeeAdapter);
    }

    // Tìm kiếm nhân viên thời gian thực theo Rule VI.3
    private void setupLiveSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEmployees(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // Gắn sự kiện nút thêm mới và đăng xuất
    private void setupActionButtons() {
        fabAddEmployee.setOnClickListener(v -> showAddEmployeeDialog());
        btnLogout.setOnClickListener(v -> performLogout());
    }

    // Tải toàn bộ danh sách nhân viên từ CSDL
    private void loadEmployees() {
        ServiceResult<List<Employee>> result = employeeService.getAllEmployees();
        if (result.isSuccess()) {
            displayEmployeeList(result.getData());
        }
    }

    // Lọc danh sách nhân viên theo từ khóa tìm kiếm
    private void filterEmployees(String keyword) {
        ServiceResult<List<Employee>> result = employeeService.searchEmployees(keyword);
        if (result.isSuccess()) {
            displayEmployeeList(result.getData());
        }
    }

    // Cập nhật giao diện danh sách và số lượng
    private void displayEmployeeList(List<Employee> list) {
        employeeAdapter.setEmployees(list);
        int count = list != null ? list.size() : 0;
        tvTotalEmployees.setText(count + " nhân viên");

        if (count == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvEmployees.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvEmployees.setVisibility(View.VISIBLE);
        }
    }

    // Hiển thị dialog thêm nhân viên mới theo Rule VI.1
    private void showAddEmployeeDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_employee, null);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etCode = dialogView.findViewById(R.id.etDialogCode);
        TextInputEditText etFullname = dialogView.findViewById(R.id.etDialogFullname);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etDialogEmail);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etDialogPhone);
        TextInputEditText etHourlyRate = dialogView.findViewById(R.id.etDialogHourlyRate);
        TextInputEditText etUsername = dialogView.findViewById(R.id.etDialogUsername);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etDialogPassword);

        tvDialogTitle.setText("Thêm nhân viên mới");

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    double rate = parseHourlyRate(etHourlyRate);
                    String code = getTextValue(etCode);
                    String fullname = getTextValue(etFullname);
                    String email = getTextValue(etEmail);
                    String phone = getTextValue(etPhone);
                    String username = getTextValue(etUsername);
                    String password = getTextValue(etPassword);

                    ServiceResult<Employee> result = employeeService.createEmployee(
                            code, fullname, email, phone, rate, username, password);

                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    if (result.isSuccess()) loadEmployees();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Callback khi bấm nút Sửa trên thẻ nhân viên
    @Override
    public void onEdit(Employee employee) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_employee, null);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etCode = dialogView.findViewById(R.id.etDialogCode);
        TextInputEditText etFullname = dialogView.findViewById(R.id.etDialogFullname);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etDialogEmail);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etDialogPhone);
        TextInputEditText etHourlyRate = dialogView.findViewById(R.id.etDialogHourlyRate);
        LinearLayout layoutAccount = dialogView.findViewById(R.id.layoutAccountSection);

        tvDialogTitle.setText("Sửa thông tin nhân viên");
        layoutAccount.setVisibility(View.GONE);

        etCode.setText(employee.getCode());
        etFullname.setText(employee.getFullname());
        etEmail.setText(employee.getEmail());
        etPhone.setText(employee.getPhone());
        etHourlyRate.setText(String.valueOf(employee.getHourlyRate()));

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    employee.setCode(getTextValue(etCode));
                    employee.setFullname(getTextValue(etFullname));
                    employee.setEmail(getTextValue(etEmail));
                    employee.setPhone(getTextValue(etPhone));
                    employee.setHourlyRate(parseHourlyRate(etHourlyRate));

                    ServiceResult<Void> result = employeeService.updateEmployee(employee);
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    if (result.isSuccess()) loadEmployees();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Callback khi bấm nút Xóa trên thẻ nhân viên
    @Override
    public void onDelete(Employee employee) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa nhân viên " + employee.getFullname() + " (" + employee.getCode() + ") không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ServiceResult<Void> result = employeeService.deleteEmployee(employee.getId());
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    if (result.isSuccess()) loadEmployees();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Lấy chuỗi từ ô nhập TextInputEditText
    private String getTextValue(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    // Bọc try-catch parse mức lương chống crash ứng dụng theo Rule IV.3
    private double parseHourlyRate(TextInputEditText editText) {
        String str = getTextValue(editText);
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Thực hiện đăng xuất tài khoản
    private void performLogout() {
        authService.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
