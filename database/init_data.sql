-- =====================================================
-- INIT DATA - ONLINE AUCTION SYSTEM
-- File tham khảo: dữ liệu mẫu đầy đủ
-- =====================================================
-- Lưu ý: File này KHÔNG được chạy tự động.
-- - Server dùng: server/src/main/resources/seed.sql
-- - Client mock dùng: client/src/main/resources/mock-data.sql
-- =====================================================

-- ==================== USERS ====================
INSERT INTO users (id, username, password, email, full_name, role, balance, active) VALUES
('user-001', 'admin',   'admin',   'admin@auction.com',    'Administrator',  'ADMIN',  0.00,        TRUE),
('user-002', 'bidder1', 'bidder1', 'bidder1@auction.com',  'Nguyễn Văn A',   'BIDDER', 100000000.00, TRUE),
('user-003', 'bidder2', 'bidder2', 'bidder2@auction.com',  'Trần Thị B',     'BIDDER', 50000000.00,  TRUE),
('user-004', 'bidder3', 'bidder3', 'bidder3@auction.com',  'Phạm Văn C',     'BIDDER', 200000000.00, TRUE),
('user-005', 'seller1', 'seller1', 'seller1@auction.com',  'Lê Văn C',       'SELLER', 0.00,        TRUE),
('user-006', 'seller2', 'seller2', 'seller2@auction.com',  'Hoàng Thị D',    'SELLER', 0.00,        TRUE);

-- ==================== ITEMS ====================
INSERT INTO items (id, seller_id, name, description, category, item_type, attributes) VALUES
('item-001', 'user-005', 'BMW X5 2024',          'Xe BMW X5 đời 2024, màu trắng, full option, đã chạy 5000km', 'Xe cộ',     'VEHICLE',     '{"brand":"BMW","model":"X5","year":2024,"mileage":5000}'),
('item-002', 'user-005', 'MacBook Pro M4',       'Laptop Apple MacBook Pro M4 Max 64GB RAM, 2TB SSD',          'Điện tử',   'ELECTRONICS', '{"brand":"Apple","model":"MacBook Pro M4","ram":"64GB","storage":"2TB"}'),
('item-003', 'user-006', 'Tranh sơn dầu Hạ Long','Tranh sơn dầu phong cảnh vịnh Hạ Long, 90x120cm',            'Nghệ thuật','ART',          '{"artist":"Nguyễn Văn E","year":2024,"material":"sơn dầu"}'),
('item-004', 'user-005', 'iPhone 16 Pro Max',    'iPhone 16 Pro Max 512GB, Titan tự nhiên, mới 99%',            'Điện tử',   'ELECTRONICS', '{"brand":"Apple","model":"iPhone 16 Pro Max","storage":"512GB"}'),
('item-005', 'user-006', 'Toyota Camry 2023',    'Xe Toyota Camry 2.5Q, đã đi 15000km, bảo dưỡng đầy đủ',      'Xe cộ',     'VEHICLE',     '{"brand":"Toyota","model":"Camry 2.5Q","year":2023,"mileage":15000}'),
('item-006', 'user-006', 'Đồng hồ Rolex',        'Đồng hồ Rolex Submariner Date, chính hãng, 2022',             'Nghệ thuật','ART',          '{"artist":"Rolex","year":2022,"material":"thép không gỉ"}'),
('item-007', 'user-005', 'Sony PlayStation 5',   'PS5 Slim Digital Edition, kèm 2 tay cầm, 5 đĩa game',         'Điện tử',   'ELECTRONICS', '{"brand":"Sony","model":"PS5 Slim"}'),
('item-008', 'user-006', 'Tượng đồng Phật',      'Tượng Phật Thích Ca bằng đồng, cao 60cm, nặng 15kg',          'Nghệ thuật','ART',          '{"artist":"Làng nghề Đồng Đại Bái","year":2023,"material":"đồng"}');

