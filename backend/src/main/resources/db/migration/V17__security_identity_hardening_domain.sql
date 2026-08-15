-- Sporekart v3.0 Security & Identity Hardening Domain Migration
-- Migration: V17__security_identity_hardening_domain.sql
-- Description: Establishes users, roles, permissions, sessions, refresh_tokens, and security_audit_events tables

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(100) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_CUSTOMER',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
    id VARCHAR(50) PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permissions (
    id VARCHAR(100) PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id VARCHAR(100) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id VARCHAR(50) NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id VARCHAR(50) NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id VARCHAR(100) NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS sessions (
    id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_info VARCHAR(255),
    ip_address VARCHAR(50),
    revoked_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id VARCHAR(100) PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    user_id VARCHAR(100) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    token_family VARCHAR(100) NOT NULL,
    is_rotated BOOLEAN NOT NULL DEFAULT FALSE,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS security_audit_events (
    id VARCHAR(100) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    actor_id VARCHAR(100),
    target_id VARCHAR(100),
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Seed Roles & Permissions
INSERT INTO roles (id, description) VALUES
('ROLE_CUSTOMER', 'Standard customer with access to own orders, cart, and profile'),
('ROLE_ADMIN', 'Platform administrator with management capabilities');

INSERT INTO permissions (id, description) VALUES
('READ_CATALOG', 'Permission to browse product catalog'),
('CREATE_ORDER', 'Permission to place orders'),
('READ_OWN_ORDER', 'Permission to view own order history'),
('ADMIN_ACCESS', 'Permission to perform administrative actions'),
('MANAGE_INVENTORY', 'Permission to update inventory stock');

INSERT INTO role_permissions (role_id, permission_id) VALUES
('ROLE_CUSTOMER', 'READ_CATALOG'),
('ROLE_CUSTOMER', 'CREATE_ORDER'),
('ROLE_CUSTOMER', 'READ_OWN_ORDER'),
('ROLE_ADMIN', 'READ_CATALOG'),
('ROLE_ADMIN', 'CREATE_ORDER'),
('ROLE_ADMIN', 'READ_OWN_ORDER'),
('ROLE_ADMIN', 'ADMIN_ACCESS'),
('ROLE_ADMIN', 'MANAGE_INVENTORY');

-- Seed Default Test Users
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status) VALUES
('cust-101', 'cust101@sporekart.com', '$2a$10$e4gRkGgM1L9X2V3Y4Z5W6u7V8W9X0Y1Z2A3B4C5D6E7F8G9H0I1J2', 'Default', 'Customer', 'ROLE_CUSTOMER', 'ACTIVE'),
('admin-1', 'admin1@sporekart.com', '$2a$10$e4gRkGgM1L9X2V3Y4Z5W6u7V8W9X0Y1Z2A3B4C5D6E7F8G9H0I1J2', 'Default', 'Admin', 'ROLE_ADMIN', 'ACTIVE');

INSERT INTO user_roles (user_id, role_id) VALUES
('cust-101', 'ROLE_CUSTOMER'),
('admin-1', 'ROLE_ADMIN');

-- Indexes for Security Query Optimization
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_expires_at ON sessions(expires_at);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens(token_family);
CREATE INDEX idx_security_audit_actor ON security_audit_events(actor_id);
CREATE INDEX idx_security_audit_created ON security_audit_events(created_at);