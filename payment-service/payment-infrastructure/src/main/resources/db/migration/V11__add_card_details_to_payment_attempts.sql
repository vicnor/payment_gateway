ALTER TABLE payment_attempts
    ADD COLUMN card_brand        VARCHAR(50),
    ADD COLUMN card_last4        VARCHAR(4),
    ADD COLUMN card_expiry_month INTEGER,
    ADD COLUMN card_expiry_year  INTEGER;