-- ==================== AUCTIONS ====================
INSERT INTO auctions (id, item_id, seller_id, start_time, end_time, starting_price, current_price, min_increment, status, current_winner_id, anti_sniping_enabled, anti_sniping_extension_seconds) VALUES
('auc-001', 'item-001', 'user-005', DATEADD('DAY', 1, CURRENT_TIMESTAMP), DATEADD('DAY', 8, CURRENT_TIMESTAMP), 3000000000.00, 3000000000.00, 50000000.00, 'DRAFT',   NULL,      TRUE,  30),
('auc-002', 'item-002', 'user-005', DATEADD('DAY', 1, CURRENT_TIMESTAMP), DATEADD('DAY', 4, CURRENT_TIMESTAMP), 50000000.00,  50000000.00,  1000000.00,  'DRAFT',   NULL,      TRUE,  30),
('auc-003', 'item-003', 'user-006', DATEADD('DAY', 2, CURRENT_TIMESTAMP), DATEADD('DAY', 9, CURRENT_TIMESTAMP), 80000000.00,  80000000.00,  2000000.00,  'DRAFT',   NULL,      FALSE, 0),
('auc-004', 'item-004', 'user-005', DATEADD('HOUR', -2, CURRENT_TIMESTAMP), DATEADD('HOUR', 1, CURRENT_TIMESTAMP), 35000000.00, 38500000.00, 500000.00,   'RUNNING', 'user-002', TRUE,  30),
('auc-005', 'item-005', 'user-006', DATEADD('HOUR', -4, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, CURRENT_TIMESTAMP), 700000000.00,720000000.00, 10000000.00, 'RUNNING', 'user-004', TRUE,  30),
('auc-006', 'item-006', 'user-006', DATEADD('HOUR', 6, CURRENT_TIMESTAMP), DATEADD('DAY', 7, CURRENT_TIMESTAMP), 150000000.00,150000000.00, 5000000.00,  'DRAFT',   NULL,      TRUE,  30),
('auc-007', 'item-007', 'user-005', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), DATEADD('HOUR', 2, CURRENT_TIMESTAMP), 12000000.00,13500000.00,  300000.00,   'RUNNING', 'user-003', FALSE, 0),
('auc-008', 'item-008', 'user-006', DATEADD('MINUTE', -30, CURRENT_TIMESTAMP), DATEADD('HOUR', 4, CURRENT_TIMESTAMP), 50000000.00,55000000.00,2000000.00,  'RUNNING', NULL,      TRUE,  20),
('auc-009', 'item-001', 'user-005', DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 2900000000.00, 3250000000.00, 50000000.00, 'FINISHED','user-002', TRUE,  30);

-- ==================== BID TRANSACTIONS ====================
INSERT INTO bid_transactions (id, auction_id, bidder_id, amount, bid_time, is_auto_bid) VALUES
('tx-001', 'auc-004', 'user-002', 35500000.00, DATEADD('MINUTE', -100, CURRENT_TIMESTAMP), FALSE),
('tx-002', 'auc-004', 'user-003', 37000000.00, DATEADD('MINUTE', -80, CURRENT_TIMESTAMP),  FALSE),
('tx-003', 'auc-004', 'user-002', 38500000.00, DATEADD('MINUTE', -50, CURRENT_TIMESTAMP),  FALSE),
('tx-004', 'auc-005', 'user-004', 710000000.00, DATEADD('MINUTE', -200, CURRENT_TIMESTAMP),FALSE),
('tx-005', 'auc-005', 'user-002', 715000000.00, DATEADD('MINUTE', -150, CURRENT_TIMESTAMP),TRUE),
('tx-006', 'auc-005', 'user-004', 720000000.00, DATEADD('MINUTE', -100, CURRENT_TIMESTAMP),FALSE),
('tx-007', 'auc-005', 'user-002', 720000000.00, DATEADD('MINUTE', -50, CURRENT_TIMESTAMP), TRUE),
('tx-008', 'auc-007', 'user-003', 12300000.00, DATEADD('MINUTE', -55, CURRENT_TIMESTAMP), FALSE),
('tx-009', 'auc-007', 'user-004', 13000000.00, DATEADD('MINUTE', -40, CURRENT_TIMESTAMP), FALSE),
('tx-010', 'auc-007', 'user-003', 13500000.00, DATEADD('MINUTE', -20, CURRENT_TIMESTAMP), FALSE),
('tx-011', 'auc-008', 'user-002', 55000000.00, DATEADD('MINUTE', -25, CURRENT_TIMESTAMP), FALSE),
('tx-012', 'auc-009', 'user-002', 2950000000.00, DATEADD('DAY', -3, CURRENT_TIMESTAMP),   FALSE),
('tx-013', 'auc-009', 'user-004', 3050000000.00, DATEADD('DAY', -2, CURRENT_TIMESTAMP),   FALSE),
('tx-014', 'auc-009', 'user-002', 3100000000.00, DATEADD('DAY', -1, CURRENT_TIMESTAMP),   FALSE),
('tx-015', 'auc-009', 'user-003', 3150000000.00, DATEADD('HOUR', -12, CURRENT_TIMESTAMP), TRUE),
('tx-016', 'auc-009', 'user-002', 3250000000.00, DATEADD('HOUR', -2, CURRENT_TIMESTAMP),  FALSE);

-- ==================== AUTO BID CONFIGS ====================
INSERT INTO auto_bid_configs (id, auction_id, bidder_id, max_bid, increment_amount, is_active) VALUES
('ab-001', 'auc-004', 'user-003', 40000000.00, 1000000.00, TRUE),
('ab-002', 'auc-005', 'user-002', 730000000.00, 5000000.00, TRUE);
