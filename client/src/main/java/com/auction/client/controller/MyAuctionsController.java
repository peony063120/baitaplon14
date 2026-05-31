package com.auction.client.controller;

import com.auction.client.model.ClientModel;
import com.auction.client.service.DataService;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.enums.AuctionStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình quản lý các phiên đấu giá của Seller.
 */
public class MyAuctionsController {

    @FXML private VBox auctionListBox;
    @FXML private Label statusLabel;

    private final ClientModel clientModel = ClientModel.getInstance();
    private List<AuctionDTO> myAuctions = new ArrayList<>();

    @FXML
    public void initialize() {
        loadMyAuctions();
    }

    public void loadMyAuctions() {
        String sellerId = clientModel.getCurrentUser() != null
                ? clientModel.getCurrentUser().getId() : "demo";
        statusLabel.setText("🔄 Đang tải...");

        DataService.getInstance().loadMyAuctions(
                sellerId,
                auctions -> {
                    myAuctions = auctions;
                    renderAuctionList();
                    statusLabel.setText(myAuctions.isEmpty()
                            ? "📋 Bạn chưa có phiên đấu giá nào"
                            : "📊 Tổng cộng: " + myAuctions.size() + " phiên");
                },
                error -> statusLabel.setText("❌ Lỗi kết nối: " + error)
        );
    }

    private void renderAuctionList() {
        Platform.runLater(() -> {
            auctionListBox.getChildren().clear();
            for (AuctionDTO dto : myAuctions) {
                auctionListBox.getChildren().add(buildAuctionRow(dto));
            }
        });
    }

    private VBox buildAuctionRow(AuctionDTO dto) {
        // Header row: tên + giá + status
        Label nameLabel = new Label(dto.getItemName());
        nameLabel.setStyle("-fx-font-weight: 800; -fx-font-size: 14px; -fx-text-fill: #111;");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        String statusText = dto.getStatus() != null ? dto.getStatus().getDisplayName() : "Chưa xác định";
        String statusColor = getStatusColor(dto.getStatus());
        Label statusChip = new Label(statusText);
        statusChip.setStyle("-fx-background-color: " + statusColor + "22; -fx-text-fill: " + statusColor
                + "; -fx-padding: 2 10 2 10; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: 700;");

        Label priceLabel = new Label(String.format("%,.0f VNĐ", dto.getCurrentPrice()));
        priceLabel.setStyle("-fx-text-fill: #0e7490; -fx-font-weight: 800; -fx-font-size: 14px;");

        HBox topRow = new HBox(12, nameLabel, statusChip, priceLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Sub info
        String endTimeStr = dto.getEndTime() != null ? "Kết thúc: " + dto.getEndTime().toString().substring(0, 16) : "";
        String bidsStr = "Lượt đặt: " + dto.getTotalBids();
        Label subInfo = new Label(bidsStr + (endTimeStr.isEmpty() ? "" : "   |   " + endTimeStr));
        subInfo.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        // Action buttons
        Button detailBtn = new Button("🔍 Chi tiết");
        detailBtn.getStyleClass().add("secondary-btn");
        detailBtn.setStyle("-fx-cursor: hand;");
        detailBtn.setOnAction(e -> openAuctionDetailPopup(dto));

        Button editBtn = new Button("✏️ Sửa");
        editBtn.getStyleClass().add("secondary-btn");
        editBtn.setOnAction(e -> editAuction(dto.getId()));

        Button bidsBtn = new Button("📋 Lịch sử");
        bidsBtn.getStyleClass().add("secondary-btn");
        bidsBtn.setOnAction(e -> viewBids(dto.getId()));

        Button startBtn = new Button("▶️ Bắt đầu");
        startBtn.getStyleClass().add("primary-btn");
        startBtn.setOnAction(e -> startAuctionNow(dto.getId()));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.getStyleClass().add("danger-btn");
        deleteBtn.setOnAction(e -> deleteAuction(dto.getId()));

        boolean isActiveOrDone = dto.getStatus() != null &&
                (dto.getStatus() == AuctionStatus.RUNNING || dto.getStatus() == AuctionStatus.FINISHED);
        if (isActiveOrDone) {
            startBtn.setDisable(true);
            editBtn.setDisable(true);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox btnRow = new HBox(8, spacer, detailBtn, bidsBtn, editBtn, startBtn, deleteBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(8, topRow, subInfo, btnRow);
        card.getStyleClass().add("surface");
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 6, 0, 0, 2);");
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle()
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.14), 10, 0, 0, 4);"));
        return card;
    }

    private String getStatusColor(AuctionStatus status) {
        if (status == null) return "#888";
        return switch (status) {
            case RUNNING -> "#16a34a";
            case DRAFT -> "#ca8a04";
            case FINISHED, PAID -> "#2563eb";
            default -> "#888";
        };
    }

    // ==================== ACTIONS ====================

    private void openAuctionDetailPopup(AuctionDTO auction) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/client/view/auction_detail.fxml"));
            Stage owner = (Stage) auctionListBox.getScene().getWindow();
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(owner);
            popupStage.initStyle(StageStyle.DECORATED);
            popupStage.setTitle("Chi tiết đấu giá — " + auction.getItemName());
            popupStage.setMinWidth(700);
            popupStage.setMinHeight(600);
            Scene scene = new Scene(loader.load(), 750, 680);
            popupStage.setScene(scene);
            AuctionDetailController controller = loader.getController();
            controller.loadAuctionDetails(auction.getId());
            popupStage.showAndWait();
        } catch (Exception e) {
            statusLabel.setText("❌ Không thể mở: " + e.getMessage());
        }
    }

    public void deleteAuction(String auctionId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn xóa phiên đấu giá này?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // Mock: xóa khỏi list local
                myAuctions.removeIf(a -> auctionId.equals(a.getId()));
                renderAuctionList();
                statusLabel.setText("✅ Đã xóa phiên đấu giá (demo)");
            }
        });
    }

    public void editAuction(String auctionId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/client/view/create_auction.fxml"));
            Stage owner = (Stage) auctionListBox.getScene().getWindow();
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Chỉnh sửa phiên đấu giá");
            stage.show();
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi: " + e.getMessage());
        }
    }

    public void viewBids(String auctionId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/client/view/bid_history.fxml"));
            Stage owner = (Stage) auctionListBox.getScene().getWindow();
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner);
            stage.setScene(new Scene(loader.load(), 600, 500));
            BidHistoryController controller = loader.getController();
            controller.loadBidHistory(auctionId);
            stage.setTitle("Lịch sử đặt giá");
            stage.show();
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi: " + e.getMessage());
        }
    }

    @FXML
    public void onRefresh() { loadMyAuctions(); }

    @FXML
    public void onCreateNew() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/create_auction.fxml"));
            Stage owner = (Stage) auctionListBox.getScene().getWindow();
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Tạo phiên đấu giá mới");
            stage.showAndWait();
            loadMyAuctions();
        } catch (Exception e) { statusLabel.setText("❌ Lỗi: " + e.getMessage()); }
    }

    public void startAuctionNow(String auctionId) {
        // Mock: cập nhật trạng thái local
        myAuctions.stream().filter(a -> auctionId.equals(a.getId())).findFirst()
                .ifPresent(a -> {
                    a.setStatus(AuctionStatus.RUNNING);
                    renderAuctionList();
                    statusLabel.setText("✅ Đã bắt đầu phiên đấu giá (demo)");
                });
    }
}