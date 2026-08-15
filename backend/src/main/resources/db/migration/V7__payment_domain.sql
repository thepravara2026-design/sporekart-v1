-- Flyway Migration V7: Payment Domain Tables & Constraints

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    payment_reference VARCHAR(100) NOT NULL UNIQUE,
    order_id UUID NOT NULL REFERENCES orders(id),
    customer_id VARCHAR(100) NOT NULL,
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(20) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    active_attempt_id UUID NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_customer_id ON payments (customer_id);

CREATE TABLE payment_attempts (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    attempt_reference VARCHAR(100) NOT NULL UNIQUE,
    provider VARCHAR(50) NOT NULL,
    provider_order_id VARCHAR(100) NULL,
    provider_payment_id VARCHAR(100) NULL,
    provider_signature VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    failure_code VARCHAR(100) NULL,
    failure_reason VARCHAR(255) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_attempts_payment_id ON payment_attempts (payment_id);
CREATE INDEX idx_attempts_provider_order ON payment_attempts (provider, provider_order_id);
CREATE INDEX idx_attempts_provider_payment ON payment_attempts (provider, provider_payment_id);

CREATE TABLE payment_webhook_events (
    id UUID PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,
    provider_event_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    signature_verified BOOLEAN NOT NULL DEFAULT FALSE,
    processing_status VARCHAR(20) NOT NULL,
    error_reason VARCHAR(255) NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT uq_provider_event UNIQUE (provider, provider_event_id)
);

CREATE INDEX idx_webhook_events_status ON payment_webhook_events (processing_status);
