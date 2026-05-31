package com.auction.server.controller;

import com.auction.common.dto.*;
import com.auction.common.exception.InvalidBidException;
import com.auction.common.observer.AuctionSubject;
import com.auction.common.observer.ClientObserver;
import com.auction.server.observer.ServerClientObserver;
import com.auction.server.config.ServerConfig;
import com.auction.server.dao.UserDAO;
import com.auction.common.entity.User;
import com.auction.common.exception.AuctionNotFoundException;
import com.auction.common.enums.AuctionStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuctionController auctionController;
    private final UserController userController;
    private final BidController bidController;
    private final AuctionSubject auctionSubject;
    private BufferedReader in;
    private PrintWriter out;
    private ClientObserver clientObserver;

    private static final Map<String, Long> lastBidTimes = new ConcurrentHashMap<>();
    private final long defaultAuctionDurationHours;
    private final long minBidIntervalSeconds;

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

        ServerConfig config = ServerConfig.getInstance();
        this.defaultAuctionDurationHours = config.getDefaultAuctionDurationHours();
        this.minBidIntervalSeconds = config.getMinBidIntervalSeconds();
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            clientObserver = new ServerClientObserver(socket);
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
                case "GET_ACTIVE_AUCTIONS" -> handleGetActiveAuctions();
                case "GET_AUCTION" -> handleGetAuction(payload);
                case "GET_MY_AUCTIONS" -> handleGetMyAuctions(payload);
                case "CREATE_AUCTION" -> handleCreateAuction(payload);
                case "PLACE_BID" -> handlePlaceBid(payload);
                case "CANCEL_BID" -> handleCancelBid(payload);
                case "GET_BID_HISTORY" -> handleGetBidHistory(payload);
                case "CONFIGURE_AUTO_BID" -> handleConfigureAutoBid(payload);
                case "CANCEL_AUTO_BID" -> handleCancelAutoBid(payload);
                case "UPDATE_PROFILE" -> handleUpdateProfile(payload);
                case "ADD_BALANCE" -> handleAddBalance(payload);
                case "CHANGE_PASSWORD" -> handleChangePassword(payload);
                case "GET_PENDING_AUCTIONS" -> handleGetPendingAuctions();
                case "APPROVE_AUCTION" -> handleApproveAuction(payload);
                case "REJECT_AUCTION" -> handleRejectAuction(payload);
                case "GET_USER_COUNT" -> handleGetUserCount();
                case "GET_BALANCE" -> handleGetBalance(payload);
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
            out.println("LOGIN_OK:" + resp.getSessionToken() + ":" + resp.getUserId() + ":" + p[0] + ":" + resp.getRole() + ":" + resp.getBalance());
        } else {
            out.println("LOGIN_FAIL:" + (resp != null ? resp.getMessage() : "Invalid credentials"));
        }
    }

    private void handleRegister(String payload) {
        String[] p = payload.split(":");
        UserDTO dto = new UserDTO(null, p[0], p[1], p[2], p[3], p[4], 0.0);
        dto.setActive(true);
        userController.register(dto);
        out.println("REGISTER_OK");
    }

    private void handleAddBalance(String payload) {
        String[] p = payload.split(":");
        String userId = p[0];
        double amount = Double.parseDouble(p[1]);
        userController.addBalance(userId, amount);
        out.println("BALANCE_OK");
    }

    private void handleUpdateProfile(String payload) {
        String[] p = payload.split(":");
        UserDTO dto = new UserDTO();
        dto.setFullName(p[1]);
        dto.setEmail(p[2]);
        userController.updateProfile(p[0], dto);
        out.println("UPDATE_OK");
    }

    private void handleChangePassword(String payload) {
        String[] p = payload.split(":");
        userController.changePassword(p[0], p[1], p[2]);
        out.println("CHANGE_OK");
    }

    private void handleGetUserCount() {
        int count = com.auction.server.dao.UserDAO.getInstance().getAllUsers().size();
        out.println("USER_COUNT:" + count);
    }

    private void handleGetBalance(String userId) {
        UserDTO dto = userController.getUserProfile(userId);
        if (dto != null) {
            out.println("BALANCE:" + dto.getBalance());
        } else {
            out.println("ERROR:User not found");
        }
    }

    private void handleGetPendingAuctions() {
        java.util.List<com.auction.common.dto.AuctionDTO> all = auctionController.getAllAuctions();
        java.util.List<com.auction.common.dto.AuctionDTO> pending = new java.util.ArrayList<>();
        for (com.auction.common.dto.AuctionDTO a : all) {
            if (a.getStatus() == AuctionStatus.PENDING) {
                pending.add(a);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("PENDING_COUNT:").append(pending.size());
        for (com.auction.common.dto.AuctionDTO a : pending) {
            sb.append("||PENDING:").append(a.getId()).append(":")
                    .append(a.getItemName() != null ? a.getItemName() : "").append(":")
                    .append(a.getCurrentPrice()).append(":")
                    .append(a.getSellerId() != null ? a.getSellerId() : "").append(":")
                    .append(a.getStartTime() != null ? a.getStartTime().toString() : "").append(":")
                    .append(a.getEndTime() != null ? a.getEndTime().toString() : "");
        }
        out.println(sb.toString());
    }

    private void handleApproveAuction(String payload) {
        String[] p = payload.split(":");
        String auctionId = p[0];
        auctionController.updateAuctionStatus(auctionId, AuctionStatus.RUNNING);
        out.println("APPROVE_OK");
    }

    private void handleRejectAuction(String payload) {
        String[] p = payload.split(":");
        String auctionId = p[0];
        auctionController.updateAuctionStatus(auctionId, AuctionStatus.CANCELLED);
        out.println("REJECT_OK");
    }

    private void handleGetAllAuctions() {
        java.util.List<com.auction.common.dto.AuctionDTO> auctions = auctionController.getAllAuctions();
        StringBuilder sb = new StringBuilder();
        sb.append("AUCTIONS_COUNT:").append(auctions.size());

        for (com.auction.common.dto.AuctionDTO a : auctions) {
            sb.append("||AUCTION:").append(a.getId()).append(":").append(a.getItemName())
                    .append(":").append(a.getCurrentPrice()).append(":").append(a.getStatus().name())
                    .append(":").append(a.getCategory())
                    .append(":").append(a.getRemainingTimeMillis())
                    .append(":").append(a.getStartingPrice())
                    .append(":").append(a.getTotalBids())
                    .append(":").append(imageRef(a));
        }
        out.println(sb.toString());
    }

    private void handleGetActiveAuctions() {
        java.util.List<com.auction.common.dto.AuctionDTO> auctions = auctionController.getActiveAuctions();
        StringBuilder sb = new StringBuilder();
        sb.append("AUCTIONS_COUNT:").append(auctions.size());

        for (com.auction.common.dto.AuctionDTO a : auctions) {
            sb.append("||AUCTION:").append(a.getId()).append(":").append(a.getItemName())
                    .append(":").append(a.getCurrentPrice()).append(":").append(a.getStatus().name())
                    .append(":").append(a.getCategory())
                    .append(":").append(a.getRemainingTimeMillis())
                    .append(":").append(a.getStartingPrice())
                    .append(":").append(a.getTotalBids())
                    .append(":").append(imageRef(a));
        }
        out.println(sb.toString());
    }

    private void handleGetAuction(String id) {
        try {
            AuctionDTO a = auctionController.getAuction(id);
            if (a != null) {
                String winnerName = resolveUsername(a.getCurrentWinnerId());
                out.println("AUCTION:" + a.getId() + ":" + a.getItemName()
                        + ":" + a.getCurrentPrice() + ":" + a.getStatus().name()
                        + ":" + (a.getCurrentWinnerId() != null ? a.getCurrentWinnerId() : "")
                        + ":" + a.getTotalBids()
                        + ":" + a.getRemainingTimeMillis()
                        + ":" + a.getStartingPrice()
                        + ":" + a.getMinIncrement()
                        + ":" + imageRef(a)
                        + ":" + winnerName);
            } else {
                out.println("ERROR:Auction not found");
            }
        } catch (AuctionNotFoundException e) {
            out.println("ERROR:" + e.getMessage());
        }
    }

    private static String resolveUsername(String userId) {
        if (userId == null || userId.isBlank()) {
            return "";
        }
        User user = UserDAO.getInstance().findUserById(userId);
        return user != null ? user.getUsername() : userId;
    }

    private static String imageRef(AuctionDTO a) {
        if (a.getImagePath() == null) {
            return "";
        }
        return a.getImagePath();
    }

    private void handleGetMyAuctions(String sellerId) {
        java.util.List<AuctionDTO> auctions = auctionController.getAuctionsBySeller(sellerId);
        StringBuilder sb = new StringBuilder();
        sb.append("AUCTIONS_COUNT:").append(auctions.size());
        for (AuctionDTO a : auctions) {
            sb.append("||AUCTION:").append(a.getId()).append(":")
                    .append(a.getItemName() != null ? a.getItemName() : "").append(":")
                    .append(a.getCurrentPrice()).append(":")
                    .append(a.getStatus()).append(":")
                    .append(a.getCategory() != null ? a.getCategory() : "").append(":")
                    .append(a.getEndTime() != null ? a.getEndTime().toString() : "");
        }
        out.println(sb.toString());
    }

    private void handleCreateAuction(String payload) {
        try {
            String[] p = payload.split("\\|", -1);
            AuctionDTO dto = new AuctionDTO();
            String itemName = p.length > 0 ? p[0].replace("\\n", "\n") : "";
            String itemDescription = p.length > 1 ? p[1].replace("\\n", "\n") : "";
            double startingPrice = p.length > 2 ? Double.parseDouble(p[2]) : 0;
            String sellerUsername = p.length > 3 ? p[3] : "";
            LocalDateTime startTime = p.length > 4 && !p[4].isEmpty() ? LocalDateTime.parse(p[4]) : LocalDateTime.now();
            LocalDateTime endTime = p.length > 5 && !p[5].isEmpty() ? LocalDateTime.parse(p[5]) : startTime.plusHours(defaultAuctionDurationHours);
            double minIncrement = p.length > 6 ? Double.parseDouble(p[6]) : 1.0;
            String category = p.length > 7 ? p[7] : "";
            String imagePath = p.length > 8 ? p[8] : "";

            dto.setItemName(itemName);
            dto.setItemDescription(itemDescription);
            dto.setStartingPrice(startingPrice);
            dto.setCurrentPrice(startingPrice);
            dto.setSellerId(sellerUsername);
            dto.setStartTime(startTime);
            dto.setEndTime(endTime);
            dto.setMinIncrement(minIncrement);
            dto.setCategory(category);
            dto.setImagePath(imagePath);
            dto.setStatus(AuctionStatus.PENDING);

            auctionController.createAuction(dto);
            out.println("CREATE_AUCTION_OK");
        } catch (Exception e) {
            out.println("ERROR:Create auction failed: " + e.getMessage());
        }
    }

    private void handlePlaceBid(String payload) throws InvalidBidException {
        System.out.println("[SERVER DEBUG] PLACE_BID Payload received: " + payload);

        String[] p = payload.split(":");
        if (p.length < 3) {
            out.println("ERROR:Invalid payload format for PLACE_BID");
            return;
        }

        String auctionId = p[0];
        String bidderId = p[1];
        double amount = 0;

        try {
            amount = Double.parseDouble(p[2]);
        } catch (NumberFormatException e) {
            out.println("ERROR:Server failed to parse bid amount");
            return;
        }

        boolean isAutoBid = p.length > 3 && Boolean.parseBoolean(p[3]);

        String bidKey = bidderId + ":" + auctionId;
        long now = System.currentTimeMillis();
        Long lastBidTime = lastBidTimes.get(bidKey);

        if (lastBidTime != null) {
            long elapsed = now - lastBidTime;
            if (elapsed < minBidIntervalSeconds * 1000) {
                out.println("ERROR:Bidding too fast. Please wait " +
                        (minBidIntervalSeconds - elapsed/1000) + " more seconds.");
                return;
            }
        }

        lastBidTimes.put(bidKey, now);

        BidRequest req = new BidRequest(auctionId, bidderId, amount, isAutoBid);
        bidController.placeBid(req);

        // TRẢ VỀ PHẢN HỒI CHUẨN ĐỒNG BỘ ĐỂ CLIENT NHẬN DIỆN LỆNH THÀNH CÔNG
        out.println("BID_OK");
    }

    private void handleCancelBid(String payload) {
        String[] p = payload.split(":");
        String auctionId = p[0];
        String bidderId = p[1];
        try {
            bidController.cancelBid(auctionId, bidderId);
            out.println("CANCEL_BID_OK");
        } catch (Exception e) {
            out.println("ERROR:" + e.getMessage());
        }
    }

    private void handleGetBidHistory(String auctionId) {
        java.util.List<com.auction.common.entity.BidTransaction> history = bidController.getBidHistory(auctionId);
        StringBuilder sb = new StringBuilder();
        sb.append("BID_HISTORY_COUNT:").append(history.size());

        for (com.auction.common.entity.BidTransaction b : history) {
            long bidTimeMillis = b.getBidTime() != null
                    ? b.getBidTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : 0L;
            String bidderName = resolveUsername(b.getBidderId());
            sb.append("||BID:").append(auctionId).append(":").append(b.getBidderId()).append(":")
                    .append(bidderName).append(":").append(b.getAmount())
                    .append(":").append(bidTimeMillis).append(":").append(b.isAutoBid());
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