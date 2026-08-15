-- ============================================================
-- SPOREKART v3.0 - FLYWAY MIGRATION V16
-- SPRINT 4J: PLATFORM HARDENING - INDEXES AND CONSTRAINTS
-- ============================================================
-- Adds missing composite indexes for high-frequency query patterns
-- identified during Sprint 4J reconnaissance.
-- Adds CHECK constraints on monetary columns missing them.
-- NOTE: IF NOT EXISTS for indexes requires H2 2.0+/PostgreSQL 9.5+.
-- The DO block for CHECK constraint is skipped (H2 incompatible);
-- the constraint is applied via ALTER TABLE below and is idempotent
-- because Flyway versioned migrations run exactly once.
-- ============================================================

-- Returns: composite indexes for customer query patterns
CREATE INDEX IF NOT EXISTS idx_returns_customer_status
    ON returns (customer_id, status);

CREATE INDEX IF NOT EXISTS idx_returns_customer_created
    ON returns (customer_id, created_at DESC);

-- Refund Records: missing customer + status composite index
CREATE INDEX IF NOT EXISTS idx_refund_records_customer_status
    ON refund_records (customer_id, status);

-- Refund Records: CHECK constraint on amount (amount is DECIMAL NOT NULL)
ALTER TABLE refund_records
    ADD CONSTRAINT chk_refund_records_amount_positive CHECK (amount > 0);

-- Support Tickets: composite index for customer + status
CREATE INDEX IF NOT EXISTS idx_support_tickets_customer_status
    ON support_tickets (customer_id, status);

CREATE INDEX IF NOT EXISTS idx_support_tickets_created_at
    ON support_tickets (created_at DESC);

-- Product Reviews: composite index for product + status (review listing)
CREATE INDEX IF NOT EXISTS idx_product_reviews_product_status
    ON product_reviews (product_id, status);

CREATE INDEX IF NOT EXISTS idx_product_reviews_customer_status
    ON product_reviews (customer_id, status);

-- Payment Webhook Events: idempotency lookup index
CREATE INDEX IF NOT EXISTS idx_payment_webhook_events_provider_event
    ON payment_webhook_events (provider_event_id, processing_status);

-- Orders: composite index for order history + status filtering
CREATE INDEX IF NOT EXISTS idx_orders_customer_created
    ON orders (customer_id, created_at DESC);

-- Order Items: product-level analytics query support
CREATE INDEX IF NOT EXISTS idx_order_items_product_id
    ON order_items (product_id);

-- Shipments: carrier + status for reconciliation queries
CREATE INDEX IF NOT EXISTS idx_shipments_provider_status
    ON shipments (provider, status);

-- Inventory: SKU + threshold for availability checks
CREATE INDEX IF NOT EXISTS idx_inventory_sku_threshold
    ON inventory_items (sku, low_stock_threshold);