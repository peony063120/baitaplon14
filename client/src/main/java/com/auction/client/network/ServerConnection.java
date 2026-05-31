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
 * ServerConnection — Manages a single persistent TCP socket connection between
 * the JavaFX Client and the Auction Server.
 * IMPORTANT — sendRequest() usage rules:
 *   sendRequest() blocks the calling thread for up to 10 seconds waiting for a response.
 *   Do NOT call this method from the JavaFX Application Thread
 *   (not directly in setOnAction, initialize, or any UI callback).
 *   Always wrap in javafx.concurrent.Task or ExecutorService.
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
   * Destroy current singleton instance and disconnect socket.
   * Call this before creating a new connection (after network loss
   * and wanting to reconnect), or when the application shuts down.
   */
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

  /** Tracks synchronous requests waiting for matching response lines from the server. */
  private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

  private ServerConnection() {
    this.protocol         = new MessageProtocol();
    this.realtimeListener = RealtimeListener.getInstance();
  }

  /**
   * Establish socket connection to server and start background listener thread.
   * @param host Server address
   * @param port Connection port
   * @throws IOException If connection cannot be established
   */
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

    // Create daemon thread that won't block application shutdown
    listenerThread = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "auction-server-listener");
      t.setDaemon(true);
      return t;
    });
    listenerThread.submit(this::listenLoop);
  }

  /**
   * Safely disconnect, close socket and release system resources.
   * After disconnect(), call resetInstance() before connecting again.
   */
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
    // Cancel all pending synchronous requests to prevent memory leaks
    pendingRequests.forEach((id, future) -> future.cancel(true));
    pendingRequests.clear();
    LOGGER.info("ServerConnection: Disconnected and released pending requests.");
  }

  /**
   * Send a request to the server and block synchronously until a matching
   * response is received or timeout occurs.
   * This method BLOCKS the calling thread for up to 10 seconds.
   * Do NOT call from JavaFX Application Thread — will freeze the UI.
   * Always call from a background thread (javafx.concurrent.Task or ExecutorService).
   *
   * @param requestMessage Message map created via {@link MessageProtocol#buildRequestMessage}
   * @return Raw JSON string received from server
   * @throws IOException If connection lost, transport error, or timeout
   */
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

    // Synchronize output to prevent concurrent write conflicts
    synchronized (out) {
      out.println(rawJson);
      LOGGER.fine("ServerConnection -> SERVER: " + rawJson);
    }

    if (requestId == null) {
      return ""; // No response needed for one-way messages
    }

    try {
      // Block and wait for server response with up to 10-second timeout
      return responseFuture.get(10, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      pendingRequests.remove(requestId);
      throw new IOException("ServerConnection: Timeout waiting for server response for request ID: " + requestId);
    } catch (InterruptedException | ExecutionException e) {
      pendingRequests.remove(requestId);
      throw new IOException("ServerConnection: Request interrupted or processing failed: " + e.getMessage());
    }
  }

  /**
   * Send simple text request (without Map).
   * This method BLOCKS the calling thread.
   */
  public String sendRequest(String command) throws IOException {
    if (!connected || out == null) {
      throw new IOException("ServerConnection: Cannot send request — Network disconnected.");
    }

    String requestId = java.util.UUID.randomUUID().toString();
    CompletableFuture<String> responseFuture = new CompletableFuture<>();
    pendingRequests.put(requestId, responseFuture);

    // Send raw text command (text protocol) — server expects "COMMAND:payload" format
    synchronized (out) {
      out.println(command);
      LOGGER.fine("ServerConnection -> SERVER (text): " + command);
    }

    try {
      return responseFuture.get(10, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      pendingRequests.remove(requestId);
      throw new IOException("ServerConnection: Timeout waiting for response from Server");
    } catch (InterruptedException | ExecutionException e) {
      pendingRequests.remove(requestId);
      throw new IOException("ServerConnection: Request interrupted: " + e.getMessage());
    }
  }

  /**
   * Background loop continuously reading line-delimited data from the socket input stream.
   */
  private void listenLoop() {
    LOGGER.info("ServerConnection: Listener thread started.");
    try {
      String line;
      while (connected && (line = in.readLine()) != null) {
        handleIncoming(line);
      }
    } catch (IOException e) {
      if (connected) {
        LOGGER.warning("ServerConnection: Connection lost unexpectedly — " + e.getMessage());
        connected = false;
        // Dispatch system message about connection loss
        realtimeListener.dispatch("CONNECTION_LOST", e.getMessage());
      }
    }
    LOGGER.info("ServerConnection: Listener thread ended.");
  }

  /**
   * Parse raw JSON data received from server and dispatch to the correct destination.
   */
  private void handleIncoming(String rawJson) {
    // --- Step 1: Try parsing as JSON ---
    Map<String, Object> envelope = null;
    try {
      envelope = protocol.decodeToMap(rawJson);
    } catch (Exception jsonEx) {
      // Server may return plain text like "ERROR:...", "LOGIN_OK:...", etc.
      // Complete any pending request with this raw text
      if (!pendingRequests.isEmpty()) {
        // Get the first waiting future (text protocol has no requestId)
        String firstKey = pendingRequests.keySet().iterator().next();
        CompletableFuture<String> future = pendingRequests.remove(firstKey);
        if (future != null) {
          LOGGER.fine("ServerConnection: Completing pending request with raw text response: " + rawJson);
          future.complete(rawJson);
          return;
        }
      }
      // No pending request → log received content for debugging
      LOGGER.fine("ServerConnection: Received plain text (not JSON): " + rawJson);
      realtimeListener.dispatch("INBOUND_TEXT", rawJson);
      return;
    }

    // --- Step 2: Dispatch by requestId or message type ---
    try {
      String type      = protocol.getMessageType(envelope);
      String requestId = (String) envelope.get("requestId");

      // Check if this is a response to a pending synchronous request
      if (requestId != null && pendingRequests.containsKey(requestId)) {
        CompletableFuture<String> future = pendingRequests.remove(requestId);
        if (future != null) {
          future.complete(rawJson);
          return;
        }
      }

      // Otherwise, handle as an asynchronous real-time push notification
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