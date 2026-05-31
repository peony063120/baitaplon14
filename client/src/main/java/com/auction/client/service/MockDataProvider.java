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
        List<AuctionDTO> list = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            AuctionDTO dto = new AuctionDTO();
            dto.setId("mock_auc_" + i);
            dto.setItemName("Demo Product " + i);
            dto.setCurrentPrice(1_000_000 * i);
            dto.setStartingPrice(500_000 * i);
            dto.setStatus(i % 2 == 0 ? AuctionStatus.RUNNING : AuctionStatus.DRAFT);
            dto.setEndTime(LocalDateTime.now().plusDays(i));
            dto.setTotalBids(i * 2);
            // CHANGED: "Mô tả sản phẩm số " -> "Description for product No. "
            dto.setItemDescription("Description for product No. " + i);
            dto.setSellerName("Demo Seller");
            // CHANGED: "Điện tử" / "Xe cộ" / "Nghệ thuật" -> "Electronics" / "Vehicles" / "Art"
            dto.setCategoryName(i % 3 == 0 ? "Electronics" : (i % 2 == 0 ? "Vehicles" : "Art"));
            list.add(dto);
        }
        return list;
    }

    public static AuctionDTO getAuctionDetail(String auctionId) {
        AuctionDTO dto = new AuctionDTO();
        dto.setId(auctionId);
        dto.setItemName("Demo Product Details");
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
        // CHANGED: Chuỗi mô tả chi tiết sản phẩm chuyển sang Tiếng Anh
        dto.setItemDescription("Detailed product description used for UI testing layout purposes.");
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

    public static LoginResponse getLoginResponse() {
        return new LoginResponse(true, "Login successful (Demo mode)",
                "user_demo", "demo_user", "BIDDER",
                "mock_session_123", 50_000_000);
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