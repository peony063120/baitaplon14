package com.auction.client.network;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * ServerConnection — Quản lý kết nối persistent TCP socket duy nhất giữa
 * ứng dụng JavaFX Client và máy chủ đấu giá (Auction Server).
 * QUAN TRỌNG — Quy tắc sử dụng sendRequest():
 * sendRequest() sẽ block luồng gọi tối đa 10 giây chờ phản hồi từ server.
 * Tuyệt đối KHÔNG gọi phương thức này từ JavaFX Application Thread (
 * không gọi trực tiếp trong setOnAction, initialize, hay bất kỳ callback UI nào).
 * Luôn bọc trong javafx.concurrent.Task hoặc ExecutorService
 */
public class ServerConnection {

  private static final Logger LOGGER = Logger.getLogger(ServerConnection.class.getName());
  private static volatile ServerConnection instance;

  public static ServerConnection getInstance() {
    if (instance == null) {
      synchronized (ServerConnection.class) {
        if (instance == null) {
          instance = new ServerConnection();
        }
      }
    }
    return instance;
  }

  /**
   * Hủy bỏ Singleton instance hiện tại và ngắt kết nối socket.
   * Gọi phương thức này trước khi tạo kết nối mới (sau khi mạng bị
   * mất và muốn reconnect), hoặc khi ứng dụng tắt.
   */
  public static synchronized void resetInstance() {
    if (instance != null) {
      instance.disconnect();
      instance = null;
      // CHANGED: "ServerConnection: Instance đã được reset — sẵn sàng kết nối lại." -> "ServerConnection: Instance reset successfully — ready for reconnection."
      LOGGER.info("ServerConnection: Instance reset successfully — ready for reconnection.");
    }
  }

  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;

  private final MessageProtocol protocol;
  private final RealtimeListener realtimeListener;
  private ExecutorService listenerThread;

  private volatile boolean connected = false;

  /** Theo dõi các yêu cầu đồng bộ đang chờ dòng phản hồi tương ứng từ phía máy chủ (Server). */
  private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

  private ServerConnection() {
    this.protocol         = new MessageProtocol();
    this.realtimeListener = RealtimeListener.getInstance();
  }

