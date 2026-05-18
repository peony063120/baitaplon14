package com.auction.client.model;

import com.auction.common.dto.UserDTO;
import com.auction.common.entity.User;

public class ClientModel {

    private User currentUser;
    private Session session;

    public ClientModel() {
    }

    /**
     * Đăng nhập người dùng, tạo session mới nếu thành công.
     * User object được nhận từ server trả về, không khởi tạo trực tiếp.
     */
    public boolean login(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }

        // TODO: gọi ServerConnection.sendRequest() để xác thực
        // Server trả về User object (subclass cụ thể) sau khi xác thực thành công
        // Ví dụ: this.currentUser = ServerConnection.getInstance().authenticate(username, password);

        // Tạm thời: chỉ tạo session, currentUser sẽ được set sau khi nhận response từ server
        this.session = new Session(java.util.UUID.randomUUID().toString(), username);
        return true;
    }

    /**
     * Đăng xuất, xóa thông tin user và session hiện tại
     */
    public void logout() {
        this.currentUser = null;
        this.session = null;
    }

    /**
     * Đăng ký tài khoản mới
     */
    public boolean register(UserDTO userDTO) {
        if (userDTO == null) {
            return false;
        }
        // TODO: gọi ServerConnection để đăng ký
        return true;
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public String toString() {
        return "ClientModel{" +
                "currentUser=" + currentUser +
                ", session=" + session +
                '}';
    }
}