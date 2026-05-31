package com.auction.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.auction.client.components.AuctionCard;
import com.auction.client.service.DataService;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.enums.AuctionStatus;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DashboardController {

    @FXML private Label activeAuctionsCount;
    @FXML private Label totalRevenue;
    @FXML private LineChart<Number, Number> revenueChart;
    @FXML private TilePane auctionGrid;

    private List<AuctionDTO> allAuctions = new ArrayList<>();
    private String currentFilter = "all";

    @FXML
    public void initialize() {
        loadAuctions();
    }

    public void loadAuctions() {
        DataService.getInstance().loadAuctions(
                auctions -> {
                    allAuctions = auctions;
                    applyFilter();
                    updateStats();
                    updateChart();
                },
                error -> {
                    if (activeAuctionsCount != null)
                        // CHANGED: "Lỗi kết nối" -> "Connection Error"
                        activeAuctionsCount.setText("Connection Error");
                    // CHANGED: "Không thể tải danh sách đấu giá: " -> "Cannot load auction list: "
                    System.err.println("Cannot load auction list: " + error);
                }
        );
    }

    private void updateChart() {
        Platform.runLater(() -> {
            if (revenueChart == null) return;
            revenueChart.getData().clear();
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Revenue by Day");

            var finishedAuctions = allAuctions.stream()
                    .filter(a -> a.getStatus() == AuctionStatus.FINISHED || a.getStatus() == AuctionStatus.PAID)
                    .collect(Collectors.groupingBy(
                            a -> a.getEndTime() != null ? a.getEndTime().toLocalDate() : null,
                            Collectors.summingDouble(AuctionDTO::getCurrentPrice)
                    ));

            int dayIndex = 1;
            for (var entry : finishedAuctions.entrySet()) {
                if (entry.getKey() != null) {
                    series.getData().add(new XYChart.Data<>(dayIndex++, entry.getValue()));
                }
            }

            if (series.getData().isEmpty()) {
                series.getData().add(new XYChart.Data<>(1, 0));
            }
            revenueChart.getData().add(series);
        });
    }

    private void updateStats() {
        long runningCount = allAuctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.RUNNING)
                .count();
        activeAuctionsCount.setText(String.valueOf(runningCount));

        double revenue = allAuctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.FINISHED || a.getStatus() == AuctionStatus.PAID)
                .mapToDouble(AuctionDTO::getCurrentPrice)
                .sum();
        // CHANGED: Thay đổi ký hiệu tiền tệ từ "₫" sang "VND" đặt phía trước theo tiêu chuẩn quốc tế
        totalRevenue.setText(String.format("VND %,.0f", revenue));
    }

    private void applyFilter() {
        List<AuctionDTO> filtered = new ArrayList<>(allAuctions);
        switch (currentFilter) {
            case "ending":
                filtered = allAuctions.stream()
                        .filter(a -> a.getStatus() == AuctionStatus.RUNNING)
                        .sorted((a, b) -> {
                            if (a.getEndTime() == null) return 1;
                            if (b.getEndTime() == null) return -1;
                            return a.getEndTime().compareTo(b.getEndTime());
                        })
                        .limit(20).collect(Collectors.toList());
                break;
            case "newest":
                filtered = allAuctions.stream()
                        .sorted((a, b) -> {
                            if (a.getStartTime() == null) return 1;
                            if (b.getStartTime() == null) return -1;
                            return b.getStartTime().compareTo(a.getStartTime());
                        })
                        .limit(20).collect(Collectors.toList());
                break;
            case "highest":
                filtered = allAuctions.stream()
                        .sorted((a, b) -> Double.compare(b.getCurrentPrice(), a.getCurrentPrice()))
                        .limit(20).collect(Collectors.toList());
                break;
            default:
                break;
        }
        renderAuctions(filtered);
    }

    private void renderAuctions(List<AuctionDTO> auctions) {
        Platform.runLater(() -> {
            auctionGrid.getChildren().clear();
            for (AuctionDTO dto : auctions) {
                AuctionCard card = new AuctionCard(dto, this::openAuctionDetailPopup);
                auctionGrid.getChildren().add(card);
            }
        });
    }

    /**
     * Mở chi tiết phiên đấu giá dạng POPUP (Modal dialog) thay vì Stage mới.
     */
    private void openAuctionDetailPopup(AuctionDTO auction) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/client/view/auction_detail.fxml"));

            // Lấy stage hiện tại để đặt làm owner cho popup
            Stage owner = (Stage) auctionGrid.getScene().getWindow();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(owner);
            popupStage.initStyle(StageStyle.DECORATED);
            // CHANGED: "Chi tiết đấu giá — " -> "Auction Details — "
            popupStage.setTitle("Auction Details — " + auction.getItemName());
            popupStage.setMinWidth(700);
            popupStage.setMinHeight(600);

            Scene scene = new Scene(loader.load(), 750, 680);
            popupStage.setScene(scene);

            AuctionDetailController controller = loader.getController();
            controller.loadAuctionDetails(auction.getId());

            popupStage.showAndWait(); // Modal: chặn window cha cho đến khi đóng
        } catch (Exception e) {
            e.printStackTrace();
            // CHANGED: "Không thể mở chi tiết: " -> "Cannot open details: "
            showErrorPopup("Cannot open details: " + e.getMessage());
        }
    }

    private void showErrorPopup(String message) {
        Stage errStage = new Stage();
        errStage.initModality(Modality.APPLICATION_MODAL);
        Label lbl = new Label(message);
        lbl.setWrapText(true);
        // CHANGED: "Đóng" -> "Close"
        Button btn = new Button("Close");
        btn.setOnAction(e -> errStage.close());
        VBox box = new VBox(16, lbl, btn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(24));
        errStage.setScene(new Scene(box, 360, 150));
        // CHANGED: "Lỗi" -> "Error"
        errStage.setTitle("Error");
        errStage.show();
    }

    // ==================== PHƯƠNG THỨC GỌI TỪ MAIN CONTROLLER ====================

    @FXML
    public void filterByCategory(javafx.event.ActionEvent event) {
        if (event.getSource() instanceof javafx.scene.control.ToggleButton button) {
            Object userData = button.getUserData();
            String category = userData != null ? userData.toString() : "all";
            this.currentFilter = category;
            applyFilter();
        }
    }

    public void filterByCategory(String category) {
        this.currentFilter = category != null ? category : "all";
        applyFilter();
    }

    public void setFilter(String filterType) {
        this.currentFilter = filterType != null ? filterType : "all";
        applyFilter();
    }

    public void searchAuctions(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            applyFilter();
            return;
        }
        List<AuctionDTO> filtered = allAuctions.stream()
                .filter(auction -> auction.getItemName() != null
                        && auction.getItemName().toLowerCase(Locale.ROOT).contains(normalized))
                .collect(Collectors.toList());
        renderAuctions(filtered);
    }
}