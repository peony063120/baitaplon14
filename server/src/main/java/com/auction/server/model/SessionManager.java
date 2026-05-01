package com.auction.server.model;

import com.auction.common.entity.User;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionManager (Singleton)
 * Quản lý các phiên đăng nhập của user (sessionId -> User).
 */
public class SessionManager {
    private static SessionManager instance;
    private final Map<String, User> sessions;          // sessionId -> User
    private final Map<String, String> userToSession;   // userId -> sessionId

    private SessionManager() {
        sessions = new ConcurrentHashMap<>();
        userToSession = new ConcurrentHashMap<>();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Tạo session mới cho user.
     * Nếu user đã có session, xóa session cũ trước.
     */
    public String createSession(User user) {
        // Xóa session cũ nếu tồn tại
        String oldSessionId = userToSession.get(user.getId());
        if (oldSessionId != null) {
            sessions.remove(oldSessionId);
        }
        String newSessionId = UUID.randomUUID().toString();
        sessions.put(newSessionId, user);
        userToSession.put(user.getId(), newSessionId);
        return newSessionId;
    }

    /**
     * Lấy user theo sessionId.
     */
    public User getUser(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Hủy session.
     */
    public void invalidateSession(String sessionId) {
        User user = sessions.remove(sessionId);
        if (user != null) {
            userToSession.remove(user.getId());
        }
    }

    /**
     * Kiểm tra session có hợp lệ không.
     */
    public boolean isValidSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    /**
     * Lấy sessionId của user.
     */
    public String getSessionIdByUser(String userId) {
        return userToSession.get(userId);
    }
}