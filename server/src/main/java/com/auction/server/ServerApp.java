package com.auction.server;

import com.auction.server.config.ServerConfig;
import com.auction.server.controller.AuctionController;
import com.auction.server.controller.BidController;
import com.auction.server.controller.UserController;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.DatabaseConnection;
import com.auction.server.dao.UserDAO;
import com.auction.server.handler.ClientHandler;
import com.auction.server.listener.AuctionEventListener;
import com.auction.server.listener.AuctionEventListenerImpl;
import com.auction.server.listener.BidEventListener;
import com.auction.server.listener.BidEventListenerImpl;
import com.auction.server.model.AuctionManager;
import com.auction.server.scheduler.AuctionScheduler;
import com.auction.server.scheduler.AutoBidProcessor;
import com.auction.server.service.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ServerApp
 * Điểm khởi chạy chính của server.
 * Khởi tạo các thành phần: config, database, DAO, service, controller,
 * scheduler, processor, listener, và bắt đầu lắng nghe kết nối client.
 */
public class ServerApp {

    private static final int PORT = ServerConfig.getInstance().getPort();
    private static ServerSocket serverSocket;
    private static ExecutorService clientThreadPool;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        System.out.println("Starting Online Auction Server...");

        try {
            // 1. Khởi tạo kết nối database (Singleton)
            DatabaseConnection.getInstance().getConnection();
            System.out.println("Database connection established.");

            // 2. Khởi tạo DAO (Singleton - tự động qua getInstance)
            UserDAO.getInstance();
            AuctionDAO.getInstance();

            // 3. Khởi tạo services
            AuctionService auctionService = new AuctionService();
            UserService userService = new UserService();
            BiddingService biddingService = new BiddingService();
            AutoBidService autoBidService = new AutoBidService();
            NotificationService notificationService = new NotificationService();
            AntiSnipingService antiSnipingService = new AntiSnipingService();
            ConcurrentBidManager concurrentBidManager = new ConcurrentBidManager();

            // 4. Khởi tạo controllers
            AuctionController auctionController = new AuctionController(auctionService);
            UserController userController = new UserController(userService);
            BidController bidController = new BidController(biddingService);

            // 5. Đăng ký listeners (nếu cần)
            AuctionEventListener auctionListener = new AuctionEventListenerImpl();
            BidEventListener bidListener = new BidEventListenerImpl();
            // Gắn listener vào service nếu có phương thức đăng ký
            // Ví dụ: biddingService.addBidListener(bidListener);
            // Hoặc để sau khi có instance cụ thể

            // 6. Khởi động scheduler và processor
            AuctionScheduler scheduler = AuctionScheduler.getInstance();
            AutoBidProcessor autoBidProcessor = AutoBidProcessor.getInstance();
            autoBidProcessor.start();   // chạy định kỳ

            // 7. Khởi tạo thread pool cho client handlers
            clientThreadPool = Executors.newCachedThreadPool();

            // 8. Mở server socket
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            System.out.println("Waiting for client connections...");

            // 9. Vòng lặp chấp nhận client
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Tạo ClientHandler với các controllers
                ClientHandler handler = new ClientHandler(clientSocket,
                        auctionController, userController, bidController);
                clientThreadPool.submit(handler);
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    /**
     * Dừng server, giải phóng tài nguyên.
     */
    public static void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (clientThreadPool != null) {
                clientThreadPool.shutdownNow();
            }
            AuctionScheduler.getInstance().shutdown();
            AutoBidProcessor.getInstance().shutdown();
            DatabaseConnection.getInstance().closeConnection();
            System.out.println("Server shutdown complete.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}