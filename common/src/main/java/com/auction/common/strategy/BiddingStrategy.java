package com.auction.common.strategy;

import com.auction.common.entity.Auction;
import com.auction.common.dto.BidRequest;
import com.auction.common.exception.InvalidBidException;

public interface BiddingStrategy {

  boolean execute(Auction auction, BidRequest request) throws InvalidBidException;

  default void validateNotNull(Auction auction, BidRequest request)
      throws InvalidBidException {
    if (auction == null || request == null) {
      throw new InvalidBidException("Auction and BidRequest must not be null.");
    }
  }
}