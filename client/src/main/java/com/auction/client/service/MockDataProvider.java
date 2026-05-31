package com.auction.client.service;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.LoginResponse;
import com.auction.common.dto.UserDTO;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MockDataProvider {

    public static List<AuctionDTO> getAuctions() {
        MockAuctionStore store = MockAuctionStore.getInstance();
        store.init();
        MockBidSimulator.getInstance().start();
        return store.getAuctions();
    }

    public static AuctionDTO getAuctionDetail(String auctionId) {
        MockAuctionStore store = MockAuctionStore.getInstance();
        store.init();
        AuctionDTO fromStore = store.findById(auctionId);
        if (fromStore != null) {
            AuctionDTO dto = new AuctionDTO();
            dto.setId(fromStore.getId());
            dto.setItemName(fromStore.getItemName());
            dto.setCurrentPrice(fromStore.getCurrentPrice());
            dto.setStartingPrice(fromStore.getStartingPrice());
            dto.setStatus(fromStore.getStatus());
            dto.setEndTime(fromStore.getEndTime());
            dto.setTotalBids(fromStore.getTotalBids());
            dto.setItemDescription(fromStore.getItemDescription());
            dto.setSellerName(fromStore.getSellerName());
            dto.setCategory(fromStore.getCategory());
            dto.setCategoryName(fromStore.getCategoryName());
            dto.setCurrentWinnerId(fromStore.getCurrentWinnerId());
            dto.setCurrentWinnerName(fromStore.getCurrentWinnerName());
            dto.setMinIncrement(Math.max(10000, fromStore.getCurrentPrice() * 0.02));
            dto.setAntiSnipingEnabled(true);
            dto.setAntiSnipingExtensionSeconds(30);
            return dto;
        }
        AuctionDTO dto = new AuctionDTO();
        dto.setId(auctionId);
        dto.setItemName("Auction Detail");
        dto.setCurrentPrice(2_500_000);
        dto.setStartingPrice(1_000_000);
        dto.setStatus(AuctionStatus.RUNNING);
        dto.setEndTime(LocalDateTime.now().plusHours(23));
        dto.setTotalBids(12);
        dto.setCurrentWinnerId("bidder_demo");
        dto.setCurrentWinnerName("Demo Bidder");
        dto.setMinIncrement(100_000);
        dto.setAntiSnipingEnabled(true);
        dto.setAntiSnipingExtensionSeconds(30);
        dto.setItemDescription("Detailed product description for UI testing.");
        dto.setSellerName("Demo Seller");
        return dto;
    }

    public static List<BidTransaction> getBidHistory(String auctionId) {
        List<BidTransaction> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            BidTransaction bid = new BidTransaction(
                    "tx_mock_" + i,
                    auctionId,
                    "bidder_" + i,
                    1_000_000 + i * 200_000,
                    LocalDateTime.now().minusMinutes(i * 10),
                    i % 2 == 0
            );
            list.add(bid);
        }
        return list;
    }

    public static LoginResponse getLoginResponse(String role) {
        String mockId = switch (role.toUpperCase()) {
            case "ADMIN" -> "user_admin_mock";
            case "SELLER" -> "user_seller_mock";
            default -> "user_bidder_mock";
        };
        String mockUser = switch (role.toUpperCase()) {
            case "ADMIN" -> "admin_mock";
            case "SELLER" -> "seller_mock";
            default -> "bidder_mock";
        };
        double balance = "BIDDER".equalsIgnoreCase(role) ? 50_000_000 : 0.0;
        return new LoginResponse(true, "Login successful (mock " + role + ")",
                mockId, mockUser, role.toUpperCase(),
                "mock_session_123", balance);
    }

    public static LoginResponse getLoginResponse() {
        return getLoginResponse("BIDDER");
    }

    public static UserDTO getCurrentUser() {
        UserDTO dto = new UserDTO();
        dto.setId("user_demo");
        dto.setUsername("demo_user");
        dto.setFullName("Demo User");
        dto.setEmail("demo@auction.com");
        dto.setRole("BIDDER");
        dto.setBalance(50_000_000);
        dto.setActive(true);
        return dto;
    }

    public static List<AuctionDTO> getMyAuctions(String sellerId) {
        List<AuctionDTO> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            AuctionDTO dto = new AuctionDTO();
            dto.setId("my_auc_" + i);
            dto.setItemName("My Product " + i);
            dto.setCurrentPrice(2_000_000 * i);
            dto.setStatus(AuctionStatus.DRAFT);
            dto.setEndTime(LocalDateTime.now().plusDays(7));
            dto.setTotalBids(0);
            list.add(dto);
        }
        return list;
    }
}