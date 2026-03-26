CREATE TABLE market_prices (
    id BIGSERIAL PRIMARY KEY,
    symbol_id BIGINT NOT NULL,
    price NUMERIC(19, 4) NOT NULL CHECK (price > 0),
    volume NUMERIC(19, 8) NOT NULL CHECK (volume >= 0),
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_market_price_symbol FOREIGN KEY (symbol_id) 
        REFERENCES symbols(id) ON DELETE CASCADE
);

CREATE INDEX idx_market_price_symbol ON market_prices(symbol_id);
CREATE INDEX idx_market_price_timestamp ON market_prices(timestamp);
