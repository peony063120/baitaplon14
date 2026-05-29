package com.auction.client;

import com.auction.client.network.ServerConnection;
import javafx.application.Application;

public final class ClientApp {
    private ClientApp() {
    }

    public static void main(String[] args) {
        Application.launch(AuctionClientApplication.class, args);
    }

    public static void showLoginScreen() throws Exception {
        AuctionClientApplication.showLoginScreen();
    }

    public static void showMainScreen() throws Exception {
        AuctionClientApplication.showMainScreen();
    }

    public static ServerConnection getServerConnection() {
        return AuctionClientApplication.getServerConnection();
    }
}
