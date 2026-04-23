CREATE TABLE llm_providers (
    id SMALLSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    model_id VARCHAR(100) NOT NULL,
    api_key_env VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    priority SMALLINT,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_llm_providers_active_priority ON llm_providers (active, priority);