-- V3__seed_price_history.sql
-- Historical price snapshots for demonstration purposes
-- This simulates market data history over the last 6 months

-- ============================================
-- AAPL - Apple Historical Prices
-- ============================================
INSERT INTO price_snapshots (symbol, source, price, currency, change_pct_24h, volume_24h, fetched_at)
VALUES
    ('AAPL', 'alphavantage', '145.0000', 'USD', '1.50', '48000000', NOW() - INTERVAL '180 days'),
    ('AAPL', 'alphavantage', '148.5000', 'USD', '2.40', '51000000', NOW() - INTERVAL '160 days'),
    ('AAPL', 'alphavantage', '152.0000', 'USD', '2.35', '52500000', NOW() - INTERVAL '140 days'),
    ('AAPL', 'alphavantage', '158.2500', 'USD', '0.85', '50000000', NOW() - INTERVAL '120 days'),
    ('AAPL', 'alphavantage', '162.7500', 'USD', '2.84', '54000000', NOW() - INTERVAL '90 days'),
    ('AAPL', 'alphavantage', '168.5000', 'USD', '3.50', '53000000', NOW() - INTERVAL '60 days'),
    ('AAPL', 'alphavantage', '172.3000', 'USD', '2.26', '49500000', NOW() - INTERVAL '30 days'),
    ('AAPL', 'alphavantage', '175.4500', 'USD', '1.25', '52000000', NOW());

-- ============================================
-- GOOGL - Google Historical Prices
-- ============================================
INSERT INTO price_snapshots (symbol, source, price, currency, change_pct_24h, volume_24h, fetched_at)
VALUES
    ('GOOGL', 'alphavantage', '125.2500', 'USD', '0.50', '28000000', NOW() - INTERVAL '150 days'),
    ('GOOGL', 'alphavantage', '128.7500', 'USD', '2.75', '29000000', NOW() - INTERVAL '120 days'),
    ('GOOGL', 'alphavantage', '131.5000', 'USD', '2.16', '28500000', NOW() - INTERVAL '90 days'),
    ('GOOGL', 'alphavantage', '135.2500', 'USD', '2.87', '30000000', NOW() - INTERVAL '60 days'),
    ('GOOGL', 'alphavantage', '140.7500', 'USD', '4.01', '29500000', NOW() - INTERVAL '30 days'),
    ('GOOGL', 'alphavantage', '138.2000', 'USD', '-0.85', '28500000', NOW());

-- ============================================
-- MSFT - Microsoft Historical Prices
-- ============================================
INSERT INTO price_snapshots (symbol, source, price, currency, change_pct_24h, volume_24h, fetched_at)
VALUES
    ('MSFT', 'alphavantage', '380.7500', 'USD', '1.00', '21000000', NOW() - INTERVAL '120 days'),
    ('MSFT', 'alphavantage', '385.2500', 'USD', '1.18', '22000000', NOW() - INTERVAL '90 days'),
    ('MSFT', 'alphavantage', '395.5000', 'USD', '2.68', '23000000', NOW() - INTERVAL '60 days'),
    ('MSFT', 'alphavantage', '410.2500', 'USD', '3.71', '22500000', NOW() - INTERVAL '30 days'),
    ('MSFT', 'alphavantage', '424.5000', 'USD', '2.10', '22000000', NOW());

-- ============================================
-- TSLA - Tesla Historical Prices
-- ============================================
INSERT INTO price_snapshots (symbol, source, price, currency, change_pct_24h, volume_24h, fetched_at)
VALUES
    ('TSLA', 'alphavantage', '225.5000', 'USD', '-2.50', '110000000', NOW() - INTERVAL '100 days'),
    ('TSLA', 'alphavantage', '235.7500', 'USD', '4.55', '115000000', NOW() - INTERVAL '70 days'),
    ('TSLA', 'alphavantage', '240.2500', 'USD', '1.91', '112000000', NOW() - INTERVAL '40 days'),
    ('TSLA', 'alphavantage', '242.8000', 'USD', '-1.50', '118000000', NOW());

-- ============================================
-- AMZN - Amazon Historical Prices
-- ============================================
INSERT INTO price_snapshots (symbol, source, price, currency, change_pct_24h, volume_24h, fetched_at)
VALUES
    ('AMZN', 'alphavantage', '170.2500', 'USD', '0.25', '34000000', NOW() - INTERVAL '80 days'),
    ('AMZN', 'alphavantage', '175.5000', 'USD', '3.10', '35000000', NOW() - INTERVAL '50 days'),
    ('AMZN', 'alphavantage', '180.7500', 'USD', '2.98', '35500000', NOW() - INTERVAL '20 days'),
    ('AMZN', 'alphavantage', '187.3000', 'USD', '0.45', '35600000', NOW());

-- ============================================
-- BINANCE:BTCUSDT - Bitcoin Historical Prices
-- ============================================
INSERT INTO price_snapshots (symbol, source, price, currency, change_pct_24h, volume_24h, fetched_at)
VALUES
    ('BINANCE:BTCUSDT', 'finnhub', '58000.0000', 'USD', '2.50', '30000000000', NOW() - INTERVAL '200 days'),
    ('BINANCE:BTCUSDT', 'finnhub', '59500.0000', 'USD', '2.59', '30500000000', NOW() - INTERVAL '160 days'),
    ('BINANCE:BTCUSDT', 'finnhub', '61000.0000', 'USD', '2.52', '31000000000', NOW() - INTERVAL '120 days'),
    ('BINANCE:BTCUSDT', 'finnhub', '62500.0000', 'USD', '2.46', '31200000000', NOW() - INTERVAL '80 days'),
    ('BINANCE:BTCUSDT', 'finnhub', '64000.0000', 'USD', '2.40', '31500000000', NOW() - INTERVAL '40 days'),
    ('BINANCE:BTCUSDT', 'finnhub', '62543.5000', 'USD', '3.75', '31500000000', NOW());

-- ============================================
-- BINANCE:ETHUSDT - Ethereum Historical Prices
-- ============================================
INSERT INTO price_snapshots (symbol, source, price, currency, change_pct_24h, volume_24h, fetched_at)
VALUES
    ('BINANCE:ETHUSDT', 'finnhub', '2400.0000', 'USD', '1.50', '14000000000', NOW() - INTERVAL '180 days'),
    ('BINANCE:ETHUSDT', 'finnhub', '2550.0000', 'USD', '6.25', '14300000000', NOW() - INTERVAL '140 days'),
    ('BINANCE:ETHUSDT', 'finnhub', '2800.0000', 'USD', '9.80', '14500000000', NOW() - INTERVAL '100 days'),
    ('BINANCE:ETHUSDT', 'finnhub', '3100.0000', 'USD', '10.71', '14700000000', NOW() - INTERVAL '60 days'),
    ('BINANCE:ETHUSDT', 'finnhub', '3350.0000', 'USD', '8.06', '14800000000', NOW() - INTERVAL '20 days'),
    ('BINANCE:ETHUSDT', 'finnhub', '3428.2000', 'USD', '2.50', '14800000000', NOW());


