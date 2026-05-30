package com.auction.client.controller;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.BidHistoryDTO;
import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test các logic nghiệp vụ thuần Java trong controller layer.
 * Không test các phương thức phụ thuộc JavaFX, ServerConnection, hay Platform.runLater.
 *
 * Cấu trúc: mỗi inner class tương ứng một controller.
 */
class AuctionControllerTest {

  // RegisterController — validateInput()
  @Nested
  @DisplayName("RegisterController — Kiểm tra validate đầu vào đăng ký")
  class RegisterValidationTest {

    /**
     * Tạo một bộ dữ liệu hợp lệ dạng mảng String
     * [username, password, confirmPassword, email, fullName]
     */
    private String[] validData() {
      return new String[]{"alice123", "secret123", "secret123", "alice@example.com", "Alice Nguyen"};
    }

    @Test
    @DisplayName("Tên đăng nhập hợp lệ — trả về true")
    void validUsername_ShouldReturnTrue() {
      String[] d = validData();
      assertTrue(RegisterValidationHelper.validate(d[0], d[1], d[2], d[3], d[4]));
    }

    @Test
    @DisplayName("Tên đăng nhập rỗng — thất bại")
    void emptyUsername_ShouldFail() {
      assertFalse(RegisterValidationHelper.validate("", "pass123", "pass123", "a@b.com", "Tên"));
    }

    @Test
    @DisplayName("Tên đăng nhập dưới 3 ký tự — thất bại")
    void shortUsername_ShouldFail() {
      assertFalse(RegisterValidationHelper.validate("ab", "pass123", "pass123", "a@b.com", "Tên"));
    }

    @Test
    @DisplayName("Mật khẩu rỗng — thất bại")
    void emptyPassword_ShouldFail() {
      assertFalse(RegisterValidationHelper.validate("alice123", "", "", "a@b.com", "Tên"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "12", "123", "1234", "12345"})
    @DisplayName("Mật khẩu dưới 6 ký tự — thất bại")
    void shortPassword_ShouldFail(String shortPass) {
      assertFalse(RegisterValidationHelper.validate("alice123", shortPass, shortPass, "a@b.com", "Tên"));
    }

    @Test
    @DisplayName("Mật khẩu xác nhận không khớp — thất bại")
    void mismatchConfirmPassword_ShouldFail() {
      assertFalse(RegisterValidationHelper.validate("alice123", "pass123", "different", "a@b.com", "Tên"));
    }

    @Test
    @DisplayName("Email thiếu ký tự @ — thất bại")
    void emailMissingAtSign_ShouldFail() {
      assertFalse(RegisterValidationHelper.validate("alice123", "pass123", "pass123", "invalidemail.com", "Tên"));
    }

    @Test
    @DisplayName("Email thiếu dấu chấm — thất bại")
    void emailMissingDot_ShouldFail() {
      assertFalse(RegisterValidationHelper.validate("alice123", "pass123", "pass123", "user@domain", "Tên"));
    }

    @Test
    @DisplayName("Email rỗng — thất bại")
    void emptyEmail_ShouldFail() {
      assertFalse(RegisterValidationHelper.validate("alice123", "pass123", "pass123", "", "Tên"));
    }

    @Test
    @DisplayName("Họ tên rỗng — thất bại")
    void emptyFullName_ShouldFail() {
      assertFalse(RegisterValidationHelper.validate("alice123", "pass123", "pass123", "a@b.com", ""));
    }
  }

  // CreateAuctionController — validateAuctionData()
  @Nested
  @DisplayName("CreateAuctionController — Kiểm tra validate dữ liệu phiên đấu giá")
  class CreateAuctionValidationTest {

    @Test
    @DisplayName("Tên sản phẩm rỗng — thất bại")
    void emptyItemName_ShouldFail() {
      assertFalse(AuctionValidationHelper.validate("", "Mô tả", 1000, true, 10));
    }

    @Test
    @DisplayName("Mô tả sản phẩm rỗng — thất bại")
    void emptyDescription_ShouldFail() {
      assertFalse(AuctionValidationHelper.validate("iPhone", "", 1000, true, 10));
    }

