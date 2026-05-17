package com.auction.client.components;

import com.auction.common.entity.BidTransaction;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class BidCard extends HBox {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    public BidCard(BidTransaction bid, boolean isCurrentUser) {
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-padding: 10; -fx-background-color: #F8F4ED; -fx-background-radius: 8;");

        // Avatar / Icon
        Label avatarLabel = new Label(bid.getBidderId().substring(0, 1).toUpperCase());
        avatarLabel.setStyle("""
            -fx-background-color: #C9A84C;
            -fx-text-fill: #0D1B2A;
            -fx-font-weight: bold;
            -fx-padding: 8;
            -fx-background-radius: 20;
            -fx-min-width: 36;
            -fx-min-height: 36;
            -fx-alignment: center;
            """);
        avatarLabel.setAlignment(Pos.CENTER);

        // Info
        VBox infoBox = new VBox(4);
        Label nameLabel = new Label(bid.getBidderId());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Label timeLabel = new Label(bid.getBidTime().format(TIME_FORMAT));
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7A8D;");
        infoBox.getChildren().addAll(nameLabel, timeLabel);

        // Amount
        Label amountLabel = new Label(formatPrice(bid.getAmount()));
        amountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #D97706;");
        amountLabel.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(amountLabel, javafx.scene.layout.Priority.ALWAYS);

        // Auto-bid indicator
        if (bid.isAutoBid()) {
            Label autoLabel = new Label("🤖 Auto");
            autoLabel.setStyle("-fx-font-size: 10px; -fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-padding: 2 6; -fx-background-radius: 10;");
            amountLabel.setGraphic(autoLabel);
            amountLabel.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
        }

        getChildren().addAll(avatarLabel, infoBox, amountLabel);
    }

    private String formatPrice(double price) {
        return String.format("₫ %,.0f", price);
    }
}