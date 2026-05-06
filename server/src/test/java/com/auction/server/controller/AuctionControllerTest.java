package com.auction.server.controller;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.exception.AuctionNotFoundException;
import com.auction.server.service.AuctionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionControllerTest {

    @Mock private AuctionService auctionService;
    @InjectMocks private AuctionController auctionController;

    private AuctionDTO auctionDTO;

    @BeforeEach
    void setUp() {
        auctionDTO = new AuctionDTO();
        auctionDTO.setId("auc1");
        auctionDTO.setItemId("item1");
        auctionDTO.setSellerId("seller1");
        auctionDTO.setCurrentPrice(100.0);
    }

    @Test
    void getAllAuctions_ShouldReturnList() {
        when(auctionService.getAllAuctions()).thenReturn(List.of(auctionDTO));
        List<AuctionDTO> result = auctionController.getAllAuctions();
        assertEquals(1, result.size());
        assertEquals("auc1", result.get(0).getId());
    }

    @Test
    void getAuction_ShouldReturnAuction() throws AuctionNotFoundException {
        when(auctionService.getAuction("auc1")).thenReturn(auctionDTO);
        AuctionDTO result = auctionController.getAuction("auc1");
        assertNotNull(result);
        assertEquals("auc1", result.getId());
    }

    @Test
    void getAuction_NotFound_ShouldThrowException() throws AuctionNotFoundException {
        when(auctionService.getAuction("unknown")).thenThrow(new AuctionNotFoundException("unknown"));
        assertThrows(AuctionNotFoundException.class, () -> auctionController.getAuction("unknown"));
    }

    @Test
    void createAuction_ShouldCallService() {
        doNothing().when(auctionService).createAuction(auctionDTO);
        auctionController.createAuction(auctionDTO);
        verify(auctionService, times(1)).createAuction(auctionDTO);
    }

    @Test
    void updateAuction_ShouldCallService() throws AuctionNotFoundException {
        doNothing().when(auctionService).updateAuction("auc1", auctionDTO);
        auctionController.updateAuction("auc1", auctionDTO);
        verify(auctionService, times(1)).updateAuction("auc1", auctionDTO);
    }

    @Test
    void deleteAuction_ShouldCallService() throws AuctionNotFoundException {
        doNothing().when(auctionService).deleteAuction("auc1");
        auctionController.deleteAuction("auc1");
        verify(auctionService, times(1)).deleteAuction("auc1");
    }
}