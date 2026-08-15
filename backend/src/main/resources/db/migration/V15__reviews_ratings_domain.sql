-- ============================================================
-- SPOREKART v3.0 - FLYWAY MIGRATION V15
-- SPRINT 4I: CUSTOMER REVIEWS, RATINGS & TRUST GOVERNANCE
-- ============================================================

CREATE TABLE product_reviews (
    id VARCHAR(36) PRIMARY KEY,
    review_reference VARCHAR(32) NOT NULL UNIQUE,
    product_id VARCHAR(36) NOT NULL,
    order_id VARCHAR(36) NOT NULL,
    order_item_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(64) NOT NULL,
    rating INT NOT NULL,
    quality_rating INT,
    value_rating INT,
    title VARCHAR(255) NOT NULL,
    comment TEXT NOT NULL,
    is_verified_purchase BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(32) NOT NULL,
    rejection_reason VARCHAR(255),
    helpful_count INT NOT NULL DEFAULT 0,
    unhelpful_count INT NOT NULL DEFAULT 0,
    media_urls TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_customer_order_item_review UNIQUE (customer_id, order_item_id)
);

CREATE INDEX idx_product_reviews_product ON product_reviews(product_id);
CREATE INDEX idx_product_reviews_customer ON product_reviews(customer_id);
CREATE INDEX idx_product_reviews_status ON product_reviews(status);
CREATE INDEX idx_product_reviews_rating ON product_reviews(rating);

CREATE TABLE review_helpfulness_votes (
    id VARCHAR(36) PRIMARY KEY,
    review_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(64) NOT NULL,
    is_helpful BOOLEAN NOT NULL,
    voted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_review_customer_vote UNIQUE (review_id, customer_id),
    CONSTRAINT fk_helpfulness_review FOREIGN KEY (review_id) REFERENCES product_reviews(id) ON DELETE CASCADE
);

CREATE INDEX idx_helpfulness_review ON review_helpfulness_votes(review_id);

CREATE TABLE review_merchant_replies (
    id VARCHAR(36) PRIMARY KEY,
    review_id VARCHAR(36) NOT NULL,
    author_id VARCHAR(64) NOT NULL,
    reply_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_merchant_reply_review FOREIGN KEY (review_id) REFERENCES product_reviews(id) ON DELETE CASCADE
);

CREATE INDEX idx_merchant_reply_review ON review_merchant_replies(review_id);

CREATE TABLE product_rating_summaries (
    product_id VARCHAR(36) PRIMARY KEY,
    average_rating NUMERIC(3, 2) NOT NULL DEFAULT 0.00,
    total_reviews_count INT NOT NULL DEFAULT 0,
    star1_count INT NOT NULL DEFAULT 0,
    star2_count INT NOT NULL DEFAULT 0,
    star3_count INT NOT NULL DEFAULT 0,
    star4_count INT NOT NULL DEFAULT 0,
    star5_count INT NOT NULL DEFAULT 0,
    verified_purchase_count INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
