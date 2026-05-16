package com.auction.client.controller;

/**
 * Màn hình lịch sử đặt giá của 1 phiên.
 * Hiển thị bảng gồm tên bidder, số tiền, thời gian, có phải auto-bid không.
 * Hỗ trợ lọc theo ngày, sắp xếp theo giá, và xuất ra file CSV
 */

import com.auction.client.network.ResponseHandler;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.BidHistoryDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BidHistoryController {

    @FXML private TableView<BidHistoryDTO> historyTable;
    @FXML private TableColumn<BidHistoryDTO, String> bidderColumn;
    @FXML private TableColumn<BidHistoryDTO, Double> amountColumn;
    @FXML private TableColumn<BidHistoryDTO, LocalDateTime> timeColumn;
    @FXML private TableColumn<BidHistoryDTO, Boolean> autoBidColumn;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Label statusLabel;

    private String currentAuctionId;
    private List<BidHistoryDTO> allBids;

    @FXML
    public void initialize() {
        bidderColumn.setCellValueFactory(new PropertyValueFactory<>("bidderName"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        autoBidColumn.setCellValueFactory(new PropertyValueFactory<>("autoBid"));
    }

    public void loadBidHistory(String auctionId) {
        this.currentAuctionId = auctionId;
        String response = ServerConnection.getInstance().sendRequest("GET_BID_HISTORY:" + auctionId);
        allBids = ResponseHandler.parseBidHistory(response);

        Platform.runLater(() -> {
            historyTable.getItems().setAll(allBids);
            statusLabel.setText("Tổng: " + allBids.size() + " lượt đặt giá");
        });
    }

    public void exportToCSV(String filename) {
        if (allBids == null || allBids.isEmpty()) {
            statusLabel.setText("Không có dữ liệu để xuất");
            return;
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("Bidder,Amount,Time,AutoBid");
            allBids.forEach(b -> pw.printf("%s,%.0f,%s,%s%n",
                    b.getBidderName(), b.getAmount(), b.getTimestamp(), b.isAutoBid()));
            statusLabel.setText("Xuất CSV thành công: " + filename);
        } catch (Exception e) {
            statusLabel.setText("Lỗi xuất CSV: " +  e.getMessage());
        }
    }

    @FXML
    public void handleExportCSV() {
        exportToCSV("bid_history_" + currentAuctionId + ".csv");
    }

    public void filterByDate(LocalDateTime start, LocalDateTime end) {
        if (allBids == null) return;
        List<BidHistoryDTO> filtered = allBids.stream().filter(b -> b.getTimestamp() != null
                && !b.getTimestamp().isBefore(start)
                && !b.getTimestamp().isAfter(end)).collect(Collectors.toList());

        Platform.runLater(() -> {
            historyTable.getItems().setAll(filtered);
            statusLabel.setText("Lọc: " + filtered.size() + " kết quả");
        });
    }

    @FXML
    public void handleFilter() {
        if (fromDatePicker.getValue() == null || toDatePicker.getValue() == null) {
            statusLabel.setText("Vui lòng chọn khoảng ngày");
            return;
        }
        filterByDate(fromDatePicker.getValue().atStartOfDay(), toDatePicker.getValue().atTime(23, 59, 59));
    }

    public void sortByAmount(boolean ascending) {
        if (allBids == null) return;
        List<BidHistoryDTO> sorted = allBids.stream()
                .sorted((a, b) -> ascending ? Double.compare(a.getAmount(), b.getAmount()) :
                        Double.compare(b.getAmount(), a.getAmount())).collect(Collectors.toList());
        Platform.runLater(() -> historyTable.getItems().setAll(sorted));
    }

    @FXML public void handleSortAsc() { sortByAmount(true); }
    @FXML public void handleSortDesc() { sortByAmount(false); }
}
