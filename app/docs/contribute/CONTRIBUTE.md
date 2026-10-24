# TÀI LIỆU HƯỚNG DẪN ĐÓNG GÓP & PHÂN CÔNG CHI TIẾT (CONTRIBUTION GUIDE)

Dự án: **EMPLOYEE WORKING HOURS AND SALARY MANAGEMENT**  
Mục tiêu: Đạt điểm tối đa thông qua tốc độ hoàn thành nhanh, code chuẩn phân tầng (Layered Architecture), ổn định và kiểm thử không phát sinh lỗi.

---

## 1. QUY CHUẨN ĐÓNG GÓP & QUY TRÌNH LÀM VIỆC (WORKFLOW)

### 1.1. Chiến lược phân nhánh Git (Git Branching Model)
Mỗi thành viên làm việc trên nhánh tính năng độc lập, không commit trực tiếp vào `main`:
* `main`: Nhánh chạy ổn định, luôn build được.
* `setup-core`: Nhánh khởi tạo CSDL, Model và tiện ích dùng chung (Nguyễn Minh Hoàng).
* `feature/auth-employee`: Nhánh tính năng xác thực và quản lý nhân viên (Nguyễn Minh Hoàng).
* `feature/attendance`: Nhánh tính năng chấm công và tính giờ làm (Phạm Hải Long).
* `feature/salary`: Nhánh tính năng tính lương và quản lý bảng lương (Nguyễn Văn Hiển Vinh).

### 1.2. Quy chuẩn Commit Message
Cú pháp: `[Tên-Module] Hành động cụ thể`
* Ví dụ:
  * `[Core] Khoi tao SQLite DatabaseHelper va seed data mau`
  * `[Auth] Hoan thien LoginActivity va Quick Fill buttons`
  * `[Attendance] Implement logic chan duplicate check-in va tinh totalHours`
  * `[Salary] Tinh toan luong thang va tien overtime x1.5`

### 1.3. Quy chuẩn Kiến trúc (Layered Architecture)
Tất cả thành viên tuân thủ nghiêm ngặt 4 tầng:
1. **Model**: Chỉ chứa thuộc tính POJO, constructor, getter/setter.
2. **Repository**: Thao tác trực tiếp với SQLite, `Cursor`, câu lệnh SQL, bọc trong Transaction.
3. **Service**: Xử lý logic nghiệp vụ, tính toán, kiểm tra dữ liệu đầu vào (`ServiceResult<T>`), không chứa UI.
4. **Controller / Adapter**: Nhận dữ liệu từ Service và hiển thị lên Activity/Dialog/RecyclerView.

---

## 2. PHÂN CÔNG CHI TIẾT THEO THÀNH VIÊN (WORK BREAKDOWN STRUCTURE)

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                   PHÂN CÔNG CÔNG VIỆC                                  │
├──────────────────────────┬─────────────────────────────┬───────────────────────────────┤
│ 1. NGUYỄN MINH HOÀNG     │ 2. PHẠM HẢI LONG            │ 3. NGUYỄN VĂN HIỂN VINH       │
│ Core, Auth & Quản lý NV  │ Chấm công & Giờ làm việc    │ Tính lương & Tăng ca (OT)     │
└──────────────────────────┴─────────────────────────────┴───────────────────────────────┘
```

---

### 👤 THÀNH VIÊN 1: NGUYỄN MINH HOÀNG (Lead / Core, Auth & Employee Management)

#### Trách nhiệm chính:
Thiết lập nền móng kiến trúc, hệ thống Database dùng chung, cơ chế phân quyền tài khoản và toàn bộ tính năng quản trị nhân sự.

#### Danh sách File & Module phụ trách:
1. **Tầng Core & Utils dùng chung (Làm trước để 2 bạn pull về)**:
   * `values/colors.xml`: Thiết lập bảng màu chuẩn Material 3 (Primary, Secondary, Success, Warning, Danger, Background, Surface).
   * `utils/DateUtils.java`: Xử lý định dạng ngày tháng (`yyyy-MM-dd`, `HH:mm:ss`), tính khoảng cách giờ giữa 2 mốc thời gian.
   * `utils/CurrencyUtils.java`: Định dạng tiền tệ VND (`#,### VNĐ`).
   * `utils/SharedPrefManager.java`: Quản lý phiên đăng nhập (Session) của Admin và Employee.
   * `model/Account.java`: `(id, username, password, role)`
   * `model/Employee.java`: `(id, code, fullname, email, phone, hourlyRate, accountId)`
   * `model/WorkLog.java`: `(id, employeeId, workDate, checkInTime, checkOutTime, totalHours, status)`
   * `model/Salary.java`: `(id, employeeId, month, year, totalHours, overtimeHours, totalSalary, status)`
   * `repository/DatabaseHelper.java`: 
     * Khởi tạo 4 bảng SQLite chuẩn khóa ngoại (`FOREIGN KEY`).
     * Viết hàm `seedInitialData()`: nạp sẵn 1 Admin, 3-4 Nhân viên cùng một số bản ghi chấm công và bảng lương để mở app là test được ngay.
