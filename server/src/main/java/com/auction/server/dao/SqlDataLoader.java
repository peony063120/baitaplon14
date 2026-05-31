package com.auction.server.dao;

import com.auction.common.entity.*;
import com.auction.common.enums.AuctionStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SqlDataLoader {

    private static final String SEED_FILE = "/seed.sql";

    public static void loadSeedData() {
        System.out.println("[SqlDataLoader] Loading seed data from " + SEED_FILE + " ...");
        try {
            String sql = readSqlFile(SEED_FILE);
            if (sql == null || sql.isBlank()) {
                System.out.println("[SqlDataLoader] No seed file found, skipping.");
                return;
            }

            Connection conn = DatabaseConnection.getInstance().getConnection();
            executeSql(conn, sql);

            loadUsersFromDb(conn);
            loadAuctionsFromDb(conn);
            loadBidTransactionsFromDb(conn);

            System.out.println("[SqlDataLoader] Seed data loaded successfully.");
        } catch (Exception e) {
            System.err.println("[SqlDataLoader] Failed to load seed data: " + e.getMessage());
        }
    }

    private static String readSqlFile(String path) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(SqlDataLoader.class.getResourceAsStream(path)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("--") && !line.trim().startsWith("//")) {
                    sb.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            return null;
        }
        return sb.toString();
    }

    private static void executeSql(Connection conn, String sql) throws SQLException {
        String[] statements = sql.split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String st : statements) {
                String trimmed = st.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    private static void loadUsersFromDb(Connection conn) throws SQLException {
        UserDAO userDAO = UserDAO.getInstance();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            while (rs.next()) {
                String id = rs.getString("id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String email = rs.getString("email");
                String fullName = rs.getString("full_name");
                String role = rs.getString("role");
                double balance = rs.getDouble("balance");

                User user;
                switch (role.toUpperCase()) {
                    case "BIDDER":
                        user = new Bidder(username, password, email, fullName, balance);
                        break;
                    case "SELLER":
                        user = new Seller(username, password, email, fullName);
                        break;
                    case "ADMIN":
                        user = new Admin(username, password, email, fullName, "SUPER");
                        break;
                    default:
                        user = new Bidder(username, password, email, fullName, 0.0);
                }
                user.setId(rs.getString("id"));
                user.setActive(rs.getBoolean("active"));
                userDAO.saveUser(user);
            }
        }
    }

    private static void loadAuctionsFromDb(Connection conn) throws SQLException {
        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        try (Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(
                  "SELECT a.*, i.category AS item_category, i.name AS item_name FROM auctions a " +
                  "LEFT JOIN items i ON a.item_id = i.id")) {
            while (rs.next()) {
                Auction auction = new Auction(
                        rs.getString("id"),
                        rs.getString("item_id"),
                        rs.getString("seller_id"),
                        rs.getTimestamp("start_time") != null
                                ? rs.getTimestamp("start_time").toLocalDateTime()
                                : LocalDateTime.now(),
                        rs.getTimestamp("end_time") != null
                                ? rs.getTimestamp("end_time").toLocalDateTime()
                                : LocalDateTime.now().plusDays(7),
                        rs.getDouble("current_price"),
                        rs.getString("current_winner_id"),
                        AuctionStatus.valueOf(rs.getString("status"))
                );
                auction.setMinIncrement(rs.getDouble("min_increment"));
                auction.setAntiSnipingEnabled(rs.getBoolean("anti_sniping_enabled"));
                auction.setAntiSnipingExtensionSeconds(rs.getDouble("anti_sniping_extension_seconds"));
                auction.setCategory(rs.getString("item_category"));
                auction.setItemName(rs.getString("item_name"));
                auctionDAO.saveAuction(auction);
            }
        }
    }

    private static void loadBidTransactionsFromDb(Connection conn) throws SQLException {
        BidTransactionDAO bidDAO = BidTransactionDAO.getInstance();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM bid_transactions")) {
            while (rs.next()) {
                BidTransaction tx = new BidTransaction(
                        rs.getString("id"),
                        rs.getString("auction_id"),
                        rs.getString("bidder_id"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("bid_time") != null
                                ? rs.getTimestamp("bid_time").toLocalDateTime()
                                : LocalDateTime.now(),
                        rs.getBoolean("is_auto_bid")
                );
                bidDAO.saveBidTransaction(tx);
            }
        }
    }
}
