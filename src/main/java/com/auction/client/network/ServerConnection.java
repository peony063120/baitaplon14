package com.auction.client.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerConnection {
    private static final Logger logger = LoggerFactory.getLogger(ServerConnection.class);

    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    public static void connect(String host, int port) throws IOException {
        logger.info("Connecting to server at {}:{}", host, port);
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        logger.info("Connected to server");
    }

    public static Map<String, Object> login(String username, String password) throws IOException {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        String json = mapper.writeValueAsString(credentials);
        out.println("LOGIN " + json);

        String response = in.readLine();
        logger.debug("Login response: {}", response);

        return parseResponse(response);
    }

    public static Map<String, Object> register(String username, String password,
                                               String email, String fullName, String role) throws IOException {
        Map<String, String> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", password);
        userData.put("email", email);
        userData.put("fullName", fullName);
        userData.put("role", role);

        String json = mapper.writeValueAsString(userData);
        out.println("REGISTER " + json);

        String response = in.readLine();
        return parseResponse(response);
    }

    public static String getAuctions() throws IOException {
        out.println("GET_AUCTIONS");
        String response = in.readLine();
        if (response != null && response.startsWith("DATA ")) {
            return response.substring(5);
        }
        return null;
    }

    public static String getAuction(String auctionId) throws IOException {
        out.println("GET_AUCTION " + auctionId);
        String response = in.readLine();
        if (response != null && response.startsWith("DATA ")) {
            return response.substring(5);
        }
        return null;
    }

    // ✅ THÊM METHOD NÀY
    public static String getUser(String userId) throws IOException {
        out.println("GET_USER " + userId);
        String response = in.readLine();
        if (response != null && response.startsWith("DATA ")) {
            return response.substring(5);
        }
        return null;
    }

    public static boolean placeBid(String auctionId, String bidderId, double amount) throws IOException {
        Map<String, Object> bidData = new HashMap<>();
        bidData.put("auctionId", auctionId);
        bidData.put("bidderId", bidderId);
        bidData.put("amount", amount);

        String json = mapper.writeValueAsString(bidData);
        out.println("PLACE_BID " + json);

        String response = in.readLine();
        Map<String, Object> result = parseResponse(response);
        return (boolean) result.getOrDefault("success", false);
    }

    public static boolean setupAutoBid(String auctionId, String bidderId,
                                       double maxBid, double increment) throws IOException {
        Map<String, Object> autoBidData = new HashMap<>();
        autoBidData.put("auctionId", auctionId);
        autoBidData.put("bidderId", bidderId);
        autoBidData.put("maxBid", maxBid);
        autoBidData.put("increment", increment);

        String json = mapper.writeValueAsString(autoBidData);
        out.println("SETUP_AUTO_BID " + json);

        String response = in.readLine();
        Map<String, Object> result = parseResponse(response);
        return (boolean) result.getOrDefault("success", false);
    }

    public static void disconnect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            logger.info("Disconnected from server");
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseResponse(String response) throws IOException {
        Map<String, Object> result = new HashMap<>();

        if (response == null) {
            result.put("success", false);
            result.put("message", "No response from server");
            return result;
        }

        if (response.startsWith("RESPONSE ")) {
            String json = response.substring(9);
            return mapper.readValue(json, Map.class);
        } else if (response.startsWith("ERROR ")) {
            result.put("success", false);
            result.put("message", response.substring(6));
            return result;
        } else if (response.startsWith("DATA ")) {
            result.put("success", true);
            result.put("data", response.substring(5));
            return result;
        }

        result.put("success", false);
        result.put("message", "Unknown response format");
        return result;
    }
}