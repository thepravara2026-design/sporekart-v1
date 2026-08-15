-- Flyway Migration V13: Inventory Hardening & Damaged Stock Accounting

ALTER TABLE inventory_items ADD COLUMN IF NOT EXISTS damaged_quantity INT NOT NULL DEFAULT 0 CHECK (damaged_quantity >= 0);
ALTER TABLE inventory_items ADD COLUMN IF NOT EXISTS low_stock_threshold INT NOT NULL DEFAULT 5 CHECK (low_stock_threshold >= 0);

CREATE INDEX IF NOT EXISTS idx_inventory_low_stock ON inventory_items (sku, on_hand_quantity, reserved_quantity, damaged_quantity);
