package com.example.excercise2.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.excercise2.R;
import com.example.excercise2.model.WorkLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Adapter hiển thị danh sách nhật ký chấm công lên RecyclerView
public class WorkLogAdapter extends RecyclerView.Adapter<WorkLogAdapter.WorkLogViewHolder> {
    private final Context context;
    private final List<WorkLog> workLogList = new ArrayList<>();
    private OnItemClickListener listener;

    // Interface bắt sự kiện khi người dùng bấm vào thẻ ca làm
    public interface OnItemClickListener {
        void onItemClick(WorkLog workLog);
    }

    public WorkLogAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Cập nhật lại toàn bộ danh sách chấm công
    public void submitList(List<WorkLog> newList) {
        workLogList.clear();
        if (newList != null) {
            workLogList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_work_log, parent, false);
        return new WorkLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkLogViewHolder holder, int position) {
        WorkLog item = workLogList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return workLogList.size();
    }

    // Lớp ViewHolder lưu trữ view để tối ưu hiệu năng cuộn danh sách
    class WorkLogViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvWorkDate;
        private final TextView tvStatusBadge;
        private final TextView tvCheckInTime;
        private final TextView tvCheckOutTime;
        private final TextView tvTotalHours;

        public WorkLogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkDate = itemView.findViewById(R.id.tvWorkDate);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvCheckInTime = itemView.findViewById(R.id.tvCheckInTime);
            tvCheckOutTime = itemView.findViewById(R.id.tvCheckOutTime);
            tvTotalHours = itemView.findViewById(R.id.tvTotalHours);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(workLogList.get(pos));
                }
            });
        }

        // Đổ dữ liệu và gán màu badge trạng thái cho từng ca làm việc
        public void bind(WorkLog item) {
            tvWorkDate.setText(item.getWorkDate());
            tvCheckInTime.setText(item.getCheckInTime());

            // Xử lý hiển thị giờ ra
            if (item.getCheckOutTime() != null && !item.getCheckOutTime().isEmpty()) {
                tvCheckOutTime.setText(item.getCheckOutTime());
            } else {
                tvCheckOutTime.setText("--:--:--");
            }

            // Xử lý hiển thị tổng giờ
            tvTotalHours.setText(String.format(Locale.US, "%.1fh", item.getTotalHours()));

            // Xử lý màu sắc badge trạng thái theo chuẩn Material 3
            if (WorkLog.STATUS_WORKING.equalsIgnoreCase(item.getStatus())) {
                tvStatusBadge.setText("Đang làm việc");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_working);
                tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_warning_text));
            } else {
                tvStatusBadge.setText("Đã hoàn thành");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_completed);
                tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_success_text));
            }
        }
    }
}
