package com.auction.server.model;

import com.auction.common.entity.Auction;

/**
 * PriceCalculator
 * Cung cấp các phương thức tính toán liên quan đến giá đấu.
 */
public class PriceCalculator {

    /**
     * Tính giá đấu tiếp theo dựa trên giá hiện tại và bước giá tối thiểu.
     * Công thức: giá hiện tại + max(bước giá, 1% giá hiện tại) -> làm tròn lên.
     */
    public static double calculateNextBid(double currentPrice, double minIncrement) {
        if (currentPrice <= 0) return minIncrement;
        double increment = Math.max(minIncrement, currentPrice * 0.01);
        return Math.ceil((currentPrice + increment) * 100) / 100.0;
    }

    /**
     * Kiểm tra giá đấu có hợp lệ không.
     * - Nếu chưa có người thắng: giá đấu phải >= giá hiện tại (giá khởi điểm).
     * - Nếu đã có người thắng: giá đấu phải >= giá hiện tại + bước giá tối thiểu.
     */
    public static boolean isBidValid(Auction auction, double bidAmount) {
        if (auction == null) return false;
        if (!auction.isActive()) return false;

        double minAllowed;
        String currentWinnerId = auction.getCurrentWinnerId();
        if (currentWinnerId == null || currentWinnerId.isEmpty()) {
            // Chưa có bid nào → giá tối thiểu là giá hiện tại (chính là giá khởi điểm)
            minAllowed = auction.getCurrentPrice();
            return bidAmount >= minAllowed;
        } else {
            // Đã có người thắng → phải cao hơn ít nhất một bước giá
            minAllowed = auction.getCurrentPrice() + auction.getMinIncrement();
            return bidAmount >= minAllowed;
        }
    }
}