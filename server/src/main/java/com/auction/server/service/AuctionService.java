package com.auction.server.service;

import com.auction.common.dto.AuctionDTO;
import com.auction.server.dao.AuctionDAO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AuctionService.java: Quản lý vòng đời của cuộc đấu giá (tạo mới, bắt đầu, kết thúc) và các thao tác liên quan đến thông tin cuộc đấu giá.
 */
public class AuctionService {
    private final AuctionDAO auctionDAO;
    private final AuctionMapper mapper;

    public AuctionService() {
        this.auctionDAO = AuctionDAO.getInstance();
        this.mapper = new AuctionMapper(); //khởi tạo mapper
    }

    public List<AuctionDTO> getAllAuctions() {
        return auctionDAO.getAllAuctions()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    public AuctionDTO getAuction(String id) {
        return null;
    }

    public void createAuction(AuctionDTO dto) {
        // Logic sử dụng mapper.toEntity(dto) và auctionDAO.save()
    }

    public void updateAuction(String id, AuctionDTO dto) {
        // Logic update
    }

    public void deleteAuction(String id) {
        // Logic delete
    }

    // Getters & Setters
    public AuctionDAO getAuctionDAO() {
        return auctionDAO;
    }


}