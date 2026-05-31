-- Mock data for client-side testing (no server needed)
-- Format: simple INSERT statements with literal values only
-- Load by: SqlMockReader.java

-- ==================== USERS ====================
INSERT INTO users (id, username, password, email, full_name, role, balance, active) VALUES
('user-001', 'admin', 'admin', 'admin@auction.com', 'Administrator', 'ADMIN', 0.00, TRUE),
('user-002', 'bidder1', 'bidder1', 'bidder1@auction.com', 'Alice Nguyen', 'BIDDER', 1000.00, TRUE),
('user-003', 'bidder2', 'bidder2', 'bidder2@auction.com', 'Bob Tran', 'BIDDER', 500.00, TRUE),
('user-004', 'bidder3', 'bidder3', 'bidder3@auction.com', 'Charlie Pham', 'BIDDER', 2000.00, TRUE),
('user-005', 'seller1', 'seller1', 'seller1@auction.com', 'David Le', 'SELLER', 0.00, TRUE),
('user-006', 'seller2', 'seller2', 'seller2@auction.com', 'Eva Hoang', 'SELLER', 0.00, TRUE),
('user-007', 'bidder4', 'bidder4', 'bidder4@auction.com', 'Fiona Le', 'BIDDER', 3000.00, TRUE),
('user-008', 'bidder5', 'bidder5', 'bidder5@auction.com', 'George Vo', 'BIDDER', 10000.00, TRUE),
('user-009', 'seller3', 'seller3', 'seller3@auction.com', 'Helen Dang', 'SELLER', 0.00, TRUE),
('user-010', 'seller4', 'seller4', 'seller4@auction.com', 'Ivan Tran', 'SELLER', 0.00, TRUE);

-- ==================== ITEMS ====================
INSERT INTO items (id, seller_id, name, description, category, item_type) VALUES
('item-001', 'user-005', '[VEHICLE] BMW X5 2024', 'BMW X5 2024, white, fully loaded, 5000km driven', 'Vehicles', 'VEHICLE'),
('item-002', 'user-005', '[ELECTRONICS] MacBook Pro M4', 'Laptop Apple MacBook Pro M4 Max 64GB RAM, 2TB SSD', 'Electronics', 'ELECTRONICS'),
('item-003', 'user-006', '[ART] Ha Long Bay Painting', 'Ha Long Bay oil painting landscape, 90x120cm', 'Art', 'ART'),
('item-004', 'user-005', '[ELECTRONICS] iPhone 16 Pro Max', 'iPhone 16 Pro Max 512GB, Natural Titanium, 99% new', 'Electronics', 'ELECTRONICS'),
('item-005', 'user-006', '[VEHICLE] Toyota Camry 2023', 'Toyota Camry 2.5Q, 15000km driven, full maintenance', 'Vehicles', 'VEHICLE'),
('item-006', 'user-006', '[ART] Rolex Submariner Watch', 'Rolex Submariner Date, authentic, 2022', 'Art', 'ART'),
('item-007', 'user-005', '[ELECTRONICS] Sony PlayStation 5', 'PS5 Slim Digital Edition, 2 controllers, 5 games', 'Electronics', 'ELECTRONICS'),
('item-008', 'user-006', '[ART] Bronze Buddha Statue', 'Bronze Shakyamuni Buddha statue, 60cm tall, 15kg', 'Art', 'ART'),
('item-009', 'user-005', '[ELECTRONICS] Laptop Dell XPS 16', 'Laptop Dell XPS 16 Intel Ultra 9 185H, 32GB RAM, 1TB SSD', 'Electronics', 'ELECTRONICS'),
('item-010', 'user-009', '[JEWELRY] SJC Gold Bar 1L', 'SJC 9999 gold bar, 1 tael (37.5g), sealed', 'Jewelry', 'JEWELRY'),
('item-011', 'user-009', '[ELECTRONICS] Sony A7R V Camera', 'Sony Alpha A7R V full-frame, 61MP, with 24-70mm lens', 'Electronics', 'ELECTRONICS'),
('item-012', 'user-010', '[VEHICLE] Mercedes-Benz EQS 2025', 'Mercedes-Benz EQS 450+ electric, black, fully loaded, 100% new', 'Vehicles', 'VEHICLE');

-- ==================== AUCTIONS ====================
INSERT INTO auctions (id, item_id, seller_id, start_time, end_time, starting_price, current_price, min_increment, status, current_winner_id, anti_sniping_enabled, anti_sniping_extension_seconds) VALUES

-- DRAFT: upcoming
('auc-001', 'item-001', 'user-005', PLUS1DAY, PLUS8DAY, 30000.00, 30000.00, 500.00, 'DRAFT', NULL, TRUE, 30),
('auc-002', 'item-002', 'user-005', PLUS1DAY, PLUS4DAY, 500.00, 500.00, 10.00, 'DRAFT', NULL, TRUE, 30),
('auc-003', 'item-003', 'user-006', PLUS2DAY, PLUS9DAY, 800.00, 800.00, 20.00, 'DRAFT', NULL, FALSE, 0),
('auc-006', 'item-006', 'user-006', PLUS6HOUR, PLUS7DAY, 1500.00, 1500.00, 50.00, 'DRAFT', NULL, TRUE, 30),

