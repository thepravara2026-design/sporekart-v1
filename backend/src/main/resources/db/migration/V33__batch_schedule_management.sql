-- V33: Batch & Schedule Management Schema Extensions

ALTER TABLE training_batches ADD COLUMN delivery_mode VARCHAR(50) DEFAULT 'ONLINE' NOT NULL;
ALTER TABLE training_batches ADD COLUMN venue_info VARCHAR(500);
ALTER TABLE training_batches ADD COLUMN meeting_url VARCHAR(500);
ALTER TABLE training_batches ADD COLUMN timezone VARCHAR(50) DEFAULT 'Asia/Kolkata' NOT NULL;
ALTER TABLE training_batches ADD COLUMN created_by VARCHAR(64);
ALTER TABLE training_batches ADD COLUMN updated_by VARCHAR(64);

-- Performance & Lookup Indexes
CREATE INDEX idx_training_batches_start_date ON training_batches(start_date);
CREATE INDEX idx_training_batches_delivery_mode ON training_batches(delivery_mode);
CREATE INDEX idx_training_batches_created_at ON training_batches(created_at);