2. **Tầng Nghiệp vụ Xác thực & Quản lý Nhân sự**:
   * `repository/AccountRepository.java`: Tìm kiếm tài khoản theo username/password, kiểm tra role.
   * `repository/EmployeeRepository.java`: Thêm, sửa, xóa, tìm kiếm nhân viên theo mã/tên, lấy thông tin theo `accountId`.
   * `service/AuthService.java`: Logic đăng nhập, xác thực vai trò Admin / Employee, lưu session vào SharedPreferences.
   * `service/EmployeeService.java`: Validate thông tin nhân viên (mã không trùng, email/phone đúng định dạng, `hourlyRate > 0`).
3. **Tầng Giao diện (UI/Controller)**:
   * `controller/LoginActivity.java` & `activity_login.xml`: Form đăng nhập chuyên nghiệp với **2 nút Quick Fill** (`btnQuickAdmin`, `btnQuickEmployee`) để chấm thi trong 1 click.
   * `controller/AdminMainActivity.java`: Màn hình chính của Admin với thanh điều hướng hoặc tab.
   * `controller/adapter/EmployeeAdapter.java` & `item_employee.xml`: Danh sách nhân viên dạng thẻ CardView, nút Sửa/Xóa.
   * `dialog_employee.xml`: Dialog Thêm/Sửa thông tin nhân viên (bao gồm cả mức lương theo giờ `hourlyRate`).

#### Tiêu chí hoàn thành (Acceptance Criteria):
- [x] Đăng nhập đúng phân quyền: Admin vào `AdminMainActivity`, Employee vào `EmployeeMainActivity`.
- [x] Quick Fill hoạt động mượt mà, không cần gõ phím ảo khi test.
- [x] Admin thêm/sửa/xóa/tìm kiếm nhân viên thành công, CSDL cập nhật ngay lập tức.
- [x] Có dữ liệu mẫu phong phú ngay khi vừa cài app.

---

### 👤 THÀNH VIÊN 2: PHẠM HẢI LONG (Attendance Management & Working Hours Calculation)

#### Trách nhiệm chính:
Phụ trách toàn bộ chu trình chấm công hàng ngày cho nhân viên, tự động tính tổng số giờ làm việc, kiểm soát tính toàn vẹn dữ liệu (chặn duplicate, validate giờ check-out) và hiển thị lịch sử điểm danh.

