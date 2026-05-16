package com.auction.common.strategy;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.dto.BidRequest;
import com.auction.common.exception.InvalidBidException;

import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Xử lý lượt đặt giá thủ công tiêu chuẩn do người dùng thực hiện "bằng tay".
 */
public class NormalBiddingStrategy implements BiddingStrategy {

  private static final Logger logger =
      Logger.getLogger(NormalBiddingStrategy.class.getName());

  @Override
  public boolean execute(Auction auction, BidRequest request)
      throws InvalidBidException {

    // Kiểm tra giá trị rỗng (null)
    validateNotNull(auction, request);

    // Phiên đấu giá phải ở trạng thái đang hoạt động (Active)
    if (!auction.isActive()) {
      throw new InvalidBidException(String.format(
          "Phiên đấu giá '%s' không hoạt động (Trạng thái hiện tại=%s).",
          auction.getId(), auction.getStatus()
      ));
    }

    // Số tiền đặt giá phải là một số dương lớn hơn 0
    if (request.getAmount() <= 0) {
      throw new InvalidBidException(String.format(
          "Số tiền đặt giá %.2f phải lớn hơn 0.", request.getAmount()
      ));
    }

    // Số tiền phải thỏa mãn bước giá tối thiểu: Giá hiện tại + minIncrement
    double minimumRequired = auction.getCurrentPrice() + auction.getMinIncrement();
    if (request.getAmount() < minimumRequired) {
      throw new InvalidBidException(
          auction.getId(),
          auction.getCurrentPrice(),
          request.getAmount(),
          auction.getMinIncrement()
      );
    }

    // Người đặt giá không được phép là chủ sở hữu (người bán) sản phẩm
    if (!auction.canBid(request.getBidderId())) {
      throw new InvalidBidException(String.format(
          "Người dùng '%s' không được phép đặt giá trong phiên '%s' (Người bán không thể tự đấu giá sản phẩm của chính mình).",
          request.getBidderId(), auction.getId()
      ));
    }

    // Tạo giao dịch đấu giá
    BidTransaction bid = new BidTransaction(
        request.getAuctionId(),
        request.getBidderId(),
        request.getAmount(),
        LocalDateTime.now(),
        false // autoBid = false (đây là đặt giá thủ công)
    );

    boolean accepted = auction.addBid(bid);

    if (!accepted) {
      throw new InvalidBidException(String.format(
          "Hệ thống từ chối lượt đặt giá %.2f của người dùng '%s' trong phiên '%s'. "
              + "Có thể một lượt đặt giá đồng thời khác đã đẩy giá sản phẩm lên cao hơn trước đó.",
          request.getAmount(), request.getBidderId(), auction.getId()
      ));
    }

    logger.info(String.format(
        "Đặt giá thủ công thành công: người đặt='%s', số tiền=%.2f, mã phiên='%s'.",
        request.getBidderId(), request.getAmount(), auction.getId()
    ));

    return true;
  }
}