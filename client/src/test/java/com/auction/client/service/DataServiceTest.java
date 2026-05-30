package com.auction.client.service;

import com.auction.client.config.AppConfig;
import com.auction.client.network.ServerConnection;
import com.auction.common.dto.AuctionDTO;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class DataServiceTest {

  private ServerConnection originalInstance;

  @BeforeAll
  public static void initJavaFX() {
    // Khởi chạy môi trường JavaFX ngầm (Headless) phục vụ Unit Test
    // Giúp loại bỏ hoàn toàn lỗi "Toolkit not initialized" khi chạy Platform.runLater()
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException e) {
      // Nếu Toolkit đã được khởi tạo trước đó bởi TestFX, bỏ qua ngoại lệ này
    }
  }

  @BeforeEach
  public void setUp() throws Exception {
    originalInstance = ServerConnection.getInstance();
  }

  @AfterEach
  public void tearDown() throws Exception {
    // Trả lại môi trường sạch sẽ sau mỗi ca kiểm thử
    setPrivateStaticField(ServerConnection.class, "instance", originalInstance);
  }

  // Tạo thực thể thật qua Reflection và bẻ gãy Socket để tạo kịch bản mất mạng
  private ServerConnection createNetworkFailureStub() throws Exception {
    Constructor<?>[] constructors = ServerConnection.class.getDeclaredConstructors();
    if (constructors.length == 0) {
      throw new IllegalStateException("Không tìm thấy Constructor!");
    }
    Constructor<?> targetConstructor = constructors[0];
    targetConstructor.setAccessible(true);

    Object[] args = new Object[targetConstructor.getParameterCount()];
    Class<?>[] paramTypes = targetConstructor.getParameterTypes();
    for (int i = 0; i < args.length; i++) {
      if (paramTypes[i] == int.class || paramTypes[i] == Integer.class) args[i] = 0;
      else if (paramTypes[i] == boolean.class || paramTypes[i] == Boolean.class) args[i] = false;
      else args[i] = null;
    }

    ServerConnection brokenConnection = (ServerConnection) targetConstructor.newInstance(args);

    // Bẻ gãy các trường truyền thông nội bộ để ép sendRequest văng lỗi IOException
    String[] potentialFieldNames = {"socket", "clientSocket", "out", "writer", "in", "reader"};
    for (String fieldName : potentialFieldNames) {
      try {
        Field field = ServerConnection.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(brokenConnection, null);
      } catch (NoSuchFieldException e) {
        // Bỏ qua nếu class không định nghĩa trường mang tên này
      }
    }

    return brokenConnection;
  }

  // ============================ CÁC BÀI KIỂM THỬ (TEST CASES) ============================

  @Test
  public void testLoadAuctions_WhenUseMockIsTrue_ShouldReturnMockDataDirectly() throws Exception {
    if (AppConfig.USE_MOCK) {
      CountDownLatch latch = new CountDownLatch(1);
      AtomicReference<List<AuctionDTO>> resultRef = new AtomicReference<>();

      DataService.getInstance().loadAuctions(
          auctions -> {
            resultRef.set(auctions);
            latch.countDown();
          },
          error -> {
            latch.countDown();
            fail("Không được phép báo lỗi khi đang chạy chế độ dữ liệu Mock");
          }
      );

      latch.await(2, TimeUnit.SECONDS);
      assertNotNull(resultRef.get());
      assertFalse(resultRef.get().isEmpty());
    } else {
      System.out.println("Bỏ qua test vì AppConfig.USE_MOCK đang là false.");
    }
  }

  @Test
  public void testLoadAuctions_WhenNetworkFailsAndAutoFallbackIsTrue_ShouldReturnMockData() throws Exception {
    ServerConnection stubConnection = createNetworkFailureStub();
    setPrivateStaticField(ServerConnection.class, "instance", stubConnection);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<List<AuctionDTO>> resultRef = new AtomicReference<>();

    DataService.getInstance().loadAuctions(
        auctions -> {
          resultRef.set(auctions);
          latch.countDown();
        },
        error -> {
          latch.countDown();
          if (AppConfig.AUTO_FALLBACK) {
            fail("Hệ thống phải tự động fallback về dữ liệu mock chứ không được đẩy lỗi ra UI");
          }
        }
    );

    latch.await(2, TimeUnit.SECONDS);

    if (AppConfig.AUTO_FALLBACK) {
      assertNotNull(resultRef.get());
      assertEquals(6, resultRef.get().size());
    }
  }

  @Test
  public void testLoadAuctions_WhenNetworkFailsAndAutoFallbackIsFalse_ShouldCallOnError() throws Exception {
    ServerConnection stubConnection = createNetworkFailureStub();
    setPrivateStaticField(ServerConnection.class, "instance", stubConnection);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<String> errorRef = new AtomicReference<>();

    DataService.getInstance().loadAuctions(
        auctions -> {
          latch.countDown();
          if (!AppConfig.AUTO_FALLBACK) {
            fail("Mạng lỗi và không có fallback thì không thể kích hoạt luồng thành công");
          }
        },
        errorMessage -> {
          errorRef.set(errorMessage);
          latch.countDown();
        }
    );

    latch.await(2, TimeUnit.SECONDS);

    if (!AppConfig.AUTO_FALLBACK) {
      assertNotNull(errorRef.get());
    }
  }

  private void setPrivateStaticField(Class<?> clazz, String fieldName, Object newValue) throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(null, newValue);
  }
}