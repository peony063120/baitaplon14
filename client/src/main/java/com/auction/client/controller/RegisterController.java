package com.auction.client.controller;

/**
 * Xử lý màn hình đăng ký tài khoản mới.
 * Validate đầu vào (password khớp, email hợp lệ, đủ thông tin) → gửi UserDTO lên server
 * → nếu thành công thì chuyển về trang đăng nhập.
 */

import com.auction.client.model.ClientModel;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.UserDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

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

        // Hiển thị trạng thái đang xử lý
        errorLabel.setStyle("-fx-text-fill: #2563EB;");
        // CHANGED: "🔄 Đang xử lý đăng ký..." -> "🔄 Processing registration..."
        showError("🔄 Processing registration...");

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role = roleComboBox.getValue();

        // Chạy trong background thread
        new Thread(() -> {
            try {
                // Kiểm tra username đã tồn tại chưa
                boolean exists = checkUsernameExists(username);
                if (exists) {
                    Platform.runLater(() -> {
                        showError("❌ Username already exists");
                        errorLabel.setStyle("-fx-text-fill: #DC2626;");
                    });
                    return;
                }

                // Tạo DTO và đăng ký
                UserDTO dto = new UserDTO(
                        null,
                        username,
                        encryptPassword(password),
                        email,
                        fullName,
                        role,
                        0.0
                );

                boolean success = clientModel.register(dto);

                Platform.runLater(() -> {
                    if (success) {
                        // CHANGED: "✅ Đăng ký thành công! Chuyển đến trang đăng nhập..." -> "✅ Registration successful! Redirecting to login..."
                        showError("✅ Registration successful! Redirecting to login...");
                        errorLabel.setStyle("-fx-text-fill: #16A34A;");
                        // Chuyển về màn hình login sau 1.5 giây
                        new Thread(() -> {
                            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                            Platform.runLater(this::goToLogin);
                        }).start();
                    } else {
                        // CHANGED: "❌ Đăng ký thất bại. Vui lòng thử lại." -> "❌ Registration failed. Please try again."
                        showError("❌ Registration failed. Please try again.");
                        errorLabel.setStyle("-fx-text-fill: #DC2626;");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showError("❌ Server connection error: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #DC2626;");
                });
            }
        }).start();
    }

    /**
     * Kiểm tra username đã tồn tại trên server.
     * @param username Tên đăng nhập cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     * @throws IOException Nếu lỗi kết nối
     */
    public boolean checkUsernameExists(String username) throws IOException {
        String response = ServerConnection.getInstance().sendRequest("CHECK_USERNAME:" + username);
        return "EXISTS".equals(response);
    }

    /**
     * Validate dữ liệu đầu vào.
     * @return true nếu hợp lệ, false nếu không
     */
    public boolean validateInput() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();

        if (username.isEmpty()) {
            showError("⚠️ Please enter a username");
            return false;
        }
        if (username.length() < 3) {
            showError("⚠️ Username must be at least 3 characters long");
            return false;
        }
        if (password.isEmpty()) {
            showError("⚠️ Please enter a password");
            return false;
        }
        if (password.length() < 6) {
            showError("⚠️ Password must be at least 6 characters long");
            return false;
        }
        if (!password.equals(confirm)) {
            showError("⚠️ Passwords do not match");
            return false;
        }
        if (email.isEmpty()) {
            showError("⚠️ Please enter an email address");
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showError("⚠️ Invalid email address");
            return false;
        }
        if (fullName.isEmpty()) {
            showError("⚠️ Please enter your full name");
            return false;
        }

        errorLabel.setVisible(false);
        return true;
    }

    @FXML
    public void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Login");
        } catch (Exception e) {
            // CHANGED: "❌ Lỗi điều hướng: " -> "❌ Navigation error: "
            showError("❌ Navigation error: " + e.getMessage());
        }
    }

    /**
     * Mã hóa mật khẩu đơn giản.
     * @param password Mật khẩu plain text
     * @return Mật khẩu đã mã hóa
     */
    private String encryptPassword(String password) {
        // TODO: Sử dụng mã hóa mạnh hơn (BCrypt) trong production
        return Integer.toHexString(password.hashCode());
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        });
    }
}