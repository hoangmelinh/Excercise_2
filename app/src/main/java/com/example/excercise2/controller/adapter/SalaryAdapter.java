package com.example.excercise2.controller.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.excercise2.R;
import com.example.excercise2.model.Salary;
import com.example.excercise2.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Adapter hiển thị danh sách phiếu lương với CardView Material 3
public class SalaryAdapter extends RecyclerView.Adapter<SalaryAdapter.SalaryViewHolder> {

    // Interface callback để Activity xử lý sự kiện click
    public interface OnSalaryActionListener {
        void onItemClick(Salary salary);
        void onPayClick(Salary salary);
    }

    private List<Salary> salaryList = new ArrayList<>();
    private final boolean isAdminMode;
    private final OnSalaryActionListener listener;

    public SalaryAdapter(boolean isAdminMode, OnSalaryActionListener listener) {
        this.isAdminMode = isAdminMode;
        this.listener = listener;
    }

    // Cập nhật danh sách dữ liệu mới
    public void setData(List<Salary> list) {
        this.salaryList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SalaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_salary, parent, false);
        return new SalaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalaryViewHolder holder, int position) {
        Salary salary = salaryList.get(position);
        holder.bind(salary, isAdminMode, listener);
    }

    @Override
    public int getItemCount() {
        return salaryList.size();
    }

    // ViewHolder lưu trữ các View thành phần
    static class SalaryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEmployeeName;
        private final TextView tvEmployeeCode;
        private final TextView tvStatusBadge;
        private final TextView tvRegularHours;
        private final TextView tvOvertimeHours;
        private final TextView tvHourlyRate;
        private final TextView tvTotalSalary;
        private final Button btnPaySalary;

        public SalaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvEmployeeCode = itemView.findViewById(R.id.tvEmployeeCode);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvRegularHours = itemView.findViewById(R.id.tvRegularHours);
            tvOvertimeHours = itemView.findViewById(R.id.tvOvertimeHours);
            tvHourlyRate = itemView.findViewById(R.id.tvHourlyRate);
            tvTotalSalary = itemView.findViewById(R.id.tvTotalSalary);
            btnPaySalary = itemView.findViewById(R.id.btnPaySalary);
        }

        public void bind(Salary salary, boolean isAdminMode, OnSalaryActionListener listener) {
            // Tên và mã nhân viên
            String name = salary.getEmployeeName() != null ? salary.getEmployeeName() : "Nhân viên #" + salary.getEmployeeId();
            String code = salary.getEmployeeCode() != null ? salary.getEmployeeCode() : "ID:" + salary.getEmployeeId();
            tvEmployeeName.setText(name);
            tvEmployeeCode.setText(String.format(Locale.US, "Mã: %s • Tháng %02d/%d", code, salary.getMonth(), salary.getYear()));

            // Giờ làm và đơn giá
            tvRegularHours.setText(CurrencyUtils.formatHours(salary.getRegularHours()));
            tvOvertimeHours.setText(CurrencyUtils.formatHours(salary.getOvertimeHours()));
            tvHourlyRate.setText(CurrencyUtils.formatCurrency(salary.getHourlyRate()));
            tvTotalSalary.setText(CurrencyUtils.formatCurrency(salary.getTotalSalary()));

            // Định dạng Badge trạng thái theo chuẩn Material 3
            String status = salary.getStatus() != null ? salary.getStatus() : Salary.STATUS_PENDING;
            if (Salary.STATUS_PAID.equalsIgnoreCase(status)) {
                tvStatusBadge.setText("Đã thanh toán");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_success);
                tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_success_text));
                btnPaySalary.setVisibility(View.GONE);
            } else if (Salary.STATUS_CALCULATED.equalsIgnoreCase(status)) {
                tvStatusBadge.setText("Đã tính");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_info);
                tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_info_text));
                btnPaySalary.setVisibility(isAdminMode ? View.VISIBLE : View.GONE);
                btnPaySalary.setText("Chi trả");
            } else {
                tvStatusBadge.setText("Chờ duyệt");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_warning);
                tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_warning_text));
                btnPaySalary.setVisibility(isAdminMode ? View.VISIBLE : View.GONE);
                btnPaySalary.setText("Chi trả");
            }

            // Gán sự kiện click
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(salary);
            });

            btnPaySalary.setOnClickListener(v -> {
                if (listener != null) listener.onPayClick(salary);
            });
        }
    }
}
