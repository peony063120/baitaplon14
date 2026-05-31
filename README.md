# Online Auction System

A Client-Server real-time auction system supporting manual bidding, auto-bid, anti-sniping, and role-based access control (Admin / Seller / Bidder).

---

## 📋 Problem Description

An online auction platform enabling:
- **Sellers** to create auction sessions for items: Art, Electronics, Vehicles.
- **Bidders** to place real-time bids or configure Auto-bid with a max price limit.
- **Admins** to manage users and monitor the system.
- **Anti-sniping**: automatically extends the auction end time when a last-minute bid is placed.
- Real-time notifications via the Observer Pattern.

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Client UI | JavaFX |
| Network | TCP Socket (text/pipe-delimited protocol) |
| Database | H2 (in-memory, auto-seeded) |
| Build tool | Maven |
| Logging | Logback |
| Testing | JUnit 5 |

---

## ⚙️ Requirements

- **JDK**: 21+
- **Maven**: 3.8+
- **JavaFX SDK 21+**: Download from https://gluonhq.com/products/javafx/ → choose your OS → extract to a folder, e.g.:
    - Windows: `C:\javafx-sdk`
    - Linux/macOS: `~/javafx-sdk`

---

## 📁 Project Structure

```
auction-system/
├── common/          # Shared: Entity, DTO, Strategy, Observer, Factory, Utils
├── server/          # Server: Socket server, Service, DAO, Scheduler
├── client/          # Client: JavaFX UI, Controller, Network layer
├── database/        # SQL schema, reference data, migrations
└── docs/            # Docs, class diagram
```

### Main Modules

**`common`** – Shared between server and client:
- `entity/` – Business objects: `Auction`, `User`, `Bidder`, `Seller`, `Item`, `BidTransaction`, ...
- `dto/` – Data Transfer Objects: `AuctionDTO`, `BidRequest`, `LoginRequest/Response`, ...
- `strategy/` – Bidding strategies: `NormalBiddingStrategy`, `AutoBiddingStrategy`, `AntiSnipingStrategy`
- `observer/` – Observer Pattern: `Subject`, `Observer`, `AuctionSubject`, `ClientObserver`
- `factory/` – Factory Pattern: `AuctionFactory`, `ItemFactory`
- `exception/` – Custom exceptions
- `utils/` – Utilities: `DateUtils`, `JsonUtils`, `PriceUtils`, `ValidationUtils`

**`server`** – Business logic:
- `controller/` – Request handlers: `AuctionController`, `BidController`, `UserController`, `ClientHandler`
- `service/` – Business services: `AuctionService`, `BiddingService`, `AutoBidService`, `AntiSnipingService`, `NotificationService`
- `dao/` – Data access: `AuctionDAO`, `UserDAO`, `BidTransactionDAO`
- `scheduler/` – Timed tasks: `AuctionScheduler`, `StartAuctionTask`, `EndAuctionTask`, `AutoBidProcessor`
- `model/` – State management: `AuctionManager`, `BidQueueManager`, `SessionManager`

**`client`** – JavaFX UI:
- `controller/` – Screen controllers: `LoginController`, `DashboardController`, `AuctionDetailController`, ...
- `network/` – Server connection: `ServerConnection`, `RealtimeListener`, `MessageProtocol`
- `view/` – FXML layout files

---

## 📦 JAR Locations

After building:

```
server/target/server-1.0-SNAPSHOT.jar
client/target/client-1.0-SNAPSHOT.jar
```

---

## 🚀 Running Instructions

### Step 1: Build the project

```bash
mvn clean package -DskipTests
```

### Step 2: Start the Server

```bash
java -jar server/target/server-1.0-SNAPSHOT.jar
```

> The server listens on port `5050` by default.  
> Seed data is loaded automatically. Disable with `-Dserver.seed=false`.

### Step 3: Start Client

> ⚠️ Client requires JavaFX SDK — **cannot run with `java -jar` directly**.

**Windows (cmd.exe or PowerShell):**
```bash
java --module-path "C:\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -jar client/target/client-1.0-SNAPSHOT.jar
```

**Linux / macOS:**
```bash
java --module-path ~/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar client/target/client-1.0-SNAPSHOT.jar
```

> Replace `C:\javafx-sdk` / `~/javafx-sdk` with your actual JavaFX SDK path.  
> **No server?** Toggle the **MOCK MODE** button on the login screen to use local mock data.

### Running Tests

```bash
mvn test
```

---

## ✅ Features

### User Management
- [x] Register / Login
- [x] View and edit profile
- [x] Role-based access: Admin / Seller / Bidder
- [x] Change password

### Auctions
- [x] Create auction with items (Art, Electronics, Vehicle)
- [x] View auction list (Dashboard)
- [x] View auction detail
- [x] View my auction history

### Bidding
- [x] Manual bidding
- [x] Auto-bid with max price limit
- [x] Anti-sniping: time extension on last-minute bids
- [x] Bid history and price chart

### Technical
- [x] Real-time TCP Socket communication
- [x] Observer Pattern for real-time notifications
- [x] Concurrent bid handling with queue
- [x] Unit tests across all modules

---

## 📄 Report & Demo

- 📝 **Report PDF**: *https://docs.google.com/document/d/1S36VNNq40cxFLd_YeehnZef-XDtD7I3eRyls9ktEyzw/edit?usp=sharing*
- 🎥 **Video demo**: *(add link here)*

---

## Team Members

| Name                | ID       | Contribution |
|---------------------|----------|-------------|
| Hoàng Phương Nhi    | 25023508 |             |
| Nguyễn Ngọc Quỳnh   | 25023524 |             |
| Ngô Khánh Linh      | 25023488 |             |
| Đặng Thị Phương Anh | 25023432 |             |
