# EMPLOYEE WORKING HOURS AND SALARY MANAGEMENT

> Ứng dụng Android quản lý giờ làm việc, điểm danh hàng ngày và tự động tính toán tiền lương cho doanh nghiệp theo mô hình phân tầng chuẩn mực (**Layered Architecture**).

---

## 📌 TỔNG QUAN DỰ ÁN

* **Nền tảng phát triển**: Android SDK (Java), Min SDK 24, Target SDK 34+.
* **Cơ sở dữ liệu**: SQLite thuần (Android Framework SDK), đảm bảo tính ổn định cao, không phụ thuộc thư viện biên dịch bên ngoài.
* **Giao diện & Trải nghiệm (UI/UX)**: Google Material 3 Design, Force Light Mode, hỗ trợ **Quick Fill Login** để chấm bài và kiểm thử ngay lập tức trong 1 click.
* **4 Bảng CSDL chính**:
  1. `Account(id, username, password, role)`: Quản lý tài khoản và phân quyền hệ thống.
  2. `Employee(id, code, fullname, email, phone, hourlyRate, accountId)`: Thông tin hồ sơ nhân viên và đơn giá lương theo giờ.
  3. `WorkLog(id, employeeId, workDate, checkInTime, checkOutTime, totalHours, status)`: Nhật ký điểm danh hàng ngày và tổng số giờ làm.
  4. `Salary(id, employeeId, month, year, totalHours, overtimeHours, totalSalary, status)`: Bảng lương tháng, tổng giờ thường, giờ làm thêm (Overtime) và trạng thái thanh toán.
* **2 Vai trò người dùng (Roles)**:
  * **ADMIN**: Quản trị danh sách nhân viên (Thêm/Sửa/Xóa/Tìm kiếm), giám sát lịch sử chấm công, tính toán lương tháng tự động và duyệt/chi trả bảng lương.
  * **EMPLOYEE**: Điểm danh Check-in/Check-out hàng ngày, theo dõi lịch sử làm việc theo tháng, kiểm tra chi tiết bảng lương và tiền làm thêm giờ.

---

## 👥 PHÂN CÔNG NHIỆM VỤ THÀNH VIÊN

1. **Nguyễn Minh Hoàng**: Phụ trách Core Architecture, CSDL SQLite & Seed Data mẫu ban đầu, Cơ chế Đăng nhập/Phân quyền (Admin/Employee) và Quản trị Nhân sự (CRUD, Tìm kiếm, Đơn giá lương theo giờ).
2. **Phạm Hải Long**: Phụ trách Quản lý Điểm danh (Check-in/Check-out hàng ngày), Kiểm soát tính hợp lệ dữ liệu (chặn duplicate check-in, validate giờ ra), Tự động tính tổng số giờ làm việc (`totalHours`) và Lịch sử chấm công.
3. **Nguyễn Văn Hiển Vinh**: Phụ trách Quản lý Tiền lương (Tự động tính lương tháng theo tổng giờ và đơn giá), Thuật toán tính làm thêm giờ (Overtime x1.5), Quản trị bảng lương toàn công ty cho Admin và Xem chi tiết phiếu lương cá nhân cho Employee.

---

## 📖 TÀI LIỆU CHI TIẾT & HƯỚNG DẪN ĐÓNG GÓP

Chi tiết bảng phân rã công việc (WBS), danh sách file phụ trách, quy chuẩn phân tầng code và quy trình nhánh Git được mô tả chi tiết tại:
* 👉 **[Tài liệu Hướng dẫn Đóng góp & Phân công Chi tiết (docs/contribute/CONTRIBUTE.md)](docs/contribute/CONTRIBUTE.md)**

---

## 🚀 HƯỚNG DẪN CÀI ĐẶT & CHẠY THỬ NHANH

1. Mở dự án bằng **Android Studio**.
2. Đồng bộ Gradle (`Sync Project with Gradle Files`).
3. Khởi chạy trên máy ảo hoặc thiết bị thật (Android 7.0 trở lên).
4. Tại màn hình Đăng nhập:
   * Bấm nút **"Admin"** (Quick Fill) để đăng nhập ngay tài khoản Quản trị viên (`admin` / `admin123`).
   * Bấm nút **"Employee"** (Quick Fill) để đăng nhập ngay tài khoản Nhân viên (`nv01` / `123456`).
