CREATE TABLE auto_bid_configs (
                                  id VARCHAR(36) PRIMARY KEY,
                                  auction_id VARCHAR(36) NOT NULL,
                                  bidder_id VARCHAR(36) NOT NULL,
                                  max_bid DECIMAL(15,2) NOT NULL,
                                  increment_amount DECIMAL(15,2) NOT NULL,
                                  is_active BOOLEAN DEFAULT TRUE,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
                                  FOREIGN KEY (bidder_id) REFERENCES users(id) ON DELETE CASCADE,
                                  UNIQUE KEY unique_auction_bidder (auction_id, bidder_id)
);

CREATE INDEX idx_auto_bid_configs_auction ON auto_bid_configs(auction_id);