-- =============================================================
-- SPOREKART v3.0 — TRAINING MODULE
-- TRAINING 4: TRAINEE ENROLLMENT MANAGEMENT
-- V35__trainee_enrollment_management.sql
-- =============================================================

ALTER TABLE training_enrollments ADD COLUMN IF NOT EXISTS enrolled_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE training_enrollments ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM';
ALTER TABLE training_enrollments ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM';
ALTER TABLE training_enrollments ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100);

-- Unique index for idempotency enforcement
CREATE INDEX IF NOT EXISTS idx_enrollment_idempotency ON training_enrollments(idempotency_key);
