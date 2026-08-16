-- ============================================================
-- SPOREKART v3.0 — FLYWAY MIGRATION V22
-- SPRINT 6G: SEARCH & CATALOG HARDENING INDEXES
-- ============================================================
-- Adds composite indexes for multi-filter catalog queries
-- combining product status, category_id, and price bounds.
-- Uses IF NOT EXISTS for safe execution across H2 and PostgreSQL.
-- ============================================================

-- Products: composite (status, category_id, price) for filtered catalog listing
CREATE INDEX IF NOT EXISTS idx_products_status_category_price
    ON products (status, category_id, price);
