-- V29: Notification Operations & Observability Indexes Migration

CREATE INDEX IF NOT EXISTS idx_notifications_provider ON notifications(provider_name);
CREATE INDEX IF NOT EXISTS idx_notifications_created_status ON notifications(created_at, status);
CREATE INDEX IF NOT EXISTS idx_notifications_channel_status ON notifications(channel, status);
