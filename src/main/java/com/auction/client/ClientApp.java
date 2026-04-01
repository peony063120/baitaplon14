package com.auction.client;

import com.auction.client.controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Load login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Parent root = loader.load();

        stage.setTitle("Online Auction System - Login");
        stage.setScene(new Scene(root, 400, 350));
        stage.setMinWidth(400);
        stage.setMinHeight(350);
        stage.show();
    }

    public static void switchScene(String fxml, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource(fxml));
        Parent root = loader.load();
        primaryStage.setTitle(title);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}