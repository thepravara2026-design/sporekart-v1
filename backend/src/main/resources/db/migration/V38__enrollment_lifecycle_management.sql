-- =============================================================
-- SPOREKART v3.0 — TRAINING MODULE
-- TRAINING 7: ENROLLMENT LIFECYCLE & PAYMENT STATE MANAGEMENT
-- V38__enrollment_lifecycle_management.sql
-- =============================================================

ALTER TABLE training_enrollments ADD COLUMN IF NOT EXISTS enrollment_code VARCHAR(100);
ALTER TABLE training_enrollments ADD COLUMN IF NOT EXISTS price_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00;
ALTER TABLE training_enrollments ADD COLUMN IF NOT EXISTS currency VARCHAR(10) NOT NULL DEFAULT 'INR';
ALTER TABLE training_enrollments ADD COLUMN IF NOT EXISTS confirmed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE training_enrollments ADD COLUMN IF NOT EXISTS activated_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE training_enrollments ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP WITH TIME ZONE;

-- Populate enrollment_code for existing records if null
UPDATE training_enrollments 
SET enrollment_code = 'ENR-LEGACY-' || UPPER(SUBSTRING(id FROM 1 FOR 8)) 
WHERE enrollment_code IS NULL;

-- Unique index for enrollment_code
CREATE UNIQUE INDEX IF NOT EXISTS uq_enrollment_code ON training_enrollments(enrollment_code);

-- Immutable Enrollment Transition History Table
CREATE TABLE IF NOT EXISTS training_enrollment_history (
    id VARCHAR(64) PRIMARY KEY,
    enrollment_id VARCHAR(64) NOT NULL REFERENCES training_enrollments(id) ON DELETE CASCADE,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    reason VARCHAR(255),
    actor VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_enrollment_history_enrollment_id ON training_enrollment_history(enrollment_id, created_at);
