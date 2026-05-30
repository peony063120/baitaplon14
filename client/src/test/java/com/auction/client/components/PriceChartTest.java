package com.auction.client.components;

import com.auction.common.entity.BidTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PriceChart – Logic xử lý dữ liệu giá")
class PriceChartTest {

  // Model thu gọn của một điểm dữ liệu trên biểu đồ
  record DataPoint(double x, double y) {}

  // Tái hiện logic của PriceChart không phụ thuộc JavaFX
  static class PriceChartLogicHelper {
    private final List<DataPoint> data = new ArrayList<>();
    private int pointCount = 0;
    private double upperBound = 0;
    private double lowerBound = 0;
    private double xLower = 0;
    private double xUpper = 0;

    void addPricePoint(double price) {
      double priceInMillions = price / 1_000_000.0;
      pointCount++;
      data.add(new DataPoint(pointCount, priceInMillions));

      if (priceInMillions > upperBound) {
        upperBound = priceInMillions * 1.1;
      }

      if (pointCount > 20) {
        xLower = pointCount - 20;
        xUpper = pointCount;
      }
    }

    void updateWithBidHistory(List<BidTransaction> history) {
      data.clear();
      pointCount = 0;

      if (history == null || history.isEmpty()) {
        data.add(new DataPoint(0, 0));
        return;
      }

      for (BidTransaction bid : history) {
        double priceInMillions = bid.getAmount() / 1_000_000.0;
        pointCount++;
        data.add(new DataPoint(pointCount, priceInMillions));
      }

      double maxPrice = history.stream()
          .mapToDouble(BidTransaction::getAmount)
          .max()
          .orElse(100_000_000);
      upperBound = Math.ceil(maxPrice / 1_000_000) * 1.1;
      lowerBound = 0;
      xLower = 0;
      xUpper = pointCount + 2;
    }

    void addNewBid(BidTransaction bid) {
      double priceInMillions = bid.getAmount() / 1_000_000.0;
      pointCount++;
      data.add(new DataPoint(pointCount, priceInMillions));

      if (priceInMillions > upperBound) {
        upperBound = priceInMillions * 1.1;
      }
    }

    void clear() {
      data.clear();
      pointCount = 0;
      data.add(new DataPoint(0, 0));
    }

    List<DataPoint> getData()   { return data; }
    int getPointCount()         { return pointCount; }
    double getUpperBound()      { return upperBound; }
    double getLowerBound()      { return lowerBound; }
    double getXLower()          { return xLower; }
    double getXUpper()          { return xUpper; }
  }


  // Tiện ích tạo BidTransaction stub
  private BidTransaction makeBid(double amount) {
    return new BidTransaction(
        "auction-1",
        "user-test",
        amount,
        LocalDateTime.now(),
        false
    );
  }

  private PriceChartLogicHelper chart;

  @BeforeEach
  void setUp() {
    chart = new PriceChartLogicHelper();
  }

  // ================================================================

  @Nested
  @DisplayName("addPricePoint – Thêm điểm giá mới")
  class AddPricePointTests {

    @Test
    @DisplayName("Thêm một điểm thì pointCount tăng lên 1")
    void addingOnePointIncrementsPointCount() {
      chart.addPricePoint(50_000_000);
      assertEquals(1, chart.getPointCount());
    }

    @Test
    @DisplayName("Giá trị Y được chuyển đổi sang đơn vị triệu đúng")
    void yValueIsCorrectlyConvertedToMillions() {
      chart.addPricePoint(50_000_000); // 50 triệu
      DataPoint point = chart.getData().get(0);
      assertEquals(50.0, point.y(), 0.001);
    }

    @Test
    @DisplayName("Trục X tăng tuần tự bắt đầu từ 1")
    void xAxisIncrementsSequentiallyStartingFromOne() {
      chart.addPricePoint(10_000_000);
      chart.addPricePoint(20_000_000);
      chart.addPricePoint(30_000_000);
      assertEquals(1.0, chart.getData().get(0).x(), 0.001);
      assertEquals(2.0, chart.getData().get(1).x(), 0.001);
      assertEquals(3.0, chart.getData().get(2).x(), 0.001);
    }

    @Test
    @DisplayName("upperBound được cập nhật khi giá mới vượt mức cũ")
    void upperBoundUpdatesWhenNewPriceExceedsCurrent() {
      chart.addPricePoint(100_000_000); // 100 triệu
      double expectedUpper = 100.0 * 1.1;
      assertEquals(expectedUpper, chart.getUpperBound(), 0.001);
    }

