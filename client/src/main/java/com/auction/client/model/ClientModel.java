package com.auction.client.model;

import com.auction.common.dto.UserDTO;
import com.auction.common.entity.User;

public class ClientModel {

    private static ClientModel instance;
    private User currentUser;
    private Session session;

    // Private constructor cho Singleton
    private ClientModel() {
    }

    // Phương thức getInstance (Singleton)
    public static synchronized ClientModel getInstance() {
        if (instance == null) {
            instance = new ClientModel();
        }
        return instance;
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

    /**
     * Lấy số dư của user hiện tại
     */
    public double getBalance() {
        if (currentUser instanceof com.auction.common.entity.Bidder) {
            return ((com.auction.common.entity.Bidder) currentUser).getBalance();
        }
        return 0.0;
    }

    /**
     * Lưu thông tin session sau khi đăng nhập thành công.
     * @param sessionToken Token phiên đăng nhập
     * @param userId ID người dùng
     * @param username Tên đăng nhập
     * @param role Vai trò (BIDDER, SELLER, ADMIN)
     * @param balance Số dư tài khoản
     */
    public void setSession(String sessionToken, String userId, String username, String role, double balance) {
        this.session = new Session(sessionToken, userId);

        if ("BIDDER".equalsIgnoreCase(role)) {
            com.auction.common.entity.Bidder user = new com.auction.common.entity.Bidder(
                    username, "", "", username, balance
            );
            user.setId(userId);
            this.currentUser = user;
            return;
        }

        User user = new User(username, "", "", username) {
            @Override
            public String getRole() {
                return role;
            }
        };
        user.setId(userId);
        this.currentUser = user;
    }

    @Override
    public String toString() {
        return "ClientModel{" +
                "currentUser=" + currentUser +
                ", session=" + session +
                '}';
    }
}
