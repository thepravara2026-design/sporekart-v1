-- V31: Training Module Foundation & Architecture Schema

CREATE TABLE training_programs (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    price_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE training_batches (
    id VARCHAR(64) PRIMARY KEY,
    program_id VARCHAR(64) NOT NULL REFERENCES training_programs(id),
    batch_code VARCHAR(100) NOT NULL UNIQUE,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    total_capacity INT NOT NULL,
    occupied_seats INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE batch_schedules (
    id VARCHAR(64) PRIMARY KEY,
    batch_id VARCHAR(64) NOT NULL REFERENCES training_batches(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL,
    location VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE training_enrollments (
    id VARCHAR(64) PRIMARY KEY,
    batch_id VARCHAR(64) NOT NULL REFERENCES training_batches(id),
    trainee_id VARCHAR(64) NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    payment_reference VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_batch_trainee UNIQUE (batch_id, trainee_id)
);

CREATE INDEX idx_training_batches_program_id ON training_batches(program_id);
CREATE INDEX idx_training_batches_status ON training_batches(status);
CREATE INDEX idx_batch_schedules_batch_id ON batch_schedules(batch_id);
CREATE INDEX idx_training_enrollments_batch_id ON training_enrollments(batch_id);
CREATE INDEX idx_training_enrollments_trainee_id ON training_enrollments(trainee_id);
CREATE INDEX idx_training_enrollments_status ON training_enrollments(status);
