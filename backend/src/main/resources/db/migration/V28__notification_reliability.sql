-- V28: Notification Reliability, Resilience & Automated Reconciliation Migration

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS last_attempt_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS next_retry_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS reconciliation_attempt_count INT DEFAULT 0;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS last_reconciliation_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_notifications_stale_processing ON notifications(status, updated_at);
CREATE INDEX IF NOT EXISTS idx_notifications_reconciliation ON notifications(status, last_provider_update_at, created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_next_retry ON notifications(status, next_retry_at);
