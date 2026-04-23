CREATE TABLE news_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    headline TEXT NOT NULL,
    source VARCHAR(100) NOT NULL,
    url TEXT,
    sentiment VARCHAR(20) CHECK (sentiment IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE')),
    fetched_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_news_expires ON news_cache(expires_at);
