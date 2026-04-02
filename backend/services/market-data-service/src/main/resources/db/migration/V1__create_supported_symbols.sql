-- V1__init_market_data.sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE supported_symbols (
    symbol        VARCHAR(20)  PRIMARY KEY,
    type          VARCHAR(20)  NOT NULL CHECK (type IN ('STOCK','CRYPTO','FOREX')),
    api_source    VARCHAR(50)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    base_currency VARCHAR(10),
    added_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_supported_symbols_type   ON supported_symbols(type);
CREATE INDEX idx_supported_symbols_active ON supported_symbols(active);

COMMENT ON TABLE supported_symbols IS 'Supported trading symbols across different asset types';
COMMENT ON COLUMN supported_symbols.symbol IS 'Unique symbol identifier (e.g., AAPL, BTC-USD, EUR)';
COMMENT ON COLUMN supported_symbols.type IS 'Asset type: STOCK, CRYPTO, or FOREX';
COMMENT ON COLUMN supported_symbols.api_source IS 'External API source (e.g., alphavantage, coingecko, nbp)';
COMMENT ON COLUMN supported_symbols.active IS 'Whether this symbol is actively tracked';
COMMENT ON COLUMN supported_symbols.base_currency IS 'Base currency for forex pairs';

CREATE TABLE price_snapshots (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol          VARCHAR(20) NOT NULL REFERENCES supported_symbols(symbol) ON DELETE CASCADE,
    source          VARCHAR(50) NOT NULL,
    price           NUMERIC(18,4) NOT NULL CHECK (price > 0),
    currency        VARCHAR(10) NOT NULL,
    change_pct_24h  NUMERIC(8,4),
    volume_24h      NUMERIC(24,4) CHECK (volume_24h >= 0),
    fetched_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_snap_symbol_fetched ON price_snapshots (symbol, fetched_at DESC);
CREATE INDEX idx_snap_fetched        ON price_snapshots (fetched_at DESC);

COMMENT ON TABLE price_snapshots IS 'Historical price snapshots for market data';
COMMENT ON COLUMN price_snapshots.symbol IS 'Reference to supported_symbols.symbol';
COMMENT ON COLUMN price_snapshots.source IS 'Data source that provided this snapshot';
COMMENT ON COLUMN price_snapshots.price IS 'Price value (using NUMERIC for precision)';
COMMENT ON COLUMN price_snapshots.currency IS 'Currency in which price is denominated';
COMMENT ON COLUMN price_snapshots.change_pct_24h IS '24-hour percentage change';
COMMENT ON COLUMN price_snapshots.volume_24h IS '24-hour trading volume';
COMMENT ON COLUMN price_snapshots.fetched_at IS 'Timestamp when data was fetched';
