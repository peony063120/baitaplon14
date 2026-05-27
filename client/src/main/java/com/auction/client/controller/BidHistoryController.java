package com.auction.client.controller;

/**
 * Màn hình lịch sử đặt giá của 1 phiên.
 * Hiển thị bảng gồm tên bidder, số tiền, thời gian, có phải auto-bid không.
 * Hỗ trợ lọc theo ngày, sắp xếp theo giá, và xuất ra file CSV
 */

import com.auction.client.network.ResponseHandler;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.BidHistoryDTO;
import com.auction.common.entity.BidTransaction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BidHistoryController {

    @FXML private TableView<BidHistoryDTO> historyTable;
    @FXML private TableColumn<BidHistoryDTO, String> bidderColumn;
    @FXML private TableColumn<BidHistoryDTO, Double> amountColumn;
    @FXML private TableColumn<BidHistoryDTO, String> timeColumn;
    @FXML private TableColumn<BidHistoryDTO, Boolean> autoBidColumn;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Label statusLabel;

    private String currentAuctionId;
    private List<BidHistoryDTO> allBids;

    private final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    public void initialize() {
        bidderColumn.setCellValueFactory(new PropertyValueFactory<>("bidderName"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("timestampString"));
        autoBidColumn.setCellValueFactory(new PropertyValueFactory<>("autoBid"));

        // Custom cell factory cho cột autoBid
        autoBidColumn.setCellFactory(column -> new TableCell<BidHistoryDTO, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "🤖 Auto" : "👤 Thủ công");
                }
            }
        });

        // Định dạng cột tiền
        amountColumn.setCellFactory(column -> new TableCell<BidHistoryDTO, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VNĐ", item));
                }
            }
        });
    }

    /**
     * Load lịch sử đấu giá từ server dựa trên auctionId.
     * @param auctionId ID của phiên đấu giá
     */
    public void loadBidHistory(String auctionId) {
        this.currentAuctionId = auctionId;

        try {
            // Gửi request lên server
            String response = ServerConnection.getInstance().sendRequest("GET_BID_HISTORY:" + auctionId);

            // SỬA: Dùng ResponseHandler.parseBidHistory() trả về List<BidTransaction>
            List<BidTransaction> transactions = ResponseHandler.parseBidHistory(response);
            allBids = convertToBidHistoryDTO(transactions);

            Platform.runLater(() -> {
                historyTable.getItems().setAll(allBids);
                statusLabel.setText("📊 Tổng: " + allBids.size() + " lượt đặt giá");
            });
        } catch (IOException e) {
            showError("Lỗi kết nối: " + e.getMessage());
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Chuyển đổi từ List<BidTransaction> sang List<BidHistoryDTO>.
     * @param transactions Danh sách giao dịch từ server
     * @return Danh sách DTO để hiển thị trên TableView
     */
    private List<BidHistoryDTO> convertToBidHistoryDTO(List<BidTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }

        return transactions.stream()
                .map(tx -> new BidHistoryDTO(
                        tx.getBidderId(),           // bidderName (tạm dùng ID)
                        tx.getAmount(),
                        tx.getBidTime(),
                        tx.isAutoBid()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Xuất lịch sử ra file CSV.
     * @param filename Tên file xuất
     */
    public void exportToCSV(String filename) {
        if (allBids == null || allBids.isEmpty()) {
            statusLabel.setText("⚠️ Không có dữ liệu để xuất");
            return;
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("Bidder,Amount,Time,AutoBid");
            for (BidHistoryDTO bid : allBids) {
                pw.printf("%s,%.0f,%s,%s%n",
                        bid.getBidderName(),
                        bid.getAmount(),
                        bid.getTimestamp() != null ? bid.getTimestamp().format(TIME_FORMATTER) : "",
                        bid.isAutoBid() ? "Yes" : "No");
            }
            statusLabel.setText("✅ Xuất CSV thành công: " + filename);
        } catch (IOException e) {
            statusLabel.setText("❌ Lỗi xuất CSV: " + e.getMessage());
        }
    }

    @FXML
    public void handleExportCSV() {
        exportToCSV("bid_history_" + currentAuctionId + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
    }

    /**
     * Lọc lịch sử theo khoảng thời gian.
     * @param start Thời gian bắt đầu
     * @param end Thời gian kết thúc
     */
    public void filterByDate(LocalDateTime start, LocalDateTime end) {
        if (allBids == null) return;

        List<BidHistoryDTO> filtered = allBids.stream()
                .filter(b -> b.getTimestamp() != null)
                .filter(b -> !b.getTimestamp().isBefore(start) && !b.getTimestamp().isAfter(end))
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            historyTable.getItems().setAll(filtered);
            statusLabel.setText("🔍 Lọc: " + filtered.size() + " kết quả (tổng " + allBids.size() + ")");
        });
    }

    @FXML
    public void handleFilter() {
        if (fromDatePicker.getValue() == null || toDatePicker.getValue() == null) {
            statusLabel.setText("⚠️ Vui lòng chọn khoảng ngày");
            return;
        }
        filterByDate(fromDatePicker.getValue().atStartOfDay(),
                toDatePicker.getValue().atTime(23, 59, 59));
    }

    @FXML
    public void handleResetFilter() {
        if (allBids != null) {
            Platform.runLater(() -> {
                historyTable.getItems().setAll(allBids);
                statusLabel.setText("📊 Hiển thị tất cả: " + allBids.size() + " lượt");
            });
        }
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
    }

    /**
     * Sắp xếp theo giá.
     * @param ascending true: tăng dần, false: giảm dần
     */
    public void sortByAmount(boolean ascending) {
        if (allBids == null) return;

        List<BidHistoryDTO> sorted = new ArrayList<>(allBids);
        if (ascending) {
            sorted.sort((a, b) -> Double.compare(a.getAmount(), b.getAmount()));
        } else {
            sorted.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        }

        Platform.runLater(() -> {
            historyTable.getItems().setAll(sorted);
            statusLabel.setText("📊 Sắp xếp theo giá " + (ascending ? "tăng dần" : "giảm dần"));
        });
    }

    /**
     * Load lịch sử đặt giá theo userId (cho ProfileController).
     * @param userId ID của người dùng
     */
    public void loadBidHistoryByUser(String userId) {
        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("GET_USER_BID_HISTORY:" + userId);
                List<BidTransaction> history = ResponseHandler.parseBidHistoryFromText(response);
                List<BidHistoryDTO> dtos = history.stream()
                        .map(b -> new BidHistoryDTO(b.getBidderId(), b.getAmount(), b.getBidTime(), b.isAutoBid()))
                        .collect(Collectors.toList());
                Platform.runLater(() -> {
                    allBids = dtos;
                    historyTable.getItems().setAll(allBids);
                    statusLabel.setText("📊 Tổng: " + allBids.size() + " lượt đặt giá");
                });
            } catch (IOException e) {
                showError("Lỗi kết nối: " + e.getMessage());
            }
        }).start();
    }



    @FXML
    public void handleSortAsc() {
        sortByAmount(true);
    }

    @FXML
    public void handleSortDesc() {
        sortByAmount(false);
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("❌ " + message);
            statusLabel.setStyle("-fx-text-fill: #DC2626;");
        });
    }
}