  /**
   * Thiết lập kết nối Socket đến máy chủ và khởi chạy luồng phụ lắng nghe dữ liệu ngầm.
   * @param host Địa chỉ máy chủ
   * @param port Cổng kết nối
   * @throws IOException Nếu không thể thiết lập kết nối
   */
  public synchronized void connect(String host, int port) throws IOException {
    if (connected) {
      // CHANGED: "ServerConnection: Đã kết nối — bỏ qua yêu cầu connect()." -> "ServerConnection: Already connected — ignoring connect() request."
      LOGGER.warning("ServerConnection: Already connected — ignoring connect() request.");
      return;
    }

    socket = new Socket(host, port);
    out    = new PrintWriter(socket.getOutputStream(), true);
    in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    connected = true;

    // CHANGED: "ServerConnection: Kết nối thành công đến máy chủ tại " -> "ServerConnection: Successfully connected to server at "
    LOGGER.info("ServerConnection: Successfully connected to server at " + host + ":" + port);

    // Tạo luồng Daemon chạy ngầm không gây treo ứng dụng khi tắt Client
    listenerThread = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "auction-server-listener");
      t.setDaemon(true);
      return t;
    });
    listenerThread.submit(this::listenLoop);
  }

  /**
   * Thực hiện ngắt kết nối an toàn, đóng Socket và giải phóng tài nguyên hệ thống.
   * Sau khi gọi disconnect(), phải gọi resetInstance() rồi mới có thể connect() lại.
   */
  public void disconnect() {
    connected = false;
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      // CHANGED: "ServerConnection: Lỗi xảy ra khi ngắt kết nối — " -> "ServerConnection: Error occurred while disconnecting — "
      LOGGER.warning("ServerConnection: Error occurred while disconnecting — " + e.getMessage());
    }
    if (listenerThread != null) {
      listenerThread.shutdownNow();
    }
    // Hủy bỏ toàn bộ các yêu cầu đồng bộ đang chờ phản hồi để tránh rò rỉ bộ nhớ
    pendingRequests.forEach((id, future) -> future.cancel(true));
    pendingRequests.clear();
    // CHANGED: "ServerConnection: Đã ngắt kết nối và giải phóng các yêu cầu đang chờ." -> "ServerConnection: Disconnected and cleared pending requests."
    LOGGER.info("ServerConnection: Disconnected and cleared pending requests.");
  }

  /**
   * Gửi một yêu cầu đến server và chặn luồng (block) đồng bộ cho đến khi nhận được
   * phản hồi khớp dữ liệu hoặc hết thời gian chờ (timeout).
   * Phương thức này BLOCK luồng gọi tối đa 10 giây.
   * Tuyệt đối KHÔNG gọi từ JavaFX Application Thread — giao diện sẽ đóng băng.
   * Luôn gọi từ background thread (javafx.concurrent.Task hoặc ExecutorService).
   *
   * @param requestMessage Bản đồ thông điệp được tạo qua {@link MessageProtocol#buildRequestMessage}
   * @return Chuỗi định dạng JSON thô nhận được từ server
   * @throws IOException Nếu mất kết nối mạng, lỗi truyền tải hoặc hết thời gian chờ (timeout)
   */
  public String sendRequest(Map<String, Object> requestMessage) throws IOException {
    if (!connected || out == null) {
      // CHANGED: "ServerConnection: Không thể gửi yêu cầu — Mạng bị ngắt kết nối." -> "ServerConnection: Cannot send request — Network disconnected."
      throw new IOException("ServerConnection: Cannot send request — Network disconnected.");
    }

    String requestId = (String) requestMessage.get("requestId");
    String rawJson   = protocol.encode(requestMessage);

    CompletableFuture<String> responseFuture = new CompletableFuture<>();
    if (requestId != null) {
      pendingRequests.put(requestId, responseFuture);
    }

    // Đảm bảo đồng bộ hóa đầu ra ghi dữ liệu tránh xung đột luồng ghi
    synchronized (out) {
      out.println(rawJson);
      LOGGER.fine("ServerConnection -> SERVER: " + rawJson);
    }

    if (requestId == null) {
      return ""; // Không cần chờ phản hồi đối với các thông báo một chiều
    }

    try {
      // Chặn luồng và đợi phản hồi từ server với thời gian tối đa là 10 giây
      return responseFuture.get(10, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      pendingRequests.remove(requestId);
      // CHANGED: "ServerConnection: Hết thời gian chờ phản hồi từ Server cho mã ID: " -> "ServerConnection: Request timeout waiting for server response for ID: "
      throw new IOException("ServerConnection: Request timeout waiting for server response for ID: " + requestId);
    } catch (InterruptedException | ExecutionException e) {
      pendingRequests.remove(requestId);
      // CHANGED: "ServerConnection: Yêu cầu bị gián đoạn hoặc quá trình xử lý thất bại: " -> "ServerConnection: Request interrupted or processing failed: "
      throw new IOException("ServerConnection: Request interrupted or processing failed: " + e.getMessage());
    }
  }

  /**
   * Gửi yêu cầu đơn giản dạng text (không dùng Map)
   * Phương thức này BLOCK luồng gọi.
   */
  public String sendRequest(String command) throws IOException {
    if (!connected || out == null) {
      // CHANGED: "ServerConnection: Không thể gửi yêu cầu — Mạng bị ngắt kết nối." -> "ServerConnection: Cannot send request — Network disconnected."
      throw new IOException("ServerConnection: Cannot send request — Network disconnected.");
    }

    String requestId = java.util.UUID.randomUUID().toString();
    CompletableFuture<String> responseFuture = new CompletableFuture<>();
    pendingRequests.put(requestId, responseFuture);

    // Gửi raw text command (text protocol) — server mong đợi định dạng "COMMAND:payload"
    synchronized (out) {
      out.println(command);
      LOGGER.fine("ServerConnection -> SERVER (text): " + command);
    }

    try {
      return responseFuture.get(10, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      pendingRequests.remove(requestId);
      // CHANGED: "ServerConnection: Hết thời gian chờ phản hồi từ Server" -> "ServerConnection: Response timeout from server"
      throw new IOException("ServerConnection: Response timeout from server");
    } catch (InterruptedException | ExecutionException e) {
      pendingRequests.remove(requestId);
      // CHANGED: "ServerConnection: Yêu cầu bị gián đoạn: " -> "ServerConnection: Request interrupted: "
      throw new IOException("ServerConnection: Request interrupted: " + e.getMessage());
    }
  }

  /**
   * Vòng lặp chạy ngầm liên tục đọc dữ liệu theo dòng từ luồng nhận của Socket.
   */
  private void listenLoop() {
    // CHANGED: "ServerConnection: Luồng lắng nghe (Listener thread) đã khởi động." -> "ServerConnection: Listener thread started."
    LOGGER.info("ServerConnection: Listener thread started.");
    try {
      String line;
      while (connected && (line = in.readLine()) != null) {
        handleIncoming(line);
      }
    } catch (IOException e) {
      if (connected) {
        // CHANGED: "ServerConnection: Kết nối bị ngắt đột ngột — " -> "ServerConnection: Connection lost abruptly — "
        LOGGER.warning("ServerConnection: Connection lost abruptly — " + e.getMessage());
        connected = false;
        // Phát đi thông điệp hệ thống thông báo mất mạng
        realtimeListener.dispatch("CONNECTION_LOST", e.getMessage());
      }
    }
    // CHANGED: "ServerConnection: Luồng lắng nghe đã kết thúc." -> "ServerConnection: Listener thread stopped."
    LOGGER.info("ServerConnection: Listener thread stopped.");
  }

  /**
   * Phân tích cú pháp dữ liệu JSON thô nhận về từ máy chủ và điều phối đến đích chính xác.
   */
  private void handleIncoming(String rawJson) {
    // --- Bước 1: Thử parse JSON ---
    Map<String, Object> envelope = null;
    try {
      envelope = protocol.decodeToMap(rawJson);
    } catch (Exception jsonEx) {
      // Server có thể trả về text thuần như "ERROR:...", "LOGIN_OK:...", v.v.
      // Hoàn thành bất kỳ pending request nào đang chờ bằng raw text này
      if (!pendingRequests.isEmpty()) {
        // Lấy future đầu tiên đang chờ (text protocol không có requestId)
        String firstKey = pendingRequests.keySet().iterator().next();
        CompletableFuture<String> future = pendingRequests.remove(firstKey);
        if (future != null) {
          // CHANGED: "ServerConnection: Completing pending request with raw text response: " (Đã sẵn là tiếng Anh)
          LOGGER.info("ServerConnection: Completing pending request with raw text response: " + rawJson);
          future.complete(rawJson);
          return;
        }
      }
      // Không có pending request → log nội dung nhận được để debug
      // CHANGED: "ServerConnection: Nhận text thuần (không phải JSON): " -> "ServerConnection: Received plain text (non-JSON): "
      LOGGER.info("ServerConnection: Received plain text (non-JSON): " + rawJson);
      realtimeListener.dispatch("INBOUND_TEXT", rawJson);
      return;
    }

    // --- Bước 2: Điều phối theo requestId hoặc loại thông điệp ---
    try {
      String type      = protocol.getMessageType(envelope);
      String requestId = (String) envelope.get("requestId");

      // Kiểm tra xem đây có phải phản hồi của một yêu cầu đồng bộ đang chờ hay không
      if (requestId != null && pendingRequests.containsKey(requestId)) {
        CompletableFuture<String> future = pendingRequests.remove(requestId);
        if (future != null) {
          future.complete(rawJson);
          return;
        }
      }

      // Nếu không, xử lý gói tin như một thông báo đẩy thời gian thực bất đồng bộ
      switch (type) {
        case MessageProtocol.TYPE_BID_UPDATE: {
          String payloadJson   = protocol.writeValueAsString(envelope.get("payload"));
          BidTransaction bid   = protocol.decodeAs(payloadJson, BidTransaction.class);
          realtimeListener.onBidUpdate(bid);
          break;
        }
        case MessageProtocol.TYPE_AUCTION_UPDATE:
        case MessageProtocol.TYPE_AUCTION_ENDED: {
          String payloadJson = protocol.writeValueAsString(envelope.get("payload"));
          Auction auction    = protocol.decodeAs(payloadJson, Auction.class);
          realtimeListener.onAuctionUpdate(auction);
          if (MessageProtocol.TYPE_AUCTION_ENDED.equals(type)) {
            realtimeListener.dispatch(MessageProtocol.TYPE_AUCTION_ENDED, auction);
          }
          break;
        }
        case MessageProtocol.TYPE_ERROR: {
          realtimeListener.dispatch(MessageProtocol.TYPE_ERROR, envelope.get("payload"));
          break;
        }
        default:
          realtimeListener.dispatch(type, rawJson);
          break;
      }
    } catch (Exception e) {
      // CHANGED: "ServerConnection: Xử lý thông điệp đến thất bại — " -> "ServerConnection: Failed to process inbound message — "
      LOGGER.warning("ServerConnection: Failed to process inbound message — " + e.getMessage());
      realtimeListener.dispatch("INBOUND_PARSE_ERROR", e.getMessage());
    }
  }

  public RealtimeListener getRealtimeListener() { return realtimeListener; }
  public MessageProtocol  getProtocol()         { return protocol; }
  public boolean          isConnected()         { return connected; }
}