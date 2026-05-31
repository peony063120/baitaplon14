package com.auction.client.network;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * ServerConnection — Manages a single persistent TCP socket connection between
 * the JavaFX Client and the Auction Server.
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

  public static synchronized void resetInstance() {
    if (instance != null) {
      instance.disconnect();
      instance = null;
      LOGGER.info("ServerConnection: Instance reset — ready for reconnection.");
    }
  }

  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;

  private final MessageProtocol protocol;
  private final RealtimeListener realtimeListener;
  private ExecutorService listenerThread;

  private volatile boolean connected = false;

  /** Tracks synchronous JSON requests waiting for matching response lines from the server. */
  private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

  /** FIFO queue for plain-text request/response pairs (one response per request, in order). */
  private final Deque<CompletableFuture<String>> textResponseQueue = new ArrayDeque<>();
  private final Object textRequestLock = new Object();

  private ServerConnection() {
    this.protocol         = new MessageProtocol();
    this.realtimeListener = RealtimeListener.getInstance();
  }

  public synchronized void connect(String host, int port) throws IOException {
    if (connected) {
      LOGGER.warning("ServerConnection: Already connected — ignoring connect().");
      return;
    }

    socket = new Socket(host, port);
    out    = new PrintWriter(socket.getOutputStream(), true);
    in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    connected = true;

    LOGGER.info("ServerConnection: Successfully connected to server at " + host + ":" + port);

    listenerThread = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "auction-server-listener");
      t.setDaemon(true);
      return t;
    });
    listenerThread.submit(this::listenLoop);
  }

  public void disconnect() {
    connected = false;
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      LOGGER.warning("ServerConnection: Error during disconnect — " + e.getMessage());
    }
    if (listenerThread != null) {
      listenerThread.shutdownNow();
    }
    pendingRequests.forEach((id, future) -> future.cancel(true));
    pendingRequests.clear();
    synchronized (textRequestLock) {
      textResponseQueue.forEach(future -> future.cancel(true));
      textResponseQueue.clear();
    }
    LOGGER.info("ServerConnection: Disconnected and released pending requests.");
  }

  public String sendRequest(Map<String, Object> requestMessage) throws IOException {
    if (!connected || out == null) {
      throw new IOException("ServerConnection: Không thể gửi yêu cầu — Mạng bị ngắt kết nối.");
    }

    String requestId = (String) requestMessage.get("requestId");
    String rawJson   = protocol.encode(requestMessage);

    CompletableFuture<String> responseFuture = new CompletableFuture<>();
    if (requestId != null) {
      pendingRequests.put(requestId, responseFuture);
    }

    synchronized (out) {
      out.println(rawJson);
      LOGGER.fine("ServerConnection -> SERVER: " + rawJson);
    }

    if (requestId == null) {
      return "";
    }

    try {
      return responseFuture.get(10, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      pendingRequests.remove(requestId);
      throw new IOException("ServerConnection: Timeout waiting for server response for request ID: " + requestId);
    } catch (InterruptedException | ExecutionException e) {
      pendingRequests.remove(requestId);
      throw new IOException("ServerConnection: Request interrupted or processing failed: " + e.getMessage());
    }
  }

  public String sendRequest(String command) throws IOException {
    if (!connected || out == null) {
      throw new IOException("ServerConnection: Cannot send request — Network disconnected.");
    }

    CompletableFuture<String> responseFuture = new CompletableFuture<>();
    synchronized (textRequestLock) {
      textResponseQueue.addLast(responseFuture);
      synchronized (out) {
        out.println(command);
        LOGGER.fine("ServerConnection -> SERVER (text): " + command);
      }
    }

    try {
      return responseFuture.get(10, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      synchronized (textRequestLock) {
        textResponseQueue.remove(responseFuture);
      }
      throw new IOException("ServerConnection: Timeout waiting for response from Server");
    } catch (InterruptedException | ExecutionException e) {
      synchronized (textRequestLock) {
        textResponseQueue.remove(responseFuture);
      }
      throw new IOException("ServerConnection: Request interrupted: " + e.getMessage());
    }
  }

  private void completeNextTextRequest(String response) {
    CompletableFuture<String> future;
    synchronized (textRequestLock) {
      future = textResponseQueue.pollFirst();
    }
    if (future != null) {
      LOGGER.fine("ServerConnection: Completed text request with response: " + response);
      future.complete(response);
    } else {
      LOGGER.fine("ServerConnection: Unexpected text response (no pending request): " + response);
      realtimeListener.dispatch("INBOUND_TEXT", response);
    }
  }

  private void listenLoop() {
    LOGGER.info("ServerConnection: Listener thread started.");
    try {
      String line;
      while (connected && (line = in.readLine()) != null) {
        handleIncoming(line.trim());
      }
    } catch (IOException e) {
      if (connected) {
        LOGGER.warning("ServerConnection: Connection lost unexpectedly — " + e.getMessage());
        connected = false;
        realtimeListener.dispatch("CONNECTION_LOST", e.getMessage());
      }
    }
    LOGGER.info("ServerConnection: Listener thread ended.");
  }

  private void handleIncoming(String rawJson) {
    if (rawJson.isEmpty()) return;

    // --- ĐÃ SỬA: KIỂM TRA XỬ LÝ CHUỖI TEXT THUẦN TRƯỚC (Bản tin Realtime dạng text thô) ---
    if (rawJson.startsWith("AUCTION_UPDATE:")) {
      LOGGER.info("ServerConnection: Realtime AUCTION_UPDATE received.");
      com.auction.common.dto.AuctionDTO dto = ResponseHandler.parseAuctionUpdateFromText(rawJson);
      if (dto != null) {
        realtimeListener.dispatch(MessageProtocol.TYPE_AUCTION_UPDATE, dto);
      }
      return;
    }
    if (rawJson.startsWith("BID_UPDATE:")) {
      LOGGER.info("ServerConnection: Realtime BID_UPDATE received.");
      realtimeListener.dispatch(MessageProtocol.TYPE_BID_UPDATE, rawJson.substring("BID_UPDATE:".length()));
      return;
    }

    // --- Bước 2: Thử parse JSON ---
    Map<String, Object> envelope = null;
    try {
      envelope = protocol.decodeToMap(rawJson);
    } catch (Exception jsonEx) {
      completeNextTextRequest(rawJson);
      return;
    }

    // --- Bước 3: Xử lý gói tin JSON chuẩn ---
    try {
      String type      = protocol.getMessageType(envelope);
      String requestId = (String) envelope.get("requestId");

      if (requestId != null && pendingRequests.containsKey(requestId)) {
        CompletableFuture<String> future = pendingRequests.remove(requestId);
        if (future != null) {
          future.complete(rawJson);
          return;
        }
      }

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
      LOGGER.warning("ServerConnection: Failed to process incoming message — " + e.getMessage());
      realtimeListener.dispatch("INBOUND_PARSE_ERROR", e.getMessage());
    }
  }

  public RealtimeListener getRealtimeListener() { return realtimeListener; }
  public MessageProtocol  getProtocol()         { return protocol; }
  public boolean          isConnected()         { return connected; }
}