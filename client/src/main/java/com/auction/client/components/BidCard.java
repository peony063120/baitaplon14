package com.auction.client.components;

import com.auction.common.entity.BidTransaction;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class BidCard extends HBox {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    public BidCard(BidTransaction bid, boolean isCurrentUser) {
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("bid-card");

        Label avatarLabel = new Label(getAvatarText(bid));
        avatarLabel.getStyleClass().add("avatar-chip");
        avatarLabel.setAlignment(Pos.CENTER);
        // TODO(asset): Replace this initial chip with user avatar image when profile assets are available.

        VBox infoBox = new VBox(4);

        // Sử dụng biến isCurrentUser để hiển thị "You" nếu đó là lượt đặt giá của chính người dùng hiện tại
        String displayName = isCurrentUser ? "You" : bid.getBidderId();
        Label nameLabel = new Label(displayName);
        nameLabel.setStyle("-fx-font-weight: 800; -fx-font-size: 13px; -fx-text-fill: #111111;");

        Label timeLabel = new Label(bid.getBidTime().format(TIME_FORMAT));
        timeLabel.getStyleClass().add("muted-text");
        timeLabel.setStyle("-fx-font-size: 11px;");
        infoBox.getChildren().addAll(nameLabel, timeLabel);

        Label amountLabel = new Label(formatPrice(bid.getAmount()));
        amountLabel.getStyleClass().add("amount-text");
        amountLabel.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(amountLabel, Priority.ALWAYS);

        if (bid.isAutoBid()) {
            Label autoLabel = new Label("Auto");
            autoLabel.getStyleClass().add("auto-chip");
            amountLabel.setGraphic(autoLabel);
            amountLabel.setContentDisplay(ContentDisplay.RIGHT);
        }

        getChildren().addAll(avatarLabel, infoBox, amountLabel);
    }

    private String getAvatarText(BidTransaction bid) {
        String bidderId = bid.getBidderId();
        if (bidderId == null || bidderId.isBlank()) {
            return "?";
        }
        return bidderId.substring(0, 1).toUpperCase();
    }

    private String formatPrice(double price) {
        return String.format("VND %,.0f", price);
    }
}