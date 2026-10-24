# BỘ QUY CHUẨN PHÁT TRIỂN ỨNG DỤNG ANDROID (UNIVERSAL ANDROID EXAM & PROJECT FRAMEWORK)

> 📌 **Tài liệu dùng chung cho mọi đề bài thi/đồ án Android (Quản lý Cửa hàng, Quản lý Khách sạn, Thư viện, Bán xe, Sinh viên, Thuốc/Dược phẩm, Vé xe/máy bay, v.v.)**  
> Toàn bộ thành viên trong nhóm và AI Agent **bắt buộc tuân thủ 100%** bộ quy chuẩn này để đảm bảo: **Code cực nhanh – Dễ hiểu dễ đọc – Tối ưu hiệu năng – Chấm điểm 100% không lỗi – Giao diện đẹp chuẩn Material 3 trên mọi dòng máy.**

---

## I. NGUYÊN TẮC CỐT TỬ ĐỂ ĐẠT ĐIỂM TỐI ĐA (CORE PRINCIPLES)

1. **Tốc độ và tính ổn định là số 1**:
   * Chỉ sử dụng **Android SDK thuần + Material 3 có sẵn trong Gradle**. Tuyệt đối **không** cài thêm thư viện ngoài (Room KSP/KAPT, Retrofit, Glide...) nếu đề không bắt buộc, nhằm triệt tiêu 100% nguy cơ lỗi build/lỗi sync trong phòng thi.
2. **Khóa chế độ sáng (Force Light Mode)**:
   * **Bắt buộc**: Nhiều máy chấm thi (OnePlus, Xiaomi, Samsung, Oppo) bật Dark Mode của hệ điều hành sẽ tự động ép nền CardView thành đen, làm chữ bị "tàng hình". Toàn bộ app phải ép cứng Light Mode.
3. **Tránh tràn thanh trạng thái (Status Bar Insets)**:
   * Mọi màn hình phải có padding cho Status Bar để camera nốt ruồi, biểu tượng pin, sóng không bao giờ đè lên tiêu đề.
4. **Seed dữ liệu mẫu tự động**:
   * Luôn chèn sẵn 5–10 bản ghi mẫu ngay trong `onCreate()` của Database để mở app lên là chấm được ngay, không mất thời gian nhập tay.

---

## II. CẤU TRÚC THƯ MỤC TIÊU CHUẨN (LAYERED ARCHITECTURE: MODEL – REPOSITORY – SERVICE – CONTROLLER)

Áp dụng mô hình phân tầng chuẩn doanh nghiệp cho toàn bộ dự án:

```text
com.example.<ten_du_an>/
│
├── model/                               # 1. Thực thể dữ liệu thuần túy (Domain Entities / POJO)
│   ├── Account.java                     # Tài khoản đăng nhập (id, username, password, role)
│   ├── Fruit.java                       # Thực thể chính (Hoa quả: tên, loại, giá, tồn kho, hạn dùng, mô tả)
│   ├── Customer.java                    # Thực thể phụ (Khách hàng: fullname, phone, email, accountId)
│   └── Order.java                       # Đơn hàng / Giao dịch (customerId, fruitName, qty, date, amount, status)
│
├── repository/                          # 2. Tầng truy xuất & lưu trữ dữ liệu (Data Access / Persistence)
│   ├── DatabaseHelper.java              # Quản lý SQLite schema, version, singleton kết nối & seed data ban đầu
│   ├── AccountRepository.java           # Truy vấn tài khoản, kiểm tra đăng nhập, đăng ký
│   ├── CustomerRepository.java          # Truy vấn & lưu trữ hồ sơ khách hàng
│   ├── FruitRepository.java             # Thao tác CRUD, tìm kiếm, đọc & trừ tồn kho hoa quả
│   ├── OrderRepository.java             # Quản lý đơn hàng, JOIN tên khách, Transaction trừ kho nguyên tử
│   └── PreferenceRepository.java        # Lưu & đọc phiên đăng nhập người dùng (SharedPreferences)
│
├── service/                             # 3. Tầng xử lý Logic Nghiệp vụ (Business Logic)
│   ├── ServiceResult.java               # Đối tượng chuẩn hóa kết quả nghiệp vụ (success, message, data)
│   ├── AuthService.java                 # Nghiệp vụ đăng nhập, đăng ký, phân quyền, điều phối session
│   ├── FruitService.java                # Nghiệp vụ kiểm tra hợp lệ, hạn sử dụng, tồn kho, lọc quả còn hạn
│   └── OrderService.java                # Nghiệp vụ tính tiền, kiểm tra hạn/kho, đặt hàng nguyên tử
│
├── controller/                          # 4. Tầng điều khiển giao diện & sự kiện (UI & Controllers)
│   ├── LoginActivity.java               # Màn hình Đăng nhập & Đăng ký
│   ├── AdminMainActivity.java           # Màn hình Quản trị viên (CRUD hoa quả, duyệt trạng thái đơn)
│   ├── CustomerMainActivity.java        # Màn hình Khách hàng (Mua sắm, lọc quả còn hạn, lịch sử đơn)
│   └── adapter/                         # Adapter hiển thị danh sách của Android UI
│       ├── FruitAdapter.java            # Hiển thị hoa quả (hỗ trợ phân quyền Admin/Khách qua callback)
│       └── OrderAdapter.java            # Hiển thị danh sách đơn hàng & tô màu badge trạng thái
│
└── utils/                               # 5. Tiện ích dùng chung thuần túy
    ├── DateUtils.java                   # Format ngày (yyyy-MM-dd), tính toán và so sánh ngày hết hạn
    ├── CurrencyUtils.java               # Định dạng tiền tệ VND (#,### đ)
    └── SharedPrefManager.java           # Lớp ủy quyền (Wrapper) tương thích ngược sang PreferenceRepository
```

