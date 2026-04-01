package com.auction.client.controller;

import com.auction.client.ClientApp;
import com.auction.client.network.ServerConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("BIDDER", "SELLER");
        roleComboBox.setValue("BIDDER");
    }

    @FXML
    public void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        // Validation
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in all fields", Alert.AlertType.ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match", Alert.AlertType.ERROR);
            return;
        }

        if (password.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters", Alert.AlertType.ERROR);
            return;
        }

        if (!email.contains("@")) {
            showAlert("Error", "Please enter a valid email", Alert.AlertType.ERROR);
            return;
        }

        registerButton.setDisable(true);
        statusLabel.setText("Creating account...");

        new Thread(() -> {
            try {
                Map<String, Object> response = ServerConnection.register(
                        username, password, email, fullName, role);

                Platform.runLater(() -> {
                    registerButton.setDisable(false);

                    if (response != null && (boolean) response.getOrDefault("success", false)) {
                        showAlert("Success", "Account created successfully! Please login.", Alert.AlertType.INFORMATION);
                        handleBackToLogin();
                    } else {
                        String message = response != null ?
                                (String) response.getOrDefault("message", "Registration failed") : "Registration failed";
                        statusLabel.setText(message);
                        statusLabel.setStyle("-fx-text-fill: red;");
                        showAlert("Registration Failed", message, Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                logger.error("Registration error", e);
                Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    statusLabel.setText("Connection error: " + e.getMessage());
                    showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    @FXML
    public void handleBackToLogin() {
        try {
            ClientApp.switchScene("/view/login.fxml", "Login");
        } catch (Exception e) {
            logger.error("Failed to load login screen", e);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}