CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    portfolio_id UUID NOT NULL,
    llm_provider_id SMALLINT NOT NULL,
    prompt_summary TEXT,
    llm_response TEXT NOT NULL,
    risk_score NUMERIC(4, 2) CHECK (risk_score >= 0.00 AND risk_score <= 10.00),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_recommendations_llm_provider
        FOREIGN KEY (llm_provider_id) REFERENCES llm_providers(id)
);

CREATE INDEX idx_rec_user ON recommendations(user_id, created_at DESC);
CREATE INDEX idx_rec_portfolio ON recommendations(portfolio_id, created_at DESC);
