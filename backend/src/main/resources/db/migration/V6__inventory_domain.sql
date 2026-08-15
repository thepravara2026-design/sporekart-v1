-- Flyway Migration V6: Inventory Domain Tables & Constraints

CREATE TABLE inventory_items (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    variant_id UUID NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    on_hand_quantity INT NOT NULL DEFAULT 0 CHECK (on_hand_quantity >= 0),
    reserved_quantity INT NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_reserved_le_onhand CHECK (reserved_quantity <= on_hand_quantity)
);

CREATE INDEX idx_inventory_sku ON inventory_items (sku);
CREATE INDEX idx_inventory_product_id ON inventory_items (product_id);

CREATE TABLE stock_reservations (
    id UUID PRIMARY KEY,
    reservation_reference VARCHAR(100) NOT NULL UNIQUE,
    order_id UUID NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    release_reason VARCHAR(100) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_reservations_order_id ON stock_reservations (order_id);
CREATE INDEX idx_reservations_status_expires ON stock_reservations (status, expires_at);

CREATE TABLE stock_reservation_items (
    id UUID PRIMARY KEY,
    reservation_id UUID NOT NULL REFERENCES stock_reservations(id) ON DELETE CASCADE,
    inventory_item_id UUID NOT NULL REFERENCES inventory_items(id),
    product_id UUID NOT NULL,
    variant_id UUID NULL,
    sku VARCHAR(100) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_res_items_reservation ON stock_reservation_items (reservation_id);

CREATE TABLE stock_movements (
    id UUID PRIMARY KEY,
    inventory_item_id UUID NOT NULL REFERENCES inventory_items(id),
    movement_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    reference_type VARCHAR(50) NULL,
    reference_id VARCHAR(100) NULL,
    previous_on_hand INT NOT NULL,
    resulting_on_hand INT NOT NULL,
    previous_reserved INT NOT NULL,
    resulting_reserved INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_movements_inventory ON stock_movements (inventory_item_id);
