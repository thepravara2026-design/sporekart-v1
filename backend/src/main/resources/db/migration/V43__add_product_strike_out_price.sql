-- Sporekart v3.0 - STRIKE-OUT / ORIGINAL PRICE DATABASE MIGRATION
-- Task ID: PRICE-STRIKEOUT-01

ALTER TABLE products
ADD COLUMN strike_out_price DECIMAL(12, 2) DEFAULT NULL;

-- Defense in depth: Check constraint to enforce strike_out_price > price when strike_out_price is present
ALTER TABLE products
ADD CONSTRAINT chk_products_strike_out_price CHECK (strike_out_price IS NULL OR strike_out_price > price);
