package com.auction.server.service;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.exception.AuctionNotFoundException;
import com.auction.common.observer.AuctionSubject;
import com.auction.server.config.ServerConfig;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.mapper.AuctionMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuctionService {
    private final AuctionDAO auctionDAO;
    private final AuctionMapper mapper;
    private final long defaultAuctionDurationHours;
    private AuctionSubject auctionSubject;

    public AuctionService() {
        this.auctionDAO = AuctionDAO.getInstance();
        this.mapper = new AuctionMapper();
        this.defaultAuctionDurationHours = ServerConfig.getInstance().getDefaultAuctionDurationHours();
        this.auctionSubject = AuctionSubject.getInstance();
    }

    // Thêm constructor này để test có thể inject mock
    public AuctionService(AuctionDAO auctionDAO) {
        this.auctionDAO = auctionDAO;
        this.mapper = new AuctionMapper();
        this.defaultAuctionDurationHours = 24; // Default 24 hours for tests
        this.auctionSubject = AuctionSubject.getInstance();
    }

    public void setAuctionSubject(AuctionSubject auctionSubject) {
        this.auctionSubject = auctionSubject;
    }
    
    public List<AuctionDTO> getAllAuctions() {
        return auctionDAO.getAllAuctions()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<AuctionDTO> getAuctionsBySeller(String sellerId) {
        return auctionDAO.getAuctionsBySeller(sellerId)
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
            auction.setStatus(AuctionStatus.PENDING);
        }
        if (auction.getStartTime() == null) {
            auction.setStartTime(LocalDateTime.now());
        }
        if (auction.getEndTime() == null) {
            // Sử dụng thời gian mặc định từ cấu hình (24 giờ)
            auction.setEndTime(LocalDateTime.now().plusHours(defaultAuctionDurationHours));
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

    public void updateAuctionStatus(String id, AuctionStatus status) throws AuctionNotFoundException {
        Auction existing = auctionDAO.getAuction(id);
        if (existing == null) {
            throw new AuctionNotFoundException("Auction not found: " + id);
        }
        existing.setStatus(status);
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