---

## III. QUY CHUẨN VIẾT CODE DỄ HIỂU & DỄ ĐỌC (CLEAN CODE RULES)

Mục tiêu: Bất kỳ ai trong nhóm hoặc giáo viên chấm bài mở file code ra đều hiểu ngay logic trong 30 giây.

### 1. Đặt tên có nghĩa & bộc lộ mục đích (Intention-Revealing Names)
* **Tên biến**: Phải diễn đạt đúng thứ nó chứa, không dùng `a`, `b`, `temp`, `data1`, `obj`.
  * ✅ Đúng: `fruitList`, `totalAmount`, `currentUserRole`, `isExpired`
  * ❌ Sai: `list`, `num`, `val`, `flag`
* **Tên hàm**: Bắt đầu bằng động từ chỉ rõ hành động:
  * ✅ Đúng: `calculateTotalPrice()`, `filterFruitsByCategory()`, `isDateExpired()`
  * ❌ Sai: `price()`, `data()`, `handle()`

### 2. Triệt tiêu Magic Strings & Magic Numbers (Dùng hằng số CONSTANTS)
Không viết chuỗi hoặc số "chết" rải rác trong code. Gom toàn bộ vào `static final`:
```java
// ✅ ĐÚNG: Khai báo rõ ràng ở đầu Model hoặc DatabaseHelper
public static final String ROLE_ADMIN = "ADMIN";
public static final String ROLE_CUSTOMER = "CUSTOMER";
public static final String STATUS_PENDING = "Pending";
public static final String STATUS_CONFIRMED = "Confirmed";
public static final String STATUS_COMPLETED = "Completed";

// ❌ SAI: Viết chuỗi cứng rải rác khắp nơi
if (user.getRole().equals("ADMIN")) { ... }
```

### 3. Quy tắc "Bảo vệ đầu dòng" (Guard Clauses / Early Return)
Tránh lồng ghép `if-else` quá 3 tầng tạo thành "kim tự tháp thụt lề". Kiểm tra điều kiện lỗi trước và thoát ngay (`return`):
```java
// ✅ ĐÚNG: Kiểm tra lỗi trước, code chính thẳng hàng, cực kỳ sạch sẽ
private void handleOrderSubmit() {
    String qtyStr = etQuantity.getText().toString().trim();
    if (qtyStr.isEmpty()) {
        showToast("Vui lòng nhập số lượng!");
        return;
    }
    
    int qty = Integer.parseInt(qtyStr);
    if (qty <= 0) {
        showToast("Số lượng phải lớn hơn 0!");
        return;
    }
    
    if (qty > availableStock) {
        showToast("Vượt quá số lượng tồn kho!");
        return;
    }

    // Logic chính thực hiện ở đây, không bị thụt lề sâu
    processOrderCreation(qty);
}
```

### 4. Mỗi hàm chỉ làm duy nhất một việc (Single Responsibility)
* Độ dài mỗi hàm **không nên vượt quá 30 dòng**.
* Nếu một hàm vừa bắt sự kiện, vừa validate, vừa tính toán tiền, vừa lưu CSDL ➔ Hãy tách thành 3 hàm con riêng biệt:
  * `validateInput()`
  * `calculateAmount()`
  * `saveToDatabase()`

