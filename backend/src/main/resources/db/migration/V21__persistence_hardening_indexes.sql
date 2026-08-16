-- ============================================================
-- SPOREKART v3.0 — FLYWAY MIGRATION V21
-- SPRINT 6F: PERSISTENCE HARDENING INDEXES
-- ============================================================
-- Adds composite indexes for high-frequency query patterns
-- identified during Sprint 6F reconnaissance that were not
-- yet covered by V3, V11, V13, or V16 migrations.
-- All indexes use IF NOT EXISTS for safe re-run on H2 and PostgreSQL.
-- ============================================================

-- Orders: composite (customer_id, status, created_at) for
-- filtered order history searches (e.g., all PAID orders for customer)
CREATE INDEX IF NOT EXISTS idx_orders_customer_status_created
    ON orders (customer_id, status, created_at DESC);

-- Payments: status + created_at for payment reconciliation
-- and batch reconciliation jobs that scan by status + time window
CREATE INDEX IF NOT EXISTS idx_payments_status_created
    ON payments (status, created_at DESC);

-- Payments: customer + status for customer-facing payment history
CREATE INDEX IF NOT EXISTS idx_payments_customer_status
    ON payments (customer_id, status);

-- Notifications: customer_id + status + created_at for
-- inbox queries (e.g., "unread notifications for customer, newest first")
CREATE INDEX IF NOT EXISTS idx_notifications_customer_status_created
    ON notifications (customer_id, status, created_at DESC);

-- Shipments: customer + created_at for customer shipment history
-- (complements existing idx_shipments_customer which lacks created_at sort)
CREATE INDEX IF NOT EXISTS idx_shipments_customer_created
    ON shipments (customer_id, created_at DESC);

-- Stock movements: reference_type + reference_id for audit trail lookups
-- (e.g., "all movements for order ORD-123" or "all movements for reservation X")
CREATE INDEX IF NOT EXISTS idx_stock_movements_reference
    ON stock_movements (reference_type, reference_id);

-- Order status history: order_id + created_at for timeline queries
-- (order timeline endpoint fetches all history for one order, sorted by time)
CREATE INDEX IF NOT EXISTS idx_order_status_history_order_created
    ON order_status_history (order_id, created_at DESC);
