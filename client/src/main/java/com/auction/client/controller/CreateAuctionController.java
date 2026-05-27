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
    }

    /**
     * Gửi yêu cầu tạo auction lên server.
     * @param auctionDTO Dữ liệu phiên đấu giá cần tạo
     */
    public void createAuction(AuctionDTO auctionDTO) {
        String sellerId = clientModel.getCurrentUser().getId();

        try {
            String response = ServerConnection.getInstance().sendRequest("CREATE_AUCTION:" +
                    auctionDTO.getItemId() + ":" + sellerId + ":" + auctionDTO.getCurrentPrice());

            if (response != null && response.startsWith("CREATE_AUCTION_OK")) {
                showSuccess();
            } else {
                String errorMsg = response != null ? response : "Không có phản hồi từ server";
                showError("Tạo phiên đấu giá thất bại: " + errorMsg);
            }
        } catch (IOException e) {
            showError("Lỗi kết nối server: " + e.getMessage());
        }
    }

    @FXML
    public void handleCreate() {
        if (!validateAuctionData()) return;

        AuctionDTO dto = new AuctionDTO();
        dto.setItemName(itemNameField.getText().trim());
        dto.setItemDescription(itemDescriptionArea.getText().trim());
        dto.setCurrentPrice(Double.parseDouble(startingPriceField.getText().trim()));
        dto.setSellerId(clientModel.getCurrentUser().getId());
        dto.setStartTime(getStartTime());
        dto.setEndTime(getStartTime().plusHours(durationSpinner.getValue()));
        dto.setStatus(AuctionStatus.DRAFT);
        dto.setMinIncrement(Double.parseDouble(minIncrementField.getText().isBlank() ? "1.0" : minIncrementField.getText().trim()));

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
        chooser.setTitle("Chọn ảnh sản phẩm");
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
            showError("Vui lòng nhập tên sản phẩm");
            return false;
        }
        if (itemDescriptionArea.getText().isBlank()) {
            showError("Vui lòng nhập mô tả sản phẩm");
            return false;
        }
        try {
            double price = Double.parseDouble(startingPriceField.getText().trim());
            if (price <= 0) {
                showError("Giá khởi điểm phải > 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Giá khởi điểm không hợp lệ");
            return false;
        }
        if (startDatePicker.getValue() == null) {
            showError("Vui lòng chọn ngày bắt đầu");
            return false;
        }
        try {
            int hour = Integer.parseInt(startHourField.getText().trim());
            if (hour < 0 || hour > 23) {
                showError("Giờ bắt đầu phải từ 0-23");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Giờ bắt đầu không hợp lệ");
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
        errorLabel.setText("✅ Tạo phiên đấu giá thành công!");
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