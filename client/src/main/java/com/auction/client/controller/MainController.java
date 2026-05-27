package com.auction.client.controller;

import com.auction.client.ClientApp;
import com.auction.client.model.ClientModel;
import com.auction.common.entity.Bidder;
import com.auction.common.entity.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML private TextField searchField;
    @FXML private Label clockLabel;
    @FXML private Text balanceLabel;
    @FXML private StackPane contentPane;
    @FXML private VBox sellerMenu;
    @FXML private ToggleButton homeNav;
    @FXML private ToggleButton liveNav;
    @FXML private ToggleButton historyNav;
    @FXML private ToggleButton walletNav;
    @FXML private ToggleButton profileNav;

    private final ClientModel clientModel = ClientModel.getInstance();
    private DashboardController dashboardController;

    @FXML
    public void initialize() {
        updateHeader();
        showDashboard();
    }

    private void updateHeader() {
        clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        User currentUser = clientModel.getCurrentUser();
        if (currentUser instanceof Bidder bidder) {
            balanceLabel.setText(String.format("VND %,.0f", bidder.getBalance()));
        } else {
            balanceLabel.setText("VND 0");
        }

        boolean seller = currentUser != null && "SELLER".equalsIgnoreCase(currentUser.getRole());
        sellerMenu.setVisible(seller);
        sellerMenu.setManaged(seller);
    }

    @FXML
    public void showDashboard() {
        dashboardController = loadContent("/view/dashboard.fxml");
        selectNav(homeNav);
    }

    @FXML
    public void showLive() {
        dashboardController = loadContent("/view/dashboard.fxml");
        if (dashboardController != null) {
            dashboardController.setFilter("ending");
        }
        selectNav(liveNav);
    }

    @FXML
    public void showHistory() {
        loadContent("/view/bid_history.fxml");
        selectNav(historyNav);
    }

    @FXML
    public void showProfile() {
        loadContent("/view/profile.fxml");
        selectNav(profileNav);
    }

    @FXML
    public void showWallet() {
        selectNav(walletNav);
        showInfo("Wallet", "Wallet actions are available from your profile for now.");
    }

    @FXML
    public void showCreateAuction() {
        loadContent("/view/create_auction.fxml");
    }

    @FXML
    public void showMyAuctions() {
        loadContent("/view/my_auctions.fxml");
    }

    @FXML
    public void showNotifications() {
        showInfo("Notifications", "No new notifications.");
    }

    @FXML
    public void onDeposit() {
        showProfile();
    }

    @FXML
    public void onWithdraw() {
        showInfo("Withdraw", "Withdrawals are not implemented yet.");
    }

    @FXML
    public void onLogout() {
        clientModel.logout();
        try {
            ClientApp.showLoginScreen();
        } catch (Exception e) {
            showInfo("Logout error", e.getMessage());
        }
    }

    @FXML
    public void filterByCategory(ActionEvent event) {
        if (dashboardController == null) {
            showDashboard();
        }
        if (dashboardController == null || !(event.getSource() instanceof ToggleButton button)) {
            return;
        }
        Object userData = button.getUserData();
        dashboardController.filterByCategory(userData != null ? userData.toString() : "all");
    }

    @FXML
    public void handleSearch() {
        if (dashboardController == null) {
            showDashboard();
        }
        if (dashboardController != null) {
            dashboardController.searchAuctions(searchField.getText());
        }
    }

    private <T> T loadContent(String resourcePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Node view = loader.load();
            contentPane.getChildren().setAll(view);
            return loader.getController();
        } catch (IOException | RuntimeException e) {
            showInfo("Screen error", "Cannot open " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    private void selectNav(ToggleButton selected) {
        for (ToggleButton button : new ToggleButton[]{homeNav, liveNav, historyNav, walletNav, profileNav}) {
            if (button != null) {
                button.setSelected(button == selected);
            }
        }
    }

    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.showAndWait();
        });
    }
}
