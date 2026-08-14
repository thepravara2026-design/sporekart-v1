-- Sporekart v3.0 - Sprint 2A Catalog Query Indexes

CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_created_at ON products(created_at);
