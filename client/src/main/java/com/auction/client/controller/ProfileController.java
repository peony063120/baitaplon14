package com.auction.client.controller;

/**
 * Profile screen.
 * Update name, email; change password; top-up balance (for Bidder); view bid history
 */

import com.auction.client.config.AppConfig;
import com.auction.client.model.ClientModel;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.UserDTO;
import com.auction.common.entity.Bidder;
import com.auction.common.entity.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

import java.io.IOException;

public class ProfileController {

    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private Label balanceLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField addBalanceField;
    @FXML private Label statusLabel;

    private final ClientModel clientModel = ClientModel.getInstance();

    @FXML
    public void initialize() {
        loadUserProfile();
        setupNumberFormatting(addBalanceField);
    }

    private void setupNumberFormatting(TextField field) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            if (change.isContentChange()) {
                if (!change.getControlNewText().matches("\\d*")) {
                    return null;
                }
            }
            return change;
        }));
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String raw = field.getText().replaceAll("\\D", "");
                if (!raw.isEmpty()) {
                    StringBuilder sb = new StringBuilder(raw);
                    for (int i = sb.length() - 3; i > 0; i -= 3) {
                        sb.insert(i, ' ');
                    }
                    field.setText(sb.toString());
                }
            }
        });
    }

    /**
     * Load current user info into UI.
     */
    public void loadUserProfile() {
        User user = clientModel.getCurrentUser();
        if (user == null) return;

        Platform.runLater(() -> {
            usernameLabel.setText(user.getUsername());
            roleLabel.setText(user.getRole());
            fullNameField.setText(user.getFullName());
            emailField.setText(user.getEmail());

            if (user instanceof Bidder bidder) {
                balanceLabel.setText(String.format("$%,.0f", bidder.getBalance()));
            } else {
                balanceLabel.setText("N/A");
            }
        });
    }

    /**
     * Update profile info on server.
     * @param userDTO DTO with new info
     */
    public void updateProfile(UserDTO userDTO) {
        String username = clientModel.getCurrentUser().getUsername();

        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("UPDATE_PROFILE:" + username +
                        ":" + userDTO.getFullName() + ":" + userDTO.getEmail());

                Platform.runLater(() -> {
                    if (response != null && response.startsWith("UPDATE_OK")) {
                        // Update info in ClientModel
                        User currentUser = clientModel.getCurrentUser();
                        if (currentUser != null) {
                            currentUser.setFullName(userDTO.getFullName());
                            currentUser.setEmail(userDTO.getEmail());
                        }
                        statusLabel.setText("✅ Profile updated successfully!");
                        statusLabel.setStyle("-fx-text-fill: #16A34A;");
                    } else {
                        statusLabel.setText("❌ Update failed: " + response);
                        statusLabel.setStyle("-fx-text-fill: #DC2626;");
                    }
                    clearStatusAfterDelay();
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Connection error: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #DC2626;");
                    clearStatusAfterDelay();
                });
            }
        }).start();
    }

    @FXML
    public void handleUpdateProfile() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();

        if (fullName.isEmpty()) {
            statusLabel.setText("⚠️ Please enter your full name");
            return;
        }
        if (email.isEmpty()) {
            statusLabel.setText("⚠️ Please enter your email");
            return;
        }

        UserDTO dto = new UserDTO();
        dto.setFullName(fullName);
        dto.setEmail(email);
        updateProfile(dto);
    }

    /**
     * Top up account balance (Bidder only).
     * @param amount Amount to add
     */
    @FXML
    public void addBalance(double amount) {
        if (amount <= 0) {
            statusLabel.setText("⚠️ Amount must be > 0");
            return;
        }

        String username = clientModel.getCurrentUser().getUsername();

        if (AppConfig.isUseMock()) {
            User user = clientModel.getCurrentUser();
            if (user instanceof Bidder bidder) {
                bidder.addBalance(amount);
                Platform.runLater(() -> {
                    balanceLabel.setText(String.format("$%,.0f", bidder.getBalance()));
                    statusLabel.setText("✅ Balance topped up successfully!");
                    statusLabel.setStyle("-fx-text-fill: #16A34A;");
                    addBalanceField.clear();
                    MainController.refreshBalance();
                    clearStatusAfterDelay();
                });
            }
            return;
        }

        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("ADD_BALANCE:" + username + ":" + amount);

                Platform.runLater(() -> {
                    if (response != null && response.startsWith("BALANCE_OK")) {
                        User user = clientModel.getCurrentUser();
                        if (user instanceof Bidder bidder) {
                            bidder.addBalance(amount);
                            balanceLabel.setText(String.format("$%,.0f", bidder.getBalance()));
                        }
                        statusLabel.setText("✅ Balance topped up successfully!");
                        statusLabel.setStyle("-fx-text-fill: #16A34A;");
                        addBalanceField.clear();
                    } else {
                        statusLabel.setText("❌ Top-up failed: " + response);
                        statusLabel.setStyle("-fx-text-fill: #DC2626;");
                    }
                    MainController.refreshBalance();
                    clearStatusAfterDelay();
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Connection error: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #DC2626;");
                    clearStatusAfterDelay();
                });
            }
        }).start();
    }

    @FXML
    public void handleAddBalance() {
        try {
            double amount = Double.parseDouble(addBalanceField.getText().replaceAll("\\D", ""));
            addBalance(amount);
        } catch (NumberFormatException e) {
            statusLabel.setText("⚠️ Invalid amount");
        }
    }

    /**
     * Change password.
     * @param oldPassword Current password
     * @param newPassword New password
     */
    @FXML
    public void changePassword(String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isEmpty()) {
            statusLabel.setText("⚠️ Please enter your current password");
            return;
        }
        if (newPassword == null || newPassword.length() < 6) {
            statusLabel.setText("⚠️ New password must be at least 6 characters");
            return;
        }

        String username = clientModel.getCurrentUser().getUsername();

        if (AppConfig.isUseMock()) {
            User user = clientModel.getCurrentUser();
            if (user != null && user.authenticate(oldPassword)) {
                user.setPassword(newPassword);
                Platform.runLater(() -> {
                    statusLabel.setText("✅ Password changed successfully!");
                    statusLabel.setStyle("-fx-text-fill: #16A34A;");
                    oldPasswordField.clear();
                    newPasswordField.clear();
                    clearStatusAfterDelay();
                });
            } else {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Current password is incorrect");
                    statusLabel.setStyle("-fx-text-fill: #DC2626;");
                    clearStatusAfterDelay();
                });
            }
            return;
        }

        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("CHANGE_PASSWORD:" + username + ":" + oldPassword + ":" + newPassword);

                Platform.runLater(() -> {
                    if (response != null && response.startsWith("CHANGE_OK")) {
                        clientModel.getCurrentUser().setPassword(newPassword);
                        statusLabel.setText("✅ Password changed successfully!");
                        statusLabel.setStyle("-fx-text-fill: #16A34A;");
                        oldPasswordField.clear();
                        newPasswordField.clear();
                    } else {
                        statusLabel.setText("❌ Current password is incorrect");
                        statusLabel.setStyle("-fx-text-fill: #DC2626;");
                    }
                    clearStatusAfterDelay();
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Connection error: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #DC2626;");
                    clearStatusAfterDelay();
                });
            }
        }).start();
    }

    @FXML
    public void handleChangePassword() {
        changePassword(oldPasswordField.getText(), newPasswordField.getText());
    }

    @FXML
    public void viewMyBids() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/bid_history.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            BidHistoryController controller = loader.getController();
            // Load bid history for current user
        String username = clientModel.getCurrentUser().getUsername();
            controller.loadBidHistoryByUser(username);

            stage.setTitle("My Bid History");
            stage.show();
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
        }
    }

    /**
     * Clear status message after 3 seconds.
     */
    private void clearStatusAfterDelay() {
        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                statusLabel.setText("");
                statusLabel.setStyle("");
            });
        }).start();
    }
}
