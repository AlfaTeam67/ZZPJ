-- V5__seed_demo_data.sql
-- Demo data for testing the application
-- Demo user (Keycloak): testuser / test123
-- Demo user UUID: should match the user ID from Keycloak (treating email as identifier)

-- Insert demo user (if not exists)
-- Note: In production, users should come from Keycloak. This is just for local testing.
INSERT INTO users (id, created_at)
VALUES ('123e4567-e89b-12d3-a456-426614174000', NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- PORTFOLIO 1: "Growth Portfolio" (Stocks focused)
-- ============================================
INSERT INTO portfolios (id, user_id, name, description, created_at)
VALUES 
    ('223e4567-e89b-12d3-a456-426614174001', '123e4567-e89b-12d3-a456-426614174000', 
     'Growth Portfolio', 'Long-term stock investments', NOW())
ON CONFLICT (user_id, name) DO NOTHING;

-- Assets in Growth Portfolio
-- AAPL - Apple Stock
INSERT INTO assets (id, portfolio_id, type, symbol, quantity, avg_buy_price, currency, added_at)
VALUES 
    ('323e4567-e89b-12d3-a456-426614174001', '223e4567-e89b-12d3-a456-426614174001',
     'STOCK', 'AAPL', 50.00000000, 150.5000, 'USD', NOW() - INTERVAL '180 days')
ON CONFLICT (portfolio_id, symbol) DO NOTHING;

-- GOOGL - Google Stock
INSERT INTO assets (id, portfolio_id, type, symbol, quantity, avg_buy_price, currency, added_at)
VALUES 
    ('323e4567-e89b-12d3-a456-426614174002', '223e4567-e89b-12d3-a456-426614174001',
     'STOCK', 'GOOGL', 30.00000000, 125.2500, 'USD', NOW() - INTERVAL '150 days')
ON CONFLICT (portfolio_id, symbol) DO NOTHING;

-- MSFT - Microsoft Stock
INSERT INTO assets (id, portfolio_id, type, symbol, quantity, avg_buy_price, currency, added_at)
VALUES 
    ('323e4567-e89b-12d3-a456-426614174003', '223e4567-e89b-12d3-a456-426614174001',
     'STOCK', 'MSFT', 25.00000000, 380.7500, 'USD', NOW() - INTERVAL '120 days')
ON CONFLICT (portfolio_id, symbol) DO NOTHING;

-- Transactions for Growth Portfolio
-- AAPL BUY transactions
INSERT INTO transactions (id, asset_id, portfolio_id, type, quantity, price, currency, fee, executed_at, notes)
VALUES 
    ('423e4567-e89b-12d3-a456-426614174001', '323e4567-e89b-12d3-a456-426614174001', 
     '223e4567-e89b-12d3-a456-426614174001', 'BUY', 30.00000000, 145.0000, 'USD', 15.00, 
     NOW() - INTERVAL '180 days', 'Initial AAPL purchase'),
    ('423e4567-e89b-12d3-a456-426614174002', '323e4567-e89b-12d3-a456-426614174001', 
     '223e4567-e89b-12d3-a456-426614174001', 'BUY', 20.00000000, 156.0000, 'USD', 10.00, 
     NOW() - INTERVAL '120 days', 'Additional AAPL purchase');

-- GOOGL BUY transactions
INSERT INTO transactions (id, asset_id, portfolio_id, type, quantity, price, currency, fee, executed_at, notes)
VALUES 
    ('423e4567-e89b-12d3-a456-426614174003', '323e4567-e89b-12d3-a456-426614174002', 
     '223e4567-e89b-12d3-a456-426614174001', 'BUY', 30.00000000, 125.2500, 'USD', 20.00, 
     NOW() - INTERVAL '150 days', 'GOOGL initial investment');

-- MSFT BUY transactions
INSERT INTO transactions (id, asset_id, portfolio_id, type, quantity, price, currency, fee, executed_at, notes)
VALUES 
    ('423e4567-e89b-12d3-a456-426614174004', '323e4567-e89b-12d3-a456-426614174003', 
     '223e4567-e89b-12d3-a456-426614174001', 'BUY', 25.00000000, 380.7500, 'USD', 25.00, 
     NOW() - INTERVAL '120 days', 'MSFT tech investment');

-- ============================================
-- PORTFOLIO 2: "Crypto Holdings"
-- ============================================
INSERT INTO portfolios (id, user_id, name, description, created_at)
VALUES 
    ('223e4567-e89b-12d3-a456-426614174002', '123e4567-e89b-12d3-a456-426614174000', 
     'Crypto Holdings', 'Digital assets and cryptocurrencies', NOW())
ON CONFLICT (user_id, name) DO NOTHING;

-- Assets in Crypto Portfolio
-- BTC - Bitcoin
INSERT INTO assets (id, portfolio_id, type, symbol, quantity, avg_buy_price, currency, added_at)
VALUES 
    ('323e4567-e89b-12d3-a456-426614174004', '223e4567-e89b-12d3-a456-426614174002',
     'CRYPTO', 'BTC-USD', 0.50000000, 58000.0000, 'USD', NOW() - INTERVAL '200 days')
ON CONFLICT (portfolio_id, symbol) DO NOTHING;

-- ETH - Ethereum
INSERT INTO assets (id, portfolio_id, type, symbol, quantity, avg_buy_price, currency, added_at)
VALUES 
    ('323e4567-e89b-12d3-a456-426614174005', '223e4567-e89b-12d3-a456-426614174002',
     'CRYPTO', 'ETH-USD', 5.00000000, 2500.0000, 'USD', NOW() - INTERVAL '180 days')
ON CONFLICT (portfolio_id, symbol) DO NOTHING;

-- Transactions for Crypto Portfolio
-- BTC transactions
INSERT INTO transactions (id, asset_id, portfolio_id, type, quantity, price, currency, fee, executed_at, notes)
VALUES 
    ('423e4567-e89b-12d3-a456-426614174005', '323e4567-e89b-12d3-a456-426614174004', 
     '223e4567-e89b-12d3-a456-426614174002', 'BUY', 0.50000000, 58000.0000, 'USD', 50.00, 
     NOW() - INTERVAL '200 days', 'Bitcoin purchase via Coinbase');

-- ETH transactions
INSERT INTO transactions (id, asset_id, portfolio_id, type, quantity, price, currency, fee, executed_at, notes)
VALUES 
    ('423e4567-e89b-12d3-a456-426614174006', '323e4567-e89b-12d3-a456-426614174005', 
     '223e4567-e89b-12d3-a456-426614174002', 'BUY', 3.00000000, 2400.0000, 'USD', 30.00, 
     NOW() - INTERVAL '180 days', 'Ethereum initial stake'),
    ('423e4567-e89b-12d3-a456-426614174007', '323e4567-e89b-12d3-a456-426614174005', 
     '223e4567-e89b-12d3-a456-426614174002', 'BUY', 2.00000000, 2600.0000, 'USD', 20.00, 
     NOW() - INTERVAL '90 days', 'Additional Ethereum purchase');



-- ============================================
-- PORTFOLIO 3: "Diversified Mixed Assets"
-- ============================================
INSERT INTO portfolios (id, user_id, name, description, created_at)
VALUES 
    ('223e4567-e89b-12d3-a456-426614174003', '123e4567-e89b-12d3-a456-426614174000', 
     'Diversified Mixed Assets', 'Mix of stocks and crypto', NOW())
ON CONFLICT (user_id, name) DO NOTHING;

-- Mix of different asset types
INSERT INTO assets (id, portfolio_id, type, symbol, quantity, avg_buy_price, currency, added_at)
VALUES 
    ('323e4567-e89b-12d3-a456-426614174008', '223e4567-e89b-12d3-a456-426614174003',
     'STOCK', 'TSLA', 15.00000000, 225.5000, 'USD', NOW() - INTERVAL '100 days'),
    ('323e4567-e89b-12d3-a456-426614174009', '223e4567-e89b-12d3-a456-426614174003',
     'STOCK', 'AMZN', 20.00000000, 170.2500, 'USD', NOW() - INTERVAL '80 days'),
    ('323e4567-e89b-12d3-a456-426614174010', '223e4567-e89b-12d3-a456-426614174003',
     'CRYPTO', 'BTC-USD', 0.25000000, 61000.0000, 'USD', NOW() - INTERVAL '60 days')
ON CONFLICT (portfolio_id, symbol) DO NOTHING;

-- Transactions for mixed portfolio
INSERT INTO transactions (id, asset_id, portfolio_id, type, quantity, price, currency, fee, executed_at, notes)
VALUES 
    ('423e4567-e89b-12d3-a456-426614174008', '323e4567-e89b-12d3-a456-426614174008', 
     '223e4567-e89b-12d3-a456-426614174003', 'BUY', 15.00000000, 225.5000, 'USD', 20.00, 
     NOW() - INTERVAL '100 days', 'Tesla growth investment'),
    ('423e4567-e89b-12d3-a456-426614174009', '323e4567-e89b-12d3-a456-426614174009', 
     '223e4567-e89b-12d3-a456-426614174003', 'BUY', 20.00000000, 170.2500, 'USD', 25.00, 
     NOW() - INTERVAL '80 days', 'Amazon retail position'),
    ('423e4567-e89b-12d3-a456-426614174010', '323e4567-e89b-12d3-a456-426614174010', 
     '223e4567-e89b-12d3-a456-426614174003', 'BUY', 0.25000000, 61000.0000, 'USD', 40.00, 
     NOW() - INTERVAL '60 days', 'Bitcoin hedge position');
