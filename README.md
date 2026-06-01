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

> JavaFX is included as a Maven dependency (`org.openjfx`). You do **not** need to download the Gluon JavaFX SDK separately unless you prefer running the client manually with a custom `--module-path`.

---

## 📁 Project Structure

```
online-auction-system/
│
├── common/                          # Shared between client & server
│   └── src/main/java/com/auction/common/
│       ├── entity/                  # Core business entities
│       │   ├── Entity.java
│       │   ├── User.java
│       │   ├── Bidder.java
│       │   ├── Seller.java
│       │   ├── Admin.java
│       │   ├── Item.java
│       │   ├── Electronics.java
│       │   ├── Art.java
│       │   ├── Vehicle.java
│       │   ├── Auction.java
│       │   ├── BidTransaction.java
│       │   └── AutoBidConfig.java
│       │
│       ├── enums/                   # Enumerations
│       │   ├── AuctionStatus.java   # DRAFT, OPEN, RUNNING, FINISHED, PAID, CANCELLED
│       │   ├── UserRole.java        # BIDDER, SELLER, ADMIN
│       │   └── ItemType.java        # ELECTRONICS, ART, VEHICLE
│       │
│       ├── observer/                # Observer pattern (realtime update)
│       │   ├── Observer.java
│       │   ├── Subject.java
│       │   ├── AuctionSubject.java
│       │   └── ClientObserver.java
│       │
│       ├── strategy/                # Strategy pattern (bidding algorithms)
│       │   ├── BiddingStrategy.java
│       │   ├── NormalBiddingStrategy.java
│       │   ├── AutoBiddingStrategy.java
│       │   └── AntiSnipingStrategy.java
│       │
│       ├── factory/                 # Factory pattern
│       │   ├── ItemFactory.java
│       │   └── AuctionFactory.java
│       │
│       ├── dto/                     # Data Transfer Objects
│       │   ├── LoginRequest.java
│       │   ├── LoginResponse.java
│       │   ├── BidRequest.java
│       │   ├── AuctionDTO.java
│       │   ├── UserDTO.java
│       │   ├── AutoBidRequest.java
│       │   └── BidHistoryDTO.java
│       │
│       ├── exception/               # Custom exceptions
│       │   ├── AuctionException.java
│       │   ├── InvalidBidException.java
│       │   ├── AuctionNotFoundException.java
│       │   ├── InsufficientBalanceException.java
│       │   ├── AuthenticationException.java
│       │   └── ConcurrentBidException.java
│       │
│       └── utils/                   # Utility classes
│           ├── DateUtils.java
│           ├── ValidationUtils.java
│           ├── JsonUtils.java
│           └── PriceUtils.java
│
├── server/
│   └── src/main/java/com/auction/server/
│       ├── controller/              # Server controllers (handle client requests)
│       │   ├── AuctionController.java
│       │   ├── UserController.java
│       │   ├── BidController.java
│       │   └── ClientHandler.java   # Thread-per-client
│       │
│       ├── service/                 # Business logic layer
│       │   ├── AuctionService.java
│       │   ├── UserService.java
│       │   ├── BiddingService.java      # synchronized placeBid()
│       │   ├── AutoBidService.java      # PriorityQueue for auto-bids
│       │   ├── NotificationService.java
│       │   ├── AntiSnipingService.java  # extends bidding time on late bids
│       │   └── ConcurrentBidManager.java # handles race conditions
│       │
│       ├── dao/                     # Data Access Objects (Singleton)
│       │   ├── UserDAO.java
│       │   ├── AuctionDAO.java
│       │   ├── BidTransactionDAO.java
│       │   └── DatabaseConnection.java
│       │
│       ├── model/                   # Server-side models (business logic support)
│       │   ├── AuctionManager.java      # Singleton - manages running auctions
│       │   ├── SessionManager.java      # Singleton - manages user sessions
│       │   ├── PriceCalculator.java
│       │   └── BidQueueManager.java     # processes concurrent bids in order
│       │
│       ├── scheduler/               # Scheduled tasks (java.util.concurrent)
│       │   ├── AuctionScheduler.java
│       │   ├── StartAuctionTask.java
│       │   ├── EndAuctionTask.java      # includes anti-sniping check
│       │   └── AutoBidProcessor.java    # periodic auto-bid execution
│       │
│       ├── config/                  # Configuration classes
│       │   ├── ServerConfig.java
│       │   ├── DatabaseConfig.java
│       │   ├── AppConfig.java
│       │   └── AntiSnipingConfig.java   # threshold & extension seconds
│       │
│       ├── listener/                # Event listeners (internal events)
│       │   ├── AuctionEventListener.java
│       │   └── BidEventListener.java
│       │
│       └── ServerApp.java           # Main server entry point
│
├── client/
│   └── src/main/java/com/auction/client/
│       ├── ClientApp.java           # Main client entry point (JavaFX)
│       │
│       ├── config/                  # Client configuration
│       │   └── AppConfig.java       # USE_MOCK, AUTO_FALLBACK flags
│       │
│       ├── service/                 # Client service layer (Hybrid)
│       │   ├── DataService.java     # Singleton - decides mock vs API
│       │   └── MockDataProvider.java # Provides mock data for testing
│       │
│       ├── controller/              # GUI Controllers (FXML)
│       │   ├── LoginController.java
│       │   ├── RegisterController.java
│       │   ├── MainController.java
│       │   ├── AuctionDetailController.java   # with realtime price chart
│       │   ├── CreateAuctionController.java
│       │   ├── MyAuctionsController.java
│       │   ├── ProfileController.java
│       │   └── BidHistoryController.java
│       │
│       ├── model/                   # Client-side models
│       │   ├── ClientModel.java     # Singleton - manages user session
│       │   ├── Session.java
│       │   ├── BidHistoryModel.java
│       │   └── PriceChartModel.java
│       │
│       ├── network/                 # Network communication
│       │   ├── ServerConnection.java   # Singleton - TCP socket
│       │   ├── RequestBuilder.java
│       │   ├── ResponseHandler.java    # Parse text/JSON responses
│       │   ├── MessageProtocol.java    # JSON encoding/decoding
│       │   └── RealtimeListener.java   # Observer on client side
│       │
│       ├── components/              # Custom JavaFX components
│       │   ├── PriceChart.java         # LineChart for bid history visualization
│       │   ├── BidCard.java
│       │   ├── AuctionCard.java
│       │   ├── TimerLabel.java
│       │   └── AutoBidConfigPane.java
│       │
│       ├── view/                    # FXML files
│       │   ├── login.fxml
│       │   ├── register.fxml
│       │   ├── main.fxml
│       │   ├── auction_detail.fxml
│       │   ├── create_auction.fxml
│       │   ├── my_auctions.fxml
│       │   ├── profile.fxml
│       │   ├── bid_history.fxml
│       │   └── price_chart.fxml      # Optional, embedded in auction_detail
│       │
│       ├── styles/                  # CSS files
│       │   ├── main.css
│       │   ├── dark-theme.css
│       │   └── components.css
│       │
│       └── resources/               # Images, icons, etc.
│           ├── images/
│           │   ├── logo.png
│           │   ├── icon-bid.png
│           │   └── icon-auction.png
│           └── i18n/
│               ├── messages.properties
│               └── messages_vi.properties
│
├── test/
│   └── src/test/java/
│       ├── com/auction/server/
│       │   ├── service/
│       │   │   ├── AuctionServiceTest.java
│       │   │   ├── BiddingServiceTest.java
│       │   │   ├── AutoBidServiceTest.java
│       │   │   └── AntiSnipingServiceTest.java
│       │   ├── dao/
│       │   │   ├── UserDAOTest.java
│       │   │   └── AuctionDAOTest.java
│       │   ├── model/
│       │   │   └── AuctionManagerTest.java
│       │   └── concurrent/
│       │       └── ConcurrentBiddingTest.java
│       │
│       └── com/auction/common/
│           ├── entity/
│           │   └── EntityTest.java
│           ├── factory/
│           │   └── ItemFactoryTest.java
│           ├── observer/
│           │   └── ObserverPatternTest.java
│           └── utils/
│               └── ValidationUtilsTest.java
│
├── database/
│   ├── schema.sql
│   ├── init_data.sql
│   └── migrations/
│       ├── V1__create_users_table.sql
│       ├── V2__create_auctions_table.sql
│       ├── V3__create_bid_transactions_table.sql
│       └── V4__create_auto_bid_configs_table.sql
│
│
├── scripts/
│   ├── start-server.sh
│   ├── start-client.sh
│   └── run-tests.sh
│
├── pom.xml                          # Maven parent POM
├── common/pom.xml
├── server/pom.xml
├── client/pom.xml
├── README.md
├── LICENSE
└── .gitignore
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

> Stop the server/client if they are running before `mvn clean` (Windows locks JAR files in `target/`).

```bash
mvn clean package -DskipTests
```

### Step 2: Start the Server (ONE machine only)

On the **server PC** (example IP `192.168.1.10`):

```bash
mvn clean install -DskipTests
java -jar server/target/server-1.0-SNAPSHOT.jar
```

> Listens on port **5050** on all interfaces (`0.0.0.0`).  
> Auction data is stored in the **central server process** (in-memory cache + H2 file `./data/auctiondb` on the server machine).  
> **Do not** run `ServerApp` on Seller/Admin PCs — each extra server has its own data and admins will see different counts.

### Step 3: Start Client (every Seller / Admin / Bidder PC)

#### Configure the same server address on ALL clients

Edit `client/src/main/resources/client.properties` on **each** client machine before run (default in repo):

```properties
server.host=192.168.1.10
server.port=5050
```

`AppConfig` resolution order: JVM `-Dserver.host` → env `SERVER_HOST` → `client.properties` → `localhost`.

On the login screen you must see **`Server: 192.168.1.10:5050 (connected)`** (green).  
If it shows `localhost` or `(not connected)`, fix `client.properties` or start the central server.  
**MOCK MODE** uses local fake data and is **not** shared between PCs — keep it off for LAN tests.

#### Option A — Maven / JavaFX (recommended)

From project root (uses `client.properties` automatically):

```powershell
mvn -f client/pom.xml javafx:run
```

**Windows PowerShell** — optional override (quote each `-D`):

```powershell
mvn -f client/pom.xml javafx:run "-Dserver.host=192.168.1.10" "-Dserver.port=5050"
```

**cmd.exe / Git Bash / Linux / macOS:**

```bash
mvn -f client/pom.xml javafx:run -Dserver.host=192.168.1.10 -Dserver.port=5050
```

**Environment variables** (no `-D` quoting):

```powershell
$env:SERVER_HOST="192.168.1.10"; $env:SERVER_PORT="5050"; mvn -f client/pom.xml javafx:run
```

#### Multi-machine checklist

| Step | Action |
|------|--------|
| 1 | One `ServerApp` on `192.168.1.10:5050`, firewall allows TCP 5050 |
| 2 | Same `server.host` in `client.properties` on every PC (or same `SERVER_HOST`) |
| 3 | Login shows **connected** to that IP; MOCK MODE **off** |
| 4 | Admin dashboard: **Connected → 192.168.1.10:5050**; use **Refresh** if needed |
| 5 | Two admins must show the **same** pending/total counts (same central server) |

**Option B — Run the packaged JAR with JavaFX from Maven cache**

After `mvn package`, JavaFX JARs are downloaded to your local Maven repository.  
Use `--module-path` pointing to those JARs (do **not** use plain `java -jar` without it).

**Windows (PowerShell):**

```powershell
$jfx = "$env:USERPROFILE\.m2\repository\org\openjfx"
$mp  = "$jfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar;" +
       "$jfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar;" +
       "$jfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar;" +
       "$jfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar"
