-- Flyway Migration V11: Commerce Hardening Indexes & Performance Tuning Constraints

CREATE INDEX IF NOT EXISTS idx_orders_status_customer ON orders (status, customer_id);
CREATE INDEX IF NOT EXISTS idx_refund_records_status ON refund_records (status);
CREATE INDEX IF NOT EXISTS idx_shipments_order_status ON shipments (order_id, status);
