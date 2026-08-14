# Sporekart v3.0 — Order Domain Specification & Integration Guide

## Overview

The `Order` module (`com.sporekart.modules.order`) establishes a durable, historically immutable commercial record for Sporekart v3.0. It receives an authenticated customer's checkout intent, calculates server-authoritative totals by reusing Sprint 3B's `CheckoutPricingService`, snapshots purchased product details and shipping address data, enforces idempotency, and transitions the customer's Cart status from `ACTIVE` to `CHECKED_OUT`.

---

## Key Domain Concepts

### 1. Historical Snapshot Immutability
Once an `Order` is created:
- Product names, SKUs, unit prices, line subtotals, line taxes, and line totals are snapshotted in `order_items`.
- Customer shipping address is snapshotted in `AddressSnapshot` (`shipping_name`, `shipping_phone`, `shipping_address_line1`, `shipping_city`, etc.).
- **Invariant**: Subsequent renames or price changes in the Catalog, or edits to the customer's profile address book, will **NEVER** modify historical Order records.

### 2. Single-Source Authoritative Pricing Engine
- Order creation does NOT duplicate or recalculate pricing logic independently.
- `OrderApplicationService` directly invokes `CheckoutPricingService.calculateCheckoutPreview(cart, shippingAddress, couponCode)`.
- Authoritative subtotals, 18% GST tax, flat/free shipping fees, and grand totals are persisted directly into the `orders` aggregate.

### 3. Concurrency-Safe Order Number Generation
- Public customer-facing order numbers are generated via `OrderNumberPort` -> `SequenceOrderNumberGenerator`.
- Format: `SPK-YYYYMMDD-XXXXXX` (e.g. `SPK-20260815-100001`).
- Backed by PostgreSQL/H2 sequence `order_number_seq` and protected by a database `UNIQUE` constraint.

### 4. Idempotency & Replay Protection
- `CreateOrderCommand` accepts an optional `idempotencyKey`.
- `OrderRepository` enforces a database index on `(customer_id, idempotency_key)`.
- If a customer retries a `POST /api/v1/orders` request with the same `idempotencyKey`, `OrderApplicationService` returns the previously created `Order` (idempotent replay) without creating duplicate orders or recalculating cart checkout.

---

## Database Schema (`V5__order_domain.sql`)

```sql
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
```

---

## REST API Specification

### `POST /api/v1/orders`
Creates a new `Order` from the authenticated customer's active Cart.
- **Header**: `Authorization: Bearer <token>`
- **Request Body**:
```json
{
  "shippingAddress": {
    "fullName": "John Doe",
    "phone": "+919876543210",
    "addressLine1": "123 Green Park",
    "addressLine2": "Apt 4B",
    "city": "Bengaluru",
    "state": "Karnataka",
    "postalCode": "560001",
    "country": "India"
  },
  "idempotencyKey": "REQ-KEY-998877",
  "customerNotes": "Please leave at front door"
}
```
- **Response**: `201 Created` with full `OrderDto`.

### `GET /api/v1/orders`
Retrieves paginated order history for the authenticated customer.
- **Params**: `page` (default 0), `size` (default 10).
- **Response**: `200 OK` with `Page<OrderSummaryDto>`.

### `GET /api/v1/orders/{orderId}`
Retrieves single order details.
- **Response**: `200 OK` with `OrderDto`. (Enforces customer isolation: returns 404 or 403 if requested by another customer).

### `POST /api/v1/orders/{orderId}/cancel`
Cancels an eligible order (status must be `CREATED` or `PAYMENT_PENDING`).
- **Response**: `200 OK` with updated `OrderDto` (status: `CANCELLED`).

---

## Future Sprint Handoff Protocols

### Sprint 3D — Inventory Reservation
- Inventory module reads `Order.getItems()`, extracting `productId`, `sku`, and `quantity` to place stock reservations without reading mutable Cart records.

### Sprint 3E — Payment Domain
- Payment module reads `order.getId()`, `order.getOrderNumber()`, `order.getGrandTotal()`, and `order.getCurrency()`.
- Payment creation and Webhooks reference `orderId`. Payments will NEVER alter historical Order totals.

### Sprint 3F — Order State Machine
- Extends initial state `CREATED` into full lifecycle (`PAYMENT_PENDING`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED`).
