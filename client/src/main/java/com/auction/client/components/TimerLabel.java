package com.auction.client.components;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.LocalDateTime;

public class TimerLabel extends Label {
    private LocalDateTime endTime;
    private Timeline timer;

    public TimerLabel() {
        getStyleClass().add("timer-label");
    }

    public TimerLabel(LocalDateTime endTime) {
        this();
        startCountdown(endTime);
    }

    public void startCountdown(LocalDateTime endTime) {
        this.endTime = endTime;
        if (timer != null) {
            timer.stop();
        }
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
            setText("TBD"); // Dịch từ "Chua xac dinh" (To Be Determined - viết tắt phổ biến trên UI)
            return;
        }

        long seconds = java.time.Duration.between(LocalDateTime.now(), endTime).getSeconds();
        if (seconds <= 0) {
            setText("Ended"); // Dịch từ "Da ket thuc"
            if (!getStyleClass().contains("ended")) {
                getStyleClass().add("ended");
            }
            if (timer != null) {
                timer.stop();
            }
            return;
        }

        getStyleClass().remove("ended");
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        // Thay đổi định dạng chuỗi hiển thị thời gian sang Tiếng Anh
        if (days > 0) {
            // "ngay" -> "d" (days), "gio" -> "h" (hours). Dùng ký tự viết tắt giúp UI card không bị vỡ/tràn text.
            setText(String.format("%dd %02dh", days, hours));
        } else if (hours > 0) {
            setText(String.format("%02d:%02d:%02d", hours, minutes, secs));
        } else if (minutes > 0) {
            setText(String.format("%02d:%02d", minutes, secs));
        } else {
            // "%02d giay" -> "%02ds" (seconds)
            setText(String.format("%02ds", secs));
        }
    }

    public void updateEndTime(LocalDateTime newEndTime) {
        this.endTime = newEndTime;
        updateDisplay();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }
}