-- RUNNING: active
('auc-004', 'item-004', 'user-005', MINUS2HOUR, PLUS1HOUR, 350.00, 385.00, 5.00, 'RUNNING', 'user-002', TRUE, 30),
('auc-005', 'item-005', 'user-006', MINUS4HOUR, PLUS3HOUR, 7000.00, 7200.00, 100.00, 'RUNNING', 'user-004', TRUE, 30),
('auc-007', 'item-007', 'user-005', MINUS1HOUR, PLUS2HOUR, 120.00, 135.00, 3.00, 'RUNNING', 'user-003', FALSE, 0),
('auc-008', 'item-008', 'user-006', MINUS30MIN, PLUS4HOUR, 500.00, 550.00, 20.00, 'RUNNING', NULL, TRUE, 20),

-- FINISHED: ended (view history)
('auc-009', 'item-001', 'user-005', MINUS3DAY, MINUS1HOUR, 29000.00, 32500.00, 500.00, 'FINISHED', 'user-002', TRUE, 30),

-- RUNNING: additional (diverse categories)
('auc-010', 'item-009', 'user-005', MINUS1HOUR, PLUS5HOUR, 450.00, 450.00, 10.00, 'RUNNING', NULL, TRUE, 30),
('auc-011', 'item-010', 'user-009', MINUS3HOUR, PLUS6HOUR, 850.00, 870.00, 20.00, 'RUNNING', 'user-007', TRUE, 20),

-- DRAFT: additional
('auc-012', 'item-011', 'user-009', PLUS1DAY, PLUS5DAY, 650.00, 650.00, 20.00, 'DRAFT', NULL, TRUE, 30),
('auc-013', 'item-012', 'user-010', PLUS2DAY, PLUS10DAY, 45000.00, 45000.00, 500.00, 'DRAFT', NULL, TRUE, 30);

-- ==================== BID TRANSACTIONS ====================
INSERT INTO bid_transactions (id, auction_id, bidder_id, amount, bid_time, is_auto_bid) VALUES

-- auc-004 (iPhone): 3 bids
('tx-001', 'auc-004', 'user-002', 355.00, MINUS100MIN, FALSE),
('tx-002', 'auc-004', 'user-003', 370.00, MINUS80MIN, FALSE),
('tx-003', 'auc-004', 'user-002', 385.00, MINUS50MIN, FALSE),

-- auc-005 (Camry): 4 bids
('tx-004', 'auc-005', 'user-004', 7100.00, MINUS200MIN, FALSE),
('tx-005', 'auc-005', 'user-002', 7150.00, MINUS150MIN, TRUE),
('tx-006', 'auc-005', 'user-004', 7200.00, MINUS100MIN, FALSE),
('tx-007', 'auc-005', 'user-002', 7200.00, MINUS50MIN, TRUE),

-- auc-007 (PS5): 3 bids
('tx-008', 'auc-007', 'user-003', 123.00, MINUS55MIN, FALSE),
('tx-009', 'auc-007', 'user-004', 130.00, MINUS40MIN, FALSE),
('tx-010', 'auc-007', 'user-003', 135.00, MINUS20MIN, FALSE),

-- auc-008 (Statue): 1 bid
('tx-011', 'auc-008', 'user-002', 550.00, MINUS25MIN, FALSE),

-- auc-009 (BMW old - FINISHED): 5 bids
('tx-012', 'auc-009', 'user-002', 29500.00, MINUS3DAY, FALSE),
('tx-013', 'auc-009', 'user-004', 30500.00, MINUS2DAY, FALSE),
('tx-014', 'auc-009', 'user-002', 31000.00, MINUS1DAY, FALSE),
('tx-015', 'auc-009', 'user-003', 31500.00, MINUS12HOUR, TRUE),
('tx-016', 'auc-009', 'user-002', 32500.00, MINUS2HOUR, FALSE),

-- auc-011 (SJC Gold): 2 bids (bidder4 vs bidder5)
('tx-017', 'auc-011', 'user-007', 860.00, MINUS120MIN, FALSE),
('tx-018', 'auc-011', 'user-008', 870.00, MINUS60MIN, FALSE);

-- ==================== AUTO BID CONFIGS ====================
INSERT INTO auto_bid_configs (id, auction_id, bidder_id, max_bid, increment_amount, is_active) VALUES
('ab-001', 'auc-004', 'user-003', 400.00, 10.00, TRUE),
('ab-002', 'auc-005', 'user-002', 7300.00, 50.00, TRUE),
('ab-003', 'auc-010', 'user-007', 500.00, 15.00, TRUE),
('ab-004', 'auc-011', 'user-008', 920.00, 30.00, TRUE);
