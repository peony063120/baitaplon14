package com.auction.client.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

  private Session session;

  @BeforeEach
  void setUp() {
    session = new Session("session-123", "user-abc");
  }

  @Test
  void testSessionCreationWithArguments() {
    // Kiểm tra khởi tạo session đầy đủ tham số xem các getter có trả về đúng giá trị không
    assertEquals("session-123", session.getSessionId());
    assertEquals("user-abc", session.getUserId());
    assertNotNull(session.getCreatedAt());
  }

  @Test
  void testDefaultConstructor() {
    // Kiểm tra constructor mặc định xem có tự động cấu hình thời gian tạo không
    Session emptySession = new Session();
    assertNotNull(emptySession.getCreatedAt());
    assertNull(emptySession.getSessionId());
  }

  @Test
  void testIsValidWhenValid() {
    // Kiểm tra session phải hợp lệ ngay khi vừa mới được tạo ra
    assertTrue(session.isValid());
  }

  @Test
  void testIsValidWhenExpired() {
    // Kiểm tra session hết hạn: giả lập thời gian tạo là 25 tiếng trước
    session.setCreatedAt(LocalDateTime.now().minusHours(25));
    assertFalse(session.isValid());
  }

  @Test
  void testIsValidWithNullFields() {
    // Kiểm tra session không hợp lệ nếu thiếu các thông tin cốt lõi (null)
    session.setSessionId(null);
    assertFalse(session.isValid());
  }
}