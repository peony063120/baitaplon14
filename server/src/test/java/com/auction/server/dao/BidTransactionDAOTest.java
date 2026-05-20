package com.auction.server.dao;

import com.auction.common.entity.BidTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BidTransactionDAOTest {

    private BidTransactionDAO bidDAO;

    @BeforeEach
    void setUp() {
        bidDAO = BidTransactionDAO.getInstance();
        bidDAO.clear(); // Xóa dữ liệu cũ trước mỗi test
    }

    @Test
    void testSaveAndGetBidHistory() {
        BidTransaction tx = new BidTransaction("auc1", "bidder1", 150.0, LocalDateTime.now(), false);
        bidDAO.saveBidTransaction(tx);

        List<BidTransaction> history = bidDAO.getBidHistory("auc1");
        assertEquals(1, history.size());
        assertEquals("bidder1", history.get(0).getBidderId());
        assertEquals(150.0, history.get(0).getAmount());
    }

    @Test
    void testGetBidsByUser() {
        BidTransaction tx1 = new BidTransaction("auc1", "bidder1", 100.0, LocalDateTime.now(), false);
        BidTransaction tx2 = new BidTransaction("auc2", "bidder1", 200.0, LocalDateTime.now(), true);

        bidDAO.saveBidTransaction(tx1);
        bidDAO.saveBidTransaction(tx2);

        List<BidTransaction> userBids = bidDAO.getBidsByUser("bidder1");
        assertTrue(userBids.size() >= 2);
    }
}