-- V2__seed_default_symbols.sql
-- Seeds a baseline set of symbols (so the scheduler has something to fetch
-- on first startup) plus an initial price snapshot per symbol for
-- demos / smoke tests. All symbols use 'finnhub' as the api_source,
-- matching the actual integrated provider.
--
-- Idempotent: safe to re-run (uses ON CONFLICT / NOT EXISTS guards).

-- ---------------------------------------------------------------------------
-- Supported symbols
-- ---------------------------------------------------------------------------
INSERT INTO supported_symbols (symbol, type, api_source, active, base_currency)
VALUES
    ('AAPL',            'STOCK',  'finnhub', true, 'USD'),
    ('MSFT',            'STOCK',  'finnhub', true, 'USD'),
    ('GOOGL',           'STOCK',  'finnhub', true, 'USD'),
    ('AMZN',            'STOCK',  'finnhub', true, 'USD'),
    ('NVDA',            'STOCK',  'finnhub', true, 'USD'),
    ('TSLA',            'STOCK',  'finnhub', true, 'USD'),
    ('META',            'STOCK',  'finnhub', true, 'USD'),
    ('BINANCE:BTCUSDT', 'CRYPTO', 'finnhub', true, 'USD'),
    ('BINANCE:ETHUSDT', 'CRYPTO', 'finnhub', true, 'USD')
ON CONFLICT (symbol) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Initial price snapshots (one per symbol)
-- These are placeholder values so the API has something to return before the
-- first scheduler tick. The real Finnhub-backed snapshots will be appended
-- by the scheduler at runtime.
-- The NOT EXISTS guard keeps this migration idempotent even though
-- price_snapshots has no unique constraint on symbol.
-- ---------------------------------------------------------------------------
INSERT INTO price_snapshots (symbol, source, price, currency, change_pct_24h, volume_24h, fetched_at)
SELECT v.symbol, v.source, v.price, v.currency, v.change_pct_24h, v.volume_24h, NOW()
FROM (VALUES
    ('AAPL',            'finnhub', 175.45,    'USD',  1.25,         52000000),
    ('MSFT',            'finnhub', 424.50,    'USD',  2.10,         22000000),
    ('GOOGL',           'finnhub', 138.20,    'USD', -0.85,         28500000),
    ('AMZN',            'finnhub', 187.30,    'USD',  0.45,         35600000),
    ('NVDA',            'finnhub', 950.10,    'USD',  3.20,         48000000),
    ('TSLA',            'finnhub', 242.80,    'USD', -1.50,        118000000),
    ('META',            'finnhub', 498.75,    'USD',  1.05,         15000000),
    ('BINANCE:BTCUSDT', 'finnhub', 62543.50,  'USD',  3.75,      31500000000),
    ('BINANCE:ETHUSDT', 'finnhub', 3428.20,   'USD',  2.50,      14800000000)
) AS v(symbol, source, price, currency, change_pct_24h, volume_24h)
WHERE NOT EXISTS (
    SELECT 1 FROM price_snapshots ps WHERE ps.symbol = v.symbol
);
