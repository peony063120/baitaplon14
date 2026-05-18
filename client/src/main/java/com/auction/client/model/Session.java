package com.auction.client.model;

import java.time.LocalDateTime;

public class Session {

    private String sessionId;
    private String userId;
    private LocalDateTime createdAt;

    public Session() {
        this.createdAt = LocalDateTime.now();
    }

    public Session(String sessionId, String userId) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Kiểm tra session có còn hợp lệ không (trong vòng 24 giờ)
     */
    public boolean isValid() {
        if (sessionId == null || userId == null || createdAt == null) {
            return false;
        }
        return createdAt.plusHours(24).isAfter(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                ", userId='" + userId + '\'' +
                ", createdAt=" + createdAt +
                ", valid=" + isValid() +
                '}';
    }
}
