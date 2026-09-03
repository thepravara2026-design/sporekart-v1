-- Flyway Migration V34: Capacity & Slot Management Invariants & Constraints
-- Module: Training Module (Training 3)

-- Add database level check constraints for capacity invariants where supported
ALTER TABLE training_batches ADD CONSTRAINT chk_tb_total_capacity CHECK (total_capacity >= 0);
ALTER TABLE training_batches ADD CONSTRAINT chk_tb_occupied_seats CHECK (occupied_seats >= 0);
ALTER TABLE training_batches ADD CONSTRAINT chk_tb_occupied_le_capacity CHECK (occupied_seats <= total_capacity);

-- Add index on occupied_seats and status to optimize atomic slot allocation and capacity query performance
CREATE INDEX idx_training_batches_occupied_capacity ON training_batches(occupied_seats, total_capacity);
CREATE INDEX idx_training_batches_status_capacity ON training_batches(status, occupied_seats, total_capacity);
