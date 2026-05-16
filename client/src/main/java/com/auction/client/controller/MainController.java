package com.auction.client.controller;

/**
 * Màn hình chính sau khi đăng nhập.
 * Hiển thị danh sách tất cả phiên đấu giá đang có,
 * cho phép tìm kiếm theo tên, click vào 1 phiên để mở chi tiết, và đăng xuất.
 */

import com.auction.client.components.AuctionCard;
import com.auction.client.model.ClientModel;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.AuctionDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class MainController {

    @FXML private FlowPane auctionListPane;
    @FXML private TextField searchField;
    @FXML private Label userLabel;
    @FXML private Label statusLabel;

    private final ClientModel clientModel = ClientModel.getInstance();
    private List<AuctionDTO> allAuctions;

    @FXML
    public void initialize() {
        if (clientModel.getCurrentUser() != null) {
            userLabel.setText("Xin chào, " + clientModel.getCurrentUser().getFullName());
        }
        loadAuctions();
    }

    public void loadAuctions() {
        statusLabel.setText("Đang tải...");
        String response = ServerConnection.getInstance().sendRequest("GET_AUCTIONS");
        allAuctions = parseAuctions(response);
        renderAuctions(allAuctions);
        statusLabel.setText("Tìm thấy " + allAuctions.size() + " phiên đấu giá");
    }

    public void openAuctionDetail(String auctionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/auction_detail.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            AuctionDetailController controller = loader.getController();
            controller.loadAuctionDetails(auctionId);

            stage.setTitle("Chi tiết đấu giá");
            stage.show();
        } catch (Exception e) {
            statusLabel.setText("Lỗi mở chi tiết: " + e.getMessage());
        }
    }

    @FXML
    public void logout() {
        clientModel.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/login.fxml"));
            Stage stage = (Stage) auctionListPane.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Đăng nhập");
        } catch (Exception e) {
            statusLabel.setText("Lỗi: " + e.getMessage());
        }
    }

    @FXML
    public void refreshAuctions() {
        loadAuctions();
    }

    @FXML
    public void searchAuctions(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            renderAuctions(allAuctions);
            return;
        }
        String lower = keyword.toLowerCase();
        List<AuctionDTO> filtered = allAuctions.stream().filter(a -> a.getItemName() != null &&
                a.getItemName().toLowerCase().contains(lower)).collect(Collectors.toList());
        renderAuctions(filtered);
        statusLabel.setText("Tìm thấy " + filtered.size() + " kết quả");
    }

    @FXML
    public void handleSearch() {
        searchAuctions(searchField.getText().trim());
    }

    // Helpers
    private void renderAuctions(List<AuctionDTO> auctions) {
        Platform.runLater(() -> {
            auctionListPane.getChildren().clear();
            for (AuctionDTO dto : auctions) {
                AuctionCard card = new AuctionCard(dto);
                card.setOnMouseClicked(e -> openAuctionDetail(dto.getId()));
                auctionListPane.getChildren().add(card);
            }
        });
    }

    private List<AuctionDTO> parseAuctions(String response) {
        // parsing đc xử lí bởi ResponseHandler
        return com.auction.client.network.ResponseHandler.parseAuctionList(response);
    }
}