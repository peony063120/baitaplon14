package com.auction.client.components;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimerLabel extends Label {
    private LocalDateTime endTime;
    private Timeline timer;

    public TimerLabel(LocalDateTime endTime) {
        this.endTime = endTime;
        setStyle("-fx-font-family: monospace; -fx-font-size: 13px; -fx-text-fill: #D97706; -fx-font-weight: bold;");
        startTimer();
    }

    private void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDisplay()));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
        updateDisplay();
    }

    private void updateDisplay() {
        if (endTime == null) {
            setText("Chưa xác định");
            return;
        }
        long seconds = java.time.Duration.between(LocalDateTime.now(), endTime).getSeconds();
        if (seconds <= 0) {
            setText("🔴 Đã kết thúc");
            timer.stop();
            return;
        }
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (days > 0) {
            setText(String.format("⏰ %d ngày %02d giờ", days, hours));
        } else if (hours > 0) {
            setText(String.format("⏰ %02d:%02d:%02d", hours, minutes, secs));
        } else if (minutes > 0) {
            setText(String.format("⏰ %02d:%02d", minutes, secs));
        } else {
            setText(String.format("⏰ %02d giây", secs));
        }
    }

    public void updateEndTime(LocalDateTime newEndTime) {
        this.endTime = newEndTime;
        updateDisplay();
    }

    public void stop() {
        if (timer != null) timer.stop();
    }
}