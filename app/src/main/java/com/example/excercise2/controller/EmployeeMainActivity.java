package com.example.excercise2.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.excercise2.R;
import com.example.excercise2.service.AuthService;
import com.google.android.material.button.MaterialButton;

// Màn hình chính của Nhân viên (Cổng thông tin và điều hướng phân hệ)
public class EmployeeMainActivity extends AppCompatActivity {
    private AuthService authService;
    private TextView tvEmployeeGreeting;
    private TextView tvWelcomeName;
    private MaterialButton btnEmployeeLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_main);

        authService = new AuthService(this);
        initViews();
        setupStatusBarInsets();
        displayUserInfo();
        setupEvents();
    }

    // Ánh xạ các thành phần giao diện
    private void initViews() {
        tvEmployeeGreeting = findViewById(R.id.tvEmployeeGreeting);
        tvWelcomeName = findViewById(R.id.tvWelcomeName);
        btnEmployeeLogout = findViewById(R.id.btnEmployeeLogout);
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

    // Hiển thị thông tin nhân viên đang đăng nhập
    private void displayUserInfo() {
        String fullname = authService.getCurrentSession().getFullname();
        String username = authService.getCurrentSession().getUsername();
        tvEmployeeGreeting.setText("Tài khoản: " + username);
        tvWelcomeName.setText("Xin chào: " + (fullname.isEmpty() ? username : fullname));
    }

    // Gắn sự kiện nút bấm
    private void setupEvents() {
        btnEmployeeLogout.setOnClickListener(v -> performLogout());
    }

    // Thực hiện đăng xuất và quay lại màn hình Login
    private void performLogout() {
        authService.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
