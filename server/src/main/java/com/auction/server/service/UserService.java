package com.auction.server.service;

import com.auction.common.dto.LoginResponse;
import com.auction.common.dto.UserDTO;
import com.auction.common.entity.Admin;
import com.auction.common.entity.Bidder;
import com.auction.common.entity.Seller;
import com.auction.common.entity.User;
import com.auction.common.exception.AuctionException;
import com.auction.common.exception.AuctionNotFoundException;
import com.auction.common.exception.InvalidBidException;
import com.auction.server.dao.UserDAO;

/**
 * UserService - Xử lý nghiệp vụ người dùng.
 */
public class UserService {
    private UserDAO userDAO;

    public UserService() {
        this.userDAO = UserDAO.getInstance();
    }

    // Đăng ký (giữ nguyên logic cũ)
    public void register(UserDTO request) {
        User newUser;
        String role = request.getRole();
        
        if (role == null) {
            role = "MEMBER";
        }
        
        switch (role.toUpperCase()) {
            case "BIDDER":
                newUser = new Bidder(
                        request.getUsername(),
                        request.getPassword(),
                        request.getEmail(),
                        request.getFullName(),
                        request.getBalance()
                );
                break;
            case "SELLER":
                newUser = new Seller(
                        request.getUsername(),
                        request.getPassword(),
                        request.getEmail(),
                        request.getFullName()
                );
                break;
            case "ADMIN":
                newUser = new Admin(
                        request.getUsername(),
                        request.getPassword(),
                        request.getEmail(),
                        request.getFullName(),
                        "SUPER"
                );
                break;
            default:
                newUser = new Member(
                        request.getUsername(),
                        request.getPassword(),
                        request.getEmail(),
                        request.getFullName()
                );
        }
        userDAO.saveUser(newUser);
    }

    // Xác thực (giữ nguyên)
    public LoginResponse authenticate(String username, String password) {
        User user = userDAO.findUserByUsername(username);
        if (user != null && user.authenticate(password)) {
            String sessionToken = java.util.UUID.randomUUID().toString();
            double balance = (user instanceof Bidder) ? ((Bidder) user).getBalance() : 0.0;
            return new LoginResponse(true, "Login successful",
                    user.getId(), user.getUsername(), user.getRole(),
                    sessionToken, balance, user.getEmail(), user.getFullName());
        }
        return new LoginResponse(false, "Invalid username or password");
    }
    // Lấy thông tin user (đã fix)
    public UserDTO getUserById(String userId) {
        User user = userDAO.findUserByUsername(userId);
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(user.getUsername());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        if (user instanceof Bidder) {
            dto.setBalance(((Bidder) user).getBalance());
        }
        dto.setActive(user.isActive());
        return dto;
    }

    // Cập nhật profile – ném exception nếu cần
    public void updateProfile(String userId, UserDTO dto) throws AuctionNotFoundException {
        User user = userDAO.findUserByUsername(userId);
        if (user == null) {
            throw new AuctionNotFoundException("User not found: " + userId);
        }
        // Chỉ cập nhật các field cho phép
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        // Không update password, role
        userDAO.saveUser(user);   // dùng saveUser thay vì updateUser
    }

    public void changePassword(String userId, String oldPassword, String newPassword)
            throws AuctionNotFoundException, IllegalArgumentException {
        User user = userDAO.findUserByUsername(userId);
        if (user == null) {
            throw new AuctionNotFoundException("User not found: " + userId);
        }
        if (!user.authenticate(oldPassword)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(newPassword);
        userDAO.saveUser(user);
    }

    // Nạp tiền – ném exception nếu có lỗi
    public void addBalance(String userId, double amount)
            throws InvalidBidException, AuctionNotFoundException, AuctionException {
        if (amount <= 0) {
            throw new InvalidBidException("Amount must be positive");
        }
        User user = userDAO.findUserByUsername(userId);
        if (user == null) {
            throw new AuctionNotFoundException("User not found: " + userId);
        }
        if (!(user instanceof Bidder)) {
            throw new AuctionException("Only bidders can add balance");
        }
        Bidder bidder = (Bidder) user;
        bidder.addBalance(amount);
        userDAO.saveUser(bidder);   // dùng saveUser
    }

    // Getter/Setter (nếu cần)
    public UserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // Inner class Member (giữ nguyên)
    private static class Member extends User {
        public Member(String username, String password, String email, String fullName) {
            super(username, password, email, fullName);
        }
        @Override
        public String getRole() {
            return "MEMBER";
        }
    }
}