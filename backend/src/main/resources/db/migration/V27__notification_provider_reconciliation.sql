-- V27: Notification Provider Reconciliation & Webhook Lifecycle Migration

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS provider_event_id VARCHAR(255);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS provider_status VARCHAR(100);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS last_provider_update_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_notifications_provider_event ON notifications(provider_name, provider_event_id);
