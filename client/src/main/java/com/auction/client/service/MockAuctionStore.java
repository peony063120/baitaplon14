package com.auction.client.service;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.enums.AuctionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MockAuctionStore {
    private static final MockAuctionStore INSTANCE = new MockAuctionStore();
    private final List<AuctionDTO> auctions = new CopyOnWriteArrayList<>();

    private MockAuctionStore() {}

    public static MockAuctionStore getInstance() {
        return INSTANCE;
    }

    public void init() {
        if (!auctions.isEmpty()) return;
        for (int i = 1; i <= 6; i++) {
            AuctionDTO dto = new AuctionDTO();
            dto.setId("mock_auc_" + i);
            dto.setItemName("Demo Product " + i);
            dto.setCurrentPrice(1_000_000 * i);
            dto.setStartingPrice(500_000 * i);
            dto.setStatus(i % 2 == 0 ? AuctionStatus.RUNNING : AuctionStatus.DRAFT);
            dto.setEndTime(LocalDateTime.now().plusDays(i));
            dto.setTotalBids(i * 2);
            dto.setItemDescription("Product description " + i);
            dto.setSellerName("Demo Seller");
            dto.setCategory(i % 3 == 0 ? "Electronics" : (i % 2 == 0 ? "Vehicles" : "Art"));
            dto.setCategoryName(i % 3 == 0 ? "Electronics" : (i % 2 == 0 ? "Vehicles" : "Art"));
            auctions.add(dto);
        }
    }

    public List<AuctionDTO> getAuctions() {
        List<AuctionDTO> copy = new ArrayList<>();
        for (AuctionDTO a : auctions) {
            AuctionDTO c = new AuctionDTO();
            c.setId(a.getId());
            c.setItemName(a.getItemName());
            c.setCurrentPrice(a.getCurrentPrice());
            c.setStartingPrice(a.getStartingPrice());
            c.setStatus(a.getStatus());
            c.setEndTime(a.getEndTime());
            c.setTotalBids(a.getTotalBids());
            c.setItemDescription(a.getItemDescription());
            c.setSellerName(a.getSellerName());
            c.setCategory(a.getCategory());
            c.setCategoryName(a.getCategoryName());
            copy.add(c);
        }
        return copy;
    }

    public boolean placeBid(String auctionId, String bidderId, String bidderName, double amount) {
        for (AuctionDTO a : auctions) {
            if (a.getId().equals(auctionId)) {
                if (a.getStatus() != AuctionStatus.RUNNING) return false;
                if (amount <= a.getCurrentPrice()) return false;
                a.setCurrentPrice(amount);
                a.setTotalBids(a.getTotalBids() + 1);
                a.setCurrentWinnerId(bidderId);
                a.setCurrentWinnerName(bidderName);
                return true;
            }
        }
        return false;
    }

    public AuctionDTO findById(String auctionId) {
        for (AuctionDTO a : auctions) {
            if (a.getId().equals(auctionId)) return a;
        }
        return null;
    }
}
