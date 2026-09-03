-- Sporekart v3.0 - Product Variants & Multi-Unit Quantity Migration
-- Task ID: SPK-CHG-PRODUCT-VARIANTS-001

CREATE TABLE product_variants (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku VARCHAR(50) NOT NULL UNIQUE,
    quantity_value DECIMAL(10, 2) NOT NULL CHECK (quantity_value > 0),
    quantity_unit VARCHAR(10) NOT NULL,
    selling_price DECIMAL(12, 2) NOT NULL CHECK (selling_price >= 0),
    strike_out_price DECIMAL(12, 2) DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_variant_strike_out_price CHECK (strike_out_price IS NULL OR strike_out_price > selling_price),
    CONSTRAINT uq_product_variant_quantity UNIQUE (product_id, quantity_value, quantity_unit)
);

CREATE INDEX idx_product_variants_product_status ON product_variants(product_id, status);
CREATE INDEX idx_product_variants_sku ON product_variants(sku);

-- DATA MIGRATION: Populate a default variant for every existing product
INSERT INTO product_variants (
    id,
    product_id,
    sku,
    quantity_value,
    quantity_unit,
    selling_price,
    strike_out_price,
    status,
    created_at,
    updated_at
)
SELECT
    RANDOM_UUID(),
    p.id,
    p.sku,
    1.00,
    CASE 
        WHEN LOWER(p.name) LIKE '%extract%' OR LOWER(p.name) LIKE '%liquid%' OR LOWER(p.description) LIKE '%liquid%' THEN 'L'
        ELSE 'KG'
    END,
    p.price,
    p.strike_out_price,
    CASE WHEN p.status = 'DRAFT' THEN 'DRAFT' ELSE 'ACTIVE' END,
    p.created_at,
    p.updated_at
FROM products p;

-- Link existing inventory items to their newly created default variant
UPDATE inventory_items ii
SET variant_id = (
    SELECT pv.id 
    FROM product_variants pv 
    WHERE pv.product_id = ii.product_id 
    LIMIT 1
)
WHERE ii.variant_id IS NULL;
