package com.auction.client.components;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.enums.AuctionStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.function.Consumer;

public class AuctionCard extends VBox {
    private AuctionDTO auction;
    private final Consumer<AuctionDTO> onBidCallback;

    public AuctionCard(AuctionDTO auction, Consumer<AuctionDTO> onBidCallback) {
        this.auction = auction;
        this.onBidCallback = onBidCallback;
        getStyleClass().add("auction-card");
        buildUI();
    }

    private void buildUI() {
        setSpacing(10);
        setMinWidth(280);
        setMaxWidth(280);

        Label imagePlaceholder = new Label(getCategoryLabel());
        imagePlaceholder.getStyleClass().add("asset-placeholder");
        imagePlaceholder.setAlignment(Pos.CENTER);
        imagePlaceholder.setMaxWidth(Double.MAX_VALUE);
        imagePlaceholder.setMinHeight(130);

        javafx.scene.Node imageNode = buildImageNode(imagePlaceholder);

        Label statusBadge = new Label(getStatusText());
        statusBadge.getStyleClass().add("card-badge");

        Label categoryLabel = new Label(getCategoryLabel().toUpperCase());
        categoryLabel.getStyleClass().add("muted-text");
        categoryLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 800;");

        Label titleLabel = new Label(auction.getItemName());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #111111;");

        HBox priceBox = new HBox(16);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        VBox currentPriceBox = new VBox(2);
        Label currentPriceLabel = new Label("Current Price");
        currentPriceLabel.getStyleClass().add("muted-text");
        currentPriceLabel.setStyle("-fx-font-size: 11px;");
        Label currentPriceValue = new Label(formatPrice(auction.getCurrentPrice()));
        currentPriceValue.getStyleClass().add("current-price");
        currentPriceBox.getChildren().addAll(currentPriceLabel, currentPriceValue);

        VBox startPriceBox = new VBox(2);
        Label startPriceLabel = new Label("Starting Price");
        startPriceLabel.getStyleClass().add("muted-text");
        startPriceLabel.setStyle("-fx-font-size: 11px;");
        Label startPriceValue = new Label(formatPrice(auction.getStartPrice()));
        startPriceValue.getStyleClass().add("muted-text");
        startPriceValue.setStyle("-fx-font-size: 13px;");
        startPriceBox.getChildren().addAll(startPriceLabel, startPriceValue);

        priceBox.getChildren().addAll(currentPriceBox, startPriceBox);

        HBox metaBox = new HBox(12);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        Label bidCountLabel = new Label(auction.getTotalBids() + " bids");
        bidCountLabel.getStyleClass().add("muted-text");
        bidCountLabel.setStyle("-fx-font-size: 12px;");

        TimerLabel timerLabel = createTimerLabel();
        HBox.setHgrow(timerLabel, Priority.ALWAYS);
        metaBox.getChildren().addAll(bidCountLabel, timerLabel);

        Button bidButton = new Button("Bid Now");
        bidButton.getStyleClass().add("bid-button");
        bidButton.setOnAction(e -> {
            if (onBidCallback != null) {
                onBidCallback.accept(auction);
            }
        });
        bidButton.setMaxWidth(Double.MAX_VALUE);

        StackPane badgePane = new StackPane();
        badgePane.getChildren().addAll(imageNode, statusBadge);
        StackPane.setAlignment(statusBadge, Pos.TOP_LEFT);
        StackPane.setMargin(statusBadge, new Insets(10, 0, 0, 10));

        getChildren().addAll(badgePane, categoryLabel, titleLabel, priceBox, metaBox, bidButton);
    }

    private javafx.scene.Node buildImageNode(Label fallback) {
        String imageRef = auction.getImagePath();
        if (imageRef != null && imageRef.startsWith("BASE64:")) {
            try {
                byte[] bytes = Base64.getDecoder().decode(imageRef.substring(7));
                ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(bytes)));
                imageView.setFitWidth(280);
                imageView.setFitHeight(130);
                imageView.setPreserveRatio(true);
                return imageView;
            } catch (IllegalArgumentException ignored) {
                return fallback;
            }
        }
        if (imageRef != null && !imageRef.isBlank()) {
            File file = new File(imageRef);
            if (file.exists()) {
                ImageView imageView = new ImageView(new Image(file.toURI().toString(), true));
                imageView.setFitWidth(280);
                imageView.setFitHeight(130);
                imageView.setPreserveRatio(true);
                return imageView;
            }
        }
        return fallback;
    }

    private String getCategoryLabel() {
        String categoryName = auction.getCategoryName();
        if (categoryName != null && !categoryName.isBlank()) {
            return categoryName;
        }

        String category = auction.getCategory();
        if (category == null || category.isBlank()) {
            return "Product";
        }

        return switch (category.toLowerCase()) {
            case "xe", "xe co", "vehicle" -> "Vehicle";
            case "dien-tu", "dien tu", "electronics" -> "Electronics";
            case "nghe-thuat", "nghe thuat", "art" -> "Art";
            case "trang-suc", "trang suc", "jewelry" -> "Jewelry";
            case "bat-dong-san", "bat dong san", "realestate" -> "Real Estate";
            case "dong-ho", "dong ho", "watch" -> "Watch";
            case "co-vat", "co vat", "antique" -> "Antique";
            default -> category;
        };
    }

    private String getStatusText() {
        AuctionStatus status = auction.getStatus();
        if (status == null) {
            return "COMING SOON";
        }
        return switch (status) {
            case RUNNING -> "LIVE";
            case OPEN -> "SOON";
            case FINISHED -> "ENDED";
            case PAID -> "PAID";
            case CANCELLED -> "CANCELLED";
            default -> "COMING SOON";
        };
    }

    private TimerLabel createTimerLabel() {
        TimerLabel timerLabel = new TimerLabel();
        AuctionStatus status = auction.getStatus();
        if (status == AuctionStatus.OPEN && auction.getStartTime() != null
                && auction.getStartTime().isAfter(LocalDateTime.now())) {
            timerLabel.startCountdownToStart(auction.getStartTime());
        } else if (status == AuctionStatus.RUNNING) {
            LocalDateTime endTime = resolveEndTime();
            if (endTime != null && endTime.isAfter(LocalDateTime.now())) {
                timerLabel.startCountdown(endTime);
            } else {
                timerLabel.setText("Live");
            }
        } else if (status == AuctionStatus.FINISHED || status == AuctionStatus.PAID) {
            timerLabel.setText("Ended");
        }
        return timerLabel;
    }

    private LocalDateTime resolveEndTime() {
        if (auction.getEndTime() != null) {
            return auction.getEndTime();
        }
        if (auction.getRemainingTimeMillis() > 0) {
            return LocalDateTime.now().plus(java.time.Duration.ofMillis(auction.getRemainingTimeMillis()));
        }
        return null;
    }

    private String formatPrice(double price) {
        if (price >= 1_000_000_000) {
            return String.format("$%.1fB", price / 1_000_000_000);
        } else if (price >= 1_000_000) {
            return String.format("$%.0fM", price / 1_000_000);
        }
        return String.format("$%,.0f", price);
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
