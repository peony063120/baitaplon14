# Online Auction System
Hệ thống đấu giá trực tuyến theo kiến trúc Client-Server, hỗ trợ đấu giá thời gian thực, tự động đặt giá (Auto-bid), và chống snipe (Anti-sniping).

---

## 📋 Mô tả bài toán

Xây dựng ứng dụng đấu giá trực tuyến cho phép:
- **Người bán (Seller)** tạo phiên đấu giá với các loại vật phẩm: Nghệ thuật, Điện tử, Phương tiện.
- **Người đấu giá (Bidder)** tham gia đặt giá theo thời gian thực hoặc cấu hình Auto-bid.
- **Quản trị viên (Admin)** quản lý người dùng và giám sát hệ thống.
- Hỗ trợ **Anti-sniping**: tự động gia hạn phiên đấu giá khi có bid vào phút cuối.
- Thông báo real-time qua mô hình Observer Pattern.

---

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17+ |
| Giao diện Client | JavaFX |
| Giao tiếp mạng | TCP Socket (JSON Protocol) |
| Cơ sở dữ liệu | MySQL / PostgreSQL |
| Migration DB | Flyway |
| Build tool | Maven |
| Logging | Logback |
| Testing | JUnit 5 |

---

## ⚙️ Yêu cầu môi trường

- **JDK**: 17 trở lên
- **Maven**: 3.8+
- **MySQL** hoặc **PostgreSQL** đang chạy
- **JavaFX SDK 17+**: Tải tại https://gluonhq.com/products/javafx/ → chọn đúng OS → giải nén ra thư mục, ví dụ:
    - Windows: `C:\javafx-sdk`
    - Linux/macOS: `~/javafx-sdk`

---

## 📁 Cấu trúc thư mục

```
auction-system/
├── common/          # Shared: Entity, DTO, Strategy, Observer, Factory, Utils
├── server/          # Server: Socket server, Service, DAO, Scheduler
├── client/          # Client: JavaFX UI, Controller, Network layer
├── database/        # SQL schema, init data, Flyway migrations
└── docs/            # Tài liệu, class diagram, logo
```

### Các module chính

**`common`** – Dùng chung giữa server và client:
- `entity/` – Các đối tượng nghiệp vụ: `Auction`, `User`, `Bidder`, `Seller`, `Item`, `BidTransaction`, ...
- `dto/` – Data Transfer Objects: `AuctionDTO`, `BidRequest`, `LoginRequest/Response`, ...
- `strategy/` – Chiến lược đặt giá: `NormalBiddingStrategy`, `AutoBiddingStrategy`, `AntiSnipingStrategy`
- `observer/` – Observer Pattern: `Subject`, `Observer`, `AuctionSubject`, `ClientObserver`
- `factory/` – Factory Pattern: `AuctionFactory`, `ItemFactory`
- `exception/` – Các exception tuỳ chỉnh
- `utils/` – Tiện ích: `DateUtils`, `JsonUtils`, `PriceUtils`, `ValidationUtils`

**`server`** – Xử lý logic nghiệp vụ:
- `controller/` – Xử lý request từ client: `AuctionController`, `BidController`, `UserController`, `ClientHandler`
- `service/` – Nghiệp vụ: `AuctionService`, `BiddingService`, `AutoBidService`, `AntiSnipingService`, `NotificationService`
- `dao/` – Truy cập CSDL: `AuctionDAO`, `UserDAO`, `BidTransactionDAO`
- `scheduler/` – Task định thời: `AuctionScheduler`, `StartAuctionTask`, `EndAuctionTask`, `AutoBidProcessor`
- `model/` – Quản lý trạng thái: `AuctionManager`, `BidQueueManager`, `SessionManager`

**`client`** – Giao diện người dùng JavaFX:
- `controller/` – Điều khiển màn hình: `LoginController`, `DashboardController`, `AuctionDetailController`, ...
- `network/` – Kết nối server: `ServerConnection`, `RealtimeListener`, `MessageProtocol`
- `view/` – FXML files cho từng màn hình

---

## 📦 Vị trí file JAR

Sau khi build, các file JAR được đặt tại:

```
server/target/server-1.0-SNAPSHOT.jar
client/target/client-1.0-SNAPSHOT.jar
```

---


## 🚀 Hướng dẫn chạy

### Bước 1: Build toàn bộ project

```bash
mvn clean package -DskipTests
```

### Bước 2: Khởi động Server

```bash
java -jar server/target/server-1.0-SNAPSHOT.jar
```

> Server mặc định lắng nghe tại cổng được cấu hình trong `ServerConfig.java` (mặc định: `9999`).

### Bước 3: Khởi động Client

> ⚠️ Client dùng JavaFX nên **không thể chạy trực tiếp bằng `java -jar`**. Cần chỉ định đường dẫn JavaFX SDK.

**Windows:**
```bash
java --module-path "C:\javafx-sdk\lib" ^
     --add-modules javafx.controls,javafx.fxml ^
     -jar client/target/client-1.0-SNAPSHOT.jar
```

**Linux / macOS:**
```bash
java --module-path ~/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar client/target/client-1.0-SNAPSHOT.jar
```

> Thay `C:\javafx-sdk` hoặc `~/javafx-sdk` bằng đường dẫn thực tế bạn đã giải nén JavaFX SDK.  
> Đảm bảo Server đã chạy **trước** khi khởi động Client.

---

## ✅ Chức năng đã hoàn thành

### Người dùng
- [x] Đăng ký / Đăng nhập
- [x] Xem và chỉnh sửa hồ sơ cá nhân
- [x] Phân quyền: Admin / Seller / Bidder

### Đấu giá
- [x] Tạo phiên đấu giá với vật phẩm (Art, Electronics, Vehicle)
- [x] Xem danh sách phiên đấu giá (Dashboard)
- [x] Xem chi tiết phiên đấu giá
- [x] Xem lịch sử đấu giá của tôi

### Đặt giá
- [x] Đặt giá thủ công (Normal Bidding)
- [x] Đặt giá tự động (Auto-bid) với giới hạn giá tối đa
- [x] Anti-sniping: gia hạn thời gian khi có bid cuối giờ
- [x] Xem lịch sử bid và biểu đồ giá

### Kỹ thuật
- [x] Giao tiếp real-time qua TCP Socket
- [x] Observer Pattern cho thông báo real-time
- [x] Xử lý đồng thời (Concurrent bid) với queue
- [x] Unit test cho toàn bộ module

---


## 📄 Báo cáo & Demo

- 📝 **Báo cáo PDF**: *(Thêm link vào đây)*
- 🎥 **Video demo**: *(Thêm link vào đây)*

---

## Thành viên
| Name                |    ID    | Contribution |
|---------------------|----------|--------------|
| Hoàng Phương Nhi    | 25023508 |  |
| Nguyễn Ngọc Quỳnh   | 25023524 |  |
| Ngô Khánh Linh      | 25023488 | |
| Đặng Thị Phương Anh | 25023432 | |