### 5. Chuẩn đặt tên biến và ID giao diện (XML ID)
* `TextView`: `tv...` (ví dụ: `tvTitle`, `tvPrice`, `tvStatusBadge`)
* `EditText`: `et...` (ví dụ: `etSearch`, `etQuantity`, `etUsername`)
* `Button`: `btn...` (ví dụ: `btnLogin`, `btnSubmit`, `btnEdit`, `btnDelete`)
* `RecyclerView`: `rv...` (ví dụ: `rvItemList`, `rvOrderHistory`)
* `Layout/Container`: `layout...` (ví dụ: `layoutTopHeader`, `layoutSearchArea`)

### 6. Bắt buộc viết Comment bằng Tiếng Việt ngắn gọn kiểu sinh viên (KHÔNG dài dòng văn mẫu)
* **Mục tiêu**: Khi đi thi, giáo viên chỉ tay vào bất kỳ hàm nào hỏi: *"Đoạn này làm gì?"*, sinh viên liếc mắt 1 giây là hiểu ngay để trả lời dõng dạc.
* **Nguyên tắc comment**:
  * **Ngắn gọn, súc tích**: Mỗi hàm chỉ cần **đúng 1 dòng** ngắn gọn nói thẳng việc nó làm.
  * **Tuyệt đối KHÔNG viết văn mẫu/Javadoc dài dòng**: Không dùng `/** ... @param ... @return */` rườm rà chiếm diện tích.
  * **Ví dụ mẫu**:
    * ✅ Đúng: `// Thêm hoa quả mới vào database`
    * ✅ Đúng: `// Trừ số lượng tồn kho khi khách đặt`
    * ✅ Đúng: `// Đóng Cursor để giải phóng RAM`
    * ✅ Đúng: `// 2 nút điền nhanh giúp thầy cô bấm test trong 1 giây`
    * ❌ Sai: Viết 4-5 dòng mô tả giải thích học thuật dài dòng.

---

## IV. QUY CHUẨN TỐI ƯU HIỆU NĂNG & AN TOÀN BỘ NHỚ (PERFORMANCE & MEMORY)

### 1. Tối ưu SQLite & Con trỏ Cursor
* **Luôn đóng Cursor**: Quên đóng Cursor là nguyên nhân số 1 gây rò rỉ bộ nhớ (Memory Leak) và văng app khi thi:
  ```java
  Cursor cursor = db.rawQuery(...);
  try {
      if (cursor != null && cursor.moveToFirst()) {
          do {
              // Đọc dữ liệu...
          } while (cursor.moveToNext());
      }
  } finally {
      if (cursor != null) cursor.close(); // BẮT BUỘC ĐÓNG
  }
  ```
* **Dùng Transaction khi ghi nhiều bản ghi**: Khi seed data mẫu hoặc thêm nhiều dòng, bọc vào `db.beginTransaction()` để tăng tốc độ ghi nhanh gấp 50 lần:
  ```java
  db.beginTransaction();
  try {
      // Thực hiện nhiều lệnh insert/update...
      db.setTransactionSuccessful();
  } finally {
      db.endTransaction();
  }
  ```
* **Tránh N+1 Query**: Tuyệt đối không viết câu lệnh query bên trong vòng lặp `for`. Hãy dùng câu lệnh `JOIN` của SQL để lấy dữ liệu liên kết trong 1 lần truy vấn duy nhất.

### 2. Tối ưu RecyclerView & Adapter
* **Không khởi tạo đối tượng nặng trong `onBindViewHolder`**:
  * Các đối tượng như `SimpleDateFormat` hoặc `DecimalFormat` tiêu tốn nhiều tài nguyên khởi tạo. **Khai báo `static final` dùng chung** trong class tiện ích (`DateUtils`), không tạo mới `new SimpleDateFormat()` mỗi khi cuộn danh sách.
* **Tận dụng ViewHolder**: Không gọi `findViewById()` bên ngoài constructor của ViewHolder.

### 3. Phòng thủ lỗi dữ liệu người dùng (Defensive Coding)
* **Luôn bọc `try-catch` khi parse số từ giao diện**: Người dùng có thể vô tình nhập ký tự đặc biệt hoặc số quá lớn gây văng app:
  ```java
  int quantity = 0;
  try {
      quantity = Integer.parseInt(qtyStr);
  } catch (NumberFormatException e) {
      showToast("Số lượng nhập vào không hợp lệ!");
      return;
  }
  ```
