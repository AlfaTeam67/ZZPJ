-- Rozszerzenie news_cache o znacznik dostawcy newsa i symbol, którego dotyczy.
-- Dzięki temu można odpytywać cache per ticker i wiedzieć, z którego źródła pochodzi nagłówek.

ALTER TABLE news_cache
    ADD COLUMN provider VARCHAR(20),
    ADD COLUMN symbol VARCHAR(20),
    ADD COLUMN external_id VARCHAR(200);

CREATE INDEX idx_news_symbol_fetched ON news_cache(symbol, fetched_at DESC);
CREATE UNIQUE INDEX uq_news_provider_external_id ON news_cache(provider, external_id) WHERE external_id IS NOT NULL;
