-- =============================================================
-- SPOREKART v3.0 — TRAINING MODULE
-- TRAINING 5: FULL BATCH & DEMAND HANDLING
-- V36__full_batch_demand_management.sql
-- =============================================================

CREATE TABLE IF NOT EXISTS training_demand_requests (
    id VARCHAR(64) PRIMARY KEY,
    batch_id VARCHAR(64) NOT NULL REFERENCES training_batches(id),
    trainee_id VARCHAR(64) NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM'
);

CREATE INDEX IF NOT EXISTS idx_training_demand_batch_status ON training_demand_requests(batch_id, status);
CREATE INDEX IF NOT EXISTS idx_training_demand_trainee_status ON training_demand_requests(trainee_id, status);
CREATE INDEX IF NOT EXISTS idx_training_demand_batch_trainee ON training_demand_requests(batch_id, trainee_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_training_demand_batch_trainee_status ON training_demand_requests(batch_id, trainee_id, status);
