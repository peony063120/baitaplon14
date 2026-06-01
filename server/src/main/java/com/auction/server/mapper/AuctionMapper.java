package com.auction.server.mapper; // Đổi lại package nếu cần

import com.auction.common.dto.AuctionDTO;
import com.auction.common.entity.Auction;
import com.auction.server.dao.UserDAO;
import com.auction.common.entity.User;

import java.time.LocalDateTime;

public class AuctionMapper {

    public AuctionDTO toDTO(Auction entity) {
        if (entity == null) return null;
        AuctionDTO dto = new AuctionDTO();
        dto.setId(entity.getId());
        dto.setItemId(entity.getItemId());
        dto.setSellerId(entity.getSellerId());
        dto.setCurrentPrice(entity.getCurrentPrice());
        dto.setStartingPrice(entity.getStartingPrice());
        dto.setStatus(entity.getStatus());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setMinIncrement(entity.getMinIncrement());
        dto.setAntiSnipingEnabled(entity.isAntiSnipingEnabled());
        dto.setAntiSnipingExtensionSeconds((int) entity.getAntiSnipingExtensionSeconds());
        dto.setCurrentWinnerId(entity.getCurrentWinnerId());
        if (entity.getCurrentWinnerId() != null && !entity.getCurrentWinnerId().isBlank()) {
            User winner = UserDAO.getInstance().findUserById(entity.getCurrentWinnerId());
            dto.setCurrentWinnerName(winner != null ? winner.getUsername() : entity.getCurrentWinnerId());
        }
        dto.setCategory(entity.getCategory());
        dto.setCategoryName(entity.getCategory());
        dto.setItemName(entity.getItemName());
        dto.setItemDescription(entity.getItemDescription());
        if (entity.getImageBase64() != null && !entity.getImageBase64().isBlank()) {
            dto.setImagePath("BASE64:" + entity.getImageBase64());
        } else {
            dto.setImagePath(entity.getImagePath());
        }
        dto.setTotalBids(entity.getBidHistory() != null ? entity.getBidHistory().size() : 0);
        return dto;
    }

    public Auction toEntity(AuctionDTO dto) {
        if (dto == null) return null;

        // Tạo Auction với constructor đầy đủ
        double startingPrice = dto.getStartingPrice() > 0 ? dto.getStartingPrice() : (dto.getCurrentPrice() > 0 ? dto.getCurrentPrice() : 0.0);
        Auction auction = new Auction(
                dto.getItemId() != null ? dto.getItemId() : "unknown",
                dto.getSellerId() != null ? dto.getSellerId() : "unknown",
                dto.getStartTime() != null ? dto.getStartTime() : LocalDateTime.now(),
                dto.getEndTime() != null ? dto.getEndTime() : LocalDateTime.now().plusDays(7),
                startingPrice
        );

        // Set ID nếu có
        if (dto.getId() != null && !dto.getId().isEmpty()) {
            auction.setId(dto.getId());
        }

        // Set các trường khác
        if (dto.getCurrentPrice() > 0) {
            auction.setCurrentPrice(dto.getCurrentPrice());
        }
        if (dto.getStartingPrice() > 0) {
            auction.setStartingPrice(dto.getStartingPrice());
        }
        if (dto.getCurrentWinnerId() != null) {
            auction.setCurrentWinnerId(dto.getCurrentWinnerId());
        }
        if (dto.getStatus() != null) {
            auction.setStatus(dto.getStatus());
        }
        auction.setAntiSnipingEnabled(dto.isAntiSnipingEnabled());
        auction.setAntiSnipingExtensionSeconds(dto.getAntiSnipingExtensionSeconds());

        if (dto.getItemName() != null) {
            auction.setItemName(dto.getItemName());
        }
        if (dto.getItemDescription() != null) {
            auction.setItemDescription(dto.getItemDescription());
        }
        if (dto.getCategory() != null) {
            auction.setCategory(dto.getCategory());
        }
        if (dto.getSellerId() != null) {
            auction.setSellerId(dto.getSellerId());
        }
        if (dto.getMinIncrement() > 0) {
            auction.setMinIncrement(dto.getMinIncrement());
        }
        if (dto.getImagePath() != null && !dto.getImagePath().isBlank()) {
            if (dto.getImagePath().startsWith("BASE64:")) {
                auction.setImageBase64(dto.getImagePath().substring(7));
            } else {
                auction.setImagePath(dto.getImagePath());
            }
        }

        return auction;
    }
}