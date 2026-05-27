CREATE TABLE bid_transactions (
                                  id VARCHAR(36) PRIMARY KEY,
                                  auction_id VARCHAR(36) NOT NULL,
                                  bidder_id VARCHAR(36) NOT NULL,
                                  amount DECIMAL(15,2) NOT NULL,
                                  bid_time TIMESTAMP NOT NULL,
                                  is_auto_bid BOOLEAN DEFAULT FALSE,
                                  FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
                                  FOREIGN KEY (bidder_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_bid_transactions_auction ON bid_transactions(auction_id);
CREATE INDEX idx_bid_transactions_bidder ON bid_transactions(bidder_id);