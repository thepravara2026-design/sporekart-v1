# Inventory Domain & Stock Reservation Specification — Sporekart v3.0

## 1. Executive Summary

The **Inventory Module** (`com.sporekart.modules.inventory`) manages real-time stock availability, concurrency-safe inventory reservations, automatic reservation TTL expiry, and immutable audit logs. It guarantees that physical stock availability is authoritative, prevents overselling during high-concurrency checkouts, and establishes clean contracts for Payment Processing (Sprint 3E) and Order State Machine transitions (Sprint 3F).

---

## 2. Invariants & Rules

1. **Available Stock Invariant**: Available stock is defined as `availableQuantity = onHandQuantity - reservedQuantity`. `availableQuantity` must **NEVER** become negative (`availableQuantity >= 0`).
2. **Reserved Stock Invariant**: `reservedQuantity` must **NEVER** exceed `onHandQuantity` (`reservedQuantity <= onHandQuantity`).
3. **Database Concurrency Controls**: Inventory updates use database-level pessimistic locking (`SELECT FOR UPDATE`) with **deterministic SKU lock ordering** (`SKU ASC`) to prevent deadlocks across concurrent multi-item checkout attempts.
4. **Single-Source Order Quantities**: Reserved quantities are extracted exclusively from the authoritative `Order` aggregate. Frontend or client quantity inputs are never trusted.
5. **Idempotency**:
   - `reserveInventoryForOrder(orderId)` is idempotent. Retrying reservation for an active order returns the existing reservation without double-reserving stock.
   - `releaseReservation(reservationId)` is idempotent. Repeated release calls maintain `RELEASED` status safely.
6. **Audit Trail**: Every inventory mutation (`INITIAL_STOCK`, `STOCK_ADJUSTMENT`, `RESERVATION`, `RELEASE`, `EXPIRY`) generates an immutable record in `stock_movements`.

---

## 3. Database Schema (`V6__inventory_domain.sql`)

### `inventory_items` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PRIMARY KEY | Unique inventory item identity |
| `product_id` | UUID | NOT NULL | Catalog product reference |
| `variant_id` | UUID | NULL | Catalog product variant reference |
| `sku` | VARCHAR(100) | NOT NULL, UNIQUE | Authoritative stock SKU |
| `on_hand_quantity` | INT | NOT NULL, DEFAULT 0, CHECK (>= 0) | Total physical stock |
| `reserved_quantity` | INT | NOT NULL, DEFAULT 0, CHECK (>= 0) | Total active reserved stock |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | Item lifecycle status |
| `version` | BIGINT | NOT NULL, DEFAULT 0 | JPA optimistic locking version |

*Check constraint*: `CHECK (reserved_quantity <= on_hand_quantity)`

### `stock_reservations` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PRIMARY KEY | Reservation identity |
| `reservation_reference` | VARCHAR(100) | NOT NULL, UNIQUE | Human-readable reference (`RES-SPK-...`) |
| `order_id` | UUID | NOT NULL, UNIQUE | Authoritative order reference |
| `status` | VARCHAR(20) | NOT NULL | `ACTIVE`, `RELEASED`, `EXPIRED`, `CONFIRMED` |
| `expires_at` | TIMESTAMP TZ | NOT NULL | Expiration deadline (default +15m) |
| `release_reason` | VARCHAR(100) | NULL | Reason for release (`CUSTOMER_CANCELLED`, `EXPIRED`, etc.) |

### `stock_reservation_items` Table
Links reservation to specific SKUs and reserved quantities.

### `stock_movements` Table
Immutable audit log recording `previous_on_hand`, `resulting_on_hand`, `previous_reserved`, `resulting_reserved`, and `movement_type`.

---

## 4. API Endpoints

- `GET /api/v1/inventory/{sku}`: Query real-time stock availability.
- `POST /api/v1/inventory/reserve/{orderId}`: Reserve stock for an active order.
- `POST /api/v1/inventory/reservations/{reservationId}/release`: Release an active reservation.
- `POST /api/v1/inventory/adjust`: Admin stock adjustment (Requires `ROLE_ADMIN`).

---

## 5. Handoff Protocols

### Sprint 3E — Payment Domain Handoff
- When a customer initiates payment, the payment service checks that a valid active `StockReservation` exists (`expiresAt > NOW()`).
- On successful payment, reservation status transitions to `CONFIRMED`.
- On payment failure or cancellation, `releaseReservation` is invoked to restore available stock.

### Sprint 3F — Order State Machine Handoff
- Order state transitions (e.g., `CREATED` -> `AWAITING_PAYMENT` -> `PAID` or `CANCELLED`) interact with `StockReservation` to release or confirm stock.
