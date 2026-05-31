package com.auction.common.strategy;

import com.auction.common.entity.Auction;
import com.auction.common.entity.AutoBidConfig;
import com.auction.common.entity.BidTransaction;
import com.auction.common.dto.BidRequest;
import com.auction.common.exception.InvalidBidException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Automatically places bids for bidders with auto-bid configurations.
 */
public class AutoBiddingStrategy implements BiddingStrategy {

  private static final Logger logger =
      Logger.getLogger(AutoBiddingStrategy.class.getName());

  private static final int MAX_RECURSION_DEPTH = 10;

  private static final Map<String, ReentrantLock> LOCK_MAP = new ConcurrentHashMap<>();

  @Override
  public boolean execute(Auction auction, BidRequest request)
      throws InvalidBidException {
    return executeInternal(auction, request, 0);
  }

  public static void removeLock(String auctionId) {
    ReentrantLock lock = LOCK_MAP.remove(auctionId);
    if (lock != null && lock.isLocked()) {
      logger.warning(String.format(
          "Warning: Lock for auction '%s' removed while still held.", auctionId
      ));
    }
  }

  /**
   * Internal recursive executor controlling concurrent access per auction.
   */
  private boolean executeInternal(Auction auction, BidRequest request, int depth)
      throws InvalidBidException {

    validateNotNull(auction, request);

    if (depth >= MAX_RECURSION_DEPTH) {
      throw new InvalidBidException(String.format(
          "Auto-bid chain stopped at depth %d (Max=%d) for auction '%s' to prevent overload.",
          depth, MAX_RECURSION_DEPTH, auction.getId()
      ));
    }

    // Per-auction fair lock for thread safety
    ReentrantLock lock = LOCK_MAP.computeIfAbsent(
        auction.getId(),
        id -> new ReentrantLock(true)
    );

    lock.lock();
    try {
      return doAutoBid(auction, request, depth);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Core auto-bidding logic.
   */
  private boolean doAutoBid(Auction auction, BidRequest request, int depth)
      throws InvalidBidException {

    if (!auction.isActive()) {
      throw new InvalidBidException(String.format("Auction '%s' has ended or is not open.", auction.getId()));
    }

    // Build priority queue of eligible auto-bid configs (highest maxBid first)
    PriorityQueue<AutoBidConfig> candidates = buildCandidateQueue(
        auction, request.getBidderId(), request.isAutoBid()
    );

    if (candidates.isEmpty()) {
      logger.fine(String.format(
          "Auto-bid [Depth=%d]: No eligible auto-bid configs found for auction '%s'.", depth, auction.getId()
      ));
      return false;
    }

    final double currentPrice = auction.getCurrentPrice();
    AutoBidConfig best = candidates.peek();

    if (best == null || !best.canBid(currentPrice)) {
      if (best != null) {
        best.setActive(false);
      }
      throw new InvalidBidException(String.format(
          "No eligible auto-bid config can exceed the current price %.2f for auction '%s'.",
          currentPrice, auction.getId()
      ));
    }

    double autoBidAmount = Math.min(
        best.getNextBid(currentPrice),
        best.getMaxBid()
    );

    if (autoBidAmount < currentPrice + auction.getMinIncrement()) {
      best.setActive(false);
      throw new InvalidBidException(
          auction.getId(),
          auction.getCurrentPrice(),
          autoBidAmount,
          auction.getMinIncrement()
      );
    }

    BidTransaction autoBidTx = new BidTransaction(
        auction.getId(),
        best.getBidderId(),
        autoBidAmount,
        LocalDateTime.now(),
        true
    );

    boolean accepted = auction.addBid(autoBidTx);

    if (!accepted) {
      throw new InvalidBidException(String.format(
          "System rejected auto-bid of %.2f from user '%s' on auction '%s'.",
          autoBidAmount, best.getBidderId(), auction.getId()
      ));
    }

    logger.info(String.format(
        "Auto-bid [Depth=%d] successful: bidder='%s', amount=%.2f (Max config=%.2f), auction='%s'.",
        depth, best.getBidderId(), autoBidAmount, best.getMaxBid(), auction.getId()
    ));

    // Chain reaction: trigger auto-bids from other users' configs
    BidRequest followUp = new BidRequest(
        auction.getId(),
        best.getBidderId(),
        autoBidAmount,
        true // isAutoBid = true
    );

    try {
      executeInternal(auction, followUp, depth + 1);
    } catch (InvalidBidException e) {
      logger.fine(String.format(
          "Auto-bid chain ended at depth=%d for auction '%s': %s",
          depth + 1, auction.getId(), e.getMessage()
      ));
    }

    return true;
  }

  /**
   * Filter and sort eligible auto-bid configs.
   */
  private PriorityQueue<AutoBidConfig> buildCandidateQueue(
      Auction auction,
      String triggeringBidderId,
      boolean isFollowUpAutoBid) {

    // Priority: highest maxBid first, then earliest createdAt
    PriorityQueue<AutoBidConfig> queue = new PriorityQueue<>(
        Comparator.comparingDouble(AutoBidConfig::getMaxBid)
            .reversed()
            .thenComparing(AutoBidConfig::getCreatedAt)
    );

    List<AutoBidConfig> configs = auction.getAutoBidConfigs();
    if (configs == null) return queue;

    String currentWinnerId = auction.getCurrentWinnerId();

    for (AutoBidConfig config : configs) {
      if (!config.isActive()) continue;
      if (config.getBidderId().equals(currentWinnerId)) continue;
      if (isFollowUpAutoBid && config.getBidderId().equals(triggeringBidderId)) continue;
      queue.offer(config);
    }

    return queue;
  }
}