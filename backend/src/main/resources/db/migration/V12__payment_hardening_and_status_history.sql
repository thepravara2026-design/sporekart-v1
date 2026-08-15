-- SPOREKART v3.0 — Sprint 4B Payment Hardening & Status History Audit Schema

CREATE TABLE IF NOT EXISTS payment_status_history (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    previous_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NOT NULL,
    source VARCHAR(50) NOT NULL,
    actor_type VARCHAR(50) NOT NULL,
    actor_id VARCHAR(100) NULL,
    provider_event_id VARCHAR(100) NULL,
    reason VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(100) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_payment_status_history_payment_id ON payment_status_history(payment_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_payment_reference ON payments(payment_reference);
CREATE INDEX IF NOT EXISTS idx_payment_attempts_provider_order_id ON payment_attempts(provider_order_id);
