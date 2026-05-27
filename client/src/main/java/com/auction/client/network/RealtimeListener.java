package com.auction.client.network;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * RealtimeListener — Đối tượng quan sát (Observer) nhận các thông báo đẩy thời gian thực
 * từ máy chủ và phân phối tới tất cả các hàm gọi ngược (callbacks) của UI hoặc logic đã đăng ký.
 *
 * SỬ DỤNG SINGLETON PATTERN: Chỉ có một instance duy nhất trong toàn bộ ứng dụng.
 */
public class RealtimeListener {

  private static final Logger LOGGER = Logger.getLogger(RealtimeListener.class.getName());

  // ==================== SINGLETON PATTERN ====================
  private static volatile RealtimeListener instance;

  /**
   * Lấy instance duy nhất của RealtimeListener (Singleton).
   * @return instance của RealtimeListener
   */
  public static RealtimeListener getInstance() {
    if (instance == null) {
      synchronized (RealtimeListener.class) {
        if (instance == null) {
          instance = new RealtimeListener();
        }
      }
    }
    return instance;
  }

  /**
   * Reset instance (dùng cho testing hoặc reconnect).
   */
  public static synchronized void resetInstance() {
    if (instance != null) {
      instance.clearAllCallbacks();
      instance = null;
    }
  }

  // ==================== FIELDS ====================
  // Bản đồ chứa danh sách các hàm Callback đã đăng ký theo từng loại sự kiện cụ thể
  private final Map<String, List<Consumer<Object>>> callbacks = new ConcurrentHashMap<>();

  private static final boolean IS_JAVAFX_AVAILABLE;

  // Khối static kiểm tra sự tồn tại của môi trường đồ họa JavaFX
  static {
    boolean available;
    try {
      Class.forName("javafx.application.Platform");
      available = true;
    } catch (ClassNotFoundException e) {
      available = false;
    }
    IS_JAVAFX_AVAILABLE = available;
  }

  // Private constructor cho Singleton
  private RealtimeListener() {}

  /**
   * Xóa tất cả callbacks (dùng khi reset instance).
   */
  private synchronized void clearAllCallbacks() {
    callbacks.clear();
    LOGGER.info("RealtimeListener: Đã xóa tất cả callbacks");
  }

  /**
   * Đăng ký một hàm Callback để lắng nghe một loại sự kiện cụ thể từ mạng.
   *
   * @param eventType Mã định danh loại sự kiện (ví dụ: BID_UPDATE, AUCTION_ENDED)
   * @param callback  Hàm xử lý sự kiện cần đăng ký
   */
  public synchronized void registerCallback(String eventType, Consumer<Object> callback) {
    if (callback == null) return;
    callbacks.computeIfAbsent(eventType, k -> new ArrayList<>()).add(callback);
    LOGGER.fine("RealtimeListener: Đã đăng ký callback thành công cho loại sự kiện '" + eventType + "'");
  }

  /**
   * Hủy bỏ toàn bộ các hàm Callback thuộc một loại sự kiện cụ thể.
   * Chỉ dùng khi chắc chắn không còn bất kỳ component nào khác đang lắng nghe
   * eventType này. Để hủy đăng ký an toàn từng callback riêng lẻ, dùng
   * {@link #unregisterCallback(String, Consumer)}.
   *
   * @param eventType Mã định danh loại sự kiện cần xóa toàn bộ callback
   */
  public synchronized void unregisterCallbacks(String eventType) {
    callbacks.remove(eventType);
    LOGGER.fine("RealtimeListener: Đã xóa toàn bộ callback của sự kiện '" + eventType + "'");
  }

  /**
   * Hủy bỏ một hàm Callback cụ thể khỏi danh sách lắng nghe của một loại sự kiện.
   * An toàn hơn unregisterCallbacks() khi nhiều màn hình cùng lắng nghe một sự kiện.
   * @param eventType Mã định danh loại sự kiện
   * @param callback  Tham chiếu chính xác đến hàm callback đã đăng ký trước đó
   */
  public synchronized void unregisterCallback(String eventType, Consumer<Object> callback) {
    if (callback == null) return;
    List<Consumer<Object>> list = callbacks.get(eventType);
    if (list != null) {
      list.remove(callback);
      if (list.isEmpty()) {
        callbacks.remove(eventType);
      }
      LOGGER.fine("RealtimeListener: Đã hủy đăng ký callback cụ thể cho sự kiện '" + eventType + "'");
    }
  }

  /**
   * Xử lý khi có sự kiện đẩy thông tin lượt đặt giá mới (Bid Update).
   *
   * @param bid Đối tượng giao dịch đặt giá mới nhận từ server
   */
  public void onBidUpdate(BidTransaction bid) {
    LOGGER.info("RealtimeListener: Nhận sự kiện BID_UPDATE — Mã phiên="
            + bid.getAuctionId() + " Số tiền=" + bid.getAmount());
    dispatch(MessageProtocol.TYPE_BID_UPDATE, bid);
  }

  /**
   * Xử lý khi có sự kiện thay đổi trạng thái hoặc thông tin phiên đấu giá (Auction Update).
   *
   * @param auction Đối tượng phiên đấu giá đã được cập nhật
   */
  public void onAuctionUpdate(Auction auction) {
    LOGGER.info("RealtimeListener: Nhận sự kiện AUCTION_UPDATE — Mã phiên=" + auction.getId()
            + " Trạng thái mới=" + auction.getStatus());
    dispatch(MessageProtocol.TYPE_AUCTION_UPDATE, auction);
  }

  /**
   * Phương thức điều phối chung: kích hoạt toàn bộ các hàm callback đã đăng ký
   * tương ứng với mã sự kiện. Tự động ép luồng chạy an toàn trên JavaFX UI Thread
   * bằng Platform.runLater() nếu môi trường JavaFX khả dụng.
   *
   * @param eventType Mã định danh loại sự kiện
   * @param payload   Dữ liệu gốc truyền kèm theo sự kiện
   */
  public void dispatch(String eventType, Object payload) {
    List<Consumer<Object>> targets;
    synchronized (this) {
      List<Consumer<Object>> registered = callbacks.get(eventType);
      if (registered == null || registered.isEmpty()) {
        LOGGER.fine("RealtimeListener: Không có hàm callback nào đăng ký cho sự kiện '" + eventType + "'");
        return;
      }
      // Tạo bản chụp nhanh (Snapshot) danh sách để giải phóng Lock đồng bộ sớm, tránh Deadlock
      targets = new ArrayList<>(registered);
    }

    for (Consumer<Object> cb : targets) {
      Runnable task = () -> {
        try {
          cb.accept(payload);
        } catch (Exception ex) {
          LOGGER.warning("RealtimeListener: Lỗi thực thi hàm callback của sự kiện '"
                  + eventType + "' — " + ex.getMessage());
        }
      };

      if (IS_JAVAFX_AVAILABLE) {
        javafx.application.Platform.runLater(task);
      } else {
        task.run();
      }
    }
  }
}