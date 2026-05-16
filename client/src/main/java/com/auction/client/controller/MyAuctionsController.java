package com.auction.client.controller;

/**
 * Màn hình quản lí các phiên đấu giá của Seller.
 * Xem danh sách phiêm của mình, sửa/xóa phiên chưa bắt đầu,
 * bắt đầu phiên ngay lập tức, xem lịch sử bid của từng phiên
 */

import com.auction.client.model.ClientModel;
import com.auction.client.network.ResponseHandler;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.AuctionDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class MyAuctionsController {

    @FXML private VBox auctionListBox;
    @FXML private Label statusLabel;

    private final ClientModel clientModel = ClientModel.getInstance();
    private List<AuctionDTO> myAuctions;

    @FXML
    public void initialize() {
        loadMyAuctions();
    }

    public void loadMyAuctions() {
        String sellerId = clientModel.getCurrentUser().getId();
        String response = ServerConnection.getInstance().sendRequest("GET_MY_AUCTIONS:" + sellerId);
        myAuctions = ResponseHandler.parseAuctionList(response);

        Platform.runLater(() -> {
            auctionListBox.getChildren().clear();
            if (myAuctions.isEmpty()) {
                statusLabel.setText("Bạn chưa có phiên đấu giá nào");
            } else {
                statusLabel.setText("Tổng cộng: " + myAuctions.size() + " phiên");
                myAuctions.forEach(this::renderAuctionRow);
            }
        });
    }

    public void deleteAuction(String auctionId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn xóa phiên đấu giá này?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                String response = ServerConnection.getInstance().sendRequest("DELETE_AUCTION:" + auctionId);
                if ("DELETE_OK".equals(response)) {
                    loadMyAuctions();
                } else {
                    statusLabel.setText("Xóa thất bại: " + response);
                }
            }
        });
    }

    public void editAuction(String auctionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/create_auction.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Chỉnh sửa phiên đấu giá");
            stage.show();
        } catch (Exception e) {
            statusLabel.setText("Lỗi: " + e.getMessage());
        }
    }

    public void viewBids(String auctionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/bid_history.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            BidHistoryController controller = loader.getController();
            controller.loadBidHistory(auctionId);

            stage.setTitle("Lịch sử đặt giá");
            stage.show();
        } catch (Exception e) {
            statusLabel.setText("Lỗi: " + e.getMessage());
        }
    }

    public void startAuctionNow(String auctionId) {
        String response = ServerConnection.getInstance().sendRequest("START_AUCTION:" + auctionId);
        if ("START_OK".equals(response)) {
            loadMyAuctions();
        } else {
            statusLabel.setText("Không thể bắt đầu: " + response);
        }
    }

    // === Helpers ===

    private void renderAuctionRow(AuctionDTO dto) {
        Label label = new Label(dto.getItemName() +
                " | " + String.format("%.0f VNĐ", dto.getCurrentPrice()) +
                " | " + dto.getStatus().getDisplayName());

        Button editBtn = new Button("Sửa");
        Button deleteBtn = new Button("Xóa");
        Button bidsBtn = new Button("Xem bids");
        Button startBtn = new Button("Bắt đầu");

        editBtn.setOnAction(e -> editAuction(dto.getId()));
        deleteBtn.setOnAction(e -> deleteAuction(dto.getId()));
        bidsBtn.setOnAction(e -> viewBids(dto.getId()));
        startBtn.setOnAction(e -> startAuctionNow(dto.getId()));

        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10, label, editBtn, deleteBtn, bidsBtn, startBtn);
        auctionListBox.getChildren().add(row);
    }
}
