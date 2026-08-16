-- ============================================================
-- SPOREKART v3.0 — FLYWAY MIGRATION V23
-- SPRINT 6H: PAYMENT & SHIPPING PROVIDER RELIABILITY HARDENING INDEXES
-- ============================================================
-- Adds composite indexes for webhook processing status queries,
-- duplicate event detection, and background provider reconciliation.
-- Uses IF NOT EXISTS for safe execution across H2 and PostgreSQL.
-- ============================================================

-- Payment Webhook Events: composite (provider, processing_status, received_at DESC)
CREATE INDEX IF NOT EXISTS idx_payment_webhooks_provider_status
    ON payment_webhook_events (provider, processing_status, received_at DESC);

-- Shipping Webhook Events: composite (provider, processed_at DESC)
CREATE INDEX IF NOT EXISTS idx_shipping_webhooks_provider_status
    ON shipping_webhook_events (provider, processed_at DESC);
