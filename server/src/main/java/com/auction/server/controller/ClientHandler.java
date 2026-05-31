package com.auction.server.controller;

import com.auction.common.dto.*;
import com.auction.common.exception.InvalidBidException;
import com.auction.common.observer.AuctionSubject;
import com.auction.common.observer.ClientObserver;
import com.auction.server.controller.AuctionController;
import com.auction.server.controller.BidController;
import com.auction.server.controller.UserController;
import com.auction.common.exception.AuctionNotFoundException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuctionController auctionController;
    private final UserController userController;
    private final BidController bidController;
    private final AuctionSubject auctionSubject;
    private BufferedReader in;
    private PrintWriter out;
    private ClientObserver clientObserver;

    public ClientHandler(Socket socket,
                         AuctionController auctionController,
                         UserController userController,
                         BidController bidController,
                         AuctionSubject auctionSubject) {
        this.socket = socket;
        this.auctionController = auctionController;
        this.userController = userController;
        this.bidController = bidController;
        this.auctionSubject = auctionSubject;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            clientObserver = new ClientObserver(socket);
            System.out.println("Client connected: " + socket.getRemoteSocketAddress());

            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message.trim());
            }
        } catch (Exception e) {
            System.err.println("ClientHandler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handleMessage(String message) {
        try {
            String[] parts = message.split(":", 2);
            String command = parts[0].toUpperCase();
            String payload = parts.length > 1 ? parts[1] : "";
            switch (command) {
                case "LOGIN" -> handleLogin(payload);
                case "REGISTER" -> handleRegister(payload);
                case "GET_AUCTIONS" -> handleGetAllAuctions();
                case "GET_AUCTION" -> handleGetAuction(payload);
                case "CREATE_AUCTION" -> handleCreateAuction(payload);
                case "PLACE_BID" -> handlePlaceBid(payload);
                case "GET_BID_HISTORY" -> handleGetBidHistory(payload);
                case "CONFIGURE_AUTO_BID" -> handleConfigureAutoBid(payload);
                case "CANCEL_AUTO_BID" -> handleCancelAutoBid(payload);
                case "SUBSCRIBE" -> handleSubscribe();
                case "UNSUBSCRIBE" -> handleUnsubscribe();
                default -> out.println("ERROR:Unknown command");
            }
        } catch (Exception e) {
            out.println("ERROR:" + e.getMessage());
        }
    }

    private void handleLogin(String payload) {
        String[] p = payload.split(":");
        LoginRequest req = new LoginRequest(p[0], p[1], p.length > 2 ? p[2] : "BIDDER");
        LoginResponse resp = userController.login(req);

        if (resp != null && resp.isSuccess()) {
            // SỬA DÒNG NÀY: Chèn thêm p[0] (tên username người dùng nhập) vào chuỗi gửi về Client
            out.println("LOGIN_OK:" + resp.getSessionToken() + ":" + resp.getUserId() + ":" + p[0] + ":" + resp.getRole() + ":" + resp.getBalance());
        } else {
            out.println("LOGIN_FAIL:" + (resp != null ? resp.getMessage() : "Invalid credentials"));
        }
    }

    private void handleRegister(String payload) {
        String[] p = payload.split(":");
        // username, password, email, fullName, role
        UserDTO dto = new UserDTO(null, p[0], p[1], p[2], p[3], p[4], 0.0);
        dto.setActive(true);
        userController.register(dto);
        out.println("REGISTER_OK");
    }

    private void handleGetAllAuctions() {
        // Đã đổi var thành List<AuctionDTO> chuẩn Java 8
        java.util.List<com.auction.common.dto.AuctionDTO> auctions = auctionController.getAllAuctions();
        StringBuilder sb = new StringBuilder();
        sb.append("AUCTIONS_COUNT:").append(auctions.size());

        for (com.auction.common.dto.AuctionDTO a : auctions) {
            sb.append("||AUCTION:").append(a.getId()).append(":").append(a.getItemName())
                    .append(":").append(a.getCurrentPrice()).append(":").append(a.getStatus().name())
                    .append(":").append(a.getRemainingTimeMillis());
        }
        out.println(sb.toString());
    }

    private void handleGetAuction(String id) {
        try {
            AuctionDTO a = auctionController.getAuction(id);
            if (a != null) {
                out.println("AUCTION:" + a.getId() + ":" + a.getItemName()
                        + ":" + a.getCurrentPrice() + ":" + a.getStatus().name()
                        + ":" + a.getCurrentWinnerId() + ":" + a.getTotalBids()
                        + ":" + a.getRemainingTimeMillis());
            } else {
                out.println("ERROR:Auction not found");
            }
        } catch (AuctionNotFoundException e) {
            out.println("ERROR:" + e.getMessage());
        }
    }

    private void handleCreateAuction(String payload) {
        String[] p = payload.split(":");
        AuctionDTO dto = new AuctionDTO();
        dto.setItemId(p[0]);
        dto.setSellerId(p[1]);
        dto.setCurrentPrice(Double.parseDouble(p[2]));
        auctionController.createAuction(dto);
        out.println("CREATE_AUCTION_OK");
    }

    private void handlePlaceBid(String payload) throws InvalidBidException {
        String[] p = payload.split(":");
        boolean isAutoBid = p.length > 3 && Boolean.parseBoolean(p[3]);
        BidRequest req = new BidRequest(p[0], p[1], Double.parseDouble(p[2]), isAutoBid);
        bidController.placeBid(req);
        out.println("BID_OK");
    }

    private void handleGetBidHistory(String auctionId) {
        // Đã đổi var thành List<BidTransaction> chuẩn Java 8
        java.util.List<com.auction.common.entity.BidTransaction> history = bidController.getBidHistory(auctionId);
        StringBuilder sb = new StringBuilder();
        sb.append("BID_HISTORY_COUNT:").append(history.size());

        for (com.auction.common.entity.BidTransaction b : history) {
            // Mẹo: Nếu b.getTime() báo đỏ, bạn hãy đổi chữ ".getTime()" thành ".getBidTime()" theo entity của bạn nhé
            sb.append("||BID:").append(b.getBidderId()).append(":").append(b.getAmount())
                    .append(":").append(b.getBidTime()).append(":").append(b.isAutoBid());
        }
        out.println(sb.toString());
    }

    private void handleConfigureAutoBid(String payload) {
        String[] p = payload.split(":");
        AutoBidRequest req = new AutoBidRequest(p[0], p[1],
                Double.parseDouble(p[2]), Double.parseDouble(p[3]), Boolean.parseBoolean(p[4]));
        bidController.configureAutoBid(req);
        out.println("AUTO_BID_OK");
    }

    private void handleCancelAutoBid(String payload) {
        String[] p = payload.split(":");
        bidController.cancelAutoBid(p[0], p[1]);
        out.println("CANCEL_AUTO_BID_OK");
    }

    private void handleSubscribe() {
        auctionSubject.registerObserver(clientObserver);
        out.println("SUBSCRIBED");
    }

    private void handleUnsubscribe() {
        auctionSubject.removeObserver(clientObserver);
        out.println("UNSUBSCRIBED");
    }

    private void cleanup() {
        auctionSubject.removeObserver(clientObserver);
        try {
            if (!socket.isClosed()) socket.close();
        } catch (Exception e) {
            System.err.println("Socket close error: " + e.getMessage());
        }
    }
}