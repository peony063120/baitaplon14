package com.auction.client.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory notification feed for the current client session.
 */
public class NotificationStore {

    private static NotificationStore instance;

    private final List<String> messages = new ArrayList<>();
    private static final int MAX_SIZE = 50;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static synchronized NotificationStore getInstance() {
        if (instance == null) {
            instance = new NotificationStore();
        }
        return instance;
    }

    public void add(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        String entry = "[" + LocalDateTime.now().format(TIME_FMT) + "] " + message;
        synchronized (messages) {
            messages.add(0, entry);
            while (messages.size() > MAX_SIZE) {
                messages.remove(messages.size() - 1);
            }
        }
    }

    public List<String> getMessages() {
        synchronized (messages) {
            return Collections.unmodifiableList(new ArrayList<>(messages));
        }
    }

    public boolean isEmpty() {
        synchronized (messages) {
            return messages.isEmpty();
        }
    }

    public void clear() {
        synchronized (messages) {
            messages.clear();
        }
    }
}
