package com.auction.client.controller;

/**
 * Xử lý màn hình đăng nhập.
 * Người dùng nhập username/password → validate → gửi lên server
 * → nếu thành công thì chuyển sang màn hình chính main.fxml
 */

import com.auction.client.ClientApp;
import com.auction.client.model.ClientModel;
import com.auction.client.service.DataService;
import com.auction.common.dto.LoginResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;   // ← THÊM roleCombo
    @FXML private Label errorLabel;

    private final ClientModel clientModel = ClientModel.getInstance();

    @FXML
    public void initialize() {
        // Khởi tạo roleCombo
        roleCombo.getItems().addAll("BIDDER", "SELLER", "ADMIN");
        roleCombo.setValue("BIDDER");
    }

    @FXML
    private void handleLogin() {
        if (!validateCredentials()) return;

        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String role = roleCombo.getValue();

        // Sử dụng DataService để login (hỗ trợ mock/API)
        DataService.getInstance().login(username, password, role,
                this::onLoginSuccess,
                this::onLoginError
        );
    }

    private void onLoginSuccess(LoginResponse loginResp) {
        if (loginResp.isSuccess()) {
            // Lưu session vào ClientModel
            clientModel.setSession(
                    loginResp.getSessionToken(),
                    loginResp.getUserId(),
                    loginResp.getUsername(),
                    loginResp.getRole(),
                    loginResp.getBalance()
            );
            // Chuyển sang màn hình chính
            try {
                ClientApp.showMainScreen();
            } catch (Exception e) {
                // CHANGED: "Lỗi chuyển màn hình: " -> "Screen transition error: "
                showError("Screen transition error: " + e.getMessage());
            }
        } else {
            showError(loginResp.getMessage());
        }
    }

    private void onLoginError(String error) {
        showError("Connection error: " + error);
    }

    @FXML
    public void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/register.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            // CHANGED: "Đăng ký" -> "Register"
            stage.setTitle("Register");
        } catch (Exception e) {
            // CHANGED: "Không thể mở trang đăng ký: " -> "Cannot open registration page: "
            showError("Cannot open registration page: " + e.getMessage());
        }
    }

    private boolean validateCredentials() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty()) {
            showError("Please enter your username");
            return false;
        }
        if (password.isEmpty()) {
            showError("Please enter your password");
            return false;
        }
        errorLabel.setVisible(false);
        return true;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}