-- Flyway Migration V10: Returns, Reverse Logistics & Refund Domain

CREATE TABLE returns (
    id UUID PRIMARY KEY,
    return_reference VARCHAR(64) NOT NULL UNIQUE,
    order_id UUID NOT NULL,
    order_reference VARCHAR(64) NOT NULL,
    customer_id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason_code VARCHAR(32) NOT NULL,
    reason_description VARCHAR(512),
    evidence_urls TEXT,
    policy_version VARCHAR(32) NOT NULL DEFAULT 'v1.0',
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP WITH TIME ZONE,
    received_at TIMESTAMP WITH TIME ZONE,
    inspected_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    reverse_shipment_id UUID,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_returns_order_id ON returns(order_id);
CREATE INDEX idx_returns_order_ref ON returns(order_reference);
CREATE INDEX idx_returns_customer ON returns(customer_id);
CREATE INDEX idx_returns_status ON returns(status);
CREATE INDEX idx_returns_created ON returns(created_at);

CREATE TABLE return_items (
    id UUID PRIMARY KEY,
    return_id UUID NOT NULL REFERENCES returns(id) ON DELETE CASCADE,
    order_item_id UUID NOT NULL,
    product_id UUID NOT NULL,
    sku VARCHAR(100) NOT NULL,
    product_name_snapshot VARCHAR(255) NOT NULL,
    requested_quantity INT NOT NULL,
    approved_quantity INT NOT NULL DEFAULT 0,
    received_quantity INT NOT NULL DEFAULT 0,
    accepted_quantity INT NOT NULL DEFAULT 0,
    rejected_quantity INT NOT NULL DEFAULT 0,
    unit_price DECIMAL(12,2) NOT NULL,
    refund_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    reason_code VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_return_items_return_id ON return_items(return_id);
CREATE INDEX idx_return_items_order_item_id ON return_items(order_item_id);

CREATE TABLE return_status_history (
    id UUID PRIMARY KEY,
    return_id UUID NOT NULL REFERENCES returns(id) ON DELETE CASCADE,
    previous_status VARCHAR(32),
    new_status VARCHAR(32) NOT NULL,
    reason VARCHAR(255),
    actor_type VARCHAR(32) NOT NULL,
    actor_id VARCHAR(128),
    correlation_id VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_return_status_hist_return_id ON return_status_history(return_id);

CREATE TABLE return_inspections (
    id UUID PRIMARY KEY,
    return_id UUID NOT NULL REFERENCES returns(id) ON DELETE CASCADE,
    inspector_id VARCHAR(128) NOT NULL,
    outcome VARCHAR(32) NOT NULL,
    notes VARCHAR(512),
    inspected_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_return_inspections_return_id ON return_inspections(return_id);

CREATE TABLE refund_records (
    id UUID PRIMARY KEY,
    refund_reference VARCHAR(64) NOT NULL UNIQUE,
    return_id UUID NOT NULL REFERENCES returns(id),
    return_reference VARCHAR(64) NOT NULL,
    order_id UUID NOT NULL,
    customer_id VARCHAR(64) NOT NULL,
    payment_id UUID NOT NULL,
    payment_reference VARCHAR(64) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(32) NOT NULL,
    failure_reason VARCHAR(255),
    provider_refund_id VARCHAR(128),
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refund_records_return_id ON refund_records(return_id);
CREATE INDEX idx_refund_records_order_id ON refund_records(order_id);
CREATE INDEX idx_refund_records_idempotency ON refund_records(idempotency_key);
