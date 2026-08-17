CREATE TABLE IF NOT EXISTS training_attendances (
    id VARCHAR(36) PRIMARY KEY,
    enrollment_id VARCHAR(36) NOT NULL,
    schedule_id VARCHAR(36) NOT NULL,
    batch_id VARCHAR(36) NOT NULL,
    trainee_id VARCHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(1000),
    marked_by VARCHAR(100) NOT NULL,
    marked_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_attendance_enrollment_schedule UNIQUE (enrollment_id, schedule_id)
);

CREATE TABLE IF NOT EXISTS training_certificates (
    id VARCHAR(36) PRIMARY KEY,
    certificate_number VARCHAR(100) NOT NULL UNIQUE,
    verification_code VARCHAR(100) NOT NULL UNIQUE,
    enrollment_id VARCHAR(36) NOT NULL UNIQUE,
    trainee_id VARCHAR(36) NOT NULL,
    trainee_name VARCHAR(255),
    program_id VARCHAR(36) NOT NULL,
    program_title VARCHAR(255),
    batch_id VARCHAR(36) NOT NULL,
    batch_code VARCHAR(100),
    issued_at TIMESTAMP NOT NULL,
    completion_date TIMESTAMP NOT NULL,
    issuer_signature VARCHAR(255),
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revocation_reason VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_cert_ver_code ON training_certificates (verification_code);
CREATE INDEX IF NOT EXISTS idx_cert_trainee ON training_certificates (trainee_id);
