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
        if (out != null && isConnected()) {
            out.println("AUCTION_UPDATE"
                    + ":ID:" + auction.getItemId() // lúc đầu: auction.getId()
                    + ":PRICE:" + auction.getCurrentPrice()
                    + ":WINNER:" + auction.getCurrentWinnerId() // lúc đầu: auction.getCurrentWinner()
                    + ":STATUS:" + auction.getStatus());
        }
    }

    public Socket getClientSocket() { return clientSocket; }

    public boolean isConnected() {
        return clientSocket != null && !clientSocket.isClosed();
    }
}