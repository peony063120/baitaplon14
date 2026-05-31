package com.auction.common.strategy;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.dto.BidRequest;
import com.auction.common.exception.InvalidBidException;

import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Handles standard manual bid placed by the user.
 */
public class NormalBiddingStrategy implements BiddingStrategy {

  private static final Logger logger =
      Logger.getLogger(NormalBiddingStrategy.class.getName());

  @Override
  public boolean execute(Auction auction, BidRequest request)
      throws InvalidBidException {

    validateNotNull(auction, request);

    if (!auction.isActive()) {
      throw new InvalidBidException(String.format(
          "Auction '%s' is not active (Current status=%s).",
          auction.getId(), auction.getStatus()
      ));
    }

    if (request.getAmount() <= 0) {
      throw new InvalidBidException(String.format(
          "Bid amount %.2f must be greater than 0.", request.getAmount()
      ));
    }

    double minimumRequired = auction.getCurrentPrice() + auction.getMinIncrement();
    if (request.getAmount() < minimumRequired) {
      throw new InvalidBidException(
          auction.getId(),
          auction.getCurrentPrice(),
          request.getAmount(),
          auction.getMinIncrement()
      );
    }

    if (!auction.canBid(request.getBidderId())) {
      throw new InvalidBidException(String.format(
          "User '%s' is not allowed to bid on auction '%s' (Seller cannot bid on own product).",
          request.getBidderId(), auction.getId()
      ));
    }

    BidTransaction bid = new BidTransaction(
        request.getAuctionId(),
        request.getBidderId(),
        request.getAmount(),
        LocalDateTime.now(),
        false
    );

    boolean accepted = auction.addBid(bid);

    if (!accepted) {
      throw new InvalidBidException(String.format(
          "System rejected bid %.2f from user '%s' on auction '%s'. Another concurrent bid may have raised the price higher.",
          request.getAmount(), request.getBidderId(), auction.getId()
      ));
    }

    logger.info(String.format(
        "Manual bid successful: bidder='%s', amount=%.2f, auction='%s'.",
        request.getBidderId(), request.getAmount(), auction.getId()
    ));

    return true;
  }
}