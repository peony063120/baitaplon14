package com.auction.client.controller;
/**
 * Màn hình chi tiết 1 phiên đấu giá.
 * Hiển thị giá hiện tại, đếm ngược thời gian, biểu đồ giá realtime.
 * Người dùng có thể đặt giá thủ công hoặc cấu hình auto-bid.
 * Implements Observer — khi server push bid mới thì onBidReceived() tự động cập nhật UI mà không cần refresh.
 */
import com.auction.client.components.PriceChart;
import com.auction.client.components.TimerLabel;
import com.auction.client.model.ClientModel;
import com.auction.client.network.RealtimeListener;
import com.auction.client.network.ResponseHandler;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.AutoBidRequest;
import com.auction.common.dto.BidRequest;
import com.auction.common.entity.BidTransaction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AuctionDetailController {

    @FXML private Label itemNameLabel;
    @FXML private Label currentPriceLabel;
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
        String response = ServerConnection.getInstance().sendRequest("GET_AUCTION:" + auctionId);
        currentAuction = ResponseHandler.parseAuction(response);

        if (currentAuction == null) {
            showError("Không tìm thấy phiên đấu giá");
            return;
        }

        Platform.runLater(() -> {
            itemNameLabel.setText(currentAuction.getItemName());
            currentPriceLabel.setText(String.format("%.0f VNĐ", currentAuction.getCurrentPrice()));
            currentWinnerLabel.setText(currentAuction.getCurrentWinnerName() != null ? currentAuction.getCurrentWinnerName() : "Chưa có");
            statusLabel.setText(currentAuction.getStatus().getDisplayName());

            if (currentAuction.getEndTime() != null) {
                timerLabel.startCountdown(currentAuction.getEndTime());
            }
        });

        // Đăng kí nhận realtime update (Observer Pattern)
        realtimeListener.registerCallback("BID_UPDATE", this::onBidReceived);
        realtimeListener.registerCallback("AUCTION_UPDATE", this::onAuctionUpdate);

        ServerConnection.getInstance().sendRequest("SUBSCRIBE");
        getBidHistory();
    }

    @FXML
    public void placeBid(double amount) {
        if (currentAuction == null) return;
        if (amount <= currentAuction.getCurrentPrice()) {
            showError(String.format("Giá phải cao hơn %.0f VNĐ", currentAuction.getCurrentPrice()));
            return;
        }

        String userId = clientModel.getCurrentUser().getId();
        BidRequest req = new BidRequest(currentAuction.getId(), userId, amount, false);
        String response = ServerConnection.getInstance().sendRequest("PLACE_BID:" + req.getAuctionId()
                + ":" + req.getBidderId()
                + ":" + req.getAmount()
                + ":" + req.isAutoBid());

        if (response != null && response.startsWith("BID_OK")) {
            errorLabel.setVisible(false);
            bidAmountField.clear();
        } else {
            showError("Đặt giá thất bại: " + response);
        }
    }

    @FXML
    public void handlePlaceBid() {
        try {
            double amount = Double.parseDouble(bidAmountField.getText().trim());
            placeBid(amount);
        } catch (NumberFormatException e) {
            showError("Vui lòng nhập số tiền hợp lệ");
        }
    }

    @FXML
    public void configureAutoBid() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/auto_bid_config.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Cấu hình Auto Bid");
            stage.show();
        } catch (Exception e) {
            showError("Lỗi mở cấu hình auto bid: " + e.getMessage());
        }
    }

    // Observer callback - đc gọi khi server push bid mới
    public void onBidReceived(Object data) {
        if (!(data instanceof BidTransaction bid)) return;
        if (!bid.getAuctionId().equals(currentAuction.getId())) return;

        Platform.runLater(() -> {
            currentAuction.setCurrentPrice(bid.getAmount());
            currentPriceLabel.setText(String.format("%.0f VNĐ", bid.getAmount()));
            currentWinnerLabel.setText(bid.getBidderId());

            // cập nhật biểu đồ giá realtime
            priceChart.addPricePoint(bid.getBidTime().toEpochSecond(java.time.ZoneOffset.UTC), bid.getAmount());
        });
    }

    // Observer callback - cập nhật trạng thái auction
    public void onAuctionUpdate(Object data) {
        if (!(data instanceof AuctionDTO dto)) return;
        if (!dto.getId().equals(currentAuction.getId())) return;

        Platform.runLater(() -> {
            currentAuction = dto;
            statusLabel.setText(dto.getStatus().getDisplayName());
            currentPriceLabel.setText(String.format("%.0f VNĐ", dto.getCurrentPrice()));

            // gia hạn timer nếu anti-sniping kéo dài phiên
            if (dto.getEndTime() != null) {
                timerLabel.startCountdown(dto.getEndTime());
            }
        });
    }

    public void getBidHistory() {
        if (currentAuction == null) return;
        String response = ServerConnection.getInstance().sendRequest("GET_BID_HISTORY:" + currentAuction.getId());
        // parse và hiển thị lịch sử bid
        ResponseHandler.parseBidHistory(response).forEach(bid -> {
            Platform.runLater(() -> {
                priceChart.addPricePoint(bid.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC), bid.getAmount());
            });
        });
    }

    @FXML
    public void cancelAutoBid() {
        if (currentAuction == null) return;
        String userId = clientModel.getCurrentUser().getId();
        ServerConnection.getInstance().sendRequest("CANCEL_AUTO_BID:" + currentAuction.getId() + ":" + userId);
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        });
    }
}
