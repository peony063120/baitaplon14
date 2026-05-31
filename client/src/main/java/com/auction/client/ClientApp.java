package com.auction.client;

import com.auction.client.config.AppConfig;
import com.auction.client.network.ServerConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ClientApp extends Application {
    private static Stage primaryStage;
    private static ServerConnection serverConnection;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Nếu mock mode, không cần kết nối server
        if (!AppConfig.isUseMock()) {
            serverConnection = ServerConnection.getInstance();
            try {
                serverConnection.connect("localhost", 5050);
            } catch (Exception e) {
                System.err.println("Cannot connect to server: " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Connection failed");
                alert.setHeaderText("Cannot connect to Auction Server");
                alert.setContentText("Server is not running. Use -Dapp.useMock=true to enable mock mode.");
                alert.showAndWait();
            }
        } else {
            System.out.println("[ClientApp] Mock mode enabled — no server connection needed.");
        }

        showLoginScreen();

        stage.setTitle("Online Auction System");
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        stage.show();
    }

    public static void showLoginScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("/com/auction/client/view/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ClientApp.class.getResource("/com/auction/client/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void showMainScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("/com/auction/client/view/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ClientApp.class.getResource("/com/auction/client/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void showSellerScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("/com/auction/client/view/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ClientApp.class.getResource("/com/auction/client/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void showAdminScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("/com/auction/client/view/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ClientApp.class.getResource("/com/auction/client/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void showAdminDashboard() throws Exception {
        FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("/com/auction/client/view/admin.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ClientApp.class.getResource("/com/auction/client/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static ServerConnection getServerConnection() {
        return serverConnection;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        if (serverConnection != null) {
            serverConnection.disconnect();
        }
    }
}
