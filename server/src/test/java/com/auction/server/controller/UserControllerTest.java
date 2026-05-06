package com.auction.server.controller;

import com.auction.common.dto.LoginRequest;
import com.auction.common.dto.LoginResponse;
import com.auction.common.dto.UserDTO;
import com.auction.common.exception.AuctionException;
import com.auction.common.exception.AuctionNotFoundException;
import com.auction.common.exception.InvalidBidException;
import com.auction.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @InjectMocks private UserController userController;

    private UserDTO userDTO;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("pass");
        userDTO.setEmail("test@example.com");
        userDTO.setFullName("Test User");
        userDTO.setRole("BIDDER");

        loginRequest = new LoginRequest("testuser", "pass", "BIDDER");
        loginResponse = new LoginResponse(true, "Login OK", "testuser", "testuser", "BIDDER", "session123", 100.0);
    }

    @Test
    void register_ShouldCallService() {
        doNothing().when(userService).register(any(UserDTO.class));
        userController.register(userDTO);
        verify(userService, times(1)).register(userDTO);
    }

    @Test
    void login_ShouldReturnLoginResponse() {
        when(userService.authenticate("testuser", "pass")).thenReturn(loginResponse);
        LoginResponse response = userController.login(loginRequest);
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void getUserProfile_ShouldReturnUserDTO() {
        when(userService.getUserById("testuser")).thenReturn(userDTO);
        UserDTO result = userController.getUserProfile("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void updateProfile_ShouldCallService() throws AuctionNotFoundException {
        doNothing().when(userService).updateProfile("testuser", userDTO);
        userController.updateProfile("testuser", userDTO);
        verify(userService, times(1)).updateProfile("testuser", userDTO);
    }

    @Test
    void addBalance_ShouldCallService() throws InvalidBidException, AuctionNotFoundException, AuctionException {
        doNothing().when(userService).addBalance("testuser", 100.0);
        userController.addBalance("testuser", 100.0);
        verify(userService, times(1)).addBalance("testuser", 100.0);
    }
}