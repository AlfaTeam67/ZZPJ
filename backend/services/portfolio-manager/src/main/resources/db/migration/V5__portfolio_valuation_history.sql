CREATE TABLE portfolio_valuation_history (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id   UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    valuation_date DATE NOT NULL,
    total_value    NUMERIC(19, 4) NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_portfolio_valuation_date UNIQUE (portfolio_id, valuation_date)
);

CREATE INDEX idx_portfolio_valuation_portfolio_id ON portfolio_valuation_history(portfolio_id);
CREATE INDEX idx_portfolio_valuation_date ON portfolio_valuation_history(valuation_date);
