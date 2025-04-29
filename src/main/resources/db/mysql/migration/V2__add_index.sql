ALTER TABLE portfolio_gain_history
    ADD INDEX idx_portfolio_id_create_at (portfolio_id, create_at DESC);