    @Test
    @DisplayName("Giá khởi điểm bằng 0 — thất bại")
    void startingPriceIsZero_ShouldFail() {
      assertFalse(AuctionValidationHelper.validate("iPhone", "Mô tả", 0, true, 10));
    }

    @Test
    @DisplayName("Giá khởi điểm âm — thất bại")
    void negativeStartingPrice_ShouldFail() {
      assertFalse(AuctionValidationHelper.validate("iPhone", "Mô tả", -500, true, 10));
    }

    @Test
    @DisplayName("Chưa chọn ngày bắt đầu — thất bại")
    void missingStartDate_ShouldFail() {
      assertFalse(AuctionValidationHelper.validate("iPhone", "Mô tả", 1000, false, 10));
    }

    @Test
    @DisplayName("Giờ bắt đầu ngoài khoảng 0-23 — thất bại")
    void startHourOutOfRange_ShouldFail() {
      assertFalse(AuctionValidationHelper.validate("iPhone", "Mô tả", 1000, true, 25));
    }

    @Test
    @DisplayName("Giờ bắt đầu âm — thất bại")
    void negativeStartHour_ShouldFail() {
      assertFalse(AuctionValidationHelper.validate("iPhone", "Mô tả", 1000, true, -1));
    }

    @Test
    @DisplayName("Tất cả dữ liệu hợp lệ — trả về true")
    void allDataValid_ShouldReturnTrue() {
      assertTrue(AuctionValidationHelper.validate("iPhone 15", "Điện thoại mới", 5_000_000, true, 9));
    }
  }

  // AuctionDetailController — placeBid() validation
  @Nested
  @DisplayName("AuctionDetailController — Kiểm tra logic đặt giá")
  class PlaceBidValidationTest {

    @Test
    @DisplayName("Giá đặt thấp hơn giá hiện tại — không hợp lệ")
    void bidLowerThanCurrentPrice_ShouldBeInvalid() {
      double currentPrice = 1_000_000;
      double bidAmount = 900_000;
      assertFalse(BidValidationHelper.isBidValid(bidAmount, currentPrice));
    }

    @Test
    @DisplayName("Giá đặt bằng giá hiện tại — không hợp lệ")
    void bidEqualToCurrentPrice_ShouldBeInvalid() {
      double currentPrice = 1_000_000;
      assertFalse(BidValidationHelper.isBidValid(currentPrice, currentPrice));
    }

    @Test
    @DisplayName("Giá đặt cao hơn giá hiện tại — hợp lệ")
    void bidHigherThanCurrentPrice_ShouldBeValid() {
      double currentPrice = 1_000_000;
      double bidAmount = 1_100_000;
      assertTrue(BidValidationHelper.isBidValid(bidAmount, currentPrice));
    }

    @Test
    @DisplayName("Giá đặt bằng 0 — không hợp lệ")
    void bidIsZero_ShouldBeInvalid() {
      assertFalse(BidValidationHelper.isBidValid(0, 500_000));
    }

    @Test
    @DisplayName("Giá đặt âm — không hợp lệ")
    void negativeBid_ShouldBeInvalid() {
      assertFalse(BidValidationHelper.isBidValid(-100, 500_000));
    }
  }

  // BidHistoryController — filterByDate()
  @Nested
  @DisplayName("BidHistoryController — Kiểm tra lọc lịch sử theo ngày")
  class FilterByDateTest {

    private List<BidHistoryDTO> sampleBids() {
      List<BidHistoryDTO> bids = new ArrayList<>();
      bids.add(new BidHistoryDTO("userA", 500_000, LocalDateTime.of(2025, 1, 10, 9, 0), false));
      bids.add(new BidHistoryDTO("userB", 700_000, LocalDateTime.of(2025, 1, 15, 14, 0), true));
      bids.add(new BidHistoryDTO("userC", 900_000, LocalDateTime.of(2025, 1, 20, 18, 30), false));
      bids.add(new BidHistoryDTO("userD", 1_200_000, LocalDateTime.of(2025, 2, 5, 10, 0), false));
      return bids;
    }