#### Danh sách File & Module phụ trách:
1. **Tầng Dữ liệu & Nghiệp vụ Chấm công**:
   * `repository/WorkLogRepository.java`:
     * Thêm bản ghi check-in mới.
     * Cập nhật giờ check-out và `totalHours`.
     * `getTodayWorkLog(int employeeId)`: Lấy trạng thái chấm công trong ngày hôm nay.
     * `getWorkLogsByEmployee(int employeeId)`: Lấy toàn bộ lịch sử điểm danh của nhân viên.
     * `getWorkLogsByMonth(int employeeId, int month, int year)`: Lấy danh sách chấm công phục vụ tổng kết tháng.
   * `service/WorkLogService.java`:
     * **Xác thực Check-in**: Chặn điểm danh trùng lặp (nếu hôm nay đã có bản ghi thì không cho check-in tiếp).
     * **Xác thực Check-out**: Giờ ra phải sau giờ vào; tính `totalHours = (checkOut - checkIn)` làm tròn 1-2 chữ số thập phân.
     * **Xử lý Incomplete Records**: Phát hiện và cảnh báo các ca chưa check-out của những ngày trước.
     * **Tổng hợp giờ làm**: Tính tổng số giờ làm việc trong tháng cho một nhân viên.
2. **Tầng Giao diện (UI/Controller)**:
   * `controller/EmployeeMainActivity.java` (Phần Chấm công):
     * Card hiển thị trạng thái hôm nay: *Chưa điểm danh* / *Đang làm việc* / *Đã kết thúc ca*.
     * Nút **Check-in** (kích hoạt khi chưa vào ca) và Nút **Check-out** (kích hoạt khi đang làm việc).
     * Hiển thị đồng hồ thời gian thực và tổng giờ đã tích lũy trong ngày.
   * `controller/adapter/WorkLogAdapter.java` & `item_work_log.xml`:
     * Hiển thị ngày làm việc, giờ vào, giờ ra, tổng số giờ làm việc (`totalHours`), badge trạng thái (Đang làm / Đã hoàn thành).
   * Màn hình / Tab **Lịch sử Chấm công (Attendance History)**:
     * Bộ lọc theo tháng/năm để nhân viên xem lại quá trình làm việc của mình.
     * Admin cũng có thể xem lịch sử chấm công của từng nhân viên từ màn hình Admin.

#### Tiêu chí hoàn thành (Acceptance Criteria):
- [x] Check-in ghi nhận chính xác ngày và giờ hiện tại.
- [x] Không thể check-in 2 lần trong cùng một ngày (báo lỗi rõ ràng qua Toast/Snackbar).
- [x] Check-out tính toán chính xác số giờ làm việc (`totalHours`).
- [x] Danh sách lịch sử hiển thị trực quan, đúng thứ tự ngày gần nhất lên đầu.

---

### 👤 THÀNH VIÊN 3: NGUYỄN VĂN HIỂN VINH (Salary Management & Overtime Calculation)

#### Trách nhiệm chính:
Phụ trách thuật toán tự động tính toán lương tháng, tính tiền làm thêm giờ (Overtime), quản lý bảng lương toàn công ty cho Admin và xem chi tiết phiếu lương cá nhân cho Nhân viên.

#### Danh sách File & Module phụ trách:
1. **Tầng Dữ liệu & Nghiệp vụ Lương**:
   * `repository/SalaryRepository.java`:
     * Tạo/cập nhật bản ghi lương cho nhân viên theo từng tháng/năm.
     * Lấy danh sách bảng lương toàn bộ nhân viên theo tháng/năm (dành cho Admin).
     * Lấy thông tin lương chi tiết của một nhân viên theo tháng/năm.
     * Cập nhật trạng thái chi trả: `Pending` (Chờ duyệt), `Calculated` (Đã tính), `Paid` (Đã thanh toán).
   * `service/SalaryService.java`:
     * **Thuật toán tính Overtime**:
       * Giờ tiêu chuẩn: 8 giờ/ngày (hoặc 160 giờ/tháng). Số giờ vượt quá được tính vào `overtimeHours`.
       * Hệ số làm thêm giờ: `OVERTIME_RATE = 1.5` (150% đơn giá lương cơ bản).
     * **Thuật toán tính Tổng Lương**:
       $$\text{Lương giờ thường} = (\text{totalHours} - \text{overtimeHours}) \times \text{hourlyRate}$$
       $$\text{Lương tăng ca} = \text{overtimeHours} \times \text{hourlyRate} \times 1.5$$
       $$\text{totalSalary} = \text{Lương giờ thường} + \text{Lương tăng ca}$$
     * **Chức năng Tính Lương Hàng Loạt (Batch Calculation)**: Duyệt qua tất cả nhân viên trong tháng, tổng hợp giờ từ `WorkLogRepository` và sinh ra bản ghi `Salary` tương ứng.
