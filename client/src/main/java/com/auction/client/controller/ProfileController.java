package com.auction.client.controller;

/**
 * Màn hình hồ sơ cá nhân.
 * Cập nhật họ tên, email; đổi mật khẩu; nạp tiền vào tài khoản (dành cho Bidder); xem lịch sử các bid đã đặt
 */

import com.auction.client.model.ClientModel;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.UserDTO;
import com.auction.common.entity.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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

    public void loadUserProfile() {
        User user = clientModel.getCurrentUser();
        if (user == null) return;

        usernameLabel.setText(user.getUsername());
        roleLabel.setText(user.getRole());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());

        // lấy balance nếu là Bidder
        if (user instanceof com.auction.common.entity.Bidder bidder) {
            balanceLabel.setText(String.format("%.0f VNĐ", bidder.getBalance()));
        } else {
            balanceLabel.setText("N/A");
        }
    }

    @FXML
    public void updateProfile(UserDTO userDTO) {
        String userId = clientModel.getCurrentUser().getId();
        String response = ServerConnection.getInstance().sendRequest("UPDATE_PROFILE:" + userId +
                ":" + userDTO.getFullName() + ":" + userDTO.getEmail());

        if ("UPDATE_OK".equals(response)) {
            statusLabel.setText("Cập nhật thành công!");
        } else {
            statusLabel.setText("Cập nhật thất bại");
        }
    }

    @FXML
    public void handleUpdateProfile() {
        UserDTO dto = new UserDTO();
        dto.setFullName(fullNameField.getText().trim());
        dto.setEmail(emailField.getText().trim());
        updateProfile(dto);
    }

    @FXML
    public void addBalance(double amount) {
        if (amount <= 0) {
            statusLabel.setText("Số tiền phải > 0");
            return;
        }
        String userId = clientModel.getCurrentUser().getId();
        String response = ServerConnection.getInstance().sendRequest("ADD_BALANCE:" + userId + ":" + amount);

        if ("BALANCE_OK".equals(response)) {
            loadUserProfile();
            statusLabel.setText("Nạp tiền thành công!");
        } else {
            statusLabel.setText("Nạp tiền thất bại");
        }
    }

    @FXML
    public void handleAddBalance() {
        try {
            double amount = Double.parseDouble(addBalanceField.getText().trim());
            addBalance(amount);
        } catch (NumberFormatException e) {
            statusLabel.setText("Số tiền không hợp lệ");
        }
    }

    @FXML
    public void changePassword(String oldPassword, String newPassword) {
        if (newPassword.length() < 6) {
            statusLabel.setText("Mật khẩu mới phải ít nhất 6 ký tự");
            return;
        }
        String userId = clientModel.getCurrentUser().getId();
        String response = ServerConnection.getInstance().sendRequest("CHANGE_PASSWORD:" + userId + ":" + oldPassword + ":" + newPassword);

        statusLabel.setText("CHANGE_OK".equals(response) ? "Đổi mật khẩu thành công!" : "Mật khẩu cũ không đúng");
    }

    @FXML
    public void handleChangePassword() {
        changePassword(oldPasswordField.getText(), newPasswordField.getText());
    }

    @FXML
    public void viewMyBids() {
        statusLabel.setText("Tính năng đang phát triển...");
    }
}
