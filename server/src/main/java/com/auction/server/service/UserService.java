package com.auction.server.service;

import com.auction.common.dto.UserDTO;
import com.auction.common.entity.User;
import com.auction.server.dao.UserDAO;

/**
 * UserService.java: Xử lý các nghiệp vụ liên quan đến người dùng
 * như đăng ký, đăng nhập và quản lý tài khoản.
 */
public class UserService {
    private UserDAO userDAO;
    private Object passwordEncoder;

    public UserService() {
        this.userDAO = UserDAO.getInstance();
        this.passwordEncoder = new Object();
    }

    public void register(UserDTO request) {
        // Dùng class cụ thể Member thay vì lớp ẩn
        User newUser = new Member(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getFullName()
        );

        userDAO.saveUser(newUser);
    }

    public UserDTO authenticate(String username, String password) {
        User user = userDAO.findUserByUsername(username);
        if (user != null && user.authenticate(password)) {
            UserDTO dto = new UserDTO();
            dto.setId(user.getUsername());
            dto.setUsername(user.getUsername());
            return dto;
        }
        return null;
    }

    public UserDTO getUserById(String userId) {
        return new UserDTO();//lấy thông tin chi tiết
    }


    // Getters & Setters
    public UserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
}
/// định nghĩa class Member
class Member extends User {
    public Member(String username, String password, String email, String fullName) {
        super(username, password, email, fullName);
    }

    @Override
    public String getRole() {
        return "MEMBER";
    }
}