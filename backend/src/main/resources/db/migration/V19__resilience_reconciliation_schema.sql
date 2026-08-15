-- Flyway Migration V19: Resilience & Reconciliation Schema Updates

ALTER TABLE payments ALTER COLUMN status TYPE VARCHAR(50);
