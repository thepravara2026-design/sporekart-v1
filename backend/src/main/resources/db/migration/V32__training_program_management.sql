-- V32: Training Program Management Schema Extensions

ALTER TABLE training_programs ADD COLUMN slug VARCHAR(255);
ALTER TABLE training_programs ADD COLUMN category VARCHAR(100) DEFAULT 'GENERAL' NOT NULL;
ALTER TABLE training_programs ADD COLUMN duration_hours INT DEFAULT 0 NOT NULL;
ALTER TABLE training_programs ADD COLUMN created_by VARCHAR(64);
ALTER TABLE training_programs ADD COLUMN updated_by VARCHAR(64);

-- Populate slug for existing data if any exists
UPDATE training_programs SET slug = LOWER(REPLACE(title, ' ', '-')) WHERE slug IS NULL;

-- Enforce UNIQUE constraint on slug
ALTER TABLE training_programs ADD CONSTRAINT uq_training_programs_slug UNIQUE (slug);

-- Performance & Lookup Indexes
CREATE INDEX idx_training_programs_status ON training_programs(status);
CREATE INDEX idx_training_programs_category ON training_programs(category);
