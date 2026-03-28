CREATE TABLE portfolio_positions (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    quantity NUMERIC(19, 8) NOT NULL,
    average_price NUMERIC(19, 4) NOT NULL,
    current_price NUMERIC(19, 4) NOT NULL,
    CONSTRAINT fk_portfolio_positions_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    CONSTRAINT idx_portfolio_symbol UNIQUE (portfolio_id, symbol)
);

CREATE INDEX idx_portfolio_positions_symbol ON portfolio_positions(symbol);
