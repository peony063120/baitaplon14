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

    public void createAuction(AuctionDTO auctionDTO) {
        String sellerId = clientModel.getCurrentUser().getId();
        String response = ServerConnection.getInstance().sendRequest("CREATE_AUCTION:" +
                auctionDTO.getItemId() + ":" + sellerId + ":" + auctionDTO.getCurrentPrice());

        if (response != null && response.startsWith("CREATE_AUCTION_OK")) {
            showSuccess();
        } else {
            showError("Tạo phiên đấu giá thất bại");
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

        createAuction(dto);
    }

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
        // áp dụng khi cần set từ code
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
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showSuccess() {
        errorLabel.setStyle("-fx-text-fill: green;");
        errorLabel.setText("Tạo phiên đấu giá thành công!");
        errorLabel.setVisible(true);
        ((Stage) itemNameField.getScene().getWindow()).close();
    }
}
