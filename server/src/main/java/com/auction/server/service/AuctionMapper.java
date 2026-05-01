package com.auction.server.service; // Đổi lại package nếu cần

import com.auction.common.dto.AuctionDTO;
import com.auction.common.entity.Auction;

public class AuctionMapper {
    public AuctionDTO toDTO(Auction entity) {
        if (entity == null) return null;
        AuctionDTO dto = new AuctionDTO();
        dto.setId(entity.getId());
        dto.setItemId(entity.getItemId());
        dto.setCurrentPrice(entity.getCurrentPrice());
        return dto;
    }

    public Auction toEntity(AuctionDTO dto) {
        if (dto == null) return null;
        return null;
    }
}