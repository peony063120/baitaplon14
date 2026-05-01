package com.auction.server.service;

import com.auction.common.dto.UserDTO;
import com.auction.common.dto.LoginResponse;
import com.auction.server.dao.UserDAO;

/**
 * UserService.java: Xử lý các nghiệp vụ liên quan đến người dùng
 *    như đăng ký, đăng nhập và quản lý tài khoản.
 */
public class UserService {
    private UserDAO userDAO;

    public UserService() {
        this.userDAO = UserDAO.getInstance();
    }

    public void register(UserDTO request) {
    }

    public LoginResponse authenticate(String username, String password) {
        return new LoginResponse();
    }

    public UserDTO getUserById(String userId) {
        return new UserDTO();
    }

    public void updateProfile(String userId, UserDTO dto) {
    }

    public void addBalance(String userId, double amount) {
    }

    // Getters & Setters
    public UserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
}