package com.auction.client.controller;

/**
 * Xử lý màn hình đăng ký tài khoản mới.
 * Validate đầu vào (password khớp, email hợp lệ, đủ thông tin) → gửi UserDTO lên server
 * → nếu thành công thì chuyển về trang đăng nhập.
 */

import com.auction.client.model.ClientModel;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.UserDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField fullNameField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;

    private final ClientModel clientModel = ClientModel.getInstance();

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("BIDDER", "SELLER");
        roleComboBox.setValue("BIDDER");
    }

    @FXML
    public void handleRegister() {
        if (!validateInput()) return;

        // thứ tự constructor: id, username, password, email, fullName, role, balance
        UserDTO dto = new UserDTO(
                null,
                usernameField.getText().trim(),
                encryptPassword(passwordField.getText().trim()),
                emailField.getText().trim(),
                fullNameField.getText().trim(),
                roleComboBox.getValue(),
                0.0
        );
        // set password riêng vì constructor ko có
        // password đc xử lí bên ServerConnection
        boolean success = clientModel.register(dto);
        if (success) {
            goToLogin();
        } else {
            showError("Đăng ký thất bại. Username có thể đã tồn tại.");
        }
    }

    public boolean validateInput() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin");
            return false;
        }
        if (!password.equals(confirm)) {
            showError("Mật khẩu xác nhận không khớp");
            return false;
        }
        if (password.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }
        if (!email.contains("@")) {
            showError("Email không hợp lệ");
            return false;
        }
        errorLabel.setVisible(false);
        return true;
    }

    public boolean checkUsernameExists(String username) {
        String response = ServerConnection.getInstance().sendRequest("CHECK_USERNAME:" + username);
        return "EXISTS".equals(response);
    }

    @FXML
    public void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Đăng nhập");
        } catch (Exception e) {
            showError("Lỗi điều hướng: " + e.getMessage());
        }
    }

    private String encryptPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

}