    @Test
    @DisplayName("upperBound không thay đổi khi giá mới thấp hơn mức hiện tại")
    void upperBoundStaysSameWhenNewPriceIsLower() {
      chart.addPricePoint(100_000_000);
      double upperAfterFirst = chart.getUpperBound();
      chart.addPricePoint(50_000_000); // thấp hơn
      assertEquals(upperAfterFirst, chart.getUpperBound(), 0.001);
    }

    @Test
    @DisplayName("Cửa sổ X dịch chuyển sang phải khi số điểm vượt 20")
    void xWindowShiftsRightWhenPointsExceedTwenty() {
      for (int i = 1; i <= 21; i++) {
        chart.addPricePoint(i * 1_000_000.0);
      }
      assertEquals(1.0, chart.getXLower(), 0.001, "xLower phải là pointCount - 20");
      assertEquals(21.0, chart.getXUpper(), 0.001, "xUpper phải là pointCount");
    }

    @Test
    @DisplayName("Cửa sổ X không dịch chuyển khi số điểm bằng đúng 20")
    void xWindowDoesNotShiftWhenPointsEqualTwenty() {
      for (int i = 1; i <= 20; i++) {
        chart.addPricePoint(i * 1_000_000.0);
      }
      assertEquals(0.0, chart.getXLower(), 0.001, "Chưa đến 21 điểm nên xLower vẫn là 0");
    }
  }

  @Nested
  @DisplayName("updateWithBidHistory – Cập nhật từ lịch sử đấu giá")
  class UpdateWithBidHistoryTests {

    @Test
    @DisplayName("Dữ liệu cũ bị xóa sạch trước khi nạp lịch sử mới")
    void oldDataIsClearedBeforeLoadingNewHistory() {
      chart.addPricePoint(50_000_000);
      List<BidTransaction> history = List.of(makeBid(30_000_000));
      chart.updateWithBidHistory(history);
      assertEquals(1, chart.getData().size(),
          "Sau khi update, chỉ nên có đúng 1 điểm từ lịch sử mới");
    }

    @Test
    @DisplayName("Số điểm bằng số lần đấu giá trong lịch sử")
    void pointCountMatchesHistorySize() {
      List<BidTransaction> history = List.of(
          makeBid(10_000_000),
          makeBid(20_000_000),
          makeBid(30_000_000)
      );
      chart.updateWithBidHistory(history);
      assertEquals(3, chart.getPointCount());
      assertEquals(3, chart.getData().size());
    }

    @Test
    @DisplayName("Thêm một điểm rỗng (0,0) khi lịch sử là null")
    void addsEmptyPointWhenHistoryIsNull() {
      chart.updateWithBidHistory(null);
      assertEquals(1, chart.getData().size());
      assertEquals(0.0, chart.getData().get(0).x(), 0.001);
      assertEquals(0.0, chart.getData().get(0).y(), 0.001);
    }

    @Test
    @DisplayName("Thêm một điểm rỗng (0,0) khi lịch sử rỗng")
    void addsEmptyPointWhenHistoryIsEmpty() {
      chart.updateWithBidHistory(new ArrayList<>());
      assertEquals(1, chart.getData().size());
      assertEquals(0.0, chart.getData().get(0).y(), 0.001);
    }

    @Test
    @DisplayName("xUpper bằng pointCount + 2 sau khi nạp lịch sử")
    void xUpperEqualsPointCountPlusTwoAfterHistoryLoad() {
      List<BidTransaction> history = List.of(
          makeBid(10_000_000),
          makeBid(20_000_000)
      );
      chart.updateWithBidHistory(history);
      assertEquals(chart.getPointCount() + 2, chart.getXUpper(), 0.001);
    }

    @Test
    @DisplayName("upperBound được tính theo giá cao nhất nhân hệ số 1.1")
    void upperBoundIsCalculatedBasedOnMaxPrice() {
      List<BidTransaction> history = List.of(
          makeBid(10_000_000),
          makeBid(80_000_000), // cao nhất
          makeBid(50_000_000)
      );
      chart.updateWithBidHistory(history);
      // maxPrice = 80_000_000 → maxMillions = 80 → ceil(80)*1.1 = 88.0
      double expected = Math.ceil(80.0) * 1.1;
      assertEquals(expected, chart.getUpperBound(), 0.001);
    }

    @Test
    @DisplayName("Các giá trị Y được chuyển đổi đúng sang đơn vị triệu")
    void yValuesAreCorrectlyConvertedToMillions() {
      List<BidTransaction> history = List.of(
          makeBid(25_000_000),  // 25 triệu
          makeBid(150_000_000)  // 150 triệu
      );
      chart.updateWithBidHistory(history);
      assertEquals(25.0, chart.getData().get(0).y(), 0.001);
      assertEquals(150.0, chart.getData().get(1).y(), 0.001);
    }

