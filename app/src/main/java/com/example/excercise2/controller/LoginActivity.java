package com.example.excercise2.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.excercise2.R;
import com.example.excercise2.model.Account;
import com.example.excercise2.service.AuthService;
import com.example.excercise2.service.ServiceResult;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

// Màn hình Đăng nhập và điều phối phân quyền hệ thống
public class LoginActivity extends AppCompatActivity {
    private AuthService authService;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnQuickAdmin;
    private MaterialButton btnQuickEmployee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new AuthService(this);
        initViews();
        setupStatusBarInsets();
        setupQuickFillButtons();
        setupLoginButton();
    }

    // Ánh xạ các View trên giao diện
    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnQuickAdmin = findViewById(R.id.btnQuickAdmin);
        btnQuickEmployee = findViewById(R.id.btnQuickEmployee);
    }

    // Xử lý đệm chống tràn Status Bar theo Rule V.4
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

    // Cài đặt 2 nút điền nhanh tài khoản phục vụ chấm bài theo Rule VI.2
    private void setupQuickFillButtons() {
        btnQuickAdmin.setOnClickListener(v -> {
            etUsername.setText("admin");
            etPassword.setText("admin123");
        });

        btnQuickEmployee.setOnClickListener(v -> {
            etUsername.setText("hoangnm");
            etPassword.setText("123456");
        });
    }

    // Cài đặt sự kiện bấm nút Đăng nhập
    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    // Xử lý logic đăng nhập tài khoản
    private void handleLogin() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        ServiceResult<Account> result = authService.login(username, password);
        if (!result.isSuccess()) {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
        navigateToMain(result.getData().getRole());
    }

    // Điều hướng vào màn hình tương ứng với vai trò người dùng
    private void navigateToMain(String role) {
        Intent intent;
        if (Account.ROLE_ADMIN.equals(role)) {
            intent = new Intent(this, AdminMainActivity.class);
        } else {
            intent = new Intent(this, EmployeeMainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