2. **Tầng Giao diện (UI/Controller)**:
   * `controller/adapter/SalaryAdapter.java` & `item_salary.xml`:
     * Card hiển thị: Tên/Mã nhân viên, Tháng/Năm, Tổng giờ làm, Giờ OT, Mức lương theo giờ, Tổng lương VND, Badge trạng thái.
   * **Giao diện Quản lý Bảng lương (Phía Admin)**:
     * Bộ chọn Tháng/Năm, danh sách bảng lương toàn công ty.
     * Nút **"Tính lương tháng"** (Tự động quét và tính lương tất cả nhân viên).
     * Thao tác đổi trạng thái chi trả: `Chuyển sang Đã thanh toán (Paid)`.
   * **Giao diện Xem Phiếu lương (Phía Nhân viên)**:
     * Tab/Dialog hiển thị chi tiết thu nhập từng tháng: Giờ làm thường, giờ làm thêm, hệ số, tổng tiền thực lĩnh có định dạng VND rõ ràng.

#### Tiêu chí hoàn thành (Acceptance Criteria):
- [x] Tự động tính đúng lương và giờ OT theo đúng công thức quy định.
- [x] Admin tính lương tháng tự động chỉ với 1 click.
- [x] Trạng thái bảng lương cập nhật linh hoạt (`Pending` -> `Paid`) với màu sắc badge trực quan.
- [x] Nhân viên xem được chi tiết thu nhập tháng của mình một cách minh bạch.

---

## 3. TIẾN ĐỘ THỰC HIỆN & ĐIỂM CHẠM TÍCH HỢP (INTEGRATION MILESTONES)

| Giai đoạn | Nội dung công việc | Người phụ trách chính | Đầu ra (Deliverable) |
| :--- | :--- | :--- | :--- |
| **Giai đoạn 1** | Khởi tạo CSDL SQLite, 4 Model, Tiện ích, Seed Data | Nguyễn Minh Hoàng | Nhánh `main` có sẵn DB & dữ liệu test |
| **Giai đoạn 2** | Triển khai song song 3 nhánh tính năng | Cả 3 thành viên | 3 nhánh tính năng độc lập, pass build |
| **Giai đoạn 3** | Tích hợp liên module & Kiểm thử tổng thể | Cả 3 thành viên | Merge về `main`, kiểm thử không lỗi |
| **Giai đoạn 4** | Kiểm tra UI/UX, Quick Fill, đóng gói nộp bài | Cả 3 thành viên | APK hoàn chỉnh, sẵn sàng chấm thi |

---

## 4. QUY TẮC PHÒNG NGỪA LỖI & TỐI ƯU ĐIỂM SỐ
1. **Tuyệt đối không dùng Room/KSP**: Chỉ dùng SQLite thuần của Android SDK để tránh lỗi build khi chấm bài trên máy giám khảo.
2. **Cưỡng chế Light Mode**: Không để app tự chuyển Dark Mode gây lệch màu chữ/nền.
3. **Mọi thao tác danh sách đều có Confirm Dialog**: Xóa nhân viên, chốt lương đều phải có hộp thoại xác nhận để tránh bấm nhầm.
4. **Không để màn hình trống (Empty State)**: Khi chưa có dữ liệu chấm công hoặc lương, hiển thị thông báo rõ ràng "Chưa có dữ liệu" thay vì để trống.
