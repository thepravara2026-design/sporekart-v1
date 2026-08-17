-- V26: Notification Delivery Lifecycle Schema Migration

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS scheduled_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS suppressed_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_notifications_status_scheduled ON notifications(status, scheduled_at);
CREATE INDEX IF NOT EXISTS idx_notifications_user_category ON notifications(user_id, template_code);
