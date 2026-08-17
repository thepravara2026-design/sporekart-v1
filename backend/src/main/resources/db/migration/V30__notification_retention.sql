-- V30: Notification Governance, Retention & Payload Minimization Schema Migration

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS content_hash VARCHAR(64);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS payload_minimized_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_notifications_status_created ON notifications(status, created_at);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_created ON outbox_events(status, created_at);
