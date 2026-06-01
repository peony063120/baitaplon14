package com.auction.server;

import com.auction.common.observer.AuctionSubject;
import com.auction.server.config.ServerConfig;
import com.auction.server.controller.AuctionController;
import com.auction.server.controller.BidController;
import com.auction.server.controller.UserController;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.DatabaseConnection;
import com.auction.server.dao.UserDAO;
import com.auction.server.controller.ClientHandler;
import com.auction.server.listener.AuctionEventListener;
import com.auction.server.listener.AuctionEventListenerImpl;
import com.auction.server.listener.BidEventListener;
import com.auction.server.listener.BidEventListenerImpl;
import com.auction.server.scheduler.AuctionScheduler;
import com.auction.server.scheduler.AutoBidProcessor;
import com.auction.server.service.*;
import com.auction.server.dao.SqlDataLoader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    private static ServerSocket serverSocket;
    private static ExecutorService clientThreadPool;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        System.out.println("Starting Online Auction Server...");
        try {
            ServerConfig config = ServerConfig.getInstance();
            int port = config.getPort();
            DatabaseConnection.getInstance(); // Khởi tạo storage
            try {
                org.h2.tools.Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8089").start();
                System.out.println("H2 Console: http://localhost:8089");
            } catch (Exception e) {
                System.out.println("H2 Console unavailable (port 8089 may be in use): " + e.getMessage());
            }
            System.out.println("Database connection established.");

            UserDAO.getInstance();
            AuctionDAO.getInstance();

            // Load seed data từ file .sql nếu được cấu hình
            // Bật:  -Dserver.seed=true  (mặc định)
            // Tắt: -Dserver.seed=false (không load seed, server rỗng)
            boolean doSeed = Boolean.parseBoolean(System.getProperty("server.seed", "true"));
            if (doSeed) {
                SqlDataLoader.loadSeedData();
            }

            // Services
            AuctionService auctionService = new AuctionService();
            UserService userService = new UserService();
            
            // Create AutoBidService first to avoid circular dependency
            AutoBidService autoBidService = new AutoBidService();
            
            // Create BiddingService with AutoBidService
            BiddingService biddingService = new BiddingService();
            biddingService.setAutoBidService(autoBidService);
            
            // Set BiddingService back to AutoBidService
            autoBidService.setBiddingService(biddingService);
            
            NotificationService notificationService = NotificationService.getInstance();
            AntiSnipingService antiSnipingService = new AntiSnipingService();
            ConcurrentBidManager concurrentBidManager = new ConcurrentBidManager();

            // Controllers
            AuctionController auctionController = new AuctionController(auctionService);
            UserController userController = new UserController(userService);
            BidController bidController = new BidController(biddingService);

            // AuctionSubject cho observer (singleton)
            AuctionSubject auctionSubject = AuctionSubject.getInstance();

            // Inject AuctionSubject vào AuctionService
            auctionService.setAuctionSubject(auctionSubject);

            // Listeners (có thể gắn sau)
            AuctionEventListener auctionListener = new AuctionEventListenerImpl();
            BidEventListener bidListener = new BidEventListenerImpl();

            // Scheduler
            AuctionScheduler auctionScheduler = AuctionScheduler.getInstance();
            AutoBidProcessor autoBidProcessor = AutoBidProcessor.getInstance();
            // Inject BiddingService into AutoBidService via AutoBidProcessor
            autoBidProcessor.setBiddingService(biddingService);
            // ALSO inject the same AutoBidService that BiddingService uses
            autoBidProcessor.setAutoBidService(autoBidService);
            autoBidProcessor.start();

            clientThreadPool = Executors.newCachedThreadPool();
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on " + config.getHost() + ":" + port);
            System.out.println("Waiting for client connections...");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket,
                        auctionController, userController, bidController, auctionSubject);
                clientThreadPool.submit(handler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    public static void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            if (clientThreadPool != null) clientThreadPool.shutdownNow();
            AuctionScheduler.getInstance().shutdown();
            AutoBidProcessor.getInstance().shutdown();
            DatabaseConnection.getInstance().closeConnection();
            System.out.println("Server shutdown complete.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}