* **Chống rò rỉ Context**: Trong các class Singleton hoặc Quản trị viên (`SharedPrefManager`, `DatabaseHelper`), luôn truyền vào `context.getApplicationContext()` thay vì Activity context.

---

## V. HỆ THỐNG THIẾT KẾ GIAO DIỆN PHỔ QUÁT (UI/UX DESIGN SYSTEM)

### 1. Bảng màu 8 vai trò cố định (`res/values/colors.xml`)

| Tên màu | Mã HEX | Mục đích sử dụng |
| :--- | :--- | :--- |
| `primary` | `#1B5E20` (hoặc `#0F172A`) | Màu chủ đạo thương hiệu, nền thanh Header trên cùng |
| `primary_accent` | `#2E7D32` (hoặc `#2563EB`) | Màu điểm nhấn nổi bật: Giá tiền, Nút bấm chính, Icon active |
| `background` | `#F4F6F8` | Nền toàn ứng dụng (xám sáng dịu mắt, tương phản cao với Card) |
| `surface` | `#FFFFFF` | Nền của CardView, Dialog, Ô nhập liệu (BẮT BUỘC TRẮNG) |
| `text_primary` | `#111827` | Tiêu đề chính, tên sản phẩm (đen than chì, siêu rõ nét) |
| `text_secondary`| `#4B5563` | Chữ phụ, thông số, ngày tháng (xám trung tính rõ nét) |
| `text_muted` | `#6B7280` | Ghi chú, gợi ý nhập liệu hint (xám mờ) |
| `card_stroke` | `#E5E7EB` | Viền của CardView, thanh tìm kiếm, ô nhập liệu (xám nhẹ) |

### 2. Quy chuẩn nhãn trạng thái (Status Badges)
Mọi nhãn trạng thái (Hết hạn, Còn hạn, Chờ duyệt, Đã xong...) đều dùng **nền màu pastel nhạt (10-15% tint) + chữ đậm màu tương ứng + viền 1dp**:

* **Hợp lệ / Thành công / Còn hàng (Success/Valid)**:
  * Nền: `#DCFCE7` | Chữ: `#15803D` | Viền: `#BBF7D0`
* **Cảnh báo / Quá hạn / Hết hàng / Hủy (Danger/Expired)**:
  * Nền: `#FEE2E2` | Chữ: `#B91C1C` | Viền: `#FECACA`
* **Chờ duyệt / Đang chờ (Pending/Warning)**:
  * Nền: `#FEF3C7` | Chữ: `#B45309` | Viền: `#FDE68A`
* **Đã xác nhận / Đang xử lý (Info/Processing)**:
  * Nền: `#DBEAFE` | Chữ: `#1D4ED8` | Viền: `#BFDBFE`
* **Nhãn phân loại / Danh mục (Category Tag)**:
  * Nền: `#F3F4F6` | Chữ: `#374151` | Viền: `#E5E7EB`

### 3. Quy cách CardView & Nút thao tác
* **CardView**: Bo góc `16dp`, nền `@color/surface` (`#FFFFFF`), viền `@color/card_stroke` độ dày `1dp`, độ nổi `app:cardElevation="2dp"`.
* **Nút Sửa**: Nền `#EFF6FF`, viền `#BFDBFE`, chữ & icon `#1D4ED8`.
* **Nút Xóa**: Nền `#FEF2F2`, viền `#FECACA`, chữ & icon `#DC2626`. Luôn hiển thị `AlertDialog` xác nhận trước khi xóa.
* **Nút Hành Động Chính (Đặt hàng / Mượn sách / Thuê phòng)**: Nền `@color/primary_accent`, chữ trắng bold. Nếu không đủ điều kiện (hết hàng, hết hạn) ➔ Nền xám `#E5E7EB`, chữ `#9CA3AF`, vô hiệu hóa click (`setEnabled(false)`).

### 4. Quy tắc tránh đè Status Bar
Trong `onCreate()` của mọi Activity có Header, thêm đoạn code:
```java
View topHeader = findViewById(R.id.layoutTopHeader);
if (topHeader != null) {
    ViewCompat.setOnApplyWindowInsetsListener(topHeader, (v, insets) -> {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(v.getPaddingLeft(), systemBars.top + 16, v.getPaddingRight(), 16);
        return insets;
    });
}
```

---

## VI. BÍ QUYẾT TRIỂN KHAI NHANH TRONG PHÒNG THI (EXAM SHORTCUTS)

