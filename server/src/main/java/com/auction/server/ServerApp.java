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
import com.auction.common.entity.Bidder;
import com.auction.server.dao.UserDAO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    private static final int PORT = 5050;
    private static ServerSocket serverSocket;
    private static ExecutorService clientThreadPool;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        System.out.println("Starting Online Auction Server...");
        try {
            DatabaseConnection.getInstance(); // Khởi tạo storage
            org.h2.tools.Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8089").start();
            System.out.println("Database connection established.");

            UserDAO userDAO = UserDAO.getInstance();
            AuctionDAO.getInstance();

            // Seed default users for demo/testing if store is empty
            if (userDAO.findUserByUsername("demo") == null) {
                System.out.println("Seeding demo users: demo/demo, alice/alice");
                userDAO.saveUser(new Bidder("demo", "demo", "demo@example.com", "Demo User", 1000.0));
                userDAO.saveUser(new Bidder("alice", "alice", "alice@example.com", "Alice", 500.0));
            }

            // Services
            AuctionService auctionService = new AuctionService();
            UserService userService = new UserService();
            BiddingService biddingService = new BiddingService();
            AutoBidService autoBidService = new AutoBidService();
            NotificationService notificationService = NotificationService.getInstance();
            AntiSnipingService antiSnipingService = new AntiSnipingService();
            ConcurrentBidManager concurrentBidManager = new ConcurrentBidManager();

            // Controllers
            AuctionController auctionController = new AuctionController(auctionService);
            UserController userController = new UserController(userService);
            BidController bidController = new BidController(biddingService);

            // AuctionSubject cho observer
            AuctionSubject auctionSubject = new AuctionSubject();

            // Listeners (có thể gắn sau)
            AuctionEventListener auctionListener = new AuctionEventListenerImpl();
            BidEventListener bidListener = new BidEventListenerImpl();

            // Scheduler
            AuctionScheduler scheduler = AuctionScheduler.getInstance();
            AutoBidProcessor autoBidProcessor = AutoBidProcessor.getInstance();
            autoBidProcessor.start();

            clientThreadPool = Executors.newCachedThreadPool();
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            System.out.println("Waiting for client connections...");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket,
                        auctionController, userController, bidController, auctionSubject);
                clientThreadPool.submit(handler);
            }
        } catch (IOException | SQLException e) {
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