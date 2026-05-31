package com.auction.client.model;

import com.auction.common.entity.BidTransaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BidHistoryModel {

    private List<BidTransaction> bidList;

    public BidHistoryModel() {
        this.bidList = new ArrayList<>();
    }

    /**
     * Thêm một giao dịch đấu giá vào danh sách
     */
    public void addBid(BidTransaction bid) {
        if (bid != null) {
            bidList.add(bid);
        }
    }

    /**
     * Lấy toàn bộ danh sách giao dịch đấu giá
     */
    public List<BidTransaction> getBids() {
        return Collections.unmodifiableList(bidList);
    }

    /**
     * Lấy danh sách giao dịch đấu giá theo auctionId
     */
    public List<BidTransaction> getBidsForAuction(String auctionId) {
        if (auctionId == null) {
            return new ArrayList<>();
        }
        return bidList.stream()
                .filter(bid -> auctionId.equals(bid.getAuctionId()))
                .collect(Collectors.toList());
    }

    /**
     * Xóa toàn bộ lịch sử đấu giá
     */
    public void clear() {
        bidList.clear();
    }

    public void setBidList(List<BidTransaction> bidList) {
        this.bidList = bidList != null ? bidList : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "BidHistoryModel{" +
                "bidCount=" + bidList.size() +
                '}';
    }
}