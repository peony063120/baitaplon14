package com.auction.client.service;

import com.auction.common.dto.LoginResponse;
import com.auction.common.dto.UserDTO;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockUserStore {

    private static final MockUserStore INSTANCE = new MockUserStore();
    private final Map<String, UserDTO> users = new ConcurrentHashMap<>();

    private MockUserStore() {}

    public static MockUserStore getInstance() {
        return INSTANCE;
    }

    public boolean register(UserDTO dto) {
        if (dto == null || dto.getUsername() == null) return false;
        if (users.containsKey(dto.getUsername())) return false;
        users.put(dto.getUsername(), dto);
        return true;
    }

    public boolean usernameExists(String username) {
        return username != null && users.containsKey(username);
    }

    public LoginResponse login(String username, String password, String role) {
        UserDTO dto = users.get(username);
        if (dto == null) {
            return new LoginResponse(false, "User not found");
        }
        if (!dto.getPassword().equals(password)) {
            return new LoginResponse(false, "Incorrect password");
        }
        String matchedRole = role != null ? role.toUpperCase() : dto.getRole();
        if (!dto.getRole().equalsIgnoreCase(matchedRole)) {
            return new LoginResponse(false, "Role mismatch");
        }
        String sessionToken = "mock_session_" + System.currentTimeMillis();
        String userId = dto.getId() != null ? dto.getId() : "mock_user_" + username;
        double balance = dto.getBalance();
        return new LoginResponse(true, "Login successful (mock)",
                userId, username, matchedRole, sessionToken, balance);
    }

    public void seedDefaultUsers() {
        if (!users.isEmpty()) return;
        users.put("admin",   new UserDTO("user-001", "admin",   "admin",   "admin@auction.com",   "Head Administrator",  "ADMIN",  0.00));
        users.put("admin2",  new UserDTO("user-011", "admin2",  "admin2",  "admin2@auction.com",  "Deputy Administrator","ADMIN", 0.00));
        users.put("bidder1", new UserDTO("user-002", "bidder1", "bidder1", "bidder1@auction.com", "Alice Nguyen",        "BIDDER",1000.00));
        users.put("bidder2", new UserDTO("user-003", "bidder2", "bidder2", "bidder2@auction.com", "Bob Tran",            "BIDDER",500.00));
        users.put("bidder3", new UserDTO("user-004", "bidder3", "bidder3", "bidder3@auction.com", "Charlie Pham",        "BIDDER",2000.00));
        users.put("seller1", new UserDTO("user-005", "seller1", "seller1", "seller1@auction.com", "David Le",            "SELLER",0.00));
        users.put("seller2", new UserDTO("user-006", "seller2", "seller2", "seller2@auction.com", "Eva Hoang",           "SELLER",0.00));
        users.put("bidder4", new UserDTO("user-007", "bidder4", "bidder4", "bidder4@auction.com", "Fiona Le",            "BIDDER",3000.00));
        users.put("bidder5", new UserDTO("user-008", "bidder5", "bidder5", "bidder5@auction.com", "George Vo",           "BIDDER",10000.00));
        users.put("seller3", new UserDTO("user-009", "seller3", "seller3", "seller3@auction.com", "Helen Dang",          "SELLER",0.00));
        users.put("seller4", new UserDTO("user-010", "seller4", "seller4", "seller4@auction.com", "Ivan Tran",           "SELLER",0.00));
    }
}
