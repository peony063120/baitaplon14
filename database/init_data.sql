-- =====================================================
-- INIT DATA - ONLINE AUCTION SYSTEM
-- =====================================================

-- Chèn tài khoản admin mặc định
INSERT INTO users (id, username, password, email, full_name, role, balance, active)
VALUES
    ('admin-001', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrJqZQ8H6YkI7ZqX8qZq8Zq8Zq8Zq8', 'admin@auction.com', 'Administrator', 'ADMIN', 0, TRUE)
    ON DUPLICATE KEY UPDATE username=username;

-- Chèn tài khoản seller mẫu
INSERT INTO users (id, username, password, email, full_name, role, balance, active)
VALUES
    ('seller-001', 'seller1', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrJqZQ8H6YkI7ZqX8qZq8Zq8Zq8Zq8', 'seller1@auction.com', 'Nguyễn Văn A', 'SELLER', 0, TRUE),
    ('seller-002', 'seller2', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrJqZQ8H6YkI7ZqX8qZq8Zq8Zq8Zq8', 'seller2@auction.com', 'Trần Thị B', 'SELLER', 0, TRUE);

-- Chèn tài khoản bidder mẫu
INSERT INTO users (id, username, password, email, full_name, role, balance, active)
VALUES
    ('bidder-001', 'bidder1', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrJqZQ8H6YkI7ZqX8qZq8Zq8Zq8Zq8', 'bidder1@auction.com', 'Lê Văn C', 'BIDDER', 100000000, TRUE),
    ('bidder-002', 'bidder2', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrJqZQ8H6YkI7ZqX8qZq8Zq8Zq8Zq8', 'bidder2@auction.com', 'Phạm Thị D', 'BIDDER', 50000000, TRUE);

-- Chèn sản phẩm mẫu (items)
INSERT INTO items (id, seller_id, name, description, category, item_type, attributes)
VALUES
    ('item-001', 'seller-001', 'BMW X5 2024', 'Xe BMW X5 đời 2024, màu trắng, full option', 'Xe cộ', 'VEHICLE', '{"brand":"BMW","model":"X5","year":2024,"mileage":0}'),
    ('item-002', 'seller-001', 'MacBook Pro M4', 'Laptop Apple MacBook Pro M4 Max, RAM 64GB, SSD 2TB', 'Điện tử', 'ELECTRONICS', '{"brand":"Apple","model":"MacBook Pro M4","ram":"64GB","storage":"2TB"}'),
    ('item-003', 'seller-002', 'Tranh sơn dầu "Hạ Long"', 'Tranh sơn dầu phong cảnh Hạ Long, kích thước 90x120cm', 'Nghệ thuật', 'ART', '{"artist":"Nguyễn Văn E","year":2023,"material":"sơn dầu"}');

-- Chèn phiên đấu giá mẫu (auctions)
INSERT INTO auctions (id, item_id, seller_id, start_time, end_time, starting_price, current_price, min_increment, status, anti_sniping_enabled, anti_sniping_extension_seconds)
VALUES
    ('auc-001', 'item-001', 'seller-001', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 3000000000, 3000000000, 50000000, 'DRAFT', TRUE, 30),
    ('auc-002', 'item-002', 'seller-001', NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), 50000000, 50000000, 1000000, 'DRAFT', TRUE, 30),
    ('auc-003', 'item-003', 'seller-002', NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), 80000000, 80000000, 2000000, 'DRAFT', FALSE, 0);

-- Chèn cấu hình auto-bid mẫu (auto_bid_configs)
INSERT INTO auto_bid_configs (id, auction_id, bidder_id, max_bid, increment_amount, is_active)
VALUES
    ('ab-001', 'auc-001', 'bidder-001', 3500000000, 50000000, TRUE),
    ('ab-002', 'auc-002', 'bidder-002', 60000000, 1000000, TRUE);