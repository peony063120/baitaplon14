package com.auction.client.controller;

/**
 * Màn hình tạo phiên đấu giá mới dành cho Seller.
 * Nhập thông tin sản phẩm, chọn loại item, giá khởi điểm, thời gian bắt đầu và thời lượng → validate → gửi lên server.
 */

import com.auction.client.model.ClientModel;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.ItemType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class CreateAuctionController {

    @FXML private TextField itemNameField;
    @FXML private TextArea itemDescriptionArea;
    @FXML private TextField startingPriceField;
    @FXML private TextField minIncrementField;
    @FXML private DatePicker startDatePicker;
    @FXML private TextField startHourField;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private ComboBox<String> itemTypeComboBox;
    @FXML private Label errorLabel;
    @FXML private Label imagePathLabel;

    private final ClientModel clientModel = ClientModel.getInstance();

    @FXML
    public void initialize() {
        itemTypeComboBox.getItems().addAll("ELECTRONICS", "ART", "VEHICLE");
        itemTypeComboBox.setValue("ELECTRONICS");

        SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 168, 24);
        durationSpinner.setValueFactory(factory);

        imagePathLabel.setText("No image selected");
    }

    /**
     * Gửi yêu cầu tạo auction lên server.
     * @param auctionDTO Dữ liệu phiên đấu giá cần tạo
     */
    public void createAuction(AuctionDTO auctionDTO) {
        try {
            // Dùng | làm delimiter, sanitize để tránh xung đột
            String itemName = sanitize(auctionDTO.getItemName());
            String description = sanitize(auctionDTO.getItemDescription()).replace("\n", "\\n");
            String startPrice = String.valueOf(auctionDTO.getStartingPrice() > 0
                    ? auctionDTO.getStartingPrice() : auctionDTO.getCurrentPrice());
            String seller = clientModel.getCurrentUser().getUsername();
            String startTime = auctionDTO.getStartTime() != null ? auctionDTO.getStartTime().toString() : "";
            String endTime = auctionDTO.getEndTime() != null ? auctionDTO.getEndTime().toString() : "";
            String minInc = String.valueOf(auctionDTO.getMinIncrement());
            String category = sanitize(auctionDTO.getCategory() != null ? auctionDTO.getCategory() : "");

            String imagePath = sanitize(imagePathLabel.getText() != null ? imagePathLabel.getText() : "");

            String request = "CREATE_AUCTION:" + itemName + "|" + description + "|" + startPrice
                    + "|" + seller + "|" + startTime + "|" + endTime + "|" + minInc + "|" + category
                    + "|" + imagePath;

            String response = ServerConnection.getInstance().sendRequest(request);

            if (response != null && response.startsWith("CREATE_AUCTION_OK")) {
                showSuccess();
            } else {
                String errorMsg = response != null ? response : "No response from server";
                showError("Failed to create auction: " + errorMsg);
            }
        } catch (IOException e) {
            showError("Server connection error: " + e.getMessage());
        }
    }

    private String sanitize(String s) {
        return s != null ? s.replace("|", " ").replace("\r", "") : "";
    }

    @FXML
    public void handleCreate() {
        if (!validateAuctionData()) return;

        AuctionDTO dto = new AuctionDTO();
        dto.setItemName(itemNameField.getText().trim());
        dto.setItemDescription(itemDescriptionArea.getText().trim());
        dto.setStartingPrice(Double.parseDouble(startingPriceField.getText().trim()));
        dto.setCurrentPrice(Double.parseDouble(startingPriceField.getText().trim()));
        dto.setSellerId(clientModel.getCurrentUser().getUsername());
        dto.setStartTime(getStartTime());
        dto.setEndTime(getStartTime().plusHours(durationSpinner.getValue()));
        dto.setStatus(AuctionStatus.PENDING);
        dto.setMinIncrement(Double.parseDouble(minIncrementField.getText().isBlank() ? "1.0" : minIncrementField.getText().trim()));
        dto.setCategory(itemTypeComboBox.getValue());

        // Gửi request trong background thread (không block UI)
        new Thread(() -> createAuction(dto)).start();
    }

    @FXML
    public void selectItemType(ItemType type) {
        itemTypeComboBox.setValue(type.name());
    }

    public void uploadImages(List<File> files) {
        if (files == null || files.isEmpty()) return;
        imagePathLabel.setText(files.get(0).getAbsolutePath());
    }

    @FXML
    public void handleUploadImages() {
        FileChooser chooser = new FileChooser();
        // CHANGED: "Chọn ảnh sản phẩm" -> "Select Product Images"
        chooser.setTitle("Select Product Images");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        List<File> files = chooser.showOpenMultipleDialog(itemNameField.getScene().getWindow());
        uploadImages(files);
    }

    public void setStartTime(LocalDateTime start) {
        if (start != null) {
            startDatePicker.setValue(start.toLocalDate());
            startHourField.setText(String.valueOf(start.getHour()));
        }
    }

    public void setDuration(int hours) {
        durationSpinner.getValueFactory().setValue(hours);
    }

    public boolean validateAuctionData() {
        if (itemNameField.getText().isBlank()) {
            // CHANGED: "Vui lòng nhập tên sản phẩm" -> "Please enter the product name"
            showError("Please enter the product name");
            return false;
        }
        if (itemDescriptionArea.getText().isBlank()) {
            // CHANGED: "Vui lòng nhập mô tả sản phẩm" -> "Please enter the product description"
            showError("Please enter the product description");
            return false;
        }
        String imgPath = imagePathLabel.getText();
        if (imgPath == null || imgPath.isBlank() || "No image selected".equals(imgPath)) {
            showError("Please upload at least one product image");
            return false;
        }
        try {
            double price = Double.parseDouble(startingPriceField.getText().trim());
            if (price <= 0) {
                // CHANGED: "Giá khởi điểm phải > 0" -> "Starting price must be greater than 0"
                showError("Starting price must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            // CHANGED: "Giá khởi điểm không hợp lệ" -> "Invalid starting price"
            showError("Invalid starting price");
            return false;
        }
        if (startDatePicker.getValue() == null) {
            // CHANGED: "Vui lòng chọn ngày bắt đầu" -> "Please select a start date"
            showError("Please select a start date");
            return false;
        }
        try {
            int hour = Integer.parseInt(startHourField.getText().trim());
            if (hour < 0 || hour > 23) {
                // CHANGED: "Giờ bắt đầu phải từ 0-23" -> "Start hour must be between 0 and 23"
                showError("Start hour must be between 0 and 23");
                return false;
            }
        } catch (NumberFormatException e) {
            // CHANGED: "Giờ bắt đầu không hợp lệ" -> "Invalid start hour"
            showError("Invalid start hour");
            return false;
        }
        errorLabel.setVisible(false);
        return true;
    }

    // === Helpers ===

    private LocalDateTime getStartTime() {
        int hour = 0;
        try {
            hour = Integer.parseInt(startHourField.getText().trim());
        } catch (Exception ignored) {}
        return startDatePicker.getValue().atTime(hour, 0);
    }

    private void showError(String message) {
        errorLabel.setStyle("-fx-text-fill: #DC2626;");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showSuccess() {
        errorLabel.setStyle("-fx-text-fill: #16A34A;");
        // CHANGED: "✅ Tạo phiên đấu giá thành công!" -> "✅ Auction created successfully!"
        errorLabel.setText("✅ Auction created successfully!");
        errorLabel.setVisible(true);

        // Đóng cửa sổ sau 1.5 giây
        new Thread(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> {
                Stage stage = (Stage) itemNameField.getScene().getWindow();
                stage.close();
            });
        }).start();
    }
}