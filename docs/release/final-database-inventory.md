# SPOREKART v3.0 — Final Database Inventory

**Database**: PostgreSQL 16  
**Flyway Migrations**: V1 through V16  
**Total Tables**: 22 Tables

---

## 1. Table Inventory & Domain Ownership

| Table Name | Owning Domain | Primary Key | Key Indexes & Constraints |
| :--- | :--- | :--- | :--- |
| `products` | `catalog` | `UUID` / `sku` | Unique `sku`, index on `category_id`, `status` |
| `categories` | `catalog` | `UUID` | Unique `slug` |
| `customers` | `customer` | `UUID` / `email` | Unique `email` |
| `customer_addresses` | `customer` | `UUID` | FK `customer_id` |
| `carts` | `cart` | `UUID` | Unique `customer_id`, `@Version` locking |
| `cart_items` | `cart` | `UUID` | FK `cart_id`, unique `(cart_id, sku)` |
| `orders` | `order` | `UUID` | Unique `order_number`, index `(customer_id, created_at DESC)` |
| `order_items` | `order` | `UUID` | FK `order_id`, index `product_id` |
| `order_status_history` | `order` | `UUID` | FK `order_id` |
| `payments` | `payment` | `UUID` | Unique `payment_reference`, `CHECK (amount > 0)` |
| `payment_attempts` | `payment` | `UUID` | FK `payment_id`, unique `attempt_reference` |
| `payment_webhook_events` | `payment` | `UUID` | Unique `(provider, provider_event_id)`, composite index `(provider_event_id, processing_status)` |
| `inventory_items` | `inventory` | `UUID` | Unique `sku`, composite index `(sku, low_stock_threshold)`, `@Version` locking |
| `stock_reservations` | `inventory` | `UUID` | FK `order_id`, index `expires_at` |
| `shipments` | `shipment` | `UUID` | Unique `shipment_reference`, composite index `(provider, status)` |
| `shipment_tracking_events` | `shipment` | `UUID` | FK `shipment_id` |
| `returns` | `returns` | `UUID` | Unique `return_reference`, composite index `(customer_id, status)` |
| `return_items` | `returns` | `UUID` | FK `return_id` |
| `return_inspections` | `returns` | `UUID` | FK `return_id` |
| `refund_records` | `refund` | `UUID` | Unique `refund_reference`, unique `idempotency_key`, `CHECK (amount > 0)`, composite index `(customer_id, status)` |
| `support_tickets` | `support` | `VARCHAR(36)` | Unique `ticket_number`, composite index `(customer_id, status)` |
| `support_messages` | `support` | `VARCHAR(36)` | FK `ticket_id` |
| `replacement_requests` | `support` | `VARCHAR(36)` | Unique `replacement_reference`, FK `ticket_id` |
| `product_reviews` | `review` | `VARCHAR(36)` | Unique `review_reference`, composite index `(product_id, status)` |
| `review_helpfulness_votes` | `review` | `VARCHAR(36)` | Unique `(review_id, customer_id)` |
| `product_rating_summaries` | `review` | `VARCHAR(36)` | PK `product_id` |

---

## 2. Retention & Data Governance

- Transactional records (`orders`, `payments`, `refund_records`) are retained indefinitely for legal & accounting compliance.
- Audit history tables (`order_status_history`, `support_ticket_status_history`) retain full append-only transition history.