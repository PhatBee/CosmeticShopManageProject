# Frontend Cosmetic Shop

**Frontend Cosmetic Shop** là ứng dụng Android cho nền tảng thương mại điện tử chuyên về mỹ phẩm, cho phép người dùng duyệt sản phẩm, thêm vào giỏ hàng, đặt hàng, thanh toán, và quản lý tài khoản. Ứng dụng được phát triển bằng Android Java, tích hợp với backend qua các API RESTful. Dự án này được thực hiện trong khuôn khổ môn **Lập trình di động** tại Trường Đại học Sư phạm Kỹ thuật TP.HCM (HCMUTE) bởi Nhóm 38.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/PhatBee/CosmeticShopFrontend)

## Mục Lục
- [Giới Thiệu Dự Án](#giới-thiệu-dự-án)
- [Tính Năng](#tính-năng)
- [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
- [Cài Đặt](#cài-đặt)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Tích Hợp Backend](#tích-hợp-backend)
- [Các Màn Hình Giao Diện](#các-màn-hình-giao-diện)
- [Cách Sử Dụng](#cách-sử-dụng)
- [Đóng Góp](#đóng-góp)
- [Giấy Phép](#giấy-phép)
- [Liên Hệ](#liên-hệ)

## Giới Thiệu Dự Án
**Frontend Cosmetic Shop** là ứng dụng Android cho phép người dùng mua sắm mỹ phẩm trực tuyến. Ứng dụng cung cấp giao diện thân thiện, hỗ trợ các chức năng như đăng ký/đăng nhập, duyệt sản phẩm, quản lý giỏ hàng, đặt hàng, thanh toán qua VNPay, và đánh giá sản phẩm. Ứng dụng tích hợp với backend tại [CosmeticShopBackend](https://github.com/PhatBee/CosmeticShopBackend) để xử lý dữ liệu. Dự án thể hiện ứng dụng thực tiễn của lập trình di động trong thương mại điện tử.

## Tính Năng
- **Quản lý tài khoản**: Đăng ký, đăng nhập, xác thực OTP, khôi phục mật khẩu, và cập nhật thông tin cá nhân.
- **Duyệt sản phẩm**: Xem danh sách sản phẩm, tìm kiếm, lọc theo danh mục, và xem chi tiết sản phẩm.
- **Giỏ hàng**: Thêm, cập nhật, xóa sản phẩm trong giỏ hàng.
- **Đặt hàng**: Tạo đơn hàng, chọn địa chỉ giao hàng, và thanh toán (COD hoặc VNPay).
- **Quản lý đơn hàng**: Xem danh sách đơn hàng, hủy đơn hàng.
- **Đánh giá sản phẩm**: Gửi và xem đánh giá, xếp hạng sản phẩm.
- **Danh sách yêu thích**: Thêm/xóa sản phẩm vào danh sách yêu thích.
- **Quản lý địa chỉ**: Thêm, cập nhật, xóa địa chỉ giao hàng.
- **Banner quảng cáo**: Hiển thị banner khuyến mãi trên trang chủ.

## Công Nghệ Sử Dụng
- **Ngôn ngữ lập trình**: Java (Android SDK)
- **IDE**: Android Studio
- **Thư viện**:
  - **Retrofit**: Gọi API từ backend.
  - **Glide**: Tải và hiển thị hình ảnh từ Cloudinary.
  - **Material Design**: Thiết kế giao diện (Button, TextInput, RecyclerView, v.v.).
  - **VNPay SDK**: Tích hợp thanh toán VNPay.
- **Công cụ**:
  - Android Emulator hoặc thiết bị Android (API 21 trở lên).
  - Postman (kiểm tra API backend).
- **Tích hợp**:
  - Backend: [CosmeticShopBackend](https://github.com/PhatBee/CosmeticShopBackend).
  - Cloudinary: Lưu trữ hình ảnh sản phẩm.

## Cài Đặt

### Yêu Cầu
- **Android Studio**: Phiên bản mới nhất (khuyến nghị Arctic Fox hoặc cao hơn).
- **JDK**: 17 hoặc cao hơn.
- **Android SDK**: API 21 (Android 5.0) hoặc cao hơn.
- **Git**: Để clone repository.
- **Thiết bị Android**: Hoặc Android Emulator để chạy ứng dụng.
- **Backend**: Backend Cosmetic Shop đang chạy tại `http://localhost:8080` hoặc một URL công khai.

### Các Bước Cài Đặt
1. **Clone Repository**
   ```bash
   git clone https://github.com/PhatBee/CosmeticShopFrontend.git
   cd CosmeticShopFrontend
   ```

2. **Mở Dự Án Trong Android Studio**
   - Mở Android Studio.
   - Chọn **File > Open** và điều hướng đến thư mục `CosmeticShopFrontend`.
   - Chờ Android Studio đồng bộ dự án và tải các thư viện.

3. **Cấu Hình Backend URL**
   - Tìm file cấu hình API (`RetrofitClient.java` trong package `com.phatbee.cosmeticshop`).
   - Cập nhật URL backend:
     ```java
     public static final String BASE_URL = "http://localhost:8080/";
     ```
     - Nếu backend chạy trên server công khai, thay `localhost` bằng URL tương ứng (e.g., `https://your-backend-domain.com/`).

4. **Cài Đặt Thư Viện**
   - Đảm bảo các thư viện đã được khai báo trong `build.gradle` (module app):
     ```gradle
     dependencies {
         implementation 'com.squareup.retrofit2:retrofit:2.9.0'
         implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
         implementation 'com.github.bumptech.glide:glide:4.14.2'
         implementation 'com.google.android.material:material:1.10.0'
         // VNPay SDK (thêm nếu cần)
     }
     ```
   - Nhấn **Sync Project with Gradle Files** trong Android Studio.

5. **Chạy Ứng Dụng**
   - Kết nối thiết bị Android hoặc khởi động Android Emulator.
   - Nhấn **Run > Run 'app'** trong Android Studio.
   - Ứng dụng sẽ được cài đặt và chạy trên thiết bị/emulator.

6. **Kiểm Tra Kết Nối Backend**
   - Đảm bảo backend đang chạy (xem [CosmeticShopBackend README](https://github.com/PhatBee/CosmeticShopBackend)).
   - Mở ứng dụng, thử đăng nhập hoặc xem danh sách sản phẩm để kiểm tra kết nối API.

## Cấu Trúc Dự Án
```
CosmeticShopFrontend/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/phatbee/cosmeticshop/
│   │   │   │   ├── activity/      # Các Activity (MainActivity, CheckoutActivity, v.v.)
│   │   │   │   ├── adapter/       # Adapter cho RecyclerView (ProductAdapter, CartAdapter, v.v.)
│   │   │   │   ├── model/         # Model classes (Product, CartItem, Order, v.v.)
│   │   │   │   ├── retrofit/        # Retrofit setup (ApiClient, ApiService, v.v.)
│   │   │   │   └── cloudinary/          # Các lớp tiện ích (Constants, Utils, v.v.)
│   │   │   ├── res/
│   │   │   │   ├── layout/         # File XML giao diện (activity_main.xml, fragment_home.xml, v.v.)
│   │   │   │   ├── drawable/       # Hình ảnh, icon
│   │   │   │   ├── values/         # Tài nguyên (strings.xml, colors.xml, v.v.)
│   │   │   └── AndroidManifest.xml # File cấu hình ứng dụng
│   └── build.gradle                # File cấu hình module app
├── build.gradle                    # File cấu hình dự án
├── README.md                       # Tài liệu dự án
└── docs/                           # Hình ảnh giao diện, tài liệu bổ sung
```

## Tích Hợp Backend
Ứng dụng sử dụng **Retrofit** để gọi các API từ backend ([CosmeticShopBackend](https://github.com/PhatBee/CosmeticShopBackend)). Các endpoint chính bao gồm:
- **Xác thực**: `/api/auth/register`, `/api/auth/login`, `/api/auth/verify-otp`.
- **Sản phẩm**: `/api/products`, `/api/products/search`, `/api/products/category/{categoryId}`.
- **Giỏ hàng**: `/api/cart/user/{userId}`, `/api/cart/add`, `/api/cart/update`.
- **Đơn hàng**: `/api/orders/create`, `/api/orders/create-vnpay-url`.
- **Địa chỉ**: `/api/addresses/user/{userId}`, `/api/addresses/add`.
- **Wishlist**: `/api/wishlist/add`, `/api/wishlist/user/{userId}`.
- **Đánh giá**: `/api/feedback`, `/api/product-feedbacks/product/{productId}`.

### Kiểm Tra API
- Tải [Postman Collection](https://github.com/PhatBee/CosmeticShopBackend/blob/main/docs/postman_collection.json) từ backend để kiểm tra API.
- Cập nhật `BASE_URL` trong `RetrofitClient.java` để trỏ đến backend.

## Các Màn Hình Giao Diện
Ứng dụng bao gồm các màn hình chính sau:
1. **Màn hình giới thiệu**: Hiển thị logo và nút bắt đầu.
2. **Đăng nhập/Đăng ký**: Form nhập email, mật khẩu, OTP, và khôi phục mật khẩu.
3. **Trang chủ**: Hiển thị banner, danh mục, sản phẩm nổi bật, và thanh tìm kiếm.
4. **Danh sách sản phẩm**: Lọc theo danh mục, tìm kiếm sản phẩm.
5. **Chi tiết sản phẩm**: Hiển thị hình ảnh, giá, mô tả, đánh giá, và nút thêm vào giỏ hàng.
6. **Giỏ hàng**: Xem danh sách sản phẩm, cập nhật số lượng, và nút thanh toán.
7. **Thanh toán**: Chọn địa chỉ, phương thức thanh toán (COD, VNPay).
8. **Quản lý đơn hàng**: Xem danh sách đơn hàng, chi tiết đơn, và hủy đơn.
9. **Quản lý địa chỉ**: Thêm, sửa, xóa địa chỉ giao hàng.
10. **Danh sách yêu thích**: Xem và quản lý sản phẩm yêu thích.
11. **Hồ sơ người dùng**: Cập nhật thông tin cá nhân (tên, số điện thoại, giới tính).

Xem hình ảnh giao diện tại thư mục [docs/screenshots](docs/screenshots).

## Cách Sử Dụng
1. **Cài đặt ứng dụng**:
   - Build và chạy ứng dụng theo hướng dẫn ở phần [Cài Đặt](#cài-đặt).
   - Cài APK lên thiết bị Android (Settings > Allow installation from unknown sources).

2. **Đăng ký/Đăng nhập**:
   - Mở ứng dụng, vào màn hình đăng ký.
   - Nhập email, mật khẩu, và xác thực OTP gửi qua email.
   - Đăng nhập bằng email và mật khẩu đã đăng ký.

3. **Duyệt và mua sắm**:
   - Vào trang chủ, tìm kiếm sản phẩm hoặc lọc theo danh mục.
   - Nhấn vào sản phẩm để xem chi tiết, thêm vào giỏ hàng hoặc wishlist.
   - Vào giỏ hàng, chọn sản phẩm và nhấn thanh toán.

4. **Thanh toán**:
   - Chọn địa chỉ giao hàng.
   - Chọn phương thức thanh toán (COD hoặc VNPay).
   - Nếu chọn VNPay, ứng dụng sẽ chuyển hướng đến cổng thanh toán (Đang phát triểntriển)

5. **Quản lý đơn hàng**:
   - Vào mục đơn hàng để xem trạng thái (Pending, Shipped, Delivered).
   - Nhấn hủy đơn nếu cần (chỉ áp dụng với đơn Pending).

## Đóng Góp
Chúng tôi hoan nghênh mọi đóng góp! Để đóng góp:
1. Fork repository.
2. Tạo branch mới: `git checkout -b feature/your-feature`.
3. Thực hiện thay đổi và commit: `git commit -m "Thêm tính năng mới"`.
4. Push lên branch: `git push origin feature/your-feature`.
5. Tạo Pull Request trên GitHub.

