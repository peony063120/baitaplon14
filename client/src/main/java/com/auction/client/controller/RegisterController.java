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
        showError("🔄 Đang xử lý đăng ký...");

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
                        showError("❌ Tên đăng nhập đã tồn tại");
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
                        showError("✅ Đăng ký thành công! Chuyển đến trang đăng nhập...");
                        errorLabel.setStyle("-fx-text-fill: #16A34A;");
                        // Chuyển về màn hình login sau 1.5 giây
                        new Thread(() -> {
                            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                            Platform.runLater(this::goToLogin);
                        }).start();
                    } else {
                        showError("❌ Đăng ký thất bại. Vui lòng thử lại.");
                        errorLabel.setStyle("-fx-text-fill: #DC2626;");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showError("❌ Lỗi kết nối server: " + e.getMessage());
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
            showError("⚠️ Vui lòng nhập tên đăng nhập");
            return false;
        }
        if (username.length() < 3) {
            showError("⚠️ Tên đăng nhập phải có ít nhất 3 ký tự");
            return false;
        }
        if (password.isEmpty()) {
            showError("⚠️ Vui lòng nhập mật khẩu");
            return false;
        }
        if (password.length() < 6) {
            showError("⚠️ Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }
        if (!password.equals(confirm)) {
            showError("⚠️ Mật khẩu xác nhận không khớp");
            return false;
        }
        if (email.isEmpty()) {
            showError("⚠️ Vui lòng nhập email");
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showError("⚠️ Email không hợp lệ");
            return false;
        }
        if (fullName.isEmpty()) {
            showError("⚠️ Vui lòng nhập họ và tên");
            return false;
        }

        errorLabel.setVisible(false);
        return true;
    }

    @FXML
    public void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Đăng nhập");
        } catch (Exception e) {
            showError("❌ Lỗi điều hướng: " + e.getMessage());
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
