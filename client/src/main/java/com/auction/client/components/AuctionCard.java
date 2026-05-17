package com.auction.client.components;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.enums.AuctionStatus;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
import com.auction.client.components.TimerLabel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class AuctionCard extends VBox {
    private AuctionDTO auction;
    private Consumer<AuctionDTO> onBidCallback;
    private Timeline countdownTimer;

    // Style constants
    private static final String CARD_STYLE = """
        -fx-background-color: white;
        -fx-border-color: #E2D9C8;
        -fx-border-radius: 16;
        -fx-background-radius: 16;
        -fx-padding: 12;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);
        """;
    private static final String HOVER_STYLE = """
        -fx-background-color: white;
        -fx-border-color: #C9A84C;
        -fx-border-radius: 16;
        -fx-background-radius: 16;
        -fx-padding: 12;
        -fx-effect: dropshadow(gaussian, rgba(201,168,76,0.15), 8, 0, 0, 2);
        """;

    public AuctionCard(AuctionDTO auction, Consumer<AuctionDTO> onBidCallback) {
        this.auction = auction;
        this.onBidCallback = onBidCallback;
        buildUI();
        setOnMouseEntered(e -> setStyle(HOVER_STYLE));
        setOnMouseExited(e -> setStyle(CARD_STYLE));
        setStyle(CARD_STYLE);
        startCountdown();
    }

    private void buildUI() {
        setSpacing(8);
        setMinWidth(280);
        setMaxWidth(280);

        // Image/Icon area
        Label iconLabel = new Label(getIconForCategory());
        iconLabel.setFont(Font.font(48));
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setMaxWidth(Double.MAX_VALUE);
        iconLabel.setStyle("-fx-padding: 20 0 20 0; -fx-background-color: #F0EBE0; -fx-background-radius: 12;");

        // Status badge
        Label statusBadge = new Label(getStatusText());
        statusBadge.getStyleClass().add("card-badge");
        statusBadge.setStyle(getStatusStyle());

        // Category label
        Label categoryLabel = new Label(auction.getCategoryName());
        categoryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A6520; -fx-font-weight: bold; -fx-letter-spacing: 0.8px;");

        // Title
        Label titleLabel = new Label(auction.getItemName());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-wrap-text: true;");

        // Price row
        HBox priceBox = new HBox(16);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        VBox currentPriceBox = new VBox(2);
        Label currentPriceLabel = new Label("Giá hiện tại");
        currentPriceLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7A8D;");
        Label currentPriceValue = new Label(formatPrice(auction.getCurrentPrice()));
        currentPriceValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #C9A84C;");
        currentPriceBox.getChildren().addAll(currentPriceLabel, currentPriceValue);

        VBox startPriceBox = new VBox(2);
        Label startPriceLabel = new Label("Khởi điểm");
        startPriceLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7A8D;");
        Label startPriceValue = new Label(formatPrice(auction.getStartPrice()));
        startPriceValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7A8D;");
        startPriceBox.getChildren().addAll(startPriceLabel, startPriceValue);

        priceBox.getChildren().addAll(currentPriceBox, startPriceBox);

        // Meta row
        HBox metaBox = new HBox(12);
        Label bidCountLabel = new Label("🟢 " + auction.getTotalBids() + " lượt đặt");
        bidCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7A8D;");

        TimerLabel timerLabel = new TimerLabel(auction.getEndTime());
        metaBox.getChildren().addAll(bidCountLabel, timerLabel);

        // Bid button
        Button bidButton = new Button("🏷️ Đặt Giá Ngay");
        bidButton.getStyleClass().add("bid-button");
        bidButton.setStyle("""
            -fx-background-color: #C9A84C;
            -fx-text-fill: #0D1B2A;
            -fx-font-weight: bold;
            -fx-padding: 8 16;
            -fx-background-radius: 8;
            """);
        bidButton.setOnAction(e -> {
            if (onBidCallback != null) onBidCallback.accept(auction);
        });
        bidButton.setMaxWidth(Double.MAX_VALUE);

        // Stack for relative positioning
        StackPane badgePane = new StackPane();
        badgePane.getChildren().addAll(iconLabel, statusBadge);
        StackPane.setAlignment(statusBadge, Pos.TOP_LEFT);
        StackPane.setMargin(statusBadge, new Insets(8, 0, 0, 8));

        getChildren().addAll(badgePane, categoryLabel, titleLabel, priceBox, metaBox, bidButton);
    }

    private String getIconForCategory() {
        String category = auction.getCategory();
        if (category == null) return "📦";
        return switch (category.toLowerCase()) {
            case "xe", "xe co", "xe cộ", "vehicle" -> "🚗";
            case "dien-tu", "dien tu", "electronics" -> "💻";
            case "nghe-thuat", "nghe thuat", "art" -> "🎨";
            case "trang-suc", "trang suc", "jewelry" -> "💎";
            case "bat-dong-san", "bat dong san", "realestate" -> "🏠";
            case "dong-ho", "dong ho", "watch" -> "⌚";
            case "co-vat", "co vat", "antique" -> "🏺";
            default -> "📦";
        };
    }

    private String getStatusText() {
        AuctionStatus status = auction.getStatus();
        if (status == null) return "SẮP MỞ";
        return switch (status) {
            case RUNNING -> "TRỰC TIẾP";
            case FINISHED -> "KẾT THÚC";
            case PAID -> "ĐÃ THANH TOÁN";
            case CANCELLED -> "ĐÃ HỦY";
            default -> "SẮP MỞ";
        };
    }

    private String getStatusStyle() {
        AuctionStatus status = auction.getStatus();
        if (status == null) return "-fx-background-color: #2563EB; -fx-text-fill: white;";
        return switch (status) {
            case RUNNING -> "-fx-background-color: #DC2626; -fx-text-fill: white;";
            case FINISHED, PAID -> "-fx-background-color: #6B7280; -fx-text-fill: white;";
            case CANCELLED -> "-fx-background-color: #9BAAB8; -fx-text-fill: white;";
            default -> "-fx-background-color: #2563EB; -fx-text-fill: white;";
        };
    }

    private String formatPrice(double price) {
        if (price >= 1_000_000_000) {
            return String.format("₫ %.1f Tỷ", price / 1_000_000_000);
        } else if (price >= 1_000_000) {
            return String.format("₫ %.0f Tr", price / 1_000_000);
        }
        return String.format("₫ %,.0f", price);
    }

    private void startCountdown() {
        if (auction.getEndTime() == null || auction.getStatus() != AuctionStatus.RUNNING) return;
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateCountdown()));
        countdownTimer.setCycleCount(Animation.INDEFINITE);
        countdownTimer.play();
    }

    private void updateCountdown() {
        if (auction.getEndTime() == null) return;
        long remaining = java.time.Duration.between(LocalDateTime.now(), auction.getEndTime()).getSeconds();
        if (remaining <= 0) {
            if (countdownTimer != null) countdownTimer.stop();
        }
    }

    public void updateAuction(AuctionDTO newAuction) {
        this.auction = newAuction;
        refreshUI();
    }

    private void refreshUI() {
        getChildren().clear();
        buildUI();
    }
}