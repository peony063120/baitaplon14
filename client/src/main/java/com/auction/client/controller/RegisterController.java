package com.auction.client.controller;

/**
 * Handles new account registration.
 * Validates input (password match, valid email, required fields) → sends UserDTO to server
 * → on success, navigates back to login page.
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
        // Items are already defined in register.fxml (BIDDER, SELLER) with BIDDER as default
    }

    @FXML
    public void handleRegister() {
        if (!validateInput()) return;

        // Show processing status
        errorLabel.setStyle("-fx-text-fill: #2563EB;");
        showError("🔄 Processing registration...");

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role = roleComboBox.getValue();

        // Run in background thread
        new Thread(() -> {
            try {
                // Check if username already exists
                boolean exists = checkUsernameExists(username);
                if (exists) {
                    Platform.runLater(() -> {
                        showError("❌ Username already exists");
                        errorLabel.setStyle("-fx-text-fill: #DC2626;");
                    });
                    return;
                }

                // Create DTO and register
                UserDTO dto = new UserDTO(
                        null,
                        username,
                        password,
                        email,
                        fullName,
                        role,
                        0.0
                );

                boolean success = clientModel.register(dto);

                Platform.runLater(() -> {
                    if (success) {
                        showError("✅ Registration successful! Redirecting to login...");
                        errorLabel.setStyle("-fx-text-fill: #16A34A;");
                        // Redirect to login after 1.5 seconds
                        new Thread(() -> {
                            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                            Platform.runLater(this::goToLogin);
                        }).start();
                    } else {
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
     * Check if username already exists on server.
     * @param username Username to check
     * @return true if exists, false otherwise
     * @throws IOException If connection fails
     */
    public boolean checkUsernameExists(String username) throws IOException {
        String response = ServerConnection.getInstance().sendRequest("CHECK_USERNAME:" + username);
        return "EXISTS".equals(response);
    }

    /**
     * Validate input data.
     * @return true if valid, false otherwise
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
            showError("⚠️ Username must be at least 3 characters");
            return false;
        }
        if (password.isEmpty()) {
            showError("⚠️ Please enter a password");
            return false;
        }
        if (password.length() < 6) {
            showError("⚠️ Password must be at least 6 characters");
            return false;
        }
        if (!password.equals(confirm)) {
            showError("⚠️ Passwords do not match");
            return false;
        }
        if (email.isEmpty()) {
            showError("⚠️ Please enter an email");
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showError("⚠️ Invalid email format");
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
            showError("❌ Navigation error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        });
    }
}
