package com.auction.client.controller;

import com.auction.client.config.AppConfig;
import com.auction.client.model.ClientModel;
import com.auction.client.network.MessageProtocol;
import com.auction.client.network.RealtimeListener;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.enums.AuctionStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminController {

    @FXML private Label pendingCountLabel;
    @FXML private Label totalAuctionsLabel;
    @FXML private Label totalUsersLabel;
    @FXML private VBox pendingAuctionList;
    @FXML private Text noPendingText;
    @FXML private Label statusLabel;
    @FXML private Label adminNameLabel;

    private List<AuctionDTO> pendingAuctions = new ArrayList<>();
    private final Consumer<Object> auctionUpdateCallback = data -> {
        loadPendingAuctions();
        loadStats();
    };
    /** Tránh gửi nhiều request TCP đồng thời — dễ nhận nhầm response. */
    private final ExecutorService serverIo = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "admin-server-io");
        t.setDaemon(true);
        return t;
    });

    @FXML
    public void initialize() {
        com.auction.common.entity.User user = ClientModel.getInstance().getCurrentUser();
        if (user != null) {
            adminNameLabel.setText("Logged in as: " + user.getUsername());
        }
        statusLabel.setText("Connected endpoint: " + AppConfig.getServerHost() + ":" + AppConfig.getServerPort());
        RealtimeListener.getInstance().registerCallback(
                MessageProtocol.TYPE_AUCTION_UPDATE, auctionUpdateCallback);
        serverIo.submit(() -> {
            try {
                ServerConnection.getInstance().sendRequest("SUBSCRIBE");
            } catch (IOException ignored) {
                Platform.runLater(() -> statusLabel.setText("Could not subscribe to live updates"));
            }
            loadPendingAuctions();
            loadStats();
        });
    }

    private void loadStats() {
        serverIo.submit(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("GET_AUCTIONS");
                if (response != null && response.startsWith("AUCTIONS_COUNT:")) {
                    String countStr = response.substring("AUCTIONS_COUNT:".length());
                    int sep = countStr.indexOf("||");
                    int total = sep > 0 ? Integer.parseInt(countStr.substring(0, sep)) : Integer.parseInt(countStr);
                    Platform.runLater(() -> totalAuctionsLabel.setText(String.valueOf(total)));
                }
                String userResp = ServerConnection.getInstance().sendRequest("GET_USER_COUNT");
                if (userResp != null && userResp.startsWith("USER_COUNT:")) {
                    int count = Integer.parseInt(userResp.substring("USER_COUNT:".length()));
                    Platform.runLater(() -> totalUsersLabel.setText(String.valueOf(count)));
                }
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("Error loading stats"));
            }
        });
    }

    public void loadPendingAuctions() {
        serverIo.submit(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("GET_PENDING_AUCTIONS");
                Platform.runLater(() -> parsePendingResponse(response));
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("Connection error: " + e.getMessage()));
            }
        });
    }

    private void parsePendingResponse(String response) {
        if (response == null || !response.startsWith("PENDING_COUNT:")) {
            statusLabel.setText("Invalid server response");
            return;
        }
        pendingAuctions.clear();

        String countStr = response.substring("PENDING_COUNT:".length());
        int sep = countStr.indexOf("||");
        int count = 0;
        if (sep > 0) {
            count = Integer.parseInt(countStr.substring(0, sep));
            String data = countStr.substring(sep + 2);
            String[] items = data.split("\\|\\|");
            for (String item : items) {
                if (item.startsWith("PENDING:")) {
                    // id:name:price:sellerId:startTime:endTime — start/end chứa ':' nên split tối đa 5 phần
                    String[] p = item.substring(8).split(":", 5);
                    if (p.length >= 3) {
                        AuctionDTO dto = new AuctionDTO();
                        dto.setId(p[0]);
                        dto.setItemName(p[1]);
                        dto.setCurrentPrice(Double.parseDouble(p[2]));
                        dto.setStatus(AuctionStatus.PENDING);
                        if (p.length > 3) {
                            dto.setSellerId(p[3]);
                        }
                        pendingAuctions.add(dto);
                    }
                }
            }
        } else {
            count = Integer.parseInt(countStr);
        }

        pendingCountLabel.setText(String.valueOf(count));
        renderPendingAuctions();
        statusLabel.setText("");
    }

    private void renderPendingAuctions() {
        pendingAuctionList.getChildren().clear();
        if (pendingAuctions.isEmpty()) {
            noPendingText.setVisible(true);
            return;
        }
        noPendingText.setVisible(false);

        for (AuctionDTO auction : pendingAuctions) {
            VBox card = new VBox(8);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                    + "-fx-padding: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 8;");

            HBox topRow = new HBox(12);
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(auction.getItemName());
            nameLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 14px;");

            Label priceLabel = new Label(String.format("$%,.0f", auction.getCurrentPrice()));
            priceLabel.setStyle("-fx-text-fill: #0e7490; -fx-font-weight: 700;");

            Label sellerLabel = new Label("Seller: " + (auction.getSellerId() != null ? auction.getSellerId() : "N/A"));
            sellerLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

            Label pendingBadge = new Label("PENDING");
            pendingBadge.setStyle("-fx-background-color: #f59e0b22; -fx-text-fill: #d97706; "
                    + "-fx-padding: 2 10 2 10; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: 700;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button approveBtn = new Button("Approve");
            approveBtn.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; "
                    + "-fx-font-weight: 700; -fx-padding: 6 16; -fx-background-radius: 6; -fx-cursor: hand;");
            approveBtn.setOnAction(e -> approveAuction(auction.getId()));

            Button rejectBtn = new Button("Reject");
            rejectBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; "
                    + "-fx-font-weight: 700; -fx-padding: 6 16; -fx-background-radius: 6; -fx-cursor: hand;");
            rejectBtn.setOnAction(e -> rejectAuction(auction.getId()));

            topRow.getChildren().addAll(nameLabel, priceLabel, sellerLabel, pendingBadge, spacer, approveBtn, rejectBtn);
            card.getChildren().add(topRow);
            pendingAuctionList.getChildren().add(card);
        }
    }

    private void approveAuction(String auctionId) {
        serverIo.submit(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("APPROVE_AUCTION:" + auctionId);
                Platform.runLater(() -> {
                    if (response != null && response.startsWith("APPROVE_OK")) {
                        statusLabel.setText("Auction approved successfully!");
                        loadPendingAuctions();
                        loadStats();
                    } else {
                        statusLabel.setText("Failed to approve: " + response);
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("Connection error: " + e.getMessage()));
            }
        });
    }

    private void rejectAuction(String auctionId) {
        serverIo.submit(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("REJECT_AUCTION:" + auctionId);
                Platform.runLater(() -> {
                    if (response != null && response.startsWith("REJECT_OK")) {
                        statusLabel.setText("Auction rejected.");
                        loadPendingAuctions();
                        loadStats();
                    } else {
                        statusLabel.setText("Failed to reject: " + response);
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("Connection error: " + e.getMessage()));
            }
        });
    }

    @FXML
    public void onRefresh() {
        loadPendingAuctions();
        loadStats();
    }

    @FXML
    public void onLogout() {
        RealtimeListener.getInstance().unregisterCallback(
                MessageProtocol.TYPE_AUCTION_UPDATE, auctionUpdateCallback);
        ClientModel.getInstance().logout();
        try {
            com.auction.client.ClientApp.showLoginScreen();
        } catch (Exception e) {
            statusLabel.setText("Logout error: " + e.getMessage());
        }
    }
}
