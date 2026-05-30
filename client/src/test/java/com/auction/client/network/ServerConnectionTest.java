package com.auction.client.network;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ServerConnectionTest {

  private ServerConnection serverConnection;
  private ServerSocket mockServerSocket;
  private Thread mockServerThread;
  private int mockPort;
  private volatile boolean isServerRunning = true;

  @BeforeEach
  public void setUp() throws IOException {
    ServerConnection.resetInstance();
    serverConnection = ServerConnection.getInstance();
    isServerRunning = true;

    // Khởi tạo một ServerSocket cục bộ ngẫu nhiên cổng để làm Mock Server
    mockServerSocket = new ServerSocket(0);
    mockPort = mockServerSocket.getLocalPort();
  }

  @AfterEach
  public void tearDown() throws IOException {
    isServerRunning = false;
    if (serverConnection != null) {
      serverConnection.disconnect();
    }
    if (mockServerSocket != null && !mockServerSocket.isClosed()) {
      mockServerSocket.close();
    }
    ServerConnection.resetInstance();
  }

  /**
   * Hàm tiện ích tạo máy chủ giả lập phản hồi tự động theo kịch bản để test sendRequest()
   */
  private void startMockServer(String expectedResponse) {
    mockServerThread = new Thread(() -> {
      try (Socket clientSocket = mockServerSocket.accept();
           BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
           PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

        String inputLine;
        if (isServerRunning && (inputLine = in.readLine()) != null) {
          // Khi nhận được dòng dữ liệu từ Client, Server gửi ngay chuỗi phản hồi giả lập
          out.println(expectedResponse);
        }
      } catch (IOException e) {
        // Đóng kết nối an toàn khi kết thúc test
      }
    });
    mockServerThread.setDaemon(true);
    mockServerThread.start();
  }

  @Test
  public void testSingletonInstance_ShouldReturnSameInstance() {
    ServerConnection instance1 = ServerConnection.getInstance();
    ServerConnection instance2 = ServerConnection.getInstance();
    assertNotNull(instance1);
    assertSame(instance1, instance2, "ServerConnection phải áp dụng đúng mẫu Singleton.");
  }

  @Test
  public void testConnectAndDisconnect_ShouldChangeConnectionState() throws IOException {
    // Khởi động luồng server để chấp nhận kết nối suông
    startMockServer("");

    assertFalse(serverConnection.isConnected(), "Ban đầu trạng thái connected phải là false.");

    serverConnection.connect("localhost", mockPort);
    assertTrue(serverConnection.isConnected(), "Sau khi gọi connect(), trạng thái phải là true.");

    serverConnection.disconnect();
    assertFalse(serverConnection.isConnected(), "Sau khi gọi disconnect(), trạng thái phải về false.");
  }

  @Test
  public void testSendRequest_ShouldReturnExpectedRawJsonFromServer() throws Exception {
    // Giả lập chuỗi JSON phản hồi khớp requestId
    String testRequestId = "req-12345";
    String mockServerResponse = "{\"requestId\":\"" + testRequestId + "\",\"status\":\"SUCCESS\",\"payload\":\"Data\"}";

    startMockServer(mockServerResponse);
    serverConnection.connect("localhost", mockPort);

    // Chuẩn bị map thông điệp gửi đi
    Map<String, Object> requestMessage = new HashMap<>();
    requestMessage.put("requestId", testRequestId);
    requestMessage.put("command", "FETCH_AUCTIONS");

    String response = serverConnection.sendRequest(requestMessage);

    assertNotNull(response);
    assertTrue(response.contains("SUCCESS"), "Phản hồi nhận được phải chứa dữ liệu khớp từ máy chủ.");
    assertTrue(response.contains(testRequestId), "Mã requestId nhận về phải trùng khớp.");
  }

  @Test
  public void testSendRequestWithoutConnection_ShouldThrowIOException() {
    // Kiểm tra việc gửi tin khi chưa kết nối mạng
    Map<String, Object> requestMessage = new HashMap<>();
    requestMessage.put("requestId", "123");

    assertThrows(IOException.class, () -> {
      serverConnection.sendRequest(requestMessage);
    }, "Phải ném ra lỗi IOException nếu cố tình gọi gửi yêu cầu khi chưa kết nối mạng.");
  }

  @Test
  public void testSendRequestTimeout_ShouldThrowIOException() throws IOException {
    // Kiểm tra kịch bản máy chủ bị treo hoặc không phản hồi dữ liệu về dẫn đến Timeout
    // Tạo Mock Server không làm gì cả (không ghi phản hồi phản hồi)
    Thread silentServerThread = new Thread(() -> {
      try (Socket clientSocket = mockServerSocket.accept()) {
        Thread.sleep(2000);
      } catch (Exception e) {}
    });
    silentServerThread.start();

    serverConnection.connect("localhost", mockPort);

    Map<String, Object> requestMessage = new HashMap<>();
    requestMessage.put("requestId", "timeout-req");
    requestMessage.put("command", "SLOW_COMMAND");

    // Để tránh build kiểm thử (CI/CD) bị treo cứng đúng 10 giây theo code gốc, bọc kiểm tra assert Throws ngoại lệ IO
    // Test case này sẽ mất tối đa 10 giây để chạy xong do cấu hình TimeUnit.SECONDS trong mã nguồn ServerConnection.
    assertThrows(IOException.class, () -> {
      serverConnection.sendRequest(requestMessage);
    }, "Phải ném ra lỗi IOException do quá thời gian chờ phản hồi (Timeout).");
  }
}