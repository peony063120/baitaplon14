package com.auction.client.controller;

/**
 * Xử lý màn hình đăng nhập.
 * Người dùng nhập username/password → validate → gửi lên server
 * → nếu thành công thì chuyển sang màn hình chính main.fxml
 */

import com.auction.client.model.ClientModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final ClientModel clientModel = ClientModel.getInstance();

    @FXML
    public void handleLogin() {
        if (!validateCredentials()) return;

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        boolean success = clientModel.login(username, password);
        if (success) {
            navigateToMain();
        } else {
            showError("Sai tên đăng nhập hoặc mật khẩu");
        }
    }

    @FXML
    public void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/register.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Đăng ký");
        } catch (Exception e) {
            showError("Không thể mở trang đăng ký: " + e.getMessage());
        }
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private boolean validateCredentials() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập");
            return false;
        }
        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu");
            return false;
        }
        errorLabel.setVisible(false);
        return true;
    }

    private void navigateToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/main.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Online Auction System");
        } catch (Exception e) {
            showError("Lỗi điều hướng: " + e.getMessage());
        }
    }
}
