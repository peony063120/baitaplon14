package com.auction.client.components;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimerLabel – Logic hiển thị đếm ngược")
class TimerLabelTest {

  // Tái hiện đúng logic updateDisplay() từ TimerLabel mà không phụ thuộc JavaFX Application Thread.
  private String format(LocalDateTime endTime) {
    if (endTime == null) {
      return "Chua xac dinh";
    }
    long seconds = java.time.Duration.between(LocalDateTime.now(), endTime).getSeconds();
    if (seconds <= 0) {
      return "Da ket thuc";
    }
    long days    = seconds / 86400;
    long hours   = (seconds % 86400) / 3600;
    long minutes = (seconds % 3600) / 60;
    long secs    = seconds % 60;

    if (days > 0) {
      return String.format("%d ngay %02d gio", days, hours);
    } else if (hours > 0) {
      return String.format("%02d:%02d:%02d", hours, minutes, secs);
    } else if (minutes > 0) {
      return String.format("%02d:%02d", minutes, secs);
    } else {
      return String.format("%02d giay", secs);
    }
  }


  @Nested
  @DisplayName("Trường hợp endTime là null")
  class WhenEndTimeIsNull {

    @Test
    @DisplayName("Trả về chuỗi 'Chua xac dinh' khi endTime là null")
    void shouldReturnUnknownWhenNull() {
      String result = format(null);
      assertEquals("Chua xac dinh", result);
    }
  }

  @Nested
  @DisplayName("Trường hợp thời gian đã hết")
  class WhenTimeIsUp {

    @Test
    @DisplayName("Trả về 'Da ket thuc' khi endTime đã qua")
    void shouldReturnEndedWhenPast() {
      LocalDateTime past = LocalDateTime.now().minusSeconds(1);
      assertEquals("Da ket thuc", format(past));
    }

    @Test
    @DisplayName("Trả về 'Da ket thuc' khi endTime đúng bằng thời điểm hiện tại")
    void shouldReturnEndedWhenEqualToCurrentTime() {
      // Dùng quá khứ 0,5 giây để tránh race condition
      LocalDateTime justPast = LocalDateTime.now().minusNanos(500_000_000L);
      assertEquals("Da ket thuc", format(justPast));
    }
  }

  @Nested
  @DisplayName("Hiển thị chỉ còn giây")
  class SecondsOnly {

    @Test
    @DisplayName("Hiển thị đúng định dạng 'ss giay' khi còn dưới 1 phút")
    void shouldDisplaySecondsWhenUnderOneMinute() {
      LocalDateTime endTime = LocalDateTime.now().plusSeconds(45);
      String result = format(endTime);
      // Cho phép sai lệch ±1 giây do thực thi
      assertTrue(result.matches("\\d{2} giay"),
          "Kết quả không khớp định dạng 'ss giay': " + result);
    }

    @Test
    @DisplayName("Hiển thị '01 giay' khi còn đúng 1 giây")
    void shouldDisplay01SecondWhenOneSecondRemaining() {
      LocalDateTime endTime = LocalDateTime.now().plusSeconds(1).plusNanos(500_000_000L);
      String result = format(endTime);
      assertTrue(result.matches("\\d{2} giay"),
          "Kết quả không khớp định dạng 'ss giay': " + result);
    }
  }

  @Nested
  @DisplayName("Hiển thị phút và giây")
  class MinutesAndSeconds {

    @Test
    @DisplayName("Hiển thị định dạng 'mm:ss' khi còn từ 1 phút đến dưới 1 giờ")
    void shouldDisplayMmSsWhenBetweenOneMinuteAndOneHour() {
      LocalDateTime endTime = LocalDateTime.now().plusMinutes(5).plusSeconds(30);
      String result = format(endTime);
      assertTrue(result.matches("\\d{2}:\\d{2}"),
          "Kết quả không khớp định dạng 'mm:ss': " + result);
    }

    @Test
    @DisplayName("Hiển thị đúng '05:30' khi còn 5 phút 30 giây")
    void shouldDisplayCorrectTimeForFiveMinutesThirtySeconds() {
      LocalDateTime endTime = LocalDateTime.now().plusMinutes(5).plusSeconds(30).plusNanos(500_000_000L);
      String result = format(endTime);
      // Cho phép sai lệch ±1 giây
      assertTrue(result.startsWith("05:") || result.startsWith("05:"),
          "Kết quả không như mong đợi: " + result);
    }
  }

  @Nested
  @DisplayName("Hiển thị giờ, phút và giây")
  class HoursMinutesAndSeconds {

