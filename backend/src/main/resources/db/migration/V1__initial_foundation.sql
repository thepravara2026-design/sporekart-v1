-- Sporekart v3.0 Database Foundation Baseline Migration
-- Migration: V1__initial_foundation.sql
-- Description: Establishes technical database foundation and system metadata table

CREATE TABLE IF NOT EXISTS system_metadata (
    meta_key VARCHAR(100) PRIMARY KEY,
    meta_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO system_metadata (meta_key, meta_value) VALUES ('schema_version', '1.0.0');
