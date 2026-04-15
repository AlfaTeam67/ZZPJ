CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL,
    portfolio_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL CHECK (type IN ('BUY', 'SELL')),
    quantity NUMERIC(18, 8) NOT NULL CHECK (quantity > 0),
    price NUMERIC(18, 4) NOT NULL CHECK (price >= 0),
    currency VARCHAR(10) NOT NULL,
    fee NUMERIC(18, 4),
    executed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    notes TEXT,
    CONSTRAINT fk_transactions_asset FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE
);

CREATE INDEX idx_transactions_asset_id ON transactions(asset_id);
CREATE INDEX idx_transactions_portfolio_id ON transactions(portfolio_id);
CREATE INDEX idx_transactions_executed_at ON transactions(executed_at);
