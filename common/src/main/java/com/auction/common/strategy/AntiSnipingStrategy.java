package com.auction.common.strategy;

import com.auction.common.entity.Auction;
import com.auction.common.dto.BidRequest;
import com.auction.common.exception.InvalidBidException;

import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Decorator pattern wrapper that extends auction end time
 * when a bid is placed near the closing time (Anti-sniping).
 */
public class AntiSnipingStrategy implements BiddingStrategy {

  private static final Logger logger =
      Logger.getLogger(AntiSnipingStrategy.class.getName());

  private static final double DEFAULT_TRIGGER_WINDOW_SECONDS = 30.0;
  private static final double DEFAULT_EXTENSION_SECONDS = 60.0;

  private final BiddingStrategy wrapped;
  private final double triggerWindowSeconds;
  private final double extensionSeconds;

  public AntiSnipingStrategy(BiddingStrategy wrapped,
                             double triggerWindowSeconds,
                             double extensionSeconds) {
    if (wrapped == null) {
      throw new IllegalArgumentException("Wrapped strategy must not be null.");
    }
    if (triggerWindowSeconds <= 0 || extensionSeconds <= 0) {
      throw new IllegalArgumentException("Trigger window and extension seconds must be greater than 0.");
    }
    this.wrapped = wrapped;
    this.triggerWindowSeconds = triggerWindowSeconds;
    this.extensionSeconds = extensionSeconds;
  }

  public AntiSnipingStrategy(BiddingStrategy wrapped) {
    this(wrapped, DEFAULT_TRIGGER_WINDOW_SECONDS, DEFAULT_EXTENSION_SECONDS);
  }

  @Override
  public boolean execute(Auction auction, BidRequest request)
      throws InvalidBidException {

    validateNotNull(auction, request);

    LocalDateTime endTimeBeforeBid = auction.getEndTime();

    boolean accepted = wrapped.execute(auction, request);

    LocalDateTime bidTime = LocalDateTime.now();

    if (!auction.isAntiSnipingEnabled()) {
      applyExtensionIfNeeded(auction, endTimeBeforeBid, bidTime, request.getBidderId());
    }

    return accepted;
  }

  /**
   * Check time conditions and extend auction end time if triggered.
   */
  private void applyExtensionIfNeeded(Auction auction,
                                      LocalDateTime endTimeBeforeBid,
                                      LocalDateTime bidTime,
                                      String bidderId) {

    LocalDateTime triggerStart = endTimeBeforeBid.minusSeconds((long) triggerWindowSeconds);

    boolean insideWindow = !bidTime.isBefore(triggerStart) && !bidTime.isAfter(endTimeBeforeBid);

    if (insideWindow) {
      auction.extendEndTime(extensionSeconds);

      logger.info(String.format(
          "Anti-sniping triggered: User '%s' placed a last-minute bid, end time extended by %.0f seconds → New closing time: %s.",
          bidderId, extensionSeconds, auction.getEndTime()
      ));
    } else {
      logger.fine(String.format(
          "Anti-sniping not triggered: Bid time=%s, Trigger window=[%s, %s].",
          bidTime, triggerStart, endTimeBeforeBid
      ));
    }
  }

  public BiddingStrategy getWrapped() { return wrapped; }
  public double getTriggerWindowSeconds() { return triggerWindowSeconds; }
  public double getExtensionSeconds() { return extensionSeconds; }
}