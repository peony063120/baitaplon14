package com.auction.server.dao;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class AuctionDAOTest {

    private AuctionDAO auctionDAO;
    private Auction testAuction;

    @BeforeEach
    void setUp() {
        auctionDAO = AuctionDAO.getInstance();
        testAuction = new Auction("item1", "seller1",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1),
                100.0);
        testAuction.setId("auc_test_1");
        testAuction.setStatus(AuctionStatus.DRAFT);
    }

    @Test
    void testSaveAndGetAuction() {
        auctionDAO.saveAuction(testAuction);

        Auction retrieved = auctionDAO.getAuction("auc_test_1");
        assertNotNull(retrieved);
        assertEquals("auc_test_1", retrieved.getId());
        assertEquals("item1", retrieved.getItemId());
    }

    @Test
    void testGetAllAuctions() {
        auctionDAO.saveAuction(testAuction);
        Collection<Auction> auctions = auctionDAO.getAllAuctions();
        assertTrue(auctions.size() >= 1);
    }

    @Test
    void testDeleteAuction() {
        auctionDAO.saveAuction(testAuction);
        auctionDAO.deleteAuction("auc_test_1");

        Auction deleted = auctionDAO.getAuction("auc_test_1");
        assertNull(deleted);
    }

    @Test
    void testGetAuctionsByStatus() {
        testAuction.setStatus(AuctionStatus.RUNNING);
        auctionDAO.saveAuction(testAuction);

        var runningAuctions = auctionDAO.getAuctionsByStatus(AuctionStatus.RUNNING);
        assertTrue(runningAuctions.stream().anyMatch(a -> "auc_test_1".equals(a.getId())));
    }
}