ALTER TABLE transactions
    ALTER COLUMN asset_id DROP NOT NULL;

ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS fk_transactions_asset;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_asset
        FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE SET NULL;
