package com.auction.server.observer;

import com.auction.common.entity.Auction;
import com.auction.common.entity.User;
import com.auction.common.observer.ClientObserver;
import com.auction.server.dao.UserDAO;

import java.net.Socket;

/**
 * Server-side observer that enriches realtime updates with human-readable winner names.
 */
public class ServerClientObserver extends ClientObserver {

    public ServerClientObserver(Socket clientSocket) {
        super(clientSocket);
    }

    @Override
    public void update(Auction auction) {
        if (auction == null) {
            return;
        }
        String winnerId = auction.getCurrentWinnerId() != null ? auction.getCurrentWinnerId() : "";
        String winnerName = resolveUsername(winnerId);
        sendAuctionUpdate(auction.getId(), auction.getCurrentPrice(), winnerId, winnerName,
                auction.getStatus().name(),
                auction.getBidHistory() != null ? auction.getBidHistory().size() : 0);
    }

    private static String resolveUsername(String userId) {
        if (userId == null || userId.isBlank()) {
            return "";
        }
        User user = UserDAO.getInstance().findUserById(userId);
        return user != null ? user.getUsername() : userId;
    }
}
