package com.example.excercise2.service;

// Đóng gói kết quả trả về từ tầng nghiệp vụ
public class ServiceResult<T> {
    private final boolean success;
    private final String message;
    private final T data;

    // Khởi tạo kết quả dịch vụ
    public ServiceResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Tạo kết quả thành công kèm dữ liệu
    public static <T> ServiceResult<T> success(T data, String message) {
        return new ServiceResult<>(true, message, data);
    }

    // Tạo kết quả thất bại kèm thông báo lỗi
    public static <T> ServiceResult<T> error(String message) {
        return new ServiceResult<>(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
