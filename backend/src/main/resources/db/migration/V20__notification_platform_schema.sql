-- V20: Notification & Communication Platform Schema

CREATE TABLE notification_templates (
    id VARCHAR(64) PRIMARY KEY,
    template_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(256),
    category VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE notification_template_versions (
    id VARCHAR(64) PRIMARY KEY,
    template_id VARCHAR(64) NOT NULL REFERENCES notification_templates(id) ON DELETE CASCADE,
    template_code VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    version INT NOT NULL,
    locale VARCHAR(16) NOT NULL DEFAULT 'en-US',
    subject VARCHAR(256),
    body TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_template_channel_version_locale UNIQUE (template_code, channel, version, locale)
);

CREATE TABLE notifications (
    id VARCHAR(64) PRIMARY KEY,
    event_id VARCHAR(64),
    event_type VARCHAR(64) NOT NULL,
    user_id VARCHAR(64),
    customer_id VARCHAR(64),
    channel VARCHAR(32) NOT NULL,
    template_code VARCHAR(64) NOT NULL,
    template_version INT NOT NULL DEFAULT 1,
    recipient VARCHAR(256) NOT NULL,
    subject VARCHAR(256),
    body TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    priority VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    provider_name VARCHAR(64),
    provider_message_id VARCHAR(128),
    idempotency_key VARCHAR(128) UNIQUE,
    correlation_id VARCHAR(64),
    trace_id VARCHAR(64),
    attempt_count INT NOT NULL DEFAULT 0,
    read_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    CONSTRAINT uq_notification_event_channel_recipient_version UNIQUE (event_id, channel, recipient, template_version)
);

CREATE TABLE notification_delivery_attempts (
    id VARCHAR(64) PRIMARY KEY,
    notification_id VARCHAR(64) NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    attempt_number INT NOT NULL,
    provider_name VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    provider_message_id VARCHAR(128),
    error_message TEXT,
    duration_ms BIGINT NOT NULL DEFAULT 0,
    attempted_at TIMESTAMP NOT NULL
);

CREATE TABLE notification_preferences (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    category VARCHAR(32) NOT NULL,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sms_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    whatsapp_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    in_app_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_notification_category UNIQUE (user_id, category)
);

-- Indexes for performance
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_channel ON notifications(channel);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_read_at ON notifications(user_id, read_at);
CREATE INDEX idx_delivery_attempts_notification ON notification_delivery_attempts(notification_id);
CREATE INDEX idx_template_versions_active ON notification_template_versions(template_code, channel, status);
