ALTER TABLE portfolios ADD COLUMN share_token VARCHAR(128) UNIQUE;
ALTER TABLE portfolios ADD COLUMN share_token_created_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_portfolios_share_token ON portfolios(share_token) WHERE share_token IS NOT NULL;
