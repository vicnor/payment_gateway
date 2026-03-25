ALTER TABLE payments ADD COLUMN order_id            VARCHAR(255) NOT NULL;
ALTER TABLE payments ADD COLUMN capture_mode        VARCHAR(20)  NOT NULL;
ALTER TABLE payments ADD COLUMN payment_method_type VARCHAR(50)  NOT NULL;
ALTER TABLE payments ADD COLUMN payment_method_ref  VARCHAR(255) NOT NULL;
ALTER TABLE payments ADD COLUMN provider            VARCHAR(50)  NOT NULL;
