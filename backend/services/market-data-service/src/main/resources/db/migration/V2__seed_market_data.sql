-- Insert supported symbols
INSERT INTO supported_symbols (symbol, type, api_source, active, base_currency, added_at)
VALUES 
    ('AAPL', 'STOCK', 'alphavantage', TRUE, 'USD', NOW()),
    ('GOOGL', 'STOCK', 'alphavantage', TRUE, 'USD', NOW()),
    ('MSFT', 'STOCK', 'alphavantage', TRUE, 'USD', NOW()),
    ('TSLA', 'STOCK', 'alphavantage', TRUE, 'USD', NOW()),
    ('AMZN', 'STOCK', 'alphavantage', TRUE, 'USD', NOW()),
    ('BTC-USD', 'CRYPTO', 'coingecko', TRUE, 'USD', NOW()),
    ('ETH-USD', 'CRYPTO', 'coingecko', TRUE, 'USD', NOW()),
    ('EUR', 'FOREX', 'nbp', TRUE, 'PLN', NOW()),
    ('GBP', 'FOREX', 'nbp', TRUE, 'PLN', NOW());

-- Insert latest price snapshots
INSERT INTO price_snapshots (symbol, source, price, currency, change_pct_24h, volume_24h, fetched_at)
VALUES
    ('AAPL', 'alphavantage', '175.45', 'USD', '1.25', '52000000', NOW()),
    ('GOOGL', 'alphavantage', '138.20', 'USD', '-0.85', '28500000', NOW()),
    ('MSFT', 'alphavantage', '424.50', 'USD', '2.10', '22000000', NOW()),
    ('TSLA', 'alphavantage', '242.80', 'USD', '-1.50', '118000000', NOW()),
    ('AMZN', 'alphavantage', '187.30', 'USD', '0.45', '35600000', NOW()),
    ('BTC-USD', 'coingecko', '62543.50', 'USD', '3.75', '31500000000', NOW()),
    ('ETH-USD', 'coingecko', '3428.20', 'USD', '2.50', '14800000000', NOW()),
    ('EUR', 'nbp', '4.25', 'PLN', '0.10', '0', NOW()),
    ('GBP', 'nbp', '5.12', 'PLN', '-0.05', '0', NOW());
