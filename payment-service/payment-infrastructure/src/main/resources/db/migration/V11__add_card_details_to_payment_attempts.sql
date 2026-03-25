ALTER TABLE payment_attempts ADD COLUMN card_brand VARCHAR(50);
ALTER TABLE payment_attempts ADD COLUMN card_last4 VARCHAR(4);
ALTER TABLE payment_attempts ADD COLUMN card_expiry_month INTEGER;
ALTER TABLE payment_attempts ADD COLUMN card_expiry_year INTEGER;
