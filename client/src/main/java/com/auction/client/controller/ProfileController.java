package com.auction.client.controller;

/**
 * Màn hình hồ sơ cá nhân.
 * Cập nhật họ tên, email; đổi mật khẩu; nạp tiền vào tài khoản (dành cho Bidder); xem lịch sử các bid đã đặt
 */

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
    }

    /**
     * Load thông tin người dùng hiện tại lên giao diện.
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
                balanceLabel.setText(String.format("VND %,.0f", bidder.getBalance()));
            } else {
                balanceLabel.setText("N/A");
            }
        });
    }

    /**
     * Cập nhật thông tin profile lên server.
     * @param userDTO DTO chứa thông tin mới
     */
    public void updateProfile(UserDTO userDTO) {
        String userId = clientModel.getCurrentUser().getId();

        // Chạy trong background thread
        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("UPDATE_PROFILE:" + userId +
                        ":" + userDTO.getFullName() + ":" + userDTO.getEmail());

                Platform.runLater(() -> {
                    if (response != null && response.startsWith("UPDATE_OK")) {
                        // Cập nhật thông tin trong ClientModel
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
     * Nạp tiền vào tài khoản (chỉ dành cho Bidder).
     * @param amount Số tiền cần nạp
     */
    @FXML
    public void addBalance(double amount) {
        if (amount <= 0) {
            // CHANGED: "⚠️ Số tiền phải > 0" -> "⚠️ Amount must be greater than 0"
            statusLabel.setText("⚠️ Amount must be greater than 0");
            return;
        }

        String userId = clientModel.getCurrentUser().getId();

        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("ADD_BALANCE:" + userId + ":" + amount);

                Platform.runLater(() -> {
                    if (response != null && response.startsWith("BALANCE_OK")) {
                        // Cập nhật số dư trong ClientModel
                        User user = clientModel.getCurrentUser();
                        if (user instanceof Bidder bidder) {
                            bidder.addBalance(amount);
                            balanceLabel.setText(String.format("VND %,.0f", bidder.getBalance()));
                        }
                        // CHANGED: "✅ Nạp tiền thành công!" -> "✅ Deposit successful!"
                        statusLabel.setText("✅ Deposit successful!");
                        statusLabel.setStyle("-fx-text-fill: #16A34A;");
                        addBalanceField.clear();
                    } else {
                        // CHANGED: "❌ Nạp tiền thất bại: " -> "❌ Deposit failed: "
                        statusLabel.setText("❌ Deposit failed: " + response);
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
    public void handleAddBalance() {
        try {
            double amount = Double.parseDouble(addBalanceField.getText().trim());
            addBalance(amount);
        } catch (NumberFormatException e) {
            // CHANGED: "⚠️ Số tiền không hợp lệ" -> "⚠️ Invalid amount"
            statusLabel.setText("⚠️ Invalid amount");
        }
    }

    /**
     * Đổi mật khẩu.
     * @param oldPassword Mật khẩu cũ
     * @param newPassword Mật khẩu mới
     */
    @FXML
    public void changePassword(String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isEmpty()) {
            statusLabel.setText("⚠️ Please enter your old password");
            return;
        }
        if (newPassword == null || newPassword.length() < 6) {
            statusLabel.setText("⚠️ New password must be at least 6 characters long");
            return;
        }

        String userId = clientModel.getCurrentUser().getId();

        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("CHANGE_PASSWORD:" + userId + ":" + oldPassword + ":" + newPassword);

                Platform.runLater(() -> {
                    if (response != null && response.startsWith("CHANGE_OK")) {
                        statusLabel.setText("✅ Password changed successfully!");
                        statusLabel.setStyle("-fx-text-fill: #16A34A;");
                        oldPasswordField.clear();
                        newPasswordField.clear();
                    } else {
                        statusLabel.setText("❌ Incorrect old password");
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
            // Load lịch sử bid của user hiện tại
            String userId = clientModel.getCurrentUser().getId();
            controller.loadBidHistoryByUser(userId);

            // CHANGED: "Lịch sử đặt giá của tôi" -> "My Bid History"
            stage.setTitle("My Bid History");
            stage.show();
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
        }
    }

    /**
     * Xóa thông báo sau 3 giây.
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