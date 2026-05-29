package com.auction.server.service;

import com.auction.common.dto.LoginResponse;
import com.auction.common.dto.UserDTO;
import com.auction.common.entity.Bidder;
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
        if (usernameExists(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        User newUser = new Member(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getFullName(),
                request.getRole()
        );
        userDAO.saveUser(newUser);
    }

    public boolean usernameExists(String username) {
        return userDAO.findUserByUsername(username) != null;
    }

    // Xác thực (giữ nguyên)
    public LoginResponse authenticate(String username, String password) {
        User user = userDAO.findUserByUsername(username);
        if (user != null && user.authenticate(password)) {
            String sessionToken = java.util.UUID.randomUUID().toString();
            double balance = (user instanceof Bidder) ? ((Bidder) user).getBalance() : 0.0;
            return new LoginResponse(true, "Đăng nhập thành công",
                    user.getUsername(), user.getUsername(), user.getRole(),
                    sessionToken, balance);
        }
        return new LoginResponse(false, "Sai tên đăng nhập hoặc mật khẩu");
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
        private final String role;

        public Member(String username, String password, String email, String fullName, String role) {
            super(username, password, email, fullName);
            this.role = role == null || role.isBlank() ? "BIDDER" : role;
        }
        @Override
        public String getRole() {
            return role;
        }
    }
}
