# ADR 0004: Order Domain Aggregate & Historical Snapshot Strategy

## Status
Accepted

## Context
In Sporekart v3.0, once a customer completes checkout preview and submits an order, the system must produce a durable, immutable commercial record. If future changes occur in the Catalog (e.g. product price hikes, product renames, SKU modifications) or in the Customer Profile (address edits in profile address book), historical orders must remain 100% accurate.

Additionally, order creation must prevent duplicated pricing calculations and remain idempotent against network retries.

## Decision

1. **Immutable Snapshot Persistence**:
   - Product information (`productNameSnapshot`, `variantNameSnapshot`, `sku`, `unitPrice`, `lineSubtotal`, `lineTotal`) is snapshotted into `order_items`.
   - Address information (`shipping_name`, `shipping_phone`, `shipping_address_line1`, `shipping_city`, `shipping_state`, `shipping_postal_code`, `shipping_country`) is snapshotted into `AddressSnapshot` embedded in `orders`.

2. **Single-Source Authoritative Pricing Engine Reuse**:
   - Order creation directly delegates pricing calculation to Sprint 3B's `CheckoutPricingService.calculateCheckoutPreview(...)`.
   - Subtotals, 18% GST tax, shipping fees, and grand totals are persisted as calculated by the pricing engine.

3. **Idempotency Replay**:
   - `orders` table includes a database index on `(customer_id, idempotency_key)`.
   - Duplicate creation requests with identical idempotency keys return the existing `Order` without recalculating or creating duplicate records.

4. **Cart Lifecycle Transition**:
   - Upon successful order creation, the customer's active Cart status transitions from `ACTIVE` to `CHECKED_OUT`.

## Consequences
- Historical orders are completely decoupled from future Catalog or Customer address mutations.
- Single-source pricing guarantees zero monetary discrepancies between checkout preview and order creation.
- Clean boundaries are established for Sprint 3D (Inventory), Sprint 3E (Payments), and Sprint 3F (Order State Machine).
