package com.example.excercise2.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

// Tiện ích định dạng tiền tệ Việt Nam Đồng
public class CurrencyUtils {
    private static final DecimalFormat CURRENCY_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        CURRENCY_FORMAT = new DecimalFormat("#,###", symbols);
    }

    // Định dạng số tiền sang chuỗi có dấu chấm phân cách và đuôi đ
    public static synchronized String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount) + " đ";
    }

    // Định dạng số giờ làm việc (ví dụ: 8.5 giờ)
    public static String formatHours(double hours) {
        return String.format(Locale.getDefault(), "%.1f h", hours);
    }
}
