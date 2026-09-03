-- Establishes seller_profiles and seller_payouts tables, and seller_id columns
CREATE TABLE IF NOT EXISTS seller_profiles (
    id UUID PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    business_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50),
    address TEXT,
    bank_account_last4 VARCHAR(10),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS seller_payouts (
    id UUID PRIMARY KEY,
    seller_id VARCHAR(100) NOT NULL,
    payout_reference VARCHAR(100) NOT NULL,
    period VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    bank_account_last4 VARCHAR(10),
    payout_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_seller_payouts_seller_id ON seller_payouts(seller_id);

-- Add seller_id column to existing multi-tenant entities if missing
ALTER TABLE products ADD COLUMN IF NOT EXISTS seller_id VARCHAR(100);
ALTER TABLE inventory_items ADD COLUMN IF NOT EXISTS seller_id VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS seller_id VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_products_seller_id ON products(seller_id);
CREATE INDEX IF NOT EXISTS idx_inventory_items_seller_id ON inventory_items(seller_id);
CREATE INDEX IF NOT EXISTS idx_orders_seller_id ON orders(seller_id);
