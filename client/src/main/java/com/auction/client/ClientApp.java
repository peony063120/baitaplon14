package com.auction.client;

import com.auction.client.network.ServerConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ClientApp extends Application {
    private static Stage primaryStage;
    private static ServerConnection serverConnection;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        serverConnection = ServerConnection.getInstance();

        try {
            serverConnection.connect("localhost", 5000);
        } catch (Exception e) {
            System.err.println("Could not connect to the server:" + e.getMessage());
        }

        // Load login screen
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