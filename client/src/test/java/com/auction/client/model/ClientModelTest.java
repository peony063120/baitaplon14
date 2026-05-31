package com.auction.client.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class ClientModelTest {

  private ClientModel clientModel;

  @BeforeEach
  void setUp() {
    clientModel = ClientModel.getInstance();
    // Reset trạng thái Singleton để tránh lem dữ liệu giữa các bài test
    clientModel.logout();
  }

  @Test
  void testSingletonInstance() {
    // Xác minh thiết kế Pattern Singleton: hai lần lấy thực thể phải là một cấu trúc ô nhớ duy nhất
    ClientModel instance2 = ClientModel.getInstance();
    assertSame(clientModel, instance2);
  }

  @Test
  void testLoginWithEmptyOrNullCredentials() {
    // Kiểm tra việc đăng nhập phải thất bại lập tức nếu bỏ trống tài khoản hoặc mật khẩu
    assertFalse(clientModel.login("", "password"));
    assertFalse(clientModel.login("user", null));
  }

  @Test
  void testLoginSuccessCreatesSession() {
    // Kiểm tra logic tạm thời hiện tại: Đăng nhập thành công thì phải khởi tạo session tự động
    boolean result = clientModel.login("testUser", "password123");
    assertTrue(result);
    assertNotNull(clientModel.getSession());
  }

  @Test
  void testLogoutClearsSessionAndUser() {
    // Kiểm tra hàm đăng xuất phải xóa sạch cả thông tin User lẫn Session hiện hành
    clientModel.login("testUser", "password");
    clientModel.logout();

    assertNull(clientModel.getCurrentUser());
    assertNull(clientModel.getSession());
  }

    @Test
    void testRegisterWithNullDto() throws IOException {
        assertFalse(clientModel.register(null));
    }

  @Test
  void testSetSessionWithDetailsAndGetBalance() {
    // Kiểm tra việc nạp session thủ công bằng payload chi tiết và ép kiểu lấy số dư tài khoản người đấu giá (Bidder)
    clientModel.setSession("token-xyz", "user-id-01", "buyer01", "BIDDER", 500.50);

    assertNotNull(clientModel.getSession());
    assertEquals("user-id-01", clientModel.getSession().getUserId());
    assertEquals(500.50, clientModel.getBalance());
  }
}