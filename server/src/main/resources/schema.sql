-- =====================================================
-- SCHEMA SQL - ONLINE AUCTION SYSTEM
-- =====================================================

-- Tạo bảng users (lưu thông tin người dùng)
CREATE TABLE IF NOT EXISTS users (
                                     id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('BIDDER', 'SELLER', 'ADMIN')),
    balance DECIMAL(15,2) DEFAULT 0.00,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Tạo bảng items (sản phẩm đấu giá)
CREATE TABLE IF NOT EXISTS items (
                                     id VARCHAR(36) PRIMARY KEY,
    seller_id VARCHAR(36) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    item_type VARCHAR(20) CHECK (item_type IN ('ELECTRONICS', 'ART', 'VEHICLE')),
    -- Các trường đặc trưng có thể lưu dưới dạng JSON hoặc riêng lẻ (tuỳ thiết kế)
    attributes TEXT, -- JSON chứa thông số như brand, model, artist, year...
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Tạo bảng auctions (phiên đấu giá)
CREATE TABLE IF NOT EXISTS auctions (
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
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (current_winner_id) REFERENCES users(id) ON DELETE SET NULL
    );

-- Tạo bảng bid_transactions (lịch sử đặt giá)
CREATE TABLE IF NOT EXISTS bid_transactions (
                                                id VARCHAR(36) PRIMARY KEY,
    auction_id VARCHAR(36) NOT NULL,
    bidder_id VARCHAR(36) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    bid_time TIMESTAMP NOT NULL,
    is_auto_bid BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    FOREIGN KEY (bidder_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Tạo bảng auto_bid_configs (cấu hình đấu giá tự động)
CREATE TABLE IF NOT EXISTS auto_bid_configs (
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

-- Indexes để tối ưu truy vấn
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_items_seller ON items(seller_id);
CREATE INDEX idx_auctions_seller ON auctions(seller_id);
CREATE INDEX idx_auctions_status ON auctions(status);
CREATE INDEX idx_auctions_end_time ON auctions(end_time);
CREATE INDEX idx_bid_transactions_auction ON bid_transactions(auction_id);
CREATE INDEX idx_bid_transactions_bidder ON bid_transactions(bidder_id);
CREATE INDEX idx_auto_bid_configs_auction ON auto_bid_configs(auction_id);