-- Sporekart v3.0 - Multi-Image Product Carousel Migration
-- Task ID: FE-PRODUCT-IMAGE-CAROUSEL-01

CREATE TABLE product_images (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    display_order INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_product_images_order UNIQUE (product_id, display_order)
);

CREATE INDEX idx_product_images_product ON product_images(product_id);

-- No data backfill required: no existing product had image data.
