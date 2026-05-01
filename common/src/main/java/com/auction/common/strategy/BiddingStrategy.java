package com.auction.common.strategy;

import com.auction.common.entity.Auction;
import com.auction.common.dto.BidRequest;

public interface BiddingStrategy {
  boolean execute(Auction auction, BidRequest request);
}