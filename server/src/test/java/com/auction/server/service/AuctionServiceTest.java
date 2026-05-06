package com.auction.server.service;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.exception.AuctionNotFoundException;
import com.auction.server.dao.AuctionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionDAO auctionDAO;

    @InjectMocks
    private AuctionService auctionService;

    private Auction sampleAuction;

    @BeforeEach
    void setUp() {
        sampleAuction = new Auction("item1", "seller1",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1),
                100.0);
        sampleAuction.setId("auc1");
        sampleAuction.setStatus(AuctionStatus.DRAFT);
    }

    @Test
    void getAllAuctions_ShouldReturnList() {
        when(auctionDAO.getAllAuctions()).thenReturn(List.of(sampleAuction));
        List<AuctionDTO> result = auctionService.getAllAuctions();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("auc1", result.get(0).getId());
    }

    @Test
    void getAuction_Exists() throws AuctionNotFoundException {
        when(auctionDAO.getAuction("auc1")).thenReturn(sampleAuction);
        AuctionDTO dto = auctionService.getAuction("auc1");
        assertNotNull(dto);
        assertEquals("auc1", dto.getId());
    }

    @Test
    void getAuction_NotFound() {
        when(auctionDAO.getAuction("unknown")).thenReturn(null);
        assertThrows(AuctionNotFoundException.class, () -> auctionService.getAuction("unknown"));
    }

    @Test
    void createAuction() {
        AuctionDTO dto = new AuctionDTO();
        dto.setItemId("item2");
        dto.setSellerId("seller2");
        dto.setCurrentPrice(200.0);
        doNothing().when(auctionDAO).saveAuction(any(Auction.class));

        auctionService.createAuction(dto);
        verify(auctionDAO, times(1)).saveAuction(any(Auction.class));
    }

    @Test
    void updateAuction() throws AuctionNotFoundException {
        when(auctionDAO.getAuction("auc1")).thenReturn(sampleAuction);
        doAnswer(invocation -> null).when(auctionDAO).saveAuction(any());

        AuctionDTO updateDto = new AuctionDTO();
        updateDto.setCurrentPrice(250.0);
        auctionService.updateAuction("auc1", updateDto);
        assertEquals(250.0, sampleAuction.getCurrentPrice());
        verify(auctionDAO).saveAuction(sampleAuction);
    }

    @Test
    void deleteAuction() throws AuctionNotFoundException {
        when(auctionDAO.getAuction("auc1")).thenReturn(sampleAuction);
        doAnswer(invocation -> null).when(auctionDAO).deleteAuction("auc1");
        auctionService.deleteAuction("auc1");
        verify(auctionDAO).deleteAuction("auc1");
    }
}