    @Test
    @DisplayName("Lọc đúng khoảng ngày — trả về kết quả chính xác")
    void filterCorrectDateRange_ShouldReturnAccurateResults() {
      List<BidHistoryDTO> bids = sampleBids();
      LocalDateTime start = LocalDateTime.of(2025, 1, 12, 0, 0);
      LocalDateTime end = LocalDateTime.of(2025, 1, 19, 23, 59, 59);

      List<BidHistoryDTO> result = FilterHelper.filterByDate(bids, start, end);

      assertEquals(1, result.size());
      assertEquals("userB", result.get(0).getBidderName());
    }

    @Test
    @DisplayName("Lọc khoảng rộng — trả về tất cả kết quả")
    void filterWideRange_ShouldReturnAllResults() {
      List<BidHistoryDTO> bids = sampleBids();
      LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
      LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59, 59);

      assertEquals(4, FilterHelper.filterByDate(bids, start, end).size());
    }

    @Test
    @DisplayName("Lọc khoảng không có dữ liệu — trả về danh sách rỗng")
    void filterRangeWithNoData_ShouldReturnEmptyList() {
      List<BidHistoryDTO> bids = sampleBids();
      LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
      LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59, 59);

      assertTrue(FilterHelper.filterByDate(bids, start, end).isEmpty());
    }

