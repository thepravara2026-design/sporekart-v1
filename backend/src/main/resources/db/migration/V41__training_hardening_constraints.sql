-- =============================================================
-- SPOREKART v3.0 — TRAINING MODULE
-- TRAINING 14: PRODUCTION HARDENING & FINAL ACCEPTANCE
-- V41__training_hardening_constraints.sql
-- =============================================================

-- 1. Enforce actor non-null on enrollment history (audit immutability)
ALTER TABLE training_enrollment_history ALTER COLUMN actor SET NOT NULL;

-- 2. Enforce non-negative price on training programs
ALTER TABLE training_programs ADD CONSTRAINT chk_tp_price_nonneg CHECK (price_amount >= 0);

-- 3. Add audit columns to training_programs (created_by / updated_by were missing from V31/V32)
ALTER TABLE training_programs ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM';
ALTER TABLE training_programs ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM';

-- 4. Enforce capacity data integrity: occupied seats must be >= 0 at application layer too (belt & suspenders)
-- (chk_tb_occupied_seats already exists from V34 — add equivalent on training_enrollment_payments amount)
ALTER TABLE training_enrollment_payments ADD CONSTRAINT chk_tep_amount_nonneg CHECK (amount >= 0);

-- 5. Performance index: audit history lookup by actor and enrollment (powers Training 13 reporting)
CREATE INDEX IF NOT EXISTS idx_trn_enr_history_actor ON training_enrollment_history(actor);
CREATE INDEX IF NOT EXISTS idx_trn_enr_history_enrollment ON training_enrollment_history(enrollment_id, created_at);

-- 6. Performance index: enrollment by status + trainee (powers trainee dashboard queries)
CREATE INDEX IF NOT EXISTS idx_trn_enr_trainee_status ON training_enrollments(trainee_id, status);

-- 7. Performance index: certificate by trainee (powers trainee portal)
CREATE INDEX IF NOT EXISTS idx_trn_cert_trainee ON training_certificates(enrollment_id);

-- 8. Ensure training_batches.start_date has an index for cancellation-window queries
CREATE INDEX IF NOT EXISTS idx_trn_batch_start_date ON training_batches(start_date);
