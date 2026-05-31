package com.auction.client.controller;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.auction.client.components.AuctionCard;
import com.auction.client.config.AppConfig;
import com.auction.client.network.MessageProtocol;
import com.auction.client.network.RealtimeListener;
import com.auction.client.service.DataService;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.enums.AuctionStatus;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

public class DashboardController {

    @FXML private Label activeAuctionsCount;
    @FXML private Label totalRevenue;
    @FXML private LineChart<Number, Number> revenueChart;
    @FXML private TilePane auctionGrid;

    private List<AuctionDTO> allAuctions = new ArrayList<>();
    private String sortFilter = "all";
    private final Set<String> selectedCategories = new LinkedHashSet<>();
    private final RealtimeListener realtimeListener = RealtimeListener.getInstance();

    @FXML
    public void initialize() {
        loadAuctions();
        subscribeRealtime();
        if (AppConfig.isUseMock()) {
            Timeline refreshTimer = new Timeline(
                    new KeyFrame(Duration.seconds(5), e -> loadAuctions())
            );
            refreshTimer.setCycleCount(Timeline.INDEFINITE);
            refreshTimer.play();
        }
    }

    public void loadAuctions() {
        DataService.getInstance().loadAuctions(
                auctions -> {
                    if (auctions != null && (!auctions.isEmpty() || allAuctions.isEmpty())) {
                        allAuctions = auctions;
                        MainController.seedAuctionSnapshot(auctions);
                    }
                    applyFilter();
                    updateStats();
                    updateChart();
                },
                error -> {
                    if (activeAuctionsCount != null)
                        activeAuctionsCount.setText("Connection Error");
                    System.err.println("Failed to load auction list: " + error);
                }
        );
    }

    private void subscribeRealtime() {
        if (AppConfig.isUseMock()) {
            return;
        }
        realtimeListener.registerCallback(MessageProtocol.TYPE_AUCTION_UPDATE, this::onAuctionUpdate);
    }

