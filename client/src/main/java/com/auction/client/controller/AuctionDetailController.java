package com.auction.client.controller;

import com.auction.client.components.PriceChart;
import com.auction.client.components.TimerLabel;
import com.auction.client.config.AppConfig;
import com.auction.client.model.ClientModel;
import com.auction.client.network.ServerConnection;
import com.auction.client.network.RealtimeListener;
import com.auction.client.service.DataService;
import com.auction.client.service.MockAuctionStore;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.BidRequest;
import com.auction.common.entity.BidTransaction;
import com.auction.common.entity.Bidder;
import com.auction.common.entity.User;
import com.auction.common.enums.AuctionStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;

public class AuctionDetailController {

    @FXML private Label itemNameLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label startingPriceLabel;
    @FXML private Label minIncrementLabel;
    @FXML private Label currentWinnerLabel;
    @FXML private Label statusLabel;
    @FXML private TextField bidAmountField;
    @FXML private VBox bidHistoryBox;
    @FXML private PriceChart priceChart;
    @FXML private TimerLabel timerLabel;
    @FXML private Label errorLabel;

    private AuctionDTO currentAuction;
    private final ClientModel clientModel = ClientModel.getInstance();
    private final RealtimeListener realtimeListener = RealtimeListener.getInstance();

    @FXML
    public void initialize() {
        bidAmountField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.isContentChange()) {
                if (!change.getControlNewText().matches("\\d*")) {
                    return null;
                }
            }
            return change;
        }));

        bidAmountField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String raw = bidAmountField.getText().replaceAll("\\D", "");
                if (!raw.isEmpty()) {
                    StringBuilder sb = new StringBuilder(raw);
                    for (int i = sb.length() - 3; i > 0; i -= 3) {
                        sb.insert(i, ' ');
                    }
                    bidAmountField.setText(sb.toString());
                }
            }
        });
    }

    public void loadAuctionDetails(String auctionId) {
        DataService.getInstance().loadAuctionDetail(
                auctionId,
                auction -> {
                    currentAuction = auction;
                    updateUI();
                    getBidHistory();
                    subscribeRealtime();
                },
                error -> showError("Cannot load auction details: " + error)
        );
    }

    private void updateUI() {
        if (currentAuction == null) {
            return;
        }

        itemNameLabel.setText(textOrDefault(currentAuction.getItemName(), "Auction item"));
        currentPriceLabel.setText(formatCurrency(currentAuction.getCurrentPrice()));
        startingPriceLabel.setText(formatCurrency(currentAuction.getStartingPrice()));
        minIncrementLabel.setText(formatCurrency(currentAuction.getMinIncrement()));
        currentWinnerLabel.setText(textOrDefault(currentAuction.getCurrentWinnerName(), "No bids yet"));
        statusLabel.setText(currentAuction.getStatus() != null ? currentAuction.getStatus().getDisplayName() : "Pending");

        double nextMinBid = currentAuction.getCurrentPrice() + currentAuction.getMinIncrement();
        bidAmountField.setText(String.format("%.0f", nextMinBid));

        if (currentAuction.getEndTime() != null && timerLabel != null) {
            timerLabel.startCountdown(currentAuction.getEndTime());
        }
    }

    private void subscribeRealtime() {
        realtimeListener.registerCallback("BID_UPDATE", this::onBidReceived);
        realtimeListener.registerCallback("AUCTION_UPDATE", this::onAuctionUpdate);
        if (AppConfig.isUseMock()) return;
        try {
            ServerConnection.getInstance().sendRequest("SUBSCRIBE");
        } catch (Exception e) {
            System.err.println("Subscribe error: " + e.getMessage());
        }
    }

    @FXML
    public void handlePlaceBid() {
        try {
            String raw = bidAmountField.getText().trim().replaceAll("\\s+", "");
            if (raw.isEmpty()) {
                showError("Please enter a bid amount.");
                return;
            }
            placeBid(Double.parseDouble(raw));
        } catch (NumberFormatException e) {
            showError("Enter a valid bid amount.");
        }
    }

    public void placeBid(double amount) {
        if (currentAuction == null) {
            return;
        }
        if (currentAuction.getStatus() != AuctionStatus.RUNNING) {
            showError("Auction is not currently running");
            return;
        }

        double minRequiredAmount = currentAuction.getCurrentPrice() + currentAuction.getMinIncrement();
        if (amount < minRequiredAmount) {
            showError("Bid must be at least " + formatCurrency(minRequiredAmount));
            return;
        }

        User currentUser = clientModel.getCurrentUser();
        String userId = currentUser != null ? currentUser.getId() : "unknown";

        if (AppConfig.isUseMock()) {
            if (!(currentUser instanceof Bidder bidder)) {
                showError("Bidder account required.");
                return;
            }
            if (bidder.getBalance() < amount) {
                showError("Insufficient balance! You have " + formatCurrency(bidder.getBalance()));
                return;
            }
            boolean ok = MockAuctionStore.getInstance().placeBid(
                    currentAuction.getId(), userId, userId, amount);
            if (ok) {
                currentAuction.setCurrentPrice(amount);
                bidder.deductBalance(amount);
                MainController.refreshBalance();
                bidAmountField.clear();
                showSuccess("Bid placed successfully.");
            } else {
                showError("Bid failed: auction not running or amount too low.");
            }
            return;
        }

        try {
            // ĐÃ KIỂM SOÁT TẬN GỐC: Gửi request đồng bộ bình thường và an toàn tuyệt đối
            String response = ServerConnection.getInstance().sendRequest(
                    "PLACE_BID:" + currentAuction.getId() + ":" + userId + ":" + amount + ":false"
            );

            if (response != null && response.startsWith("BID_OK")) {
                if (currentUser instanceof Bidder bidder) {
                    bidder.deductBalance(amount);
                    MainController.refreshBalance();
                }
                bidAmountField.clear();
                showSuccess("Bid placed successfully."); // Chữ xanh thành công hiện lên mượt mà!
                getBidHistory();
            } else {
                showError("Bid failed: " + (response != null ? response : "Unknown error"));
            }
        } catch (Exception e) {
            showError("Connection error: " + e.getMessage());
        }
    }

    @FXML
    public void configureAutoBid() {
        showError("Auto-bid configuration screen is not available yet.");
    }

    @FXML
    public void cancelAutoBid() {
        if (currentAuction == null) return;

        if (AppConfig.isUseMock()) {
            showSuccess("Auto-bid cancelled (mock).");
            return;
        }

        String userId = clientModel.getCurrentUser() != null ? clientModel.getCurrentUser().getId() : "unknown";
        try {
            ServerConnection.getInstance().sendRequest("CANCEL_AUTO_BID:" + currentAuction.getId() + ":" + userId);
            showSuccess("Auto-bid cancelled.");
        } catch (Exception e) {
            showError("Connection error: " + e.getMessage());
        }
    }

    public void onBidReceived(Object data) {
        if (!(data instanceof BidTransaction bid) || currentAuction == null) {
            return;
        }
        if (!bid.getAuctionId().equals(currentAuction.getId())) {
            return;
        }

        Platform.runLater(() -> {
            currentAuction.setCurrentPrice(bid.getAmount());
            currentAuction.setCurrentWinnerId(bid.getBidderId());
            currentPriceLabel.setText(formatCurrency(bid.getAmount()));
            currentWinnerLabel.setText(bid.getBidderId());
            if (priceChart != null) {
                priceChart.addPricePoint(bid.getTimestamp(), bid.getAmount());
            }
            addBidHistoryRow(bid);

            double nextMinBid = bid.getAmount() + currentAuction.getMinIncrement();
            bidAmountField.setText(String.format("%.0f", nextMinBid));
        });
    }

    public void onAuctionUpdate(Object data) {
        if (!(data instanceof AuctionDTO dto) || currentAuction == null || !dto.getId().equals(currentAuction.getId())) {
            return;
        }

        Platform.runLater(() -> {
            currentAuction = dto;
            updateUI();
        });
    }

    public void getBidHistory() {
        if (currentAuction == null) {
            return;
        }

        DataService.getInstance().loadBidHistory(
                currentAuction.getId(),
                history -> {
                    bidHistoryBox.getChildren().clear();
                    for (BidTransaction bid : history) {
                        addBidHistoryRow(bid);
                    }
                    if (priceChart != null) {
                        priceChart.updateWithBidHistory(history);
                    }
                },
                error -> showError("Cannot load bid history: " + error)
        );
    }

    private void addBidHistoryRow(BidTransaction bid) {
        String time = bid.getBidTime() != null ? bid.getBidTime().toString() : "";
        String suffix = bid.isAutoBid() ? " (Auto)" : "";

        bidHistoryBox.getChildren().add(new Label(
                bid.getBidderId() + " - " + formatCurrency(bid.getAmount()) + " - " + time + suffix
        ));
    }

    private void showError(String message) {
        showMessage(message, "#DC2626");
    }

    private void showSuccess(String message) {
        showMessage(message, "#16A34A");
    }

    private void showMessage(String message, String color) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: " + color + ";");
            errorLabel.setVisible(true);
        });
    }

    private String textOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String formatCurrency(double amount) {
        return String.format("$%,.0f", amount);
    }
}