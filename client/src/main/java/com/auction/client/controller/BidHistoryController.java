package com.auction.client.controller;

/**
 * Bid history screen for an auction session.
 * Displays a table with bidder name, amount, time, and whether auto-bid.
 * Supports date filtering, sorting by price, and CSV export.
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
                    // Show "Auto" and "Manual" labels
                    setText(item ? "🤖 Auto" : "👤 Manual");
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
                    setText(String.format("$%,.0f", item));
                }
            }
        });
    }

    /**
     * Load bid history from server by auctionId.
     * @param auctionId The auction ID
     */
    public void loadBidHistory(String auctionId) {
        this.currentAuctionId = auctionId;

        try {
            // Gửi request lên server
            String response = ServerConnection.getInstance().sendRequest("GET_BID_HISTORY:" + auctionId);

            // Use ResponseHandler.parseBidHistory() returning List<BidTransaction>
            List<BidTransaction> transactions = ResponseHandler.parseBidHistory(response);
            allBids = convertToBidHistoryDTO(transactions);

            Platform.runLater(() -> {
                historyTable.getItems().setAll(allBids);
                // Show total bid count
                statusLabel.setText("📊 Total: " + allBids.size() + (allBids.size() <= 1 ? " bid" : " bids"));
            });
        } catch (IOException e) {
            showError("Connection error: " + e.getMessage());
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    /**
     * Convert List<BidTransaction> to List<BidHistoryDTO>.
     * @param transactions Transaction list from server
     * @return DTO list for TableView display
     */
    private List<BidHistoryDTO> convertToBidHistoryDTO(List<BidTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }

        return transactions.stream()
                .map(tx -> new BidHistoryDTO(
                        tx.getBidderName() != null && !tx.getBidderName().isBlank()
                                ? tx.getBidderName() : tx.getBidderId(),
                        tx.getAmount(),
                        tx.getBidTime(),
                        tx.isAutoBid()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Export history to CSV file.
     * @param filename Output filename
     */
    public void exportToCSV(String filename) {
        if (allBids == null || allBids.isEmpty()) {
            statusLabel.setText("⚠️ No data available to export");
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
            statusLabel.setText("✅ CSV exported successfully: " + filename);
        } catch (IOException e) {
            statusLabel.setText("❌ Failed to export CSV: " + e.getMessage());
        }
    }

    @FXML
    public void handleExportCSV() {
        exportToCSV("bid_history_" + currentAuctionId + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
    }

    /**
     * Filter history by date range.
     * @param start Start time
     * @param end End time
     */
    public void filterByDate(LocalDateTime start, LocalDateTime end) {
        if (allBids == null) return;

        List<BidHistoryDTO> filtered = allBids.stream()
                .filter(b -> b.getTimestamp() != null)
                .filter(b -> !b.getTimestamp().isBefore(start) && !b.getTimestamp().isAfter(end))
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            historyTable.getItems().setAll(filtered);
            statusLabel.setText("🔍 Filtered: " + filtered.size() + " results (Total: " + allBids.size() + ")");
        });
    }

    @FXML
    public void handleFilter() {
        if (fromDatePicker.getValue() == null || toDatePicker.getValue() == null) {
            statusLabel.setText("⚠️ Please select a date range");
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
                statusLabel.setText("📊 Displaying all: " + allBids.size() + (allBids.size() <= 1 ? " bid" : " bids"));            });
        }
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
    }

    /**
     * Sort by amount.
     * @param ascending true: ascending, false: descending
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
            statusLabel.setText("📊 Sorted by price (" + (ascending ? "ascending" : "descending") + ")");        });
    }

    /**
     * Load bid history by userId (for ProfileController).
     * @param userId User ID
     */
    public void loadBidHistoryByUser(String userId) {
        if (userId == null || userId.isBlank()) {
            showError("User not logged in.");
            return;
        }
        statusLabel.setText("🔄 Loading...");
        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("GET_USER_BID_HISTORY:" + userId);
                List<BidTransaction> history = ResponseHandler.parseBidHistoryFromText(response);
                List<BidHistoryDTO> dtos = history.stream()
                        .map(b -> new BidHistoryDTO(
                                b.getBidderName() != null && !b.getBidderName().isBlank()
                                        ? b.getBidderName() : b.getBidderId(),
                                b.getAmount(),
                                b.getBidTime(),
                                b.isAutoBid()))
                        .collect(Collectors.toList());
                Platform.runLater(() -> {
                    allBids = dtos;
                    historyTable.getItems().setAll(allBids);
                    statusLabel.setText(allBids.isEmpty()
                            ? "📋 No bids found for your account"
                            : "📊 Total: " + allBids.size() + (allBids.size() <= 1 ? " bid" : " bids"));
                });
            } catch (IOException e) {
                showError("Connection error: " + e.getMessage());
            }
        }, "load-user-bids").start();
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