    @Test
    @DisplayName("Bid trùng đúng mốc start — được tính vào kết quả")
    void bidMatchesExactStartTime_ShouldBeIncluded() {
      List<BidHistoryDTO> bids = sampleBids();
      LocalDateTime start = LocalDateTime.of(2025, 1, 10, 9, 0); // trùng userA
      LocalDateTime end = LocalDateTime.of(2025, 1, 10, 23, 59, 59);

      List<BidHistoryDTO> result = FilterHelper.filterByDate(bids, start, end);
      assertEquals(1, result.size());
      assertEquals("userA", result.get(0).getBidderName());
    }
  }

  // BidHistoryController — sortByAmount()
  @Nested
  @DisplayName("BidHistoryController — Kiểm tra sắp xếp theo giá")
  class SortByAmountTest {

    private List<BidHistoryDTO> sampleBids() {
      List<BidHistoryDTO> bids = new ArrayList<>();
      bids.add(new BidHistoryDTO("userA", 500_000, LocalDateTime.now(), false));
      bids.add(new BidHistoryDTO("userB", 1_500_000, LocalDateTime.now(), false));
      bids.add(new BidHistoryDTO("userC", 900_000, LocalDateTime.now(), false));
      return bids;
    }

    @Test
    @DisplayName("Sắp xếp tăng dần — phần tử đầu có giá thấp nhất")
    void sortAscending_FirstElementShouldBeLowest() {
      List<BidHistoryDTO> sorted = SortHelper.sortByAmount(sampleBids(), true);
      assertEquals(500_000, sorted.get(0).getAmount(), 0.01);
      assertEquals(1_500_000, sorted.get(sorted.size() - 1).getAmount(), 0.01);
    }

    @Test
    @DisplayName("Sắp xếp giảm dần — phần tử đầu có giá cao nhất")
    void sortDescending_FirstElementShouldBeHighest() {
      List<BidHistoryDTO> sorted = SortHelper.sortByAmount(sampleBids(), false);
      assertEquals(1_500_000, sorted.get(0).getAmount(), 0.01);
      assertEquals(500_000, sorted.get(sorted.size() - 1).getAmount(), 0.01);
    }

    @Test
    @DisplayName("Sắp xếp danh sách rỗng — không ném ngoại lệ")
    void sortEmptyList_ShouldNotThrowException() {
      assertDoesNotThrow(() -> SortHelper.sortByAmount(new ArrayList<>(), true));
    }

    @Test
    @DisplayName("Sắp xếp không thay đổi danh sách gốc")
    void sort_ShouldNotModifyOriginalList() {
      List<BidHistoryDTO> original = sampleBids();
      double firstOriginalAmount = original.get(0).getAmount();
      SortHelper.sortByAmount(original, false);
      // Danh sách gốc phải giữ nguyên (sort trả về bản copy)
      assertEquals(firstOriginalAmount, original.get(0).getAmount(), 0.01);
    }
  }

  // DashboardController — convertToAuctionDTO()
  @Nested
  @DisplayName("DashboardController — Kiểm tra chuyển đổi Auction → AuctionDTO")
  class ConvertToAuctionDTOTest {
    private Auction buildAuction(String id, String sellerId, double price, AuctionStatus status) {
      Auction a = new Auction(
          id,                               // id
          "ITEM-TEST-ID",                   // itemId
          sellerId,                         // sellerId
          LocalDateTime.now(),              // startTime
          LocalDateTime.now().plusHours(2), // endTime
          price,                            // currentPrice
          null,                             // currentWinnerId
          status                            // status
      );

      a.setMinIncrement(50_000.0);
      return a;
    }

    @Test
    @DisplayName("Chuyển đổi null — trả về danh sách rỗng")
    void convertNull_ShouldReturnEmptyList() {
      List<AuctionDTO> result = ConvertHelper.convertToAuctionDTO(null);
      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Chuyển đổi danh sách hợp lệ — ánh xạ đúng các trường")
    void convertValidList_ShouldMapFieldsCorrectly() {
      List<Auction> auctions = List.of(buildAuction("A1", "seller1", 2_000_000, AuctionStatus.RUNNING));
      List<AuctionDTO> dtos = ConvertHelper.convertToAuctionDTO(auctions);

      assertEquals(1, dtos.size());
      AuctionDTO dto = dtos.get(0);
      assertEquals("A1", dto.getId());
      assertEquals("seller1", dto.getSellerId());
      assertEquals(2_000_000, dto.getCurrentPrice(), 0.01);
      assertEquals(AuctionStatus.RUNNING, dto.getStatus());
    }

    @Test
    @DisplayName("Chuyển đổi nhiều phần tử — giữ đủ số lượng")
    void convertMultipleElements_ShouldRetainCount() {
      List<Auction> auctions = List.of(
          buildAuction("A1", "s1", 1_000_000, AuctionStatus.RUNNING),
          buildAuction("A2", "s2", 2_000_000, AuctionStatus.FINISHED),
          buildAuction("A3", "s1", 500_000, AuctionStatus.DRAFT)
      );
      assertEquals(3, ConvertHelper.convertToAuctionDTO(auctions).size());
    }

    @Test
    @DisplayName("Auction không có bidHistory — totalBids bằng 0")
    void auctionWithNullBidHistory_TotalBidsShouldBeZero() {
      Auction a = buildAuction("A1", "s1", 1_000_000, AuctionStatus.RUNNING);
      a.setBidHistory(null);

      AuctionDTO dto = ConvertHelper.convertToAuctionDTO(List.of(a)).get(0);
      assertEquals(0, dto.getTotalBids());
    }
  }

  // ProfileController — changePassword() validation
  @Nested
  @DisplayName("ProfileController — Kiểm tra điều kiện đổi mật khẩu")
  class ChangePasswordValidationTest {

    @Test
    @DisplayName("Mật khẩu cũ rỗng — thất bại")
    void emptyOldPassword_ShouldFail() {
      assertFalse(PasswordChangeHelper.validate("", "newpass123"));
    }

    @Test
    @DisplayName("Mật khẩu mới dưới 6 ký tự — thất bại")
    void shortNewPassword_ShouldFail() {
      assertFalse(PasswordChangeHelper.validate("oldpass", "abc"));
    }

    @Test
    @DisplayName("Mật khẩu mới null — thất bại")
    void nullNewPassword_ShouldFail() {
      assertFalse(PasswordChangeHelper.validate("oldpass", null));
    }

    @Test
    @DisplayName("Mật khẩu cũ null — thất bại")
    void nullOldPassword_ShouldFail() {
      assertFalse(PasswordChangeHelper.validate(null, "newpass123"));
    }

    @Test
    @DisplayName("Cả hai mật khẩu hợp lệ — trả về true")
    void bothPasswordsValid_ShouldReturnTrue() {
      assertTrue(PasswordChangeHelper.validate("oldpass", "newpass123"));
    }
  }


  // Static helper classes — trích xuất logic thuần từ các Controller
  static class RegisterValidationHelper {
    static boolean validate(String username, String password, String confirmPassword,
                            String email, String fullName) {
      if (username == null || username.trim().isEmpty()) return false;
      if (username.trim().length() < 3) return false;
      if (password == null || password.trim().isEmpty()) return false;
      if (password.trim().length() < 6) return false;
      if (!password.equals(confirmPassword)) return false;
      if (email == null || email.trim().isEmpty()) return false;
      if (!email.contains("@") || !email.contains(".")) return false;
      if (fullName == null || fullName.trim().isEmpty()) return false;
      return true;
    }
  }

  static class AuctionValidationHelper {
    static boolean validate(String itemName, String description, double startingPrice,
                            boolean hasStartDate, int startHour) {
      if (itemName == null || itemName.isBlank()) return false;
      if (description == null || description.isBlank()) return false;
      if (startingPrice <= 0) return false;
      if (!hasStartDate) return false;
      if (startHour < 0 || startHour > 23) return false;
      return true;
    }
  }

  static class BidValidationHelper {
    static boolean isBidValid(double bidAmount, double currentPrice) {
      return bidAmount > currentPrice;
    }
  }

  static class FilterHelper {
    static List<BidHistoryDTO> filterByDate(List<BidHistoryDTO> allBids,
                                            LocalDateTime start, LocalDateTime end) {
      if (allBids == null) return new ArrayList<>();
      return allBids.stream()
          .filter(b -> b.getTimestamp() != null)
          .filter(b -> !b.getTimestamp().isBefore(start) && !b.getTimestamp().isAfter(end))
          .collect(java.util.stream.Collectors.toList());
    }
  }

  static class SortHelper {
    static List<BidHistoryDTO> sortByAmount(List<BidHistoryDTO> allBids, boolean ascending) {
      if (allBids == null) return new ArrayList<>();
      List<BidHistoryDTO> sorted = new ArrayList<>(allBids);
      if (ascending) {
        sorted.sort((a, b) -> Double.compare(a.getAmount(), b.getAmount()));
      } else {
        sorted.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
      }
      return sorted;
    }
  }

  static class ConvertHelper {
    static List<AuctionDTO> convertToAuctionDTO(List<Auction> auctions) {
      if (auctions == null) return new ArrayList<>();
      return auctions.stream().map(auction -> {
        AuctionDTO dto = new AuctionDTO();
        dto.setId(auction.getId());
        dto.setItemId(auction.getItemId());
        dto.setSellerId(auction.getSellerId());
        dto.setCurrentPrice(auction.getCurrentPrice());
        dto.setStartingPrice(auction.getCurrentPrice());
        dto.setStatus(auction.getStatus());
        dto.setStartTime(auction.getStartTime());
        dto.setEndTime(auction.getEndTime());
        dto.setMinIncrement(auction.getMinIncrement());
        dto.setAntiSnipingEnabled(auction.isAntiSnipingEnabled());
        dto.setAntiSnipingExtensionSeconds((int) auction.getAntiSnipingExtensionSeconds());
        dto.setCurrentWinnerId(auction.getCurrentWinnerId());
        dto.setTotalBids(auction.getBidHistory() != null ? auction.getBidHistory().size() : 0);
        dto.setItemName(auction.getItemId() != null ? "Sản phẩm " + auction.getItemId() : "Sản phẩm");
        dto.setCategory("general");
        dto.setCategoryName("Sản phẩm");
        return dto;
      }).collect(java.util.stream.Collectors.toList());
    }
  }

  static class PasswordChangeHelper {
    static boolean validate(String oldPassword, String newPassword) {
      if (oldPassword == null || oldPassword.isEmpty()) return false;
      if (newPassword == null || newPassword.length() < 6) return false;
      return true;
    }
  }
}