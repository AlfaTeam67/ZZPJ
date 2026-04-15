CREATE TABLE assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL CHECK (type IN ('STOCK', 'CRYPTO', 'BOND')),
    symbol VARCHAR(20) NOT NULL,
    quantity NUMERIC(18, 8) NOT NULL CHECK (quantity > 0),
    avg_buy_price NUMERIC(18, 4) NOT NULL CHECK (avg_buy_price >= 0),
    currency VARCHAR(10) NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_assets_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    CONSTRAINT uk_assets_portfolio_symbol UNIQUE (portfolio_id, symbol)
);

CREATE INDEX idx_assets_portfolio_id ON assets(portfolio_id);
CREATE INDEX idx_assets_symbol ON assets(symbol);
