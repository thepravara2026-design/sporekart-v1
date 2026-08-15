-- Flyway Migration V9: Shipping, Fulfilment & Delivery Domain

CREATE TABLE shipments (
    id UUID PRIMARY KEY,
    shipment_reference VARCHAR(64) NOT NULL UNIQUE,
    order_id UUID NOT NULL,
    order_reference VARCHAR(64) NOT NULL,
    customer_id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    provider_shipment_id VARCHAR(128),
    awb VARCHAR(128),
    tracking_number VARCHAR(128),
    courier_name VARCHAR(128),
    courier_code VARCHAR(64),
    weight_grams INT,
    length_mm INT,
    width_mm INT,
    height_mm INT,
    declared_value DECIMAL(12,2),
    shipping_name VARCHAR(255) NOT NULL,
    shipping_phone VARCHAR(32) NOT NULL,
    shipping_address_line1 VARCHAR(255) NOT NULL,
    shipping_address_line2 VARCHAR(255),
    shipping_city VARCHAR(100) NOT NULL,
    shipping_state VARCHAR(100) NOT NULL,
    shipping_postal_code VARCHAR(20) NOT NULL,
    shipping_country VARCHAR(100) NOT NULL,
    estimated_delivery_at TIMESTAMP WITH TIME ZONE,
    booked_at TIMESTAMP WITH TIME ZONE,
    picked_up_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shipments_order_id ON shipments(order_id);
CREATE INDEX idx_shipments_order_ref ON shipments(order_reference);
CREATE INDEX idx_shipments_customer ON shipments(customer_id);
CREATE INDEX idx_shipments_status ON shipments(status);
CREATE INDEX idx_shipments_awb ON shipments(awb);
CREATE INDEX idx_shipments_provider ON shipments(provider);

CREATE TABLE shipment_items (
    id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    order_item_id UUID NOT NULL,
    product_id UUID NOT NULL,
    sku VARCHAR(100) NOT NULL,
    product_name_snapshot VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shipment_items_shipment_id ON shipment_items(shipment_id);

CREATE TABLE shipment_status_history (
    id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    previous_status VARCHAR(32),
    new_status VARCHAR(32) NOT NULL,
    reason VARCHAR(255),
    actor_type VARCHAR(32) NOT NULL,
    actor_id VARCHAR(128),
    provider_event_id VARCHAR(128),
    correlation_id VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shipment_status_hist_shipment_id ON shipment_status_history(shipment_id);

CREATE TABLE shipment_tracking_events (
    id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    provider_event_id VARCHAR(128) NOT NULL,
    provider_status VARCHAR(64) NOT NULL,
    normalized_status VARCHAR(32) NOT NULL,
    description VARCHAR(512),
    location VARCHAR(255),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_shipment_provider_event UNIQUE (shipment_id, provider_event_id)
);

CREATE INDEX idx_shipment_tracking_shipment_id ON shipment_tracking_events(shipment_id);

CREATE TABLE shipping_webhook_events (
    id UUID PRIMARY KEY,
    provider VARCHAR(32) NOT NULL,
    provider_event_id VARCHAR(128) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload TEXT NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_shipping_provider_webhook UNIQUE (provider, provider_event_id)
);