    @Test
    @DisplayName("lowerBound luôn được reset về 0 sau khi nạp lịch sử")
    void lowerBoundIsAlwaysResetToZero() {
      List<BidTransaction> history = List.of(makeBid(10_000_000));
      chart.updateWithBidHistory(history);
      assertEquals(0.0, chart.getLowerBound(), 0.001);
    }
  }

  // ================================================================

  @Nested
  @DisplayName("addNewBid – Thêm lượt đấu giá mới thời gian thực")
  class AddNewBidTests {

    @Test
    @DisplayName("pointCount tăng lên 1 sau mỗi lượt đấu giá mới")
    void pointCountIncrementsAfterEachNewBid() {
      chart.addNewBid(makeBid(10_000_000));
      assertEquals(1, chart.getPointCount());
      chart.addNewBid(makeBid(20_000_000));
      assertEquals(2, chart.getPointCount());
    }

    @Test
    @DisplayName("Điểm mới được thêm vào cuối danh sách dữ liệu")
    void newPointIsAppendedToTheEnd() {
      chart.addNewBid(makeBid(10_000_000));
      chart.addNewBid(makeBid(30_000_000));
      assertEquals(2, chart.getData().size());
      assertEquals(30.0, chart.getData().get(1).y(), 0.001);
    }

    @Test
    @DisplayName("upperBound cập nhật khi lượt đấu giá mới có giá cao hơn")
    void upperBoundUpdatesWhenNewBidHasHigherPrice() {
      chart.addNewBid(makeBid(50_000_000));
      double prevUpper = chart.getUpperBound();
      chart.addNewBid(makeBid(200_000_000)); // 200 triệu > prevUpper
      assertTrue(chart.getUpperBound() > prevUpper,
          "upperBound phải tăng khi giá mới cao hơn");
    }

    @Test
    @DisplayName("upperBound không thay đổi khi lượt đấu giá mới có giá thấp hơn")
    void upperBoundStaysSameWhenNewBidHasLowerPrice() {
      chart.addNewBid(makeBid(200_000_000));
      double prevUpper = chart.getUpperBound();
      chart.addNewBid(makeBid(50_000_000)); // thấp hơn
      assertEquals(prevUpper, chart.getUpperBound(), 0.001);
    }
  }

  // ================================================================

  @Nested
  @DisplayName("clear – Xóa toàn bộ dữ liệu biểu đồ")
  class ClearTests {

    @Test
    @DisplayName("Sau khi clear, pointCount trở về 0")
    void pointCountResetsToZeroAfterClear() {
      chart.addPricePoint(10_000_000);
      chart.addPricePoint(20_000_000);
      chart.clear();
      assertEquals(0, chart.getPointCount());
    }

    @Test
    @DisplayName("Sau khi clear, danh sách dữ liệu chỉ chứa 1 điểm rỗng (0,0)")
    void dataContainsOnlyOneEmptyPointAfterClear() {
      chart.addPricePoint(10_000_000);
      chart.clear();
      assertEquals(1, chart.getData().size());
      assertEquals(0.0, chart.getData().get(0).x(), 0.001);
      assertEquals(0.0, chart.getData().get(0).y(), 0.001);
    }

    @Test
    @DisplayName("Sau khi clear, có thể thêm dữ liệu mới bình thường")
    void canAddNewDataNormallyAfterClear() {
      chart.addPricePoint(10_000_000);
      chart.clear();
      chart.addPricePoint(50_000_000);
      assertEquals(2, chart.getData().size(), // điểm rỗng + 1 điểm mới
          "Sau clear có 1 điểm rỗng, thêm 1 điểm mới → phải có 2");
      assertEquals(1, chart.getPointCount());
    }
  }

  // ================================================================

  @Nested
  @DisplayName("Chuyển đổi đơn vị giá – VND sang triệu VND")
  class PriceUnitConversionTests {

    @Test
    @DisplayName("1 tỷ VND = 1000 triệu VND")
    void oneBillionVndEqualsOneThousandMillionVnd() {
      chart.addPricePoint(1_000_000_000);
      assertEquals(1000.0, chart.getData().get(0).y(), 0.001);
    }

    @Test
    @DisplayName("500 nghìn VND = 0.5 triệu VND")
    void fiveHundredThousandVndEqualsHalfAMillionVnd() {
      chart.addPricePoint(500_000);
      assertEquals(0.5, chart.getData().get(0).y(), 0.001);
    }

    @Test
    @DisplayName("Giá 0 VND cho ra 0.0 triệu VND")
    void zeroVndResultsInZeroMillions() {
      chart.addPricePoint(0);
      assertEquals(0.0, chart.getData().get(0).y(), 0.001);
    }
  }
}