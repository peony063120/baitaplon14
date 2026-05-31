package com.auction.client.controller;

/**
 * Xử lý màn hình đăng nhập.
 * Người dùng nhập username/password → validate → gửi lên server
 * → nếu thành công thì chuyển sang màn hình chính main.fxml
 */

import com.auction.client.ClientApp;
import com.auction.client.config.AppConfig;
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
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label errorLabel;
    @FXML private ToggleButton mockToggle;

    private final ClientModel clientModel = ClientModel.getInstance();

    @FXML
    public void initialize() {
        boolean mock = AppConfig.isUseMock();
        mockToggle.setSelected(mock);
        mockToggle.setText(mock ? "MOCK MODE" : "LIVE");
    }

    @FXML
    public void toggleMock() {
        boolean nowMock = mockToggle.isSelected();
        AppConfig.setUseMock(nowMock);
        mockToggle.setText(nowMock ? "MOCK MODE" : "LIVE");
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
            clientModel.setSession(
                    loginResp.getSessionToken(),
                    loginResp.getUserId(),
                    loginResp.getUsername(),
                    loginResp.getRole(),
                    loginResp.getBalance()
            );
            try {
                String role = loginResp.getRole();
                if ("ADMIN".equalsIgnoreCase(role)) {
                    ClientApp.showAdminDashboard();
                } else if ("SELLER".equalsIgnoreCase(role)) {
                    ClientApp.showSellerScreen();
                } else {
                    ClientApp.showMainScreen();
                }
            } catch (Exception e) {
                showError("Navigation error: " + e.getMessage());
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
            stage.setTitle("Register");
        } catch (Exception e) {
            showError("Cannot open registration: " + e.getMessage());
        }
    }

    private boolean validateCredentials() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty()) {
            showError("Please enter username");
            return false;
        }
        if (password.isEmpty()) {
            showError("Please enter password");
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
