package com.auction.server.service;

/**
 * Gửi thông báo cho người dùng (ví dụ: khi bị vượt giá,
 *      khi thắng cuộc, hoặc khi cuộc đấu giá sắp kết thúc).
 */
public class NotificationService {
    private String lastMessage;

    public void send(String userId, String msg) {
    }

    // Getters & Setters
    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}