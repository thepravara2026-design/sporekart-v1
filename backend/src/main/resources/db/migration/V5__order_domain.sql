-- Flyway Migration V5: Order Domain Tables & Sequence

CREATE SEQUENCE IF NOT EXISTS order_number_seq START WITH 100001 INCREMENT BY 1;

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    subtotal DECIMAL(12, 2) NOT NULL,
    discount_total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    tax_total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    shipping_fee DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(12, 2) NOT NULL,
    idempotency_key VARCHAR(100) NULL,
    shipping_name VARCHAR(200) NOT NULL,
    shipping_phone VARCHAR(50) NOT NULL,
    shipping_address_line1 VARCHAR(255) NOT NULL,
    shipping_address_line2 VARCHAR(255) NULL,
    shipping_city VARCHAR(100) NOT NULL,
    shipping_state VARCHAR(100) NOT NULL,
    shipping_postal_code VARCHAR(20) NOT NULL,
    shipping_country VARCHAR(100) NOT NULL DEFAULT 'India',
    customer_notes VARCHAR(500) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_orders_customer_idempotency ON orders (customer_id, idempotency_key);
CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_orders_order_number ON orders (order_number);
CREATE INDEX idx_orders_created_at ON orders (created_at DESC);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    variant_id UUID NULL,
    sku VARCHAR(100) NOT NULL,
    product_name_snapshot VARCHAR(255) NOT NULL,
    variant_name_snapshot VARCHAR(255) NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    line_subtotal DECIMAL(12, 2) NOT NULL,
    line_total DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
