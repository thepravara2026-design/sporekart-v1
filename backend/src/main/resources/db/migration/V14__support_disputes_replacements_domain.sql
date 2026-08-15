-- ============================================================
-- SPOREKART v3.0 - FLYWAY MIGRATION V14
-- SPRINT 4H: POST-PURCHASE SUPPORT, DISPUTES, REPLACEMENTS
-- ============================================================

CREATE TABLE support_tickets (
    id VARCHAR(36) PRIMARY KEY,
    ticket_number VARCHAR(32) NOT NULL UNIQUE,
    customer_id VARCHAR(64) NOT NULL,
    order_id VARCHAR(36),
    order_reference VARCHAR(64),
    category VARCHAR(32) NOT NULL,
    issue_type VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    priority VARCHAR(16) NOT NULL,
    source VARCHAR(32) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    assigned_agent_id VARCHAR(64),
    first_response_at TIMESTAMP WITH TIME ZONE,
    first_response_due_at TIMESTAMP WITH TIME ZONE NOT NULL,
    resolution_due_at TIMESTAMP WITH TIME ZONE NOT NULL,
    sla_status VARCHAR(16) NOT NULL,
    resolved_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_support_tickets_customer ON support_tickets(customer_id);
CREATE INDEX idx_support_tickets_order ON support_tickets(order_id);
CREATE INDEX idx_support_tickets_status ON support_tickets(status);
CREATE INDEX idx_support_tickets_priority ON support_tickets(priority);
CREATE INDEX idx_support_tickets_category ON support_tickets(category);
CREATE INDEX idx_support_tickets_assigned ON support_tickets(assigned_agent_id);
CREATE INDEX idx_support_tickets_sla ON support_tickets(sla_status);

CREATE TABLE support_messages (
    id VARCHAR(36) PRIMARY KEY,
    ticket_id VARCHAR(36) NOT NULL,
    author_id VARCHAR(64) NOT NULL,
    author_type VARCHAR(16) NOT NULL,
    visibility VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    attachment_urls TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_support_messages_ticket FOREIGN KEY (ticket_id) REFERENCES support_tickets(id) ON DELETE CASCADE
);

CREATE INDEX idx_support_messages_ticket ON support_messages(ticket_id);

CREATE TABLE replacement_requests (
    id VARCHAR(36) PRIMARY KEY,
    replacement_reference VARCHAR(32) NOT NULL UNIQUE,
    ticket_id VARCHAR(36) NOT NULL,
    ticket_number VARCHAR(32) NOT NULL,
    order_id VARCHAR(36) NOT NULL,
    order_reference VARCHAR(64) NOT NULL,
    customer_id VARCHAR(64) NOT NULL,
    order_item_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    sku VARCHAR(64) NOT NULL,
    quantity INT NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    reservation_id VARCHAR(36),
    replacement_shipment_id VARCHAR(36),
    admin_notes TEXT,
    approved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_replacement_ticket FOREIGN KEY (ticket_id) REFERENCES support_tickets(id) ON DELETE CASCADE
);

CREATE INDEX idx_replacement_ticket ON replacement_requests(ticket_id);
CREATE INDEX idx_replacement_customer ON replacement_requests(customer_id);
CREATE INDEX idx_replacement_status ON replacement_requests(status);

CREATE TABLE support_ticket_status_history (
    id VARCHAR(36) PRIMARY KEY,
    ticket_id VARCHAR(36) NOT NULL,
    previous_status VARCHAR(32),
    new_status VARCHAR(32) NOT NULL,
    reason VARCHAR(255),
    actor_type VARCHAR(16) NOT NULL,
    actor_id VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_ticket_history_ticket FOREIGN KEY (ticket_id) REFERENCES support_tickets(id) ON DELETE CASCADE
);

CREATE INDEX idx_ticket_history_ticket ON support_ticket_status_history(ticket_id);
