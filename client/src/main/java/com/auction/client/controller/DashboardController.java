package com.auction.client.controller;

import com.auction.client.components.AuctionCard;
import com.auction.client.config.AppConfig;
import com.auction.client.model.ClientModel;
import com.auction.client.network.ResponseHandler;
import com.auction.client.network.ServerConnection;
import com.auction.client.service.MockDataProvider;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label activeAuctionsCount;
    @FXML private Label totalRevenue;
    @FXML private LineChart<Number, Number> revenueChart;
    @FXML private TilePane auctionGrid;

    private List<AuctionDTO> allAuctions = new ArrayList<>();
    private String currentFilter = "all";

    @FXML
    public void initialize() {
        // Load auctions từ server (API thật)
        loadAuctions();

        // Load dữ liệu biểu đồ từ server (API thật)
        loadRevenueChartData();
    }

    /**
     * Load danh sách phiên đấu giá từ server và hiển thị lên grid.
     */
    public void loadAuctions() {
        if (AppConfig.USE_MOCK) {
            allAuctions = MockDataProvider.getAuctions();
            renderAuctions(allAuctions);
            updateStats();
            return;
        }

        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("GET_AUCTIONS");
                List<Auction> auctions = ResponseHandler.parseAuctionList(response);
                List<AuctionDTO> dtos = convertToAuctionDTO(auctions);

                Platform.runLater(() -> {
                    allAuctions = dtos;
                    applyFilter();
                    updateStats();
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    activeAuctionsCount.setText("Lỗi kết nối");
                    e.printStackTrace();
                });
            }
        }).start();
    }

    /**
     * Load dữ liệu biểu đồ doanh thu từ server (THAY THẾ MOCK DATA)
     */
    private void loadRevenueChartData() {
        if (AppConfig.USE_MOCK) {
            updateChartFromAuctions();
            return;
        }

        new Thread(() -> {
            try {
                // Gọi API lấy dữ liệu biểu đồ
                String response = ServerConnection.getInstance().sendRequest("GET_REVENUE_CHART");

                // TODO: Parse response thành dữ liệu biểu đồ
                // Giả sử server trả về format: "DATE:amount" hoặc JSON
                // Tạm thời dùng dữ liệu từ auctions để tính
                updateChartFromAuctions();

            } catch (IOException e) {
                Platform.runLater(() -> {
                    System.err.println("Không thể tải dữ liệu biểu đồ: " + e.getMessage());
                    // Fallback: dùng dữ liệu từ auctions
                    updateChartFromAuctions();
                });
            }
        }).start();
    }

    /**
     * Cập nhật biểu đồ từ dữ liệu auctions có sẵn
     */
    private void updateChartFromAuctions() {
        Platform.runLater(() -> {
            revenueChart.getData().clear();
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Revenue by Day");

            // Nhóm các auction đã kết thúc theo ngày và tính tổng doanh thu
            // Đây là dữ liệu THẬT từ server, không phải mock
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

            // Nếu không có dữ liệu, hiển thị thông báo
            if (series.getData().isEmpty()) {
                series.getData().add(new XYChart.Data<>(1, 0));
            }

            revenueChart.getData().add(series);
        });
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
            dto.setItemName(auction.getItemId() != null ? "Sản phẩm " + auction.getItemId() : "Sản phẩm");
            dto.setCategory("general");
            dto.setCategoryName("Sản phẩm");
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Cập nhật các thống kê (số lượng đấu giá đang diễn ra, tổng doanh thu).
     */
    private void updateStats() {
        long runningCount = allAuctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.RUNNING)
                .count();
        activeAuctionsCount.setText(String.valueOf(runningCount));

        double revenue = allAuctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.FINISHED || a.getStatus() == AuctionStatus.PAID)
                .mapToDouble(AuctionDTO::getCurrentPrice)
                .sum();
        totalRevenue.setText(String.format("₫ %,.0f", revenue));
    }

    /**
     * Áp dụng bộ lọc hiện tại lên danh sách auction và render lại grid.
     */
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
                        .limit(20)
                        .collect(Collectors.toList());
                break;
            case "newest":
                filtered = allAuctions.stream()
                        .sorted((a, b) -> {
                            if (a.getStartTime() == null) return 1;
                            if (b.getStartTime() == null) return -1;
                            return b.getStartTime().compareTo(a.getStartTime());
                        })
                        .limit(20)
                        .collect(Collectors.toList());
                break;
            case "highest":
                filtered = allAuctions.stream()
                        .sorted((a, b) -> Double.compare(b.getCurrentPrice(), a.getCurrentPrice()))
                        .limit(20)
                        .collect(Collectors.toList());
                break;
            default:
                break;
        }
        renderAuctions(filtered);
    }

    /**
     * Hiển thị danh sách auction lên TilePane.
     */
    private void renderAuctions(List<AuctionDTO> auctions) {
        Platform.runLater(() -> {
            auctionGrid.getChildren().clear();
            for (AuctionDTO dto : auctions) {
                AuctionCard card = new AuctionCard(dto, this::openAuctionDetail);
                auctionGrid.getChildren().add(card);
            }
        });
    }

    /**
     * Mở màn hình chi tiết phiên đấu giá.
     */
    private void openAuctionDetail(AuctionDTO auction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/auction_detail.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            AuctionDetailController controller = loader.getController();
            controller.loadAuctionDetails(auction.getId());
            stage.setTitle("Auction Detail - " + auction.getItemName());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================== PHƯƠNG THỨC GỌI TỪ MAIN CONTROLLER ====================

    public void filterByCategory(String category) {
        System.out.println("Filter by category: " + category);
        // TODO: Lọc theo danh mục từ server
    }

    public void setFilter(String filterType) {
        this.currentFilter = filterType;
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
