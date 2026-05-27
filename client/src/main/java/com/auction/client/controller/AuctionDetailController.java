package com.auction.client.controller;

import com.auction.client.components.PriceChart;
import com.auction.client.components.TimerLabel;
import com.auction.client.model.ClientModel;
import com.auction.client.network.RealtimeListener;
import com.auction.client.network.ServerConnection;
import com.auction.client.service.DataService;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.entity.BidTransaction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

        if (currentAuction.getEndTime() != null && timerLabel != null) {
            timerLabel.startCountdown(currentAuction.getEndTime());
        }
    }

    private void subscribeRealtime() {
        realtimeListener.registerCallback("BID_UPDATE", this::onBidReceived);
        realtimeListener.registerCallback("AUCTION_UPDATE", this::onAuctionUpdate);
        try {
            ServerConnection.getInstance().sendRequest("SUBSCRIBE");
        } catch (Exception e) {
            System.err.println("Subscribe error: " + e.getMessage());
        }
    }

    @FXML
    public void handlePlaceBid() {
        try {
            placeBid(Double.parseDouble(bidAmountField.getText().trim()));
        } catch (NumberFormatException e) {
            showError("Enter a valid bid amount.");
        }
    }

    public void placeBid(double amount) {
        if (currentAuction == null) {
            return;
        }
        if (amount <= currentAuction.getCurrentPrice()) {
            showError("Bid must be higher than " + formatCurrency(currentAuction.getCurrentPrice()));
            return;
        }

        String userId = clientModel.getCurrentUser() != null ? clientModel.getCurrentUser().getId() : "unknown";
        try {
            String response = ServerConnection.getInstance().sendRequest(
                    "PLACE_BID:" + currentAuction.getId() + ":" + userId + ":" + amount + ":false"
            );
            if (response != null && response.startsWith("BID_OK")) {
                bidAmountField.clear();
                showSuccess("Bid placed successfully.");
            } else {
                showError("Bid failed: " + response);
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
        if (currentAuction == null) {
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
        String suffix = bid.isAutoBid() ? " (auto)" : "";
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
        return String.format("VND %,.0f", amount);
    }
}
