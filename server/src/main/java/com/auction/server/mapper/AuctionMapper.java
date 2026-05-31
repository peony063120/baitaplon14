package com.auction.server.mapper; // Đổi lại package nếu cần

import com.auction.common.dto.AuctionDTO;
import com.auction.common.entity.Auction;

import java.time.LocalDateTime;

public class AuctionMapper {

    public AuctionDTO toDTO(Auction entity) {
        if (entity == null) return null;
        AuctionDTO dto = new AuctionDTO();
        dto.setId(entity.getId());
        dto.setItemId(entity.getItemId());
        dto.setSellerId(entity.getSellerId());
        dto.setCurrentPrice(entity.getCurrentPrice());
        dto.setStartingPrice(entity.getCurrentPrice());
        dto.setStatus(entity.getStatus());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setMinIncrement(entity.getMinIncrement());
        dto.setAntiSnipingEnabled(entity.isAntiSnipingEnabled());
        dto.setAntiSnipingExtensionSeconds((int) entity.getAntiSnipingExtensionSeconds());
        dto.setCurrentWinnerId(entity.getCurrentWinnerId());
        dto.setCategory(entity.getCategory());
        dto.setCategoryName(entity.getCategory());
        dto.setItemName(entity.getItemName());
        dto.setItemDescription(entity.getItemDescription());
        dto.setImagePath(entity.getImagePath());
        return dto;
    }

    public Auction toEntity(AuctionDTO dto) {
        if (dto == null) return null;

        // Tạo Auction với constructor đầy đủ
        Auction auction = new Auction(
                dto.getItemId() != null ? dto.getItemId() : "unknown",
                dto.getSellerId() != null ? dto.getSellerId() : "unknown",
                dto.getStartTime() != null ? dto.getStartTime() : LocalDateTime.now(),
                dto.getEndTime() != null ? dto.getEndTime() : LocalDateTime.now().plusDays(7),
                dto.getStartingPrice() > 0 ? dto.getStartingPrice() : dto.getCurrentPrice()
        );

        // Set ID nếu có
        if (dto.getId() != null && !dto.getId().isEmpty()) {
            auction.setId(dto.getId());
        }

        // Set các trường khác
        if (dto.getCurrentPrice() > 0) {
            auction.setCurrentPrice(dto.getCurrentPrice());
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
        if (dto.getImagePath() != null) {
            auction.setImagePath(dto.getImagePath());
        }
        if (dto.getSellerId() != null) {
            auction.setSellerId(dto.getSellerId());
        }
        if (dto.getStartingPrice() > 0) {
            auction.setCurrentPrice(dto.getStartingPrice());
        }

        return auction;
    }
}