    private void onAuctionUpdate(Object data) {
        if (!(data instanceof AuctionDTO dto) || dto.getId() == null) {
            return;
        }
        Platform.runLater(() -> {
            boolean found = false;
            for (AuctionDTO existing : allAuctions) {
                if (existing.getId().equals(dto.getId())) {
                    found = true;
                    existing.setCurrentPrice(dto.getCurrentPrice());
                    if (dto.getStatus() != null) {
                        existing.setStatus(dto.getStatus());
                    }
                    if (dto.getCurrentWinnerId() != null) {
                        existing.setCurrentWinnerId(dto.getCurrentWinnerId());
                    }
                    if (dto.getTotalBids() >= 0) {
                        existing.setTotalBids(dto.getTotalBids());
                    }
                    break;
                }
            }
            if (!found) {
                loadAuctions();
                return;
            }
            applyFilter();
            updateStats();
            MainController.syncBalanceFromServer();
        });
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
                            a -> {
                                if (a.getEndTime() != null) {
                                    return a.getEndTime().toLocalDate();
                                }
                                if (a.getStartTime() != null) {
                                    return a.getStartTime().toLocalDate();
                                }
                                return java.time.LocalDate.now();
                            },
                            Collectors.summingDouble(AuctionDTO::getCurrentPrice)
                    ));

            finishedAuctions.entrySet().stream()
                    .sorted(java.util.Map.Entry.comparingByKey())
                    .forEach(entry -> series.getData().add(
                            new XYChart.Data<>(series.getData().size() + 1, entry.getValue())));

            if (series.getData().isEmpty()) {
                series.getData().add(new XYChart.Data<>(1, 0));
            }
            revenueChart.getData().add(series);
        });
    }

    private void updateStats() {
        long runningCount = allAuctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.RUNNING
                        || a.getStatus() == AuctionStatus.OPEN)
                .count();
        activeAuctionsCount.setText(String.valueOf(runningCount));

        double revenue = allAuctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.FINISHED || a.getStatus() == AuctionStatus.PAID)
                .mapToDouble(AuctionDTO::getCurrentPrice)
                .sum();
        totalRevenue.setText(String.format("$%,.0f", revenue));
    }

    private void applyFilter() {
        List<AuctionDTO> filtered = allAuctions.stream()
                .filter(this::matchesSelectedCategories)
                .collect(Collectors.toCollection(ArrayList::new));

        switch (sortFilter) {
            case "ending":
                filtered = filtered.stream()
                        .filter(a -> a.getStatus() == AuctionStatus.RUNNING)
                        .sorted((a, b) -> {
                            if (a.getEndTime() == null) return 1;
                            if (b.getEndTime() == null) return -1;
                            return a.getEndTime().compareTo(b.getEndTime());
                        })
                        .limit(20).collect(Collectors.toList());
                break;
            case "newest":
                filtered = filtered.stream()
                        .sorted((a, b) -> {
                            if (a.getStartTime() == null) return 1;
                            if (b.getStartTime() == null) return -1;
                            return b.getStartTime().compareTo(a.getStartTime());
                        })
                        .limit(20).collect(Collectors.toList());
                break;
            case "highest":
                filtered = filtered.stream()
                        .sorted((a, b) -> Double.compare(b.getCurrentPrice(), a.getCurrentPrice()))
                        .limit(20).collect(Collectors.toList());
                break;
            default:
                break;
        }
        renderAuctions(filtered);
    }

    private boolean matchesSelectedCategories(AuctionDTO auction) {
        if (selectedCategories.isEmpty()) {
            return true;
        }
        String auctionCategory = normalizeCategory(auction.getCategory());
        return selectedCategories.stream()
                .anyMatch(selected -> normalizeCategory(selected).equals(auctionCategory));
    }

    static String normalizeCategory(String category) {
        if (category == null) {
            return "";
        }
        return category.toLowerCase(Locale.ROOT)
                .replace("đ", "d")
                .replaceAll("[\\s_-]+", "");
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
     * Open auction detail as a POPUP (Modal dialog) instead of a new Stage.
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
            popupStage.setTitle("Auction Detail — " + auction.getItemName());
            popupStage.setMinWidth(700);
            popupStage.setMinHeight(720);

            Scene scene = new Scene(loader.load(), 780, 820);
            popupStage.setScene(scene);

            AuctionDetailController controller = loader.getController();
            controller.loadAuctionDetails(auction.getId());

            popupStage.showAndWait();
            applyFilter();
            updateStats();
            MainController.syncBalanceFromServer();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorPopup("Could not open details: " + e.getMessage());
        }
    }

    private void showErrorPopup(String message) {
        Stage errStage = new Stage();
        errStage.initModality(Modality.APPLICATION_MODAL);
        Label lbl = new Label(message);
        lbl.setWrapText(true);
        Button btn = new Button("Close");
        btn.setOnAction(e -> errStage.close());
        VBox box = new VBox(16, lbl, btn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(24));
        errStage.setScene(new Scene(box, 360, 150));
        errStage.setTitle("Error");
        errStage.show();
    }

    // ==================== METHODS CALLED FROM MAIN CONTROLLER ====================

    @FXML
    public void filterBySort(javafx.event.ActionEvent event) {
        if (event.getSource() instanceof javafx.scene.control.ToggleButton button) {
            Object userData = button.getUserData();
            this.sortFilter = userData != null ? userData.toString() : "all";
            applyFilter();
        }
    }

    public void clearCategorySelection() {
        selectedCategories.clear();
        applyFilter();
    }

    public void toggleSidebarCategory(String category, boolean selected) {
        if (category == null || category.isBlank() || "all".equalsIgnoreCase(category)) {
            clearCategorySelection();
            return;
        }
        if (selected) {
            selectedCategories.add(category);
        } else {
            selectedCategories.remove(category);
        }
        applyFilter();
    }

    public void setFilter(String filterType) {
        this.sortFilter = filterType != null ? filterType : "all";
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