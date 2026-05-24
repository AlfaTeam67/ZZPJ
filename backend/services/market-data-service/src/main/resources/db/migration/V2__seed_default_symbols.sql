-- V2__seed_default_symbols.sql
-- Seeds a baseline set of symbols so the scheduler has something to fetch
-- on first startup. All symbols are active and use 'finnhub' as the api_source.
-- Safe to re-run (INSERT ... ON CONFLICT DO NOTHING).

INSERT INTO supported_symbols (symbol, type, api_source, active, base_currency)
VALUES
    ('AAPL',   'STOCK',  'finnhub', true, 'USD'),
    ('MSFT',   'STOCK',  'finnhub', true, 'USD'),
    ('GOOGL',  'STOCK',  'finnhub', true, 'USD'),
    ('AMZN',   'STOCK',  'finnhub', true, 'USD'),
    ('NVDA',   'STOCK',  'finnhub', true, 'USD'),
    ('TSLA',   'STOCK',  'finnhub', true, 'USD'),
    ('META',   'STOCK',  'finnhub', true, 'USD'),
    ('BINANCE:BTCUSDT', 'CRYPTO', 'finnhub', true, 'USD'),
    ('BINANCE:ETHUSDT', 'CRYPTO', 'finnhub', true, 'USD')
ON CONFLICT (symbol) DO NOTHING;
