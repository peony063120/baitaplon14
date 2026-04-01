package com.auction.client.controller;

import com.auction.client.ClientApp;
import com.auction.client.network.ServerConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.auction.client.controller.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;

    private String currentUserId;
    private String currentUserRole;

    @FXML
    public void initialize() {
        // Set up enter key to trigger login
        passwordField.setOnAction(event -> handleLogin());

        // Try to connect to server
        try {
            ServerConnection.connect("localhost", 8080);
            statusLabel.setText("Connected to server");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            logger.error("Failed to connect to server", e);
            statusLabel.setText("Cannot connect to server. Please check if server is running.");
            statusLabel.setStyle("-fx-text-fill: red;");
            loginButton.setDisable(true);
            registerButton.setDisable(true);
        }
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter username and password", Alert.AlertType.ERROR);
            return;
        }

        loginButton.setDisable(true);
        statusLabel.setText("Logging in...");

        new Thread(() -> {
            try {
                var response = ServerConnection.login(username, password);

                Platform.runLater(() -> {
                    loginButton.setDisable(false);

                    if (response != null && response.containsKey("success") &&
                            (boolean) response.get("success")) {
                        String userInfo = (String) response.get("data");
                        String[] parts = userInfo.split(":");
                        currentUserId = parts[0];
                        currentUserRole = parts[1];

                        logger.info("Login successful: {} as {}", username, currentUserRole);

                        try {
                            FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("/view/main.fxml"));
                            Parent root = loader.load();
                            MainController mainController = loader.getController();
                            mainController.setUserInfo(currentUserId, username, currentUserRole);

                            Stage stage = (Stage) loginButton.getScene().getWindow();
                            stage.setTitle("Auction System - Dashboard");
                            stage.setScene(new Scene(root));
                            stage.show();
                        } catch (Exception e) {
                            logger.error("Failed to load main screen", e);
                            showAlert("Error", "Failed to load main screen", Alert.AlertType.ERROR);
                        }
                    } else {
                        String message = response != null ?
                                (String) response.getOrDefault("message", "Login failed") : "Login failed";
                        statusLabel.setText(message);
                        statusLabel.setStyle("-fx-text-fill: red;");
                        showAlert("Login Failed", message, Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                logger.error("Login error", e);
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    statusLabel.setText("Connection error: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: red;");
                    showAlert("Connection Error", e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    @FXML
    public void handleRegister() {
        try {
            ClientApp.switchScene("/view/register.fxml", "Register New Account");
        } catch (Exception e) {
            logger.error("Failed to load register screen", e);
            showAlert("Error", "Failed to load registration screen", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUserRole() {
        return currentUserRole;
    }
}