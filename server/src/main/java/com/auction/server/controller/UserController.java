package com.auction.server.controller;

import com.auction.common.dto.LoginRequest;
import com.auction.common.dto.LoginResponse;
import com.auction.common.dto.UserDTO;
import com.auction.common.exception.AuctionException;
import com.auction.common.exception.AuctionNotFoundException;
import com.auction.common.exception.InvalidBidException;
import com.auction.server.service.UserService;

/**
 * UserController - nhận request liên quan đến User từ ClientHandler,
 * ủy thác xử lí cho UserService.
 * <p>
 * Attributes (theo diagram):
 * - userService: UserService
 * <p>
 * Methods (theo diagram):
 * + register(request: UserDTO): void
 * + login(request: LoginRequest): LoginResponse
 * + getUserProfile(userId: String): UserDTO
 * + updateProfile(userId: String, dto: UserDTO): void
 * + addBalance(userId: String, amount: double): void
 */
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void register(UserDTO request) {
        userService.register(request);
    }

    public LoginResponse login(LoginRequest request) {
        LoginResponse response = userService.authenticate(request.getUsername(), request.getPassword());
        if (response == null || !response.isSuccess()) {
            return response;
        }
        String requestedRole = request.getRole();
        if (requestedRole != null && !requestedRole.isBlank()
                && !response.getRole().equalsIgnoreCase(requestedRole)) {
            return new LoginResponse(false, "Role mismatch: please select the correct role");
        }
        return response;
    }

    public UserDTO getUserProfile(String userId) {
        return userService.getUserById(userId);
    }

    public void updateProfile(String userId, UserDTO dto) {
        try {
            userService.updateProfile(userId, dto);
        } catch (AuctionNotFoundException e) {
            // Xử lý lỗi: log hoặc gửi phản hồi lỗi về client
            e.printStackTrace();
        }
    }

    public void addBalance(String userId, double amount) {
        try {
            userService.addBalance(userId, amount);
        } catch (InvalidBidException e) {
            e.printStackTrace();
        } catch (AuctionException e) {  // bắt cả AuctionNotFoundException và AuctionException
            e.printStackTrace();
        }
    }

    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        try {
            userService.changePassword(userId, oldPassword, newPassword);
            return true;
        } catch (AuctionNotFoundException | IllegalArgumentException e) {
            return false;
        }
    }
}
