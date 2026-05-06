package com.auction.server.service;

import com.auction.common.entity.AutoBidConfig;
import com.auction.common.entity.Auction;
import com.auction.common.entity.Bidder;
import com.auction.server.dao.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoBidServiceTest {

    @Mock private UserDAO userDAO;
    private AutoBidService autoBidService;
    private Auction auction;

    @BeforeEach
    void setUp() {
        autoBidService = new AutoBidService();
        auction = new Auction("item1", LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1), 100.0);
        auction.setId("auc1");
    }

    @Test
    void registerAndProcessAutoBids() {
        AutoBidConfig config1 = new AutoBidConfig("auc1", "bidder1", 200.0, 10.0);
        autoBidService.registerAutoBid(config1);
        assertDoesNotThrow(() -> autoBidService.processAutoBids(auction));
    }

    @Test
    void cancelAutoBid() {
        AutoBidConfig config = new AutoBidConfig("auc1", "bidder1", 200.0, 10.0);
        autoBidService.registerAutoBid(config);
        autoBidService.cancelAutoBid("auc1", "bidder1");
        // should not throw
    }
}