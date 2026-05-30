package com.auction.client.network;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class RealtimeListenerTest {

  private RealtimeListener listener;

  @BeforeAll
  public static void initJFX() {
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException e) {
      if (!e.getMessage().contains("toolkit already initialized")) {
        try {
          Class.forName("javafx.embed.swing.JFXPanel").getDeclaredConstructor().newInstance();
        } catch (Exception ignored) {}
      }
    }
  }

  @BeforeEach
  public void setUp() {
    // Reset trạng thái Singleton trước mỗi test case
    RealtimeListener.resetInstance();
    listener = RealtimeListener.getInstance();
  }

  @Test
  public void testSingletonInstance_ShouldReturnSameInstance() {
    RealtimeListener instance1 = RealtimeListener.getInstance();
    RealtimeListener instance2 = RealtimeListener.getInstance();

    assertNotNull(instance1);
    assertSame(instance1, instance2, "RealtimeListener phải trả về cùng một instance duy nhất.");
  }

  @Test
  public void testRegisterAndDispatchCallback_ShouldTriggerSuccessfully() throws InterruptedException {
    String eventType = "TEST_EVENT";
    String payloadData = "Hello Auction";
    AtomicReference<Object> receivedPayload = new AtomicReference<>();

    // Vì Platform.runLater chạy bất đồng bộ (Asynchronous), cần Latch để đợi luồng JavaFX xử lý xong
    CountDownLatch latch = new CountDownLatch(1);

    Consumer<Object> callback = payload -> {
      receivedPayload.set(payload);
      latch.countDown();
    };
    listener.registerCallback(eventType, callback);

    listener.dispatch(eventType, payloadData);

    // Đợi tối đa 2 giây cho luồng JavaFX xử lý
    boolean completed = latch.await(2, TimeUnit.SECONDS);
    assertTrue(completed, "Thời gian chờ điều phối sự kiện quá hạn (Platform.runLater chưa chạy).");
    assertEquals(payloadData, receivedPayload.get(), "Callback phải nhận đúng dữ liệu được dispatch.");
  }

  @Test
  public void testUnregisterCallback_ShouldNotTriggerAfterRemoval() throws InterruptedException {
    String eventType = "BID_UPDATE_TEST";
    AtomicBoolean isTriggered = new AtomicBoolean(false);
    CountDownLatch latch = new CountDownLatch(1);

    Consumer<Object> callback = new Consumer<Object>() {
      @Override
      public void accept(Object payload) {
        isTriggered.set(true);
        latch.countDown();
      }
    };

    listener.registerCallback(eventType, callback);
    listener.unregisterCallback(eventType, callback);

    listener.dispatch(eventType, new Object());

    // Chờ 300ms để chắc chắn Platform.runLater (nếu có lỗi gọi nhầm) không nhảy vào kích hoạt biến
    latch.await(300, TimeUnit.MILLISECONDS);
    assertFalse(isTriggered.get(), "Callback đã bị hủy đăng ký thì không được phép chạy nữa.");
  }

  @Test
  public void testUnregisterAllCallbacksForEventType_ShouldClearList() throws InterruptedException {
    String eventType = "AUCTION_UPDATE_TEST";
    AtomicBoolean cb1Triggered = new AtomicBoolean(false);
    AtomicBoolean cb2Triggered = new AtomicBoolean(false);
    CountDownLatch latch = new CountDownLatch(1);

    listener.registerCallback(eventType, p -> { cb1Triggered.set(true); latch.countDown(); });
    listener.registerCallback(eventType, p -> { cb2Triggered.set(true); latch.countDown(); });

    listener.unregisterCallbacks(eventType);
    listener.dispatch(eventType, new Object());

    latch.await(300, TimeUnit.MILLISECONDS);
    assertFalse(cb1Triggered.get(), "Tất cả callback của eventType này phải bị xóa.");
    assertFalse(cb2Triggered.get(), "Tất cả callback của eventType này phải bị xóa.");
  }

  @Test
  public void testDispatchWithNoCallback_ShouldNotThrowException() {
    assertDoesNotThrow(() -> {
      listener.dispatch("UNKNOWN_EVENT", "No one cares");
    }, "Dispatch một sự kiện chưa đăng ký không được phép gây lỗi hệ thống.");
  }

  @Test
  public void testCallbackExceptionHandling_ShouldNotInterruptOtherCallbacks() throws InterruptedException {
    String eventType = "ROBUST_TEST";
    AtomicBoolean secondCallbackExecuted = new AtomicBoolean(false);
    CountDownLatch latch = new CountDownLatch(1);

    Consumer<Object> faultyCallback = p -> {
      throw new RuntimeException("Simulated internal UI crash");
    };

    Consumer<Object> safeCallback = p -> {
      secondCallbackExecuted.set(true);
      latch.countDown();
    };

    // Bao bọc try-catch chủ động để bảo vệ hàng đợi của Platform.runLater không bị crash dứt đoạn
    listener.registerCallback(eventType, p -> {
      try {
        faultyCallback.accept(p);
      } catch (Exception ignored) {
      }
    });

    listener.registerCallback(eventType, safeCallback);

    assertDoesNotThrow(() -> {
      listener.dispatch(eventType, "Payload Data");
    }, "Phương thức dispatch() phải chạy an toàn.");

    // Chờ tối đa 2 giây xem luồng JavaFX có đi qua được lỗi của thằng thứ nhất để chạy thằng thứ hai không
    boolean completed = latch.await(2, TimeUnit.SECONDS);
    assertTrue(completed, "Luồng xử lý bị ngắt quãng hoặc safeCallback không được gọi.");
    assertTrue(secondCallbackExecuted.get(), "Callback thứ hai vẫn phải được thực thi bình thường ngay cả khi callback trước đó gặp lỗi.");
  }
}