CREATE TABLE portfolios (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    total_value NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT idx_user_id_name UNIQUE (user_id, name)
);

CREATE INDEX idx_portfolios_user_id ON portfolios(user_id);
