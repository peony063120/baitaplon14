package com.auction.client.controller;

/**
 * Màn hình quản lí các phiên đấu giá của Seller.
 * Xem danh sách phiên của mình, sửa/xóa phiên chưa bắt đầu,
 * bắt đầu phiên ngay lập tức, xem lịch sử bid của từng phiên
 */

import com.auction.client.model.ClientModel;
import com.auction.client.network.ResponseHandler;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.entity.Auction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyAuctionsController {

    @FXML private VBox auctionListBox;
    @FXML private Label statusLabel;

    private final ClientModel clientModel = ClientModel.getInstance();
    private List<AuctionDTO> myAuctions = new ArrayList<>();

    @FXML
    public void initialize() {
        loadMyAuctions();
    }

    /**
     * Load danh sách phiên đấu giá của seller từ server.
     */
    public void loadMyAuctions() {
        String sellerId = clientModel.getCurrentUser().getId();
        statusLabel.setText("🔄 Đang tải...");

        // Chạy trong background thread
        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("GET_MY_AUCTIONS:" + sellerId);

                // ResponseHandler.parseAuctionList trả về List<Auction>
                List<Auction> auctions = ResponseHandler.parseAuctionList(response);

                // Chuyển đổi sang List<AuctionDTO>
                List<AuctionDTO> dtos = convertToAuctionDTO(auctions);

                Platform.runLater(() -> {
                    myAuctions = dtos;
                    renderAuctionList();
                    if (myAuctions.isEmpty()) {
                        statusLabel.setText("📋 Bạn chưa có phiên đấu giá nào");
                    } else {
                        statusLabel.setText("📊 Tổng cộng: " + myAuctions.size() + " phiên");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("❌ Lỗi kết nối: " + e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("❌ Lỗi: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Chuyển đổi từ Auction entity sang AuctionDTO.
     */
    private List<AuctionDTO> convertToAuctionDTO(List<Auction> auctions) {
        if (auctions == null) return new ArrayList<>();

        return auctions.stream().map(auction -> {
            AuctionDTO dto = new AuctionDTO();
            dto.setId(auction.getId());
            dto.setItemId(auction.getItemId());
            dto.setSellerId(auction.getSellerId());
            dto.setCurrentPrice(auction.getCurrentPrice());
            dto.setStartingPrice(auction.getCurrentPrice());
            dto.setStatus(auction.getStatus());
            dto.setStartTime(auction.getStartTime());
            dto.setEndTime(auction.getEndTime());
            dto.setMinIncrement(auction.getMinIncrement());
            dto.setAntiSnipingEnabled(auction.isAntiSnipingEnabled());
            dto.setAntiSnipingExtensionSeconds((int) auction.getAntiSnipingExtensionSeconds());
            dto.setCurrentWinnerId(auction.getCurrentWinnerId());
            dto.setTotalBids(auction.getBidHistory() != null ? auction.getBidHistory().size() : 0);
            dto.setItemName("Sản phẩm " + auction.getItemId());
            dto.setCategory("general");
            dto.setCategoryName("Sản phẩm");

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Xóa phiên đấu giá.
     * @param auctionId ID của phiên cần xóa
     */
    public void deleteAuction(String auctionId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn xóa phiên đấu giá này?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        String response = ServerConnection.getInstance().sendRequest("DELETE_AUCTION:" + auctionId);
                        Platform.runLater(() -> {
                            if ("DELETE_OK".equals(response) || response.startsWith("DELETE_OK")) {
                                statusLabel.setText("✅ Đã xóa phiên đấu giá");
                                loadMyAuctions();
                            } else {
                                statusLabel.setText("❌ Xóa thất bại: " + response);
                            }
                        });
                    } catch (IOException e) {
                        Platform.runLater(() -> statusLabel.setText("❌ Lỗi kết nối: " + e.getMessage()));
                    }
                }).start();
            }
        });
    }

    /**
     * Chỉnh sửa phiên đấu giá.
     * @param auctionId ID của phiên cần chỉnh sửa
     */
    public void editAuction(String auctionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/create_auction.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Chỉnh sửa phiên đấu giá");
            stage.show();
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Xem lịch sử đặt giá của phiên.
     * @param auctionId ID của phiên cần xem
     */
    public void viewBids(String auctionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/bid_history.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            BidHistoryController controller = loader.getController();
            controller.loadBidHistory(auctionId);

            stage.setTitle("Lịch sử đặt giá");
            stage.show();
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Bắt đầu phiên đấu giá ngay lập tức.
     * @param auctionId ID của phiên cần bắt đầu
     */
    public void startAuctionNow(String auctionId) {
        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("START_AUCTION:" + auctionId);
                Platform.runLater(() -> {
                    if ("START_OK".equals(response) || response.startsWith("START_OK")) {
                        statusLabel.setText("✅ Đã bắt đầu phiên đấu giá");
                        loadMyAuctions();
                    } else {
                        statusLabel.setText("❌ Không thể bắt đầu: " + response);
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("❌ Lỗi kết nối: " + e.getMessage()));
            }
        }).start();
    }

    // === Helpers ===

    /**
     * Hiển thị danh sách phiên đấu giá.
     */
    private void renderAuctionList() {
        Platform.runLater(() -> {
            auctionListBox.getChildren().clear();
            for (AuctionDTO dto : myAuctions) {
                renderAuctionRow(dto);
            }
        });
    }

    private void renderAuctionRow(AuctionDTO dto) {
        // Tên và giá
        Label nameLabel = new Label(dto.getItemName());
        nameLabel.setMinWidth(200);
        nameLabel.setStyle("-fx-font-weight: 800; -fx-text-fill: #111111;");

        Label priceLabel = new Label(String.format("%,.0f VNĐ", dto.getCurrentPrice()));
        priceLabel.setMinWidth(120);
        priceLabel.setStyle("-fx-text-fill: #111111; -fx-font-weight: 800;");

        Label statusLabelItem = new Label(dto.getStatus() != null ? dto.getStatus().getDisplayName() : "Chưa xác định");
        statusLabelItem.setMinWidth(100);

        // Buttons
        Button editBtn = new Button("✏️ Sửa");
        editBtn.getStyleClass().add("secondary-btn");
        editBtn.setOnAction(e -> editAuction(dto.getId()));

        Button deleteBtn = new Button("🗑️ Xóa");
        deleteBtn.getStyleClass().add("danger-btn");
        deleteBtn.setOnAction(e -> deleteAuction(dto.getId()));

        Button bidsBtn = new Button("📋 Lịch sử");
        bidsBtn.getStyleClass().add("secondary-btn");
        bidsBtn.setOnAction(e -> viewBids(dto.getId()));

        Button startBtn = new Button("▶️ Bắt đầu");
        startBtn.getStyleClass().add("primary-btn");
        startBtn.setOnAction(e -> startAuctionNow(dto.getId()));

        // Ẩn nút nếu phiên đã kết thúc hoặc đang chạy
        if (dto.getStatus() != null &&
                (dto.getStatus() == com.auction.common.enums.AuctionStatus.RUNNING ||
                        dto.getStatus() == com.auction.common.enums.AuctionStatus.FINISHED)) {
            startBtn.setDisable(true);
            editBtn.setDisable(true);
        }

        HBox row = new HBox(10, nameLabel, priceLabel, statusLabelItem, startBtn, bidsBtn, editBtn, deleteBtn);
        row.getStyleClass().add("surface");

        auctionListBox.getChildren().add(row);
    }
}
