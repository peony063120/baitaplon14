-- Seed data for Online Auction Server
-- Run by SqlDataLoader when -Dserver.seed=true
-- Uses H2-compatible SQL syntax
-- NOTE: Only users are seeded. Products must be created by SELLERs via the app.

-- ==================== USERS ====================
INSERT INTO users (id, username, password, email, full_name, role, balance, active) VALUES
('user-001', 'admin',   'admin',   'admin@auction.com',       'Head Administrator', 'ADMIN',  0.00,        TRUE),
('user-011', 'admin2',  'admin2',  'admin2@auction.com',      'Deputy Administrator','ADMIN',  0.00,        TRUE),
('user-002', 'bidder1', 'bidder1', 'bidder1@auction.com',  'Alice Nguyen',   'BIDDER', 1000.00, TRUE),
('user-003', 'bidder2', 'bidder2', 'bidder2@auction.com',  'Bob Tran',     'BIDDER', 500.00,  TRUE),
('user-004', 'bidder3', 'bidder3', 'bidder3@auction.com',  'Charlie Pham',     'BIDDER', 2000.00, TRUE),
('user-005', 'seller1', 'seller1', 'seller1@auction.com',  'David Le',       'SELLER', 0.00,        TRUE),
('user-006', 'seller2', 'seller2', 'seller2@auction.com',  'Eva Hoang',    'SELLER', 0.00,        TRUE),
('user-007', 'bidder4', 'bidder4', 'bidder4@auction.com',  'Fiona Le',       'BIDDER', 3000.00, TRUE),
('user-008', 'bidder5', 'bidder5', 'bidder5@auction.com',  'George Vo',       'BIDDER', 10000.00,TRUE),
('user-009', 'seller3', 'seller3', 'seller3@auction.com',  'Helen Dang',     'SELLER', 0.00,        TRUE),
('user-010', 'seller4', 'seller4', 'seller4@auction.com',  'Ivan Tran',     'SELLER', 0.00,        TRUE);
