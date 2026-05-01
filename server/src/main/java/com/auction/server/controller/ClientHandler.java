package com.auction.server.controller;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.AutoBidRequest;
import com.auction.common.dto.BidRequest;
import com.auction.common.dto.LoginRequest;
import com.auction.common.dto.LoginResponse;
import com.auction.common.dto.UserDTO;
import com.auction.common.observer.AuctionSubject;
import com.auction.common.observer.ClientObserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ClientHandler - implements Runnable, chạy trên thread riêng cho mỗi client.
 * Nhận message từ client qua Socket, parse lệnh và gọi đúng Controller
 *
 * Attributes (theo diagram):
 *  - socket: Socket
 *  - auctionController: AuctionController
 *
 * Methods (theo diagram):
 *  + run(): void
 *
 * Protocol (text-based, mỗi dòng là 1 lệnh):
 *  LOGIN:<username>:<password>:<role>
 *  REGISTER:<username>:<password>:<email>:<fullName>:<role>
 *  GET_AUCTIONS
 *  GET_AUCTION:<id>
 *  CREATE_AUCTION:<itemId>:<sellerId>:<startingPrice>
 *  PLACE_BID:<auctionId>:<bidderId>:<amount>:<isAutoBid>
 *  GET_BID_HISTORY:<auctionId>
 *  CONFIGURE_AUTO_BID:<userId>:<auctionId>:<maxBid>:<increment>:<enable>
 *  CANCEL_AUTO_BID:<auctionId>:<userId>
 *  SUBSCRIBE  <= đăng kí nhận realtime update (Observer)
 *  UNSUBSCRIBE
 */
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
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Tạo ClientObserver cho client này (Observer Pattern)
            clientObserver = new ClientObserver(socket);

            System.out.println("Client kết nối: " + socket.getRemoteSocketAddress());

            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message.trim());
            }

        } catch (Exception e) {
            System.err.println("ClientHandler lỗi: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Phân tích và điều hướng message từ client đến đúng Controller.
     */
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
                default -> out.println("ERROR:Unknown command: " + command);
            }
        } catch (Exception e) {
            out.println("ERROR:" + e.getMessage());
        }
    }

    // LOGIN:<username>:<password>:<role>
    private void handleLogin(String payload) {
        String[] p = payload.split(":");
        LoginRequest req = new LoginRequest(p[0], p[1], p.length > 2 ? p[2] : "BIDDER");
        LoginResponse resp = userController.login(req);
        if (resp != null && resp.isSuccess()) {
            out.println("LOGIN_OK:" + resp.getSessionToken()
            + ":" + resp.getUserId()
            + ":" + resp.getRole()
            + ":" + resp.getBalance());
        } else {
            out.println("LOGIN_FAIL:" + (resp != null ? resp.getMessage() : "Sai tên đăng nhập hoặc mật khẩu"));
        }
    }

    // REGISTER:<username>:<password>:<email>:<fullName>:<role>
    private void handleRegister(String payload) {
        String[] p = payload.split(":");
        // UserDTO constructor: (id, username, email, fullName, role, balance)
        // id để null - UserService sẽ tạo từ Entity
        UserDTO dto = new UserDTO(null, p[0], p[2], p[3], p[4], 0);
        dto.setActive(true);
        // password set riêng vì UserDTO ko có trong constructor
        userController.register(dto);
        out.println("REGISTER_OK");
    }

    private void handleGetAllAuctions() {
        var auctions = auctionController.getAllAuctions();
        out.println("AUCTIONS_COUNT:" + auctions.size());
        for (AuctionDTO a : auctions) {
            // AuctionDTO.getStatus() trả về AuctionStatus (enum), ko phải String
            out.println("AUCTION:" + a.getId()
                    + ":" + a.getItemName()
                    + ":" + a.getCurrentPrice()
                    + ":" + a.getStatus().name()
                    + ":" + a.getRemainingTimeMillis());
        }
    }

    private void handleGetAuction(String id) {
        AuctionDTO a = auctionController.getAuction(id);
        if (a != null) {
            // dùng getCurrentWinnerId() theo DTO thực tế
            out.println("AUCTION:" + a.getId()
                    + ":" + a.getItemName()
                    + ":" + a.getCurrentPrice()
                    + ":" + a.getStatus().name()
                    + ":" + a.getCurrentWinnerId()
                    + ":" + a.getTotalBids()
                    + ":" + a.getRemainingTimeMillis());
        } else {
            out.println("ERROR:Auction not found");
        }
    }

    // CREATE_AUCTION:<itemId>:<sellerId>:<startingPrice>
    private void handleCreateAuction(String payload) {
        String[] p = payload.split(":");
        AuctionDTO dto = new AuctionDTO();
        dto.setItemId(p[0]);
        dto.setSellerId(p[1]);
        dto.setCurrentPrice(Double.parseDouble(p[2]));
        auctionController.createAuction(dto);
        out.println("CREATE_AUCTION_OK");
    }

    // PLACE_BID:<auctionId>:<bidderId>:<amount>:<isAutoBid>
    private void handlePlaceBid(String payload) {
        String[] p = payload.split(":");
        boolean isAutoBid = p.length > 3 && Boolean.parseBoolean(p[3]);
        // BidRequest constructor: (auctionId, bidderId, amount, isAutoBid)
        BidRequest req = new BidRequest(p[0], p[1], Double.parseDouble(p[2]), isAutoBid);
        bidController.placeBid(req);
        out.println("BID_OK");
    }

    private void handleGetBidHistory(String auctionId) {
        var history = bidController.getBidHistory(auctionId);
        out.println("BID_HISTORY_COUNT:" + history.size());
        history.forEach(b -> out.println(
                        "BID:" + b.getBidderId()
                        + ":" + b.getAmount()
                        + ":" + b.getBidTime()
                        + ":" + b.isAutoBid()));
    }

    // CONFIGURE_AUTO_BID:<userId>:<auctionId>:<maxBid>:<increment>:<enable>
    private void handleConfigureAutoBid(String payload) {
        String[] p = payload.split(":");
        // AutoBidRequest constructor: (userId, auctionId, maxBid, increment, enable)
        AutoBidRequest req = new AutoBidRequest(p[0], p[1], Double.parseDouble(p[2]), Double.parseDouble(p[3]), Boolean.parseBoolean(p[4]));
        bidController.configureAutoBid(req);
        out.println("AUTO_BID_OK");
    }

    // CANCEL_AUTO_BID:<auctionId>:<userId>
    private void handleCancelAutoBid(String payload) {
        String[] p = payload.split(":");
        bidController.cancelAutoBid(p[0], p[1]);
        out.println("CANCEL_AUTO_BID_OK");
    }

    private void handleSubscribe() {
        // Đăng ký client vào AuctionSubject để nhận realtime update
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
            System.err.println("Lỗi đóng socket: " + e.getMessage());
        }
        System.out.println("Client ngắt kết nối: " + socket.getRemoteSocketAddress());
    }
}