java --module-path $mp --add-modules javafx.controls,javafx.fxml `
     -Dserver.host=192.168.1.10 -Dserver.port=5050 `
     -jar client/target/client-1.0-SNAPSHOT.jar
```

**Linux / macOS (bash):**

```bash
JFX="$HOME/.m2/repository/org/openjfx"
MP="$JFX/javafx-base/21.0.2/javafx-base-21.0.2.jar:\
$JFX/javafx-controls/21.0.2/javafx-controls-21.0.2.jar:\
$JFX/javafx-fxml/21.0.2/javafx-fxml-21.0.2.jar:\
$JFX/javafx-graphics/21.0.2/javafx-graphics-21.0.2.jar"
java --module-path "$MP" --add-modules javafx.controls,javafx.fxml \
     -Dserver.host=192.168.1.10 -Dserver.port=5050 \
     -jar client/target/client-1.0-SNAPSHOT.jar
```

> On Linux/macOS, replace the classifier in the path (`-win`) with your OS variant if needed (e.g. `-linux`, `-mac`).  
> Packaged JAR reads `client.properties` inside the JAR; rebuild after changing that file, or pass `-Dserver.host=...`.  
> **No server?** MOCK MODE is for offline demo only — not for multi-PC sync.

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
- 🎥 **Video demo**: *https://drive.google.com/file/d/1TRoVN4I4KK6QrNYpKfxF1JtBK53lAjhp/view*

---

## Team Members

| Name                | ID       |
|---------------------|----------|
| Hoàng Phương Nhi    | 25023508 |
| Nguyễn Ngọc Quỳnh   | 25023524 |
| Ngô Khánh Linh      | 25023488 |
| Đặng Thị Phương Anh | 25023432 |
