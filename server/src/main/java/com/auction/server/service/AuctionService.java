package com.auction.server.service;

import com.auction.common.dto.AuctionDTO;
import com.auction.server.dao.AuctionDAO;

import java.util.ArrayList;
import java.util.List;
/**
 * AuctionService.java: Quản lý vòng đời của cuộc đấu giá (tạo mới, bắt đầu, kết thúc) và các thao tác liên quan đến thông tin cuộc đấu giá.
 */
public class AuctionService {
    private AuctionDAO auctionDAO;

    public AuctionService() {
        this.auctionDAO = AuctionDAO.getInstance();
    }

    public List<AuctionDTO> getAllAuctions() {
        // Trả về List<AuctionDTO> như AuctionController yêu cầu
        return new ArrayList<AuctionDTO>();
    }

    public AuctionDTO getAuction(String id) {
        return new AuctionDTO();
    }

    public void createAuction(AuctionDTO request) {
        // Logic save
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

    public void setAuctionDAO(AuctionDAO auctionDAO) {
        this.auctionDAO = auctionDAO;
    }
}