package com.auction.client.service;

import com.auction.client.config.AppConfig;
import com.auction.client.network.ResponseHandler;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.LoginResponse;
import com.auction.common.entity.BidTransaction;
import javafx.application.Platform;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class DataService {

    private static DataService instance;
    private DataService() {}
    public static DataService getInstance() {
        if (instance == null) instance = new DataService();
        return instance;
    }

    // ==================== LOAD AUCTIONS ====================
    public void loadAuctions(Consumer<List<AuctionDTO>> onSuccess, Consumer<String> onError) {
        if (AppConfig.isUseMock()) {
            if (onSuccess != null) onSuccess.accept(MockDataProvider.getAuctions());
            return;
        }
        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("GET_AUCTIONS");
                List<AuctionDTO> auctions = ResponseHandler.parseAuctionListFromText(response);
                if (onSuccess != null) Platform.runLater(() -> onSuccess.accept(auctions));
            } catch (IOException e) {
                if (AppConfig.isAutoFallback()) {
                    System.err.println("API fail, fallback to mock: " + e.getMessage());
                    if (onSuccess != null) Platform.runLater(() -> onSuccess.accept(MockDataProvider.getAuctions()));
                } else if (onError != null) {
                    Platform.runLater(() -> onError.accept(e.getMessage()));
                }
            }
        }).start();
    }

    // ==================== LOAD AUCTION DETAIL ====================
    public void loadAuctionDetail(String auctionId, Consumer<AuctionDTO> onSuccess, Consumer<String> onError) {
        if (AppConfig.isUseMock()) {
            if (onSuccess != null) onSuccess.accept(MockDataProvider.getAuctionDetail(auctionId));
            return;
        }
        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("GET_AUCTION:" + auctionId);
                AuctionDTO auction = ResponseHandler.parseAuctionDetailFromText(response);
                if (onSuccess != null) Platform.runLater(() -> onSuccess.accept(auction));
            } catch (IOException e) {
                if (AppConfig.isAutoFallback()) {
                    if (onSuccess != null) Platform.runLater(() -> onSuccess.accept(MockDataProvider.getAuctionDetail(auctionId)));
                } else if (onError != null) {
                    Platform.runLater(() -> onError.accept(e.getMessage()));
                }
            }
        }).start();
    }

    // ==================== LOAD BID HISTORY ====================
    public void loadBidHistory(String auctionId, Consumer<List<BidTransaction>> onSuccess, Consumer<String> onError) {
        if (AppConfig.isUseMock()) {
            if (onSuccess != null) onSuccess.accept(MockDataProvider.getBidHistory(auctionId));
            return;
        }
        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("GET_BID_HISTORY:" + auctionId);
                List<BidTransaction> history = ResponseHandler.parseBidHistoryFromText(response);
                if (onSuccess != null) Platform.runLater(() -> onSuccess.accept(history));
            } catch (IOException e) {
                if (AppConfig.isAutoFallback()) {
                    if (onSuccess != null) Platform.runLater(() -> onSuccess.accept(MockDataProvider.getBidHistory(auctionId)));
                } else if (onError != null) {
                    Platform.runLater(() -> onError.accept(e.getMessage()));
                }
            }
        }).start();
    }

    // ==================== LOGIN ====================
    public void login(String username, String password, String role,
                      Consumer<LoginResponse> onSuccess, Consumer<String> onError) {
        if (AppConfig.isUseMock()) {
            MockUserStore userStore = MockUserStore.getInstance();
            LoginResponse resp = userStore.login(username, password, role);
            if (onSuccess != null) onSuccess.accept(resp);
            return;
        }
        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest(
                        "LOGIN:" + username + ":" + password + ":" + role);
                LoginResponse result = ResponseHandler.parseLoginResponse(response);
                if (onSuccess != null) Platform.runLater(() -> onSuccess.accept(result));
            } catch (IOException e) {
                if (AppConfig.isAutoFallback()) {
                    if (onSuccess != null) Platform.runLater(() -> onSuccess.accept(MockDataProvider.getLoginResponse(role)));
                } else if (onError != null) {
                    Platform.runLater(() -> onError.accept(e.getMessage()));
                }
            }
        }).start();
    }

    // ==================== LOAD MY AUCTIONS ====================
    public void loadMyAuctions(String sellerId, Consumer<List<AuctionDTO>> onSuccess, Consumer<String> onError) {
        if (AppConfig.isUseMock()) {
            if (onSuccess != null) onSuccess.accept(MockDataProvider.getMyAuctions(sellerId));
            return;
        }
        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("GET_MY_AUCTIONS:" + sellerId);
                List<AuctionDTO> auctions = ResponseHandler.parseAuctionListFromText(response);
                if (onSuccess != null) Platform.runLater(() -> onSuccess.accept(auctions));
            } catch (IOException e) {
                if (AppConfig.isAutoFallback()) {
                    if (onSuccess != null) Platform.runLater(() -> onSuccess.accept(MockDataProvider.getMyAuctions(sellerId)));
                } else if (onError != null) {
                    Platform.runLater(() -> onError.accept(e.getMessage()));
                }
            }
        }).start();
    }

    // ==================== DELETE AUCTION ====================
    public void deleteAuction(String auctionId, Consumer<Boolean> onSuccess, Consumer<String> onError) {
        if (auctionId == null || auctionId.isBlank()) {
            if (onError != null) {
                onError.accept("Invalid auction id");
            }
            return;
        }
        if (AppConfig.isUseMock()) {
            if (onSuccess != null) {
                onSuccess.accept(true);
            }
            return;
        }
        new Thread(() -> {
            try {
                String response = ServerConnection.getInstance().sendRequest("DELETE_AUCTION:" + auctionId);
                boolean ok = response != null && response.startsWith("DELETE_OK");
                if (ok) {
                    if (onSuccess != null) Platform.runLater(() -> onSuccess.accept(true));
                } else if (onError != null) {
                    String msg = response != null ? response : "Unknown error";
                    Platform.runLater(() -> onError.accept(msg));
                }
            } catch (IOException e) {
                if (onError != null) {
                    Platform.runLater(() -> onError.accept(e.getMessage()));
                }
            }
        }, "delete-auction").start();
    }
}