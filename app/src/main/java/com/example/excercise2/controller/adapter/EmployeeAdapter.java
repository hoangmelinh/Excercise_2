package com.example.excercise2.controller.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.excercise2.R;
import com.example.excercise2.model.Employee;
import com.example.excercise2.utils.CurrencyUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

// Adapter hiển thị danh sách nhân viên dạng thẻ CardView
public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    // Interface callback gửi sự kiện Sửa/Xóa về Activity
    public interface OnEmployeeActionListener {
        void onEdit(Employee employee);
        void onDelete(Employee employee);
    }

    private final List<Employee> employeeList = new ArrayList<>();
    private final OnEmployeeActionListener listener;

    // Khởi tạo Adapter với listener sự kiện
    public EmployeeAdapter(OnEmployeeActionListener listener) {
        this.listener = listener;
    }

    // Cập nhật danh sách nhân viên mới
    public void setEmployees(List<Employee> list) {
        employeeList.clear();
        if (list != null) {
            employeeList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        Employee emp = employeeList.get(position);
        holder.bind(emp, listener);
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    // ViewHolder tối ưu hiển thị thẻ nhân viên
    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCode;
        private final TextView tvFullname;
        private final TextView tvEmail;
        private final TextView tvPhone;
        private final TextView tvHourlyRate;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvFullname = itemView.findViewById(R.id.tvFullname);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvHourlyRate = itemView.findViewById(R.id.tvHourlyRate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        // Gán dữ liệu nhân viên vào các View
        public void bind(Employee emp, OnEmployeeActionListener listener) {
            tvCode.setText(emp.getCode());
            tvFullname.setText(emp.getFullname());
            tvEmail.setText(emp.getEmail().isEmpty() ? "Chưa có email" : emp.getEmail());
            tvPhone.setText(emp.getPhone().isEmpty() ? "Chưa có SĐT" : emp.getPhone());
            tvHourlyRate.setText(CurrencyUtils.formatCurrency(emp.getHourlyRate()) + "/h");

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(emp);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(emp);
            });
        }
    }
}
