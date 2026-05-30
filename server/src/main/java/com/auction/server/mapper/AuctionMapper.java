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

        return auction;
    }
}