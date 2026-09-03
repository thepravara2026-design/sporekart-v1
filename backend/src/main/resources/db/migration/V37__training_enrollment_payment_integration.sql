-- =============================================================
-- SPOREKART v3.0 — TRAINING MODULE
-- TRAINING 6: TRAINING ENROLLMENT PAYMENT INTEGRATION
-- V37__training_enrollment_payment_integration.sql
-- =============================================================

-- 1. Seed synthetic system order for Training Module payments to satisfy payments.order_id FK
MERGE INTO orders (
    id, order_number, customer_id, status, currency, subtotal, discount_total, tax_total, shipping_fee, grand_total, shipping_name, shipping_phone, shipping_address_line1, shipping_city, shipping_state, shipping_postal_code, shipping_country, created_at, updated_at
) KEY(id) VALUES (
    '00000000-0000-0000-0000-000000000001', 'TRN-SYSTEM-ORDER', 'SYSTEM', 'CREATED', 'INR', 0.00, 0.00, 0.00, 0.00, 0.00, 'SYSTEM', '0000000000', 'SYSTEM', 'SYSTEM', 'SYSTEM', '000000', 'India', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- 2. Create training_enrollment_payments table
CREATE TABLE IF NOT EXISTS training_enrollment_payments (
    id VARCHAR(64) PRIMARY KEY,
    payment_id VARCHAR(64) NOT NULL,
    batch_id VARCHAR(64) NOT NULL REFERENCES training_batches(id),
    trainee_id VARCHAR(64) NOT NULL REFERENCES users(id),
    enrollment_id VARCHAR(64) REFERENCES training_enrollments(id),
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM'
);

CREATE INDEX IF NOT EXISTS idx_trn_pay_batch_status ON training_enrollment_payments(batch_id, status);
CREATE INDEX IF NOT EXISTS idx_trn_pay_trainee_status ON training_enrollment_payments(trainee_id, status);
CREATE INDEX IF NOT EXISTS idx_trn_pay_payment_id ON training_enrollment_payments(payment_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_trn_pay_active ON training_enrollment_payments(batch_id, trainee_id, status);