### 1. Form thao tác: Luôn dùng `AlertDialog` thay vì tạo Activity mới
* Tiết kiệm 70% thời gian: Mọi thao tác **Thêm mới, Cập nhật, Đặt hàng/Giao dịch** đều dùng `AlertDialog.Builder` gắn với một file layout dialog nhỏ (`dialog_item.xml`).
* Tránh tạo quá nhiều Activity vì mất thời gian khai báo `AndroidManifest.xml` và truyền dữ liệu qua lại bằng `Intent`.

### 2. Bí quyết Login: Luôn tạo 2 nút "Điền Nhanh" (Quick Fill)
Tại `activity_login.xml`, luôn đặt 2 nút hỗ trợ chấm bài:
* `btnQuickAdmin`: Tự điền tài khoản Admin (ví dụ: `admin` / `admin123`)
* `btnQuickUser`: Tự điền tài khoản Người dùng (ví dụ: `user1` / `user123`)
* Giúp giám khảo mở app lên bấm 1 chạm là vào test được ngay, tạo ấn tượng chuyên nghiệp vượt trội.

### 3. Tìm kiếm thời gian thực (Live Search)
Gắn `TextWatcher` vào ô tìm kiếm:
```java
etSearch.addTextChangedListener(new TextWatcher() {
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        filterData(s.toString().trim());
    }
    @Override public void afterTextChanged(Editable s) {}
});
```

### 4. Xử lý Ngày tháng & Số liệu
* Toàn bộ ngày tháng lưu trong SQLite thống nhất chuẩn **`yyyy-MM-dd`**.
* Tiền tệ / Số lượng lớn luôn format qua hàm tiện ích: `formatCurrency(double amount)`.

---

## VII. CHECKLIST CÁC BƯỚC THẦN TỐC KHI TRIỂN KHAI LAYERED ARCHITECTURE

Khi triển khai dự án hoặc làm bài thi, thực hiện theo đúng thứ tự phân tầng từ dưới lên:

1. **Bước 1 - Model & Database Helper**:
   * Phân tích các bảng đề bài yêu cầu. Tạo các file POJO trong `model/`.
   * Viết `repository/DatabaseHelper.java`: Định nghĩa hằng số bảng/cột, câu lệnh tạo bảng + viết hàm `seedInitialData()` nạp sẵn bản ghi mẫu (trong đó cố tình cho 1 sản phẩm đã hết hạn để test chặn đặt hàng).
2. **Bước 2 - Repository Layer (Truy cập dữ liệu)**:
   * Viết `AccountRepository`, `CustomerRepository`, `FruitRepository`, `OrderRepository`.
   * Đặc biệt: Với thao tác đặt hàng + trừ tồn kho, viết hàm bọc SQLite Transaction nguyên tử trong `OrderRepository`.
   * Viết `PreferenceRepository` quản lý lưu phiên SharedPreferences.
3. **Bước 3 - Service Layer (Nghiệp vụ cốt lõi)**:
   * Viết `AuthService`: Đăng nhập, đăng ký, phân quyền role, lưu session.
   * Viết `FruitService`: Kiểm tra dữ liệu thêm/sửa, logic hết hạn (`DateUtils.isExpired`), kiểm tra số lượng tồn kho.
   * Viết `OrderService`: Kiểm tra chặn đặt hàng khi hết hạn/hết kho, tính tổng tiền, gọi repository tạo đơn.
4. **Bước 4 - Controller & Adapter (Giao diện & Sự kiện)**:
   * Viết `FruitAdapter` và `OrderAdapter` trong `controller/adapter/` với callback interface gửi sự kiện về Activity.
   * Tạo `LoginActivity`: Gọi `AuthService`, có 2 nút Quick Fill (Admin / Customer).
   * Tạo `AdminMainActivity`: Gọi `FruitService` và `OrderService` (Dùng Dialog Thêm/Sửa quả, cập nhật trạng thái đơn).
   * Tạo `CustomerMainActivity`: Gọi `FruitService` và `OrderService` (Xem quả, lọc quả còn hạn, đặt hàng, xem lịch sử).
5. **Bước 5 - Build & Kiểm thử**:
   * Chạy `./gradlew assembleDebug` đảm bảo build thành công 100% không lỗi.
   * Chạy `./gradlew testDebugUnitTest` kiểm tra toàn bộ unit test logic nghiệp vụ.
   * Chạy app trên máy ảo hoặc thiết bị thật để xác nhận tương tác mượt mà.

