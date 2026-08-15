-- ============================================================================
-- SPOREKART v3.0 — SPRINT 5B MIGRATION
-- V18: API Security, Idempotency & Abuse Protection Domain
-- ============================================================================

CREATE TABLE IF NOT EXISTS idempotency_records (
    id VARCHAR(36) PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL,
    actor_id VARCHAR(100) NOT NULL,
    request_path VARCHAR(255) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_code INT,
    response_body TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_idempotency_actor_key ON idempotency_records (actor_id, idempotency_key);
CREATE INDEX IF NOT EXISTS idx_idempotency_created ON idempotency_records (created_at);
