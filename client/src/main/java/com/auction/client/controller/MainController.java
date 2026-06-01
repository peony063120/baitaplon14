package com.auction.client.controller;

import com.auction.client.ClientApp;
import com.auction.client.config.AppConfig;
import com.auction.client.model.ClientModel;
import com.auction.client.model.NotificationStore;
import com.auction.client.network.MessageProtocol;
import com.auction.client.network.RealtimeListener;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.entity.Bidder;
import com.auction.common.entity.User;
import com.auction.common.enums.AuctionStatus;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainController {

    @FXML private TextField searchField;
    @FXML private Label welcomeLabel;
    @FXML private Label clockLabel;
    @FXML private Text balanceLabel;
    @FXML private StackPane contentPane;
    @FXML private VBox sellerMenu;
    @FXML private ToggleButton homeNav;
    @FXML private ToggleButton liveNav;
    @FXML private ToggleButton historyNav;
    @FXML private ToggleButton walletNav;
    @FXML private ToggleButton profileNav;
    @FXML private ToggleButton catAll;
    @FXML private ToggleButton catXe;
    @FXML private ToggleButton catDienTu;
    @FXML private ToggleButton catNgheThuat;
    @FXML private ToggleButton catTrangSuc;
    @FXML private ToggleButton catBatDongSan;
    @FXML private ToggleButton catDongHo;
    @FXML private ToggleButton catCoVat;

    private static MainController instance;

    private final ClientModel clientModel = ClientModel.getInstance();
    private DashboardController dashboardController;
    private Timeline clockTimer;
    private final Map<String, AuctionStatus> auctionStatusCache = new ConcurrentHashMap<>();
    private final Map<String, String> lastKnownWinners = new ConcurrentHashMap<>();

    @FXML
    public void initialize() {
        instance = this;
        updateHeader();
        startClockTimer();
        subscribeRealtimeNotifications();
        showDashboard();
    }

    private void subscribeRealtimeNotifications() {
        if (AppConfig.isUseMock()) {
            return;
        }
        RealtimeListener.getInstance().registerCallback(
                MessageProtocol.TYPE_AUCTION_UPDATE, this::handleAuctionUpdateNotification);
        new Thread(() -> {
            try {
                ServerConnection.getInstance().sendRequest("SUBSCRIBE");
            } catch (IOException ignored) {
                // Keep dashboard usable if subscribe fails.
            }
        }, "subscribe-realtime").start();
    }

    private void handleAuctionUpdateNotification(Object data) {
        if (!(data instanceof AuctionDTO dto) || dto.getId() == null) {
            return;
        }

        String auctionId = dto.getId();
        String itemLabel = dto.getItemName() != null && !dto.getItemName().isBlank()
                ? dto.getItemName() : auctionId;
        AuctionStatus newStatus = dto.getStatus();
        AuctionStatus previousStatus = auctionStatusCache.put(auctionId, newStatus);

        if (previousStatus != null && newStatus != null && previousStatus != newStatus) {
            if (newStatus == AuctionStatus.RUNNING && previousStatus == AuctionStatus.OPEN) {
                NotificationStore.getInstance().add("New auction session started: " + itemLabel);
            } else if (newStatus == AuctionStatus.FINISHED || newStatus == AuctionStatus.PAID) {
                NotificationStore.getInstance().add("Auction ended: " + itemLabel);
            }
        }

        User currentUser = clientModel.getCurrentUser();
        String myId = currentUser != null ? currentUser.getId() : null;
        String newWinner = dto.getCurrentWinnerId() != null ? dto.getCurrentWinnerId() : "";
        String previousWinner = lastKnownWinners.get(auctionId);

        if (myId != null && previousWinner != null && previousWinner.equals(myId)
                && !newWinner.isEmpty() && !newWinner.equals(myId)) {
            NotificationStore.getInstance().add("You were outbid on: " + itemLabel);
            syncBalanceFromServer();
        }
        lastKnownWinners.put(auctionId, newWinner);
    }

    public static void recordLeadingBid(String auctionId, String userId) {
        if (instance != null && auctionId != null && userId != null) {
            instance.lastKnownWinners.put(auctionId, userId);
        }
    }

    public static void seedAuctionSnapshot(java.util.List<AuctionDTO> auctions) {
        if (instance == null || auctions == null) {
            return;
        }
        for (AuctionDTO auction : auctions) {
            if (auction.getId() == null) {
                continue;
            }
            if (auction.getStatus() != null) {
                instance.auctionStatusCache.putIfAbsent(auction.getId(), auction.getStatus());
            }
            if (auction.getCurrentWinnerId() != null) {
                instance.lastKnownWinners.putIfAbsent(auction.getId(), auction.getCurrentWinnerId());
            }
        }
    }

    private void startClockTimer() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
        clockTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (clockLabel != null) {
                clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }));
        clockTimer.setCycleCount(Timeline.INDEFINITE);
        clockTimer.play();
    }

    private void updateHeader() {
        clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        User currentUser = clientModel.getCurrentUser();
        if (welcomeLabel != null) {
            if (currentUser != null) {
                String displayName = currentUser.getFullName();
                if (displayName == null || displayName.isBlank()) {
                    displayName = currentUser.getUsername();
                }
                welcomeLabel.setText("Welcome, " + displayName);
            } else {
                welcomeLabel.setText("");
            }
        }
        if (currentUser instanceof Bidder bidder) {
            balanceLabel.setText(String.format("$%,.0f", bidder.getBalance()));
        } else {
            balanceLabel.setText("$0");
        }

        boolean seller = currentUser != null && "SELLER".equalsIgnoreCase(currentUser.getRole());
        sellerMenu.setVisible(seller);
        sellerMenu.setManaged(seller);
    }

    @FXML
    public void showDashboard() {
        dashboardController = loadContent("/view/dashboard.fxml");
        selectNav(homeNav);
    }

    @FXML
    public void showLive() {
        dashboardController = loadContent("/view/dashboard.fxml");
        if (dashboardController != null) {
            dashboardController.setFilter("ending");
        }
        selectNav(liveNav);
    }

    @FXML
    public void showHistory() {
        BidHistoryController historyController = loadContent("/view/bid_history.fxml");
        selectNav(historyNav);
        User currentUser = clientModel.getCurrentUser();
        if (historyController != null && currentUser != null) {
            historyController.loadBidHistoryByUser(currentUser.getId());
        }
    }

    @FXML
    public void showProfile() {
        loadContent("/view/profile.fxml");
        selectNav(profileNav);
    }

    @FXML
    public void showWallet() {
        selectNav(walletNav);
        // CHANGED: "Wallet" & "Wallet actions are available..." -> Chuyển ngữ thông báo popup Ví điện tử
        showInfo("Wallet", "Wallet actions are available from your profile for now.");
    }

    @FXML
    public void showCreateAuction() {
        loadContent("/view/create_auction.fxml");
    }

    @FXML
    public void showMyAuctions() {
        loadContent("/view/my_auctions.fxml");
    }

    @FXML
    public void showNotifications() {
        NotificationStore store = NotificationStore.getInstance();
        if (store.isEmpty()) {
            showInfo("Notifications", "No new notifications.");
            return;
        }
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Notifications");
            alert.setHeaderText("Recent activity");
            TextArea area = new TextArea(String.join("\n", store.getMessages()));
            area.setEditable(false);
            area.setWrapText(true);
            area.setPrefRowCount(Math.min(12, store.getMessages().size() + 1));
            area.setPrefWidth(480);
            alert.getDialogPane().setContent(area);
            alert.showAndWait();
        });
    }

    @FXML
    public void onDeposit() {
        showProfile();
    }

    @FXML
    public void onWithdraw() {
        // CHANGED: "Withdraw" & "Withdrawals are not implemented yet." -> Chuyển ngữ thông báo popup Rút tiền
        showInfo("Withdraw", "Withdrawals are not implemented yet.");
    }

    @FXML
    public void onLogout() {
        clientModel.logout();
        try {
            ClientApp.showLoginScreen();
        } catch (Exception e) {
            // CHANGED: "Logout error" -> Chuyển ngữ thông báo tiêu đề lỗi Đăng xuất
            showInfo("Logout error", e.getMessage());
        }
    }

    @FXML
    public void filterByCategory(ActionEvent event) {
        if (!(event.getSource() instanceof ToggleButton button)) {
            return;
        }
        if (dashboardController == null) {
            showDashboard();
        }
        if (dashboardController == null) {
            return;
        }

        Object userData = button.getUserData();
        String category = userData != null ? userData.toString() : "all";

        if ("all".equalsIgnoreCase(category)) {
            clearSidebarCategorySelection();
            dashboardController.clearCategorySelection();
            return;
        }

        dashboardController.toggleSidebarCategory(category, button.isSelected());
        if (button.isSelected() && catAll != null) {
            catAll.setSelected(false);
        }
        if (!anySidebarCategorySelected() && catAll != null) {
            catAll.setSelected(true);
        }
    }

    private void clearSidebarCategorySelection() {
        for (ToggleButton categoryButton : sidebarCategoryButtons()) {
            if (categoryButton != null) {
                categoryButton.setSelected(false);
            }
        }
        if (catAll != null) {
            catAll.setSelected(true);
        }
    }

    private boolean anySidebarCategorySelected() {
        for (ToggleButton categoryButton : sidebarCategoryButtons()) {
            if (categoryButton != null && categoryButton.isSelected()) {
                return true;
            }
        }
        return false;
    }

    private ToggleButton[] sidebarCategoryButtons() {
        return new ToggleButton[]{
                catXe, catDienTu, catNgheThuat, catTrangSuc,
                catBatDongSan, catDongHo, catCoVat
        };
    }

    @FXML
    public void handleSearch() {
        if (dashboardController == null) {
            showDashboard();
        }
        if (dashboardController != null) {
            dashboardController.searchAuctions(searchField.getText());
        }
    }

    private <T> T loadContent(String resourcePath) {
        try {
            String resolvedPath = resourcePath != null && resourcePath.startsWith("/view/")
                    ? "/com/auction/client" + resourcePath
                    : resourcePath;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resolvedPath));
            Node view = loader.load();
            contentPane.getChildren().setAll(view);
            return loader.getController();
        } catch (IOException | RuntimeException e) {
            // CHANGED: "Screen error" & "Cannot open " -> Chuyển ngữ thông báo lỗi tải màn hình content
            showInfo("Screen error", "Cannot open " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    private void selectNav(ToggleButton selected) {
        for (ToggleButton button : new ToggleButton[]{homeNav, liveNav, historyNav, walletNav, profileNav}) {
            if (button != null) {
                button.setSelected(button == selected);
            }
        }
    }

    public static void refreshBalance() {
        if (instance != null) {
            Platform.runLater(() -> instance.updateBalanceDisplay());
        }
    }

    public static void syncBalanceFromServer() {
        User currentUser = ClientModel.getInstance().getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Bidder)) {
            return;
        }
        if (com.auction.client.config.AppConfig.isUseMock()) {
            refreshBalance();
            return;
        }
        String userId = currentUser.getId();
        new Thread(() -> {
            try {
                String response = com.auction.client.network.ServerConnection.getInstance()
                        .sendRequest("GET_BALANCE:" + userId);
                if (response != null && response.startsWith("BALANCE:")) {
                    double balance = Double.parseDouble(response.substring("BALANCE:".length()));
                    Platform.runLater(() -> {
                        ((Bidder) currentUser).setBalance(balance);
                        refreshBalance();
                    });
                }
            } catch (IOException ignored) {
                // Giữ số dư hiện tại nếu không đồng bộ được
            }
        }, "sync-balance").start();
    }

    private void updateBalanceDisplay() {
        User currentUser = clientModel.getCurrentUser();
        if (currentUser instanceof Bidder bidder) {
            balanceLabel.setText(String.format("$%,.0f", bidder.getBalance()));
        }
    }

    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.showAndWait();
        });
    }
}