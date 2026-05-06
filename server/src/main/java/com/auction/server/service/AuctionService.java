package com.auction.server.service;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.exception.AuctionNotFoundException;
import com.auction.server.dao.AuctionDAO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuctionService {
    private final AuctionDAO auctionDAO;
    private final AuctionMapper mapper;

    public AuctionService() {
        this.auctionDAO = AuctionDAO.getInstance();
        this.mapper = new AuctionMapper();
    }

    public List<AuctionDTO> getAllAuctions() {
        return auctionDAO.getAllAuctions()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public AuctionDTO getAuction(String id) throws AuctionNotFoundException {
        Auction auction = auctionDAO.getAuction(id);
        if (auction == null) {
            throw new AuctionNotFoundException("Auction not found: " + id);
        }
        return mapper.toDTO(auction);
    }

    public void createAuction(AuctionDTO dto) {
        Auction auction = mapper.toEntity(dto);
        if (auction.getId() == null) {
            auction.setId(UUID.randomUUID().toString());
        }
        if (auction.getStatus() == null) {
            auction.setStatus(AuctionStatus.DRAFT);
        }
        if (auction.getStartTime() == null) {
            auction.setStartTime(LocalDateTime.now());
        }
        if (auction.getEndTime() == null) {
            auction.setEndTime(LocalDateTime.now().plusDays(7));
        }
        auctionDAO.saveAuction(auction);
    }

    public void updateAuction(String id, AuctionDTO dto) throws AuctionNotFoundException {
        Auction existing = auctionDAO.getAuction(id);
        if (existing == null) {
            throw new AuctionNotFoundException("Auction not found: " + id);
        }
        if (dto.getCurrentPrice() > 0) {
            existing.setCurrentPrice(dto.getCurrentPrice());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        auctionDAO.saveAuction(existing);
    }

    public void deleteAuction(String id) throws AuctionNotFoundException {
        Auction existing = auctionDAO.getAuction(id);
        if (existing == null) {
            throw new AuctionNotFoundException("Auction not found: " + id);
        }
        auctionDAO.deleteAuction(id);
    }

    public void notifyAuctionEnded(Auction auction) {
        NotificationService.getInstance().sendAuctionEndNotification(auction);
    }

    // Thêm method này
    public void notifyAuctionStarted(Auction auction) {
        NotificationService.getInstance().sendBidUpdate(auction); // hoặc gửi thông báo riêng
    }
}