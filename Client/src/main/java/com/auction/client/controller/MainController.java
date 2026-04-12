
package com.auction.client.controller;

import com.auction.client.ClientApp;
import com.auction.client.network.ServerConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @FXML private Label userInfoLabel;
    @FXML private TabPane tabPane;  // Được sử dụng, giữ lại
    @FXML private Tab myAuctionsTab;
    @FXML private TextField searchField;
    @FXML private ListView<String> auctionListView;
    @FXML private ListView<String> myAuctionsListView;
    @FXML private ListView<String> myBidsListView;
    @FXML private Label statusLabel;

    // Profile fields
    @FXML private Label profileUsername;
    @FXML private Label profileFullName;
    @FXML private Label profileEmail;
    @FXML private Label profileRole;
    @FXML private Label profileBalance;

    private String userId;
    private String userRole;
    private String username;

    // Sửa thành final
    private final ObservableList<String> auctions = FXCollections.observableArrayList();
    private final ObservableList<String> myAuctions = FXCollections.observableArrayList();
    private final ObservableList<String> myBids = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Get user info from LoginController (tạm thời dùng giá trị mẫu)
        userId = "temp-user-id";
        username = "temp-user";
        userRole = "BIDDER";

        // Setup UI based on role
        if ("SELLER".equals(userRole)) {
            myAuctionsTab.setDisable(false);
        } else {
            myAuctionsTab.setDisable(true);
        }

        // Setup list cell factories
        auctionListView.setCellFactory(TextFieldListCell.forListView());
        myAuctionsListView.setCellFactory(TextFieldListCell.forListView());
        myBidsListView.setCellFactory(TextFieldListCell.forListView());

        // Handle double-click on auction
        auctionListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = auctionListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    String auctionId = extractAuctionId(selected);
                    openAuctionDetail(auctionId);
                }
            }
        });

        // Load data
        loadAuctions();
        if ("SELLER".equals(userRole)) {
            loadMyAuctions();
        }
        loadMyBids();
        loadProfile();

        // Set user info
        userInfoLabel.setText("Welcome, " + username + " (" + userRole + ")");
    }

    // Thêm method để set user info từ LoginController
    public void setUserInfo(String userId, String username, String userRole) {
        this.userId = userId;
        this.username = username;
        this.userRole = userRole;

        // Cập nhật UI
        userInfoLabel.setText("Welcome, " + username + " (" + userRole + ")");

        if ("SELLER".equals(userRole)) {
            myAuctionsTab.setDisable(false);
            loadMyAuctions();
        } else {
            myAuctionsTab.setDisable(true);
        }

        // Reload data
        loadAuctions();
        loadMyBids();
        loadProfile();
    }

    private void loadAuctions() {
        statusLabel.setText("Loading auctions...");

        new Thread(() -> {
            try {
                String json = ServerConnection.getAuctions();
                if (json != null) {
                    List<Map<String, Object>> auctionList = mapper.readValue(json,
                            mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

                    Platform.runLater(() -> {
                        auctions.clear();
                        for (Map<String, Object> auction : auctionList) {
                            String display = String.format("%s - $%.2f (Ends: %s)",
                                    auction.get("itemId"),
                                    ((Number) auction.get("currentPrice")).doubleValue(),
                                    auction.get("endTime"));
                            auctions.add(display);
                        }
                        auctionListView.setItems(auctions);
                        statusLabel.setText(auctions.size() + " auctions available");
                    });
                }
            } catch (Exception e) {
                logger.error("Failed to load auctions", e);
                Platform.runLater(() -> {
                    statusLabel.setText("Failed to load auctions: " + e.getMessage());
                });
            }
        }).start();
    }

    private void loadMyAuctions() {
        new Thread(() -> {
            try {
                String json = ServerConnection.getAuctions();
                if (json != null) {
                    List<Map<String, Object>> auctionList = mapper.readValue(json,
                            mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

                    Platform.runLater(() -> {
                        myAuctions.clear();
                        for (Map<String, Object> auction : auctionList) {
                            String sellerId = (String) auction.get("sellerId");
                            if (userId.equals(sellerId)) {
                                String display = String.format("%s - $%.2f (Status: %s)",
                                        auction.get("itemId"),
                                        ((Number) auction.get("currentPrice")).doubleValue(),
                                        auction.get("status"));
                                myAuctions.add(display);
                            }
                        }
                        myAuctionsListView.setItems(myAuctions);
                    });
                }
            } catch (Exception e) {
                logger.error("Failed to load my auctions", e);
            }
        }).start();
    }

    private void loadMyBids() {
        new Thread(() -> {
            try {
                // Placeholder - bạn có thể implement sau
                Platform.runLater(() -> {
                    myBids.clear();
                    myBids.add("No bids yet");
                    myBidsListView.setItems(myBids);
                });
            } catch (Exception e) {
                logger.error("Failed to load my bids", e);
            }
        }).start();
    }

    private void loadProfile() {
        new Thread(() -> {
            try {
                String json = ServerConnection.getUser(userId);
                if (json != null) {
                    Map<String, Object> user = mapper.readValue(json, Map.class);

                    Platform.runLater(() -> {
                        profileUsername.setText((String) user.get("username"));
                        profileFullName.setText((String) user.get("fullName"));
                        profileEmail.setText((String) user.get("email"));
                        profileRole.setText((String) user.get("role"));
                        if (user.containsKey("balance")) {
                            profileBalance.setText(String.format("$%.2f",
                                    ((Number) user.get("balance")).doubleValue()));
                        } else {
                            profileBalance.setText("N/A");
                        }
                    });
                }
            } catch (Exception e) {
                logger.error("Failed to load profile", e);
                Platform.runLater(() -> {
                    profileBalance.setText("Error loading profile");
                });
            }
        }).start();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase();
        if (keyword.isEmpty()) {
            auctionListView.setItems(auctions);
        } else {
            ObservableList<String> filtered = FXCollections.observableArrayList();
            for (String auction : auctions) {
                if (auction.toLowerCase().contains(keyword)) {
                    filtered.add(auction);
                }
            }
            auctionListView.setItems(filtered);
            statusLabel.setText(filtered.size() + " auctions found");
        }
    }

    @FXML
    private void handleRefresh() {
        loadAuctions();
        if ("SELLER".equals(userRole)) {
            loadMyAuctions();
        }
        loadMyBids();
    }

    @FXML
    private void handleRefreshProfile() {
        loadProfile();
    }

    @FXML
    private void handleCreateAuction() {
        try {
            ClientApp.switchScene("/view/create_auction.fxml", "Create New Auction");
        } catch (Exception e) {
            logger.error("Failed to load create auction screen", e);
            showAlert("Error", "Failed to load create auction screen", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            ServerConnection.disconnect();
            ClientApp.switchScene("/view/login.fxml", "Login");
        } catch (Exception e) {
            logger.error("Logout failed", e);
        }
    }

    private void openAuctionDetail(String auctionId) {
        try {
            // You'll need to pass auctionId to the detail screen
            showAlert("Auction Details", "Opening auction: " + auctionId, Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            logger.error("Failed to open auction detail", e);
        }
    }

    private String extractAuctionId(String display) {
        // Extract ID from display string (format: "ID - $price (Ends: time)")
        if (display.contains(" - ")) {
            return display.split(" - ")[0];
        }
        return display;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