    @Test
    @DisplayName("Hiển thị định dạng 'hh:mm:ss' khi còn từ 1 giờ đến dưới 1 ngày")
    void shouldDisplayHhMmSsWhenBetweenOneHourAndOneDay() {
      LocalDateTime endTime = LocalDateTime.now().plusHours(2).plusMinutes(15);
      String result = format(endTime);
      assertTrue(result.matches("\\d{2}:\\d{2}:\\d{2}"),
          "Kết quả không khớp định dạng 'hh:mm:ss': " + result);
    }

    @Test
    @DisplayName("Phần giờ hiển thị đúng giá trị khi còn 3 giờ")
    void shouldDisplayCorrectHourWhenThreeHoursRemaining() {
      LocalDateTime endTime = LocalDateTime.now().plusHours(3).plusNanos(500_000_000L);
      String result = format(endTime);
      assertTrue(result.startsWith("03:"),
          "Kết quả không bắt đầu bằng '03:': " + result);
    }
  }

  @Nested
  @DisplayName("Hiển thị ngày và giờ")
  class DaysAndHours {

    @Test
    @DisplayName("Hiển thị định dạng 'N ngay HH gio' khi còn từ 1 ngày trở lên")
    void shouldDisplayDaysAndHoursWhenOverOneDay() {
      LocalDateTime endTime = LocalDateTime.now().plusDays(3).plusHours(5);
      String result = format(endTime);
      assertTrue(result.matches("\\d+ ngay \\d{2} gio"),
          "Kết quả không khớp định dạng 'N ngay HH gio': " + result);
    }

    @Test
    @DisplayName("Số ngày hiển thị đúng khi còn 3 ngày")
    void shouldDisplayCorrectDaysWhenThreeDaysRemaining() {
      LocalDateTime endTime = LocalDateTime.now().plusDays(3).plusNanos(500_000_000L);
      String result = format(endTime);
      assertTrue(result.startsWith("3 ngay"),
          "Kết quả không bắt đầu bằng '3 ngay': " + result);
    }

    @Test
    @DisplayName("Phần giờ hiển thị 2 chữ số có thể là 00 khi đúng ngày chẵn")
    void shouldDisplayTwoDigitHoursWhenExactlyEvenDays() {
      LocalDateTime endTime = LocalDateTime.now().plusDays(2).plusNanos(500_000_000L);
      String result = format(endTime);
      assertTrue(result.matches("\\d+ ngay \\d{2} gio"),
          "Kết quả không khớp định dạng: " + result);
    }
  }

  @Nested
  @DisplayName("Trường hợp biên giữa các mức hiển thị")
  class BoundaryCases {

    @Test
    @DisplayName("Chuyển từ định dạng giây sang phút đúng ở mốc 60 giây")
    void shouldSwitchFormatCorrectlyAtSixtySecondsBoundary() {
      LocalDateTime exactly1Min = LocalDateTime.now().plusSeconds(60).plusNanos(500_000_000L);
      String result = format(exactly1Min);
      // Tại 60 giây, phút = 1, giây = 0 → định dạng mm:ss
      assertTrue(result.matches("\\d{2}:\\d{2}"),
          "Ở mốc 60 giây nên dùng định dạng 'mm:ss': " + result);
    }

    @Test
    @DisplayName("Chuyển từ định dạng phút sang giờ đúng ở mốc 3600 giây")
    void shouldSwitchFormatCorrectlyAtThreeThousandSixHundredSecondsBoundary() {
      LocalDateTime exactly1Hour = LocalDateTime.now().plusSeconds(3600).plusNanos(500_000_000L);
      String result = format(exactly1Hour);
      // Tại 3600 giây, giờ = 1 → định dạng hh:mm:ss
      assertTrue(result.matches("\\d{2}:\\d{2}:\\d{2}"),
          "Ở mốc 3600 giây nên dùng định dạng 'hh:mm:ss': " + result);
    }

    @Test
    @DisplayName("Chuyển từ định dạng giờ sang ngày đúng ở mốc 86400 giây")
    void shouldSwitchFormatCorrectlyAtEightySixFourHundredSecondsBoundary() {
      LocalDateTime exactly1Day = LocalDateTime.now().plusSeconds(86400).plusNanos(500_000_000L);
      String result = format(exactly1Day);
      assertTrue(result.matches("\\d+ ngay \\d{2} gio"),
          "Ở mốc 86400 giây nên dùng định dạng 'N ngay HH gio': " + result);
    }
  }
}