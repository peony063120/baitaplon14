package com.auction.common.observer;

import com.auction.common.entity.Auction;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ClientObserver — implements Observer.
 * Đại diện cho một client đang xem phiên đấu giá.
 * Khi AuctionSubject notify, gửi dữ liệu auction mới nhất qua Socket tới client.
 */
public class ClientObserver implements Observer {

    private final Socket clientSocket;
    private PrintWriter out;

    public ClientObserver(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (Exception e) {
            System.err.println("ClientObserver: lỗi mở output stream - " + e.getMessage());
        }
    }

    @Override
    public void update(Auction auction) {
        if (auction == null) {
            return;
        }
        sendAuctionUpdate(
                auction.getId(),
                auction.getCurrentPrice(),
                auction.getCurrentWinnerId() != null ? auction.getCurrentWinnerId() : "",
                null,
                auction.getStatus().name());
    }

    protected void sendAuctionUpdate(String auctionId, double price, String winnerId,
                                     String winnerName, String status) {
        if (out == null || !isConnected()) {
            return;
        }
        StringBuilder message = new StringBuilder("AUCTION_UPDATE")
                .append(":ID:").append(auctionId)
                .append(":PRICE:").append(price)
                .append(":WINNER:").append(winnerId != null ? winnerId : "");
        if (winnerName != null && !winnerName.isBlank()) {
            message.append(":WINNER_NAME:").append(winnerName);
        }
        message.append(":STATUS:").append(status);
        out.println(message.toString());
    }

    public Socket getClientSocket() { return clientSocket; }

    public boolean isConnected() {
        return clientSocket != null && !clientSocket.isClosed();
    }
}