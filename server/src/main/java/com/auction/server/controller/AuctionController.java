package com.auction.server.controller;

import com.auction.common.dto.AuctionDTO;
import com.auction.server.service.AuctionService;
import java.util.List;

/**
 * AuctionController - nhận request liên quan đến Auction từ ClientHandler,
 * ủy thác xử lí cho AuctionService.
 *
 * Attributes (theo diagram):
 *  - auctionService: AuctionService
 *
 * Methods (theo diagram):
 *  + getAllAuctions(): List<AuctionDTO>
 *  + getAuction(id): AuctionDTO
 *  + createAuction(request): void
 *  + updateAuction(id, dto): void
 *  + deleteAuction(id): void
 */
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public List<AuctionDTO> getAuctions() {
        return auctionService.getAllAuctions();
    }

    public AuctionDTO getAuction(String id) {
        return auctionService.getAuction(id);
    }

    public void createAuction(AuctionDTO request) {
        auctionService.createAuction(request);
    }

    public void updateAuction(String id, AuctionDTO dto) {
        auctionService.updateAuction(id, dto);
    }

    public void deleteAuction(String id) {
        auctionService.deleteAuction(id);
    }
}
