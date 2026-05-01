package com.auction.server.controller;

import com.auction.common.dto.LoginRequest;
import com.auction.common.dto.LoginResponse;
import com.auction.common.dto.UserDTO;
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
        return userService.authenticate(request.getUsername(), request.getPassword());
    }

    public UserDTO getUserProfile(String userId) {
        return userService.getUserById(userId);
    }

    public void updateProfile(String userId, UserDTO dto) {
        userService.updateProfile(userId, dto);
    }

    public void addBalance(String userId, double amount) {
        userService.addBalance(userId, amount);
    }
}
