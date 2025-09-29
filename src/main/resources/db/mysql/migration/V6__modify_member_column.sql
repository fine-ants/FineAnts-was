ALTER TABLE member
    ADD COLUMN browser_notify BIT NOT NULL DEFAULT b'0';
ALTER TABLE member
    ADD COLUMN max_loss_notify BIT NOT NULL DEFAULT b'0';
ALTER TABLE member
    ADD COLUMN target_gain_notify BIT NOT NULL DEFAULT b'0';
ALTER TABLE member
    ADD COLUMN target_price_notify BIT NOT NULL DEFAULT b'0';
