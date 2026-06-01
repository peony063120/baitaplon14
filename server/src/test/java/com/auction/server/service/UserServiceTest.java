package com.auction.server.service;

import com.auction.common.dto.LoginResponse;
import com.auction.common.dto.UserDTO;
import com.auction.common.entity.Bidder;
import com.auction.common.entity.User;
import com.auction.common.exception.AuctionException;
import com.auction.common.exception.AuctionNotFoundException;
import com.auction.common.exception.InvalidBidException;
import com.auction.server.dao.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserService userService;

    private UserDTO userDTO;
    private Bidder bidder;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setUsername("john");
        userDTO.setPassword("secret");
        userDTO.setEmail("john@example.com");
        userDTO.setFullName("John Doe");
        userDTO.setRole("BIDDER");

        bidder = new Bidder("john", "secret", "john@example.com", "John Doe");
        bidder.addBalance(100.0);
    }

    @Test
    void register_Success() {
        // register() không gọi findUserByUsername, chỉ gọi saveUser
        doNothing().when(userDAO).saveUser(any(User.class));

        assertDoesNotThrow(() -> userService.register(userDTO));
        verify(userDAO, times(1)).saveUser(any(User.class));
    }

    @Test
    void authenticate_Success() {
        when(userDAO.findUserByUsername("john")).thenReturn(bidder);
        LoginResponse response = userService.authenticate("john", "secret");
        assertTrue(response.isSuccess());
        assertEquals("john", response.getUsername());
        assertEquals(100.0, response.getBalance());
        assertNotNull(response.getSessionToken());
    }

    @Test
    void authenticate_Fail_WrongPassword() {
        when(userDAO.findUserByUsername("john")).thenReturn(bidder);
        LoginResponse response = userService.authenticate("john", "wrong");
        assertFalse(response.isSuccess());
        assertEquals("Invalid username or password", response.getMessage());
    }

    @Test
    void authenticate_Fail_UserNotFound() {
        when(userDAO.findUserByUsername("unknown")).thenReturn(null);
        LoginResponse response = userService.authenticate("unknown", "pass");
        assertFalse(response.isSuccess());
    }

    @Test
    void getUserById_Exists() {
        bidder.setId("user-john-1");
        when(userDAO.findUserById("user-john-1")).thenReturn(bidder);
        UserDTO dto = userService.getUserById("user-john-1");
        assertNotNull(dto);
        assertEquals("user-john-1", dto.getId());
        assertEquals("john", dto.getUsername());
        assertEquals(100.0, dto.getBalance());
    }

    @Test
    void getUserById_FallbackUsername() {
        when(userDAO.findUserById("john")).thenReturn(null);
        when(userDAO.findUserByUsername("john")).thenReturn(bidder);
        UserDTO dto = userService.getUserById("john");
        assertNotNull(dto);
        assertEquals("john", dto.getUsername());
    }

    @Test
    void getUserById_NotFound() {
        when(userDAO.findUserById("unknown")).thenReturn(null);
        when(userDAO.findUserByUsername("unknown")).thenReturn(null);
        UserDTO dto = userService.getUserById("unknown");
        assertNull(dto);
    }

    @Test
    void updateProfile_Success() throws AuctionNotFoundException {
        when(userDAO.findUserByUsername("john")).thenReturn(bidder);
        doNothing().when(userDAO).saveUser(any(User.class));

        userDTO.setEmail("new@example.com");
        userDTO.setFullName("John Updated");
        assertDoesNotThrow(() -> userService.updateProfile("john", userDTO));
        verify(userDAO, times(1)).saveUser(bidder);
        assertEquals("new@example.com", bidder.getEmail());
        assertEquals("John Updated", bidder.getFullName());
    }

    @Test
    void updateProfile_UserNotFound() {
        when(userDAO.findUserByUsername("unknown")).thenReturn(null);
        assertThrows(AuctionNotFoundException.class,
                () -> userService.updateProfile("unknown", userDTO));
    }

    @Test
    void addBalance_Success() throws AuctionException, AuctionNotFoundException, InvalidBidException {
        when(userDAO.findUserByUsername("john")).thenReturn(bidder);
        doNothing().when(userDAO).saveUser(any(User.class));

        assertDoesNotThrow(() -> userService.addBalance("john", 50.0));
        assertEquals(150.0, bidder.getBalance());
        verify(userDAO, times(1)).saveUser(bidder);
    }

    @Test
    void addBalance_NegativeAmount() {
        assertThrows(InvalidBidException.class,
                () -> userService.addBalance("john", -10.0));
    }

    @Test
    void addBalance_UserNotFound() {
        when(userDAO.findUserByUsername("unknown")).thenReturn(null);
        assertThrows(AuctionNotFoundException.class,
                () -> userService.addBalance("unknown", 100.0));
    }

    @Test
    void addBalance_NotABidder() {
        User member = new User("member", "pass", "m@x.com", "Member") {
            @Override public String getRole() { return "MEMBER"; }
        };
        when(userDAO.findUserByUsername("member")).thenReturn(member);
        assertThrows(AuctionException.class,
                () -> userService.addBalance("member", 100.0));
    }
}