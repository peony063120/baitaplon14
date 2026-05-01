package com.auction.common.strategy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class BidLockManager {
  // Sử dụng Map để mỗi Auction ID có duy nhất một ReentrantLock
  private static final ConcurrentHashMap<String, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();

  public static ReentrantLock getLock(String auctionId) {
    return auctionLocks.computeIfAbsent(auctionId, k -> new ReentrantLock());
  }
}
