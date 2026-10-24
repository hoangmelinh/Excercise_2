package com.example.excercise2.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// Tiện ích xử lý ngày tháng và tính toán giờ làm việc
public class DateUtils {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";

    // Khai báo formatter dùng chung để tái sử dụng bộ nhớ
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());

    // Lấy ngày hiện tại theo định dạng yyyy-MM-dd
    public static synchronized String getCurrentDate() {
        return DATE_FORMATTER.format(new Date());
    }

    // Lấy giờ hiện tại theo định dạng HH:mm:ss
    public static synchronized String getCurrentTime() {
        return TIME_FORMATTER.format(new Date());
    }

    // Tính số giờ làm việc chênh lệch giữa giờ vào và giờ ra
    public static synchronized double calculateHoursDifference(String checkInTime, String checkOutTime) {
        if (checkInTime == null || checkOutTime == null || checkInTime.isEmpty() || checkOutTime.isEmpty()) {
            return 0.0;
        }

        try {
            Date in = TIME_FORMATTER.parse(checkInTime);
            Date out = TIME_FORMATTER.parse(checkOutTime);
            if (in == null || out == null) return 0.0;

            long diffMillis = out.getTime() - in.getTime();
            if (diffMillis <= 0) return 0.0;

            double hours = (double) diffMillis / (1000.0 * 60.0 * 60.0);
            return Math.round(hours * 100.0) / 100.0;
        } catch (ParseException e) {
            return 0.0;
        }
    }

    // Lấy tháng (1 - 12) từ chuỗi ngày yyyy-MM-dd
    public static synchronized int getMonthFromDate(String dateStr) {
        try {
            Date date = DATE_FORMATTER.parse(dateStr);
            if (date == null) return Calendar.getInstance().get(Calendar.MONTH) + 1;
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal.get(Calendar.MONTH) + 1;
        } catch (ParseException e) {
            return Calendar.getInstance().get(Calendar.MONTH) + 1;
        }
    }

    // Lấy năm từ chuỗi ngày yyyy-MM-dd
    public static synchronized int getYearFromDate(String dateStr) {
        try {
            Date date = DATE_FORMATTER.parse(dateStr);
            if (date == null) return Calendar.getInstance().get(Calendar.YEAR);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal.get(Calendar.YEAR);
        } catch (ParseException e) {
            return Calendar.getInstance().get(Calendar.YEAR);
        }
    }
}
