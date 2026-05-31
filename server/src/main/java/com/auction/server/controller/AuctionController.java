package com.auction.server.controller;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.exception.AuctionNotFoundException;
import com.auction.server.service.AuctionService;

import java.util.List;

/**
 * AuctionController - nhận request liên quan đến Auction từ ClientHandler,
 * ủy thác xử lý cho AuctionService.
 *
 * Theo diagram server.html:
 *   - auctionService: AuctionService
 *   + getAllAuctions(): List<AuctionDTO>
 *   + getAuction(id: String): AuctionDTO
 *   + createAuction(request: AuctionDTO): void
 *   + updateAuction(id: String, dto: AuctionDTO): void
 *   + deleteAuction(id: String): void
 */
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public List<AuctionDTO> getAllAuctions() {
        return auctionService.getAllAuctions();
    }

    public List<AuctionDTO> getActiveAuctions() {
        return auctionService.getActiveAuctions();
    }

    public List<AuctionDTO> getAuctionsBySeller(String sellerId) {
        return auctionService.getAuctionsBySeller(sellerId);
    }

    public AuctionDTO getAuction(String id) throws AuctionNotFoundException {
        return auctionService.getAuction(id);
    }

    public void createAuction(AuctionDTO request) {
        auctionService.createAuction(request);
    }

    public void updateAuction(String id, AuctionDTO dto) throws AuctionNotFoundException {
        auctionService.updateAuction(id, dto);
    }

    public void deleteAuction(String id) throws AuctionNotFoundException {
        auctionService.deleteAuction(id);
    }

    public void updateAuctionStatus(String id, com.auction.common.enums.AuctionStatus status) {
        try {
            auctionService.updateAuctionStatus(id, status);
        } catch (AuctionNotFoundException e) {
            e.printStackTrace();
        }
    }
}