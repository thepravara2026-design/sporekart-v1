-- Sporekart v3.0 Grower Backend Foundation Migration
-- Migration: V42__grower_backend_foundation.sql
-- Description: Establishes grower_profiles table, seeds ROLE_GROWER & permissions, adds grower_id multi-tenant columns to products, inventory_items, orders, shipments.

-- Seed ROLE_GROWER
INSERT INTO roles (id, description) VALUES
('ROLE_GROWER', 'Registered mushroom grower / producer with catalog, inventory, and order fulfillment capabilities')
ON CONFLICT (id) DO NOTHING;

-- Seed Grower Permissions
INSERT INTO permissions (id, description) VALUES
('GROWER_READ_PROFILE', 'Permission to view own grower profile'),
('GROWER_MANAGE_PROFILE', 'Permission to update own grower profile & settings'),
('GROWER_READ_PRODUCTS', 'Permission to view owned products'),
('GROWER_MANAGE_PRODUCTS', 'Permission to create/update owned products'),
('GROWER_READ_INVENTORY', 'Permission to view owned inventory stock'),
('GROWER_MANAGE_INVENTORY', 'Permission to adjust owned stock levels'),
('GROWER_READ_ORDERS', 'Permission to view orders for owned products'),
('GROWER_FULFILL_ORDER', 'Permission to transition status on owned orders'),
('GROWER_READ_SHIPMENTS', 'Permission to track shipments for owned orders'),
('GROWER_READ_REPORTS', 'Permission to access grower operational analytics')
ON CONFLICT (id) DO NOTHING;

-- Assign permissions to ROLE_GROWER
INSERT INTO role_permissions (role_id, permission_id) VALUES
('ROLE_GROWER', 'GROWER_READ_PROFILE'),
('ROLE_GROWER', 'GROWER_MANAGE_PROFILE'),
('ROLE_GROWER', 'GROWER_READ_PRODUCTS'),
('ROLE_GROWER', 'GROWER_MANAGE_PRODUCTS'),
('ROLE_GROWER', 'GROWER_READ_INVENTORY'),
('ROLE_GROWER', 'GROWER_MANAGE_INVENTORY'),
('ROLE_GROWER', 'GROWER_READ_ORDERS'),
('ROLE_GROWER', 'GROWER_FULFILL_ORDER'),
('ROLE_GROWER', 'GROWER_READ_SHIPMENTS'),
('ROLE_GROWER', 'GROWER_READ_REPORTS')
ON CONFLICT DO NOTHING;

-- Also assign READ_CATALOG and READ_OWN_ORDER to ROLE_GROWER
INSERT INTO role_permissions (role_id, permission_id) VALUES
('ROLE_GROWER', 'READ_CATALOG'),
('ROLE_GROWER', 'READ_OWN_ORDER')
ON CONFLICT DO NOTHING;

-- Create grower_profiles table
CREATE TABLE IF NOT EXISTS grower_profiles (
    id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    business_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50),
    farm_address TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    low_stock_alert_threshold INT NOT NULL DEFAULT 5,
    auto_acknowledge_orders BOOLEAN NOT NULL DEFAULT FALSE,
    preferred_carrier VARCHAR(100) DEFAULT 'Standard Express',
    default_fulfillment_location VARCHAR(255) DEFAULT 'Main Facility',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add Multi-Tenant grower_id columns
ALTER TABLE products ADD COLUMN IF NOT EXISTS grower_id VARCHAR(100);
ALTER TABLE inventory_items ADD COLUMN IF NOT EXISTS grower_id VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS grower_id VARCHAR(100);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS grower_id VARCHAR(100);

-- Indexes for Multi-Tenant Data Scoping Performance
CREATE INDEX IF NOT EXISTS idx_products_grower_id ON products(grower_id);
CREATE INDEX IF NOT EXISTS idx_inventory_items_grower_id ON inventory_items(grower_id);
CREATE INDEX IF NOT EXISTS idx_orders_grower_id ON orders(grower_id);
CREATE INDEX IF NOT EXISTS idx_shipments_grower_id ON shipments(grower_id);

-- Seed Default Test Grower Account
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status)
VALUES ('grower-1', 'grower1@sporekart.com', '$2a$10$e4gRkGgM1L9X2V3Y4Z5W6u7V8W9X0Y1Z2A3B4C5D6E7F8G9H0I1J2', 'Spore', 'Grower', 'ROLE_GROWER', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
VALUES ('grower-1', 'ROLE_GROWER')
ON CONFLICT DO NOTHING;

INSERT INTO grower_profiles (id, user_id, business_name, contact_email, contact_phone, farm_address, status)
VALUES ('grower-profile-1', 'grower-1', 'Apex Spore Farms', 'grower1@sporekart.com', '+1-555-0199', '100 Mycology Way, Mushroom Valley, CA', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Seed Second Test Grower Account for Cross-Grower Isolation Tests
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status)
VALUES ('grower-2', 'grower2@sporekart.com', '$2a$10$e4gRkGgM1L9X2V3Y4Z5W6u7V8W9X0Y1Z2A3B4C5D6E7F8G9H0I1J2', 'Fungi', 'Producer', 'ROLE_GROWER', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
VALUES ('grower-2', 'ROLE_GROWER')
ON CONFLICT DO NOTHING;

INSERT INTO grower_profiles (id, user_id, business_name, contact_email, contact_phone, farm_address, status)
VALUES ('grower-profile-2', 'grower-2', 'BioMyco Cultivators', 'grower2@sporekart.com', '+1-555-0299', '200 Spore Lane, Fungi Ridge, OR', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Backfill legacy records to grower-1 so existing demo data remains populated
UPDATE products SET grower_id = 'grower-1' WHERE grower_id IS NULL;
UPDATE inventory_items SET grower_id = 'grower-1' WHERE grower_id IS NULL;
UPDATE orders SET grower_id = 'grower-1' WHERE grower_id IS NULL;
UPDATE shipments SET grower_id = 'grower-1' WHERE grower_id IS NULL;
