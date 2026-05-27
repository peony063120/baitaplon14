CREATE TABLE auctions (
                          id VARCHAR(36) PRIMARY KEY,
                          item_id VARCHAR(36) NOT NULL,
                          seller_id VARCHAR(36) NOT NULL,
                          start_time TIMESTAMP NOT NULL,
                          end_time TIMESTAMP NOT NULL,
                          starting_price DECIMAL(15,2) NOT NULL,
                          current_price DECIMAL(15,2) NOT NULL,
                          current_winner_id VARCHAR(36),
                          min_increment DECIMAL(15,2) DEFAULT 10000.00,
                          status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELLED')),
                          anti_sniping_enabled BOOLEAN DEFAULT FALSE,
                          anti_sniping_extension_seconds INT DEFAULT 0,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
                          FOREIGN KEY (current_winner_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_auctions_seller ON auctions(seller_id);
CREATE INDEX idx_auctions_status ON auctions(status);
CREATE INDEX idx_auctions_end_time ON auctions(end_time);