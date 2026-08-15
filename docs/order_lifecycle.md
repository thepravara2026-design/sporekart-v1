# Order Lifecycle & State Machine Architecture — SPOREKART v3.0

## 1. Overview
Sprint 3F establishes a production-grade, authoritative Order State Machine and purchase lifecycle coordinator in SPOREKART v3.0. The Order module is the single source of truth for business order status and transition audit history, while respecting domain boundaries across Cart, Checkout, Inventory, Payment, and Shipping.

## 2. Order State Transition Matrix

| Current State | Target State | Trigger / Event | Allowed Actors | Side Effects |
|---|---|---|---|---|
| `CREATED` | `PAYMENT_PENDING` | Payment initiation | `CUSTOMER`, `SYSTEM` | Payment attempt created |
| `CREATED` / `PAYMENT_PENDING` | `CONFIRMED` / `PAID` | Payment callback / webhook verified | `PAYMENT` | Stock reservation confirmed |
| `CREATED` / `PAYMENT_PENDING` | `PAYMENT_FAILED` | Payment authorization failed / declined | `PAYMENT`, `SYSTEM` | — |
| `CREATED` / `PAYMENT_PENDING` / `CONFIRMED` | `CANCELLED` | Customer or Admin cancellation | `CUSTOMER`, `ADMIN` | Stock reservation released |
| `CREATED` / `PAYMENT_PENDING` | `EXPIRED` | Reservation / payment window timeout | `SYSTEM` | Stock reservation released |
| `CONFIRMED` | `PROCESSING` | Warehouse prep initiated | `ADMIN` | — |
| `PROCESSING` | `READY_FOR_FULFILMENT` | Fulfilment prep completed | `ADMIN` | — |
| `READY_FOR_FULFILMENT` | `SHIPPED` | Courier pickup & dispatch | `SHIPPING`, `ADMIN` | Tracking reference recorded |
| `SHIPPED` | `OUT_FOR_DELIVERY` | Package in transit | `SHIPPING`, `ADMIN` | — |
| `OUT_FOR_DELIVERY` | `DELIVERED` | Package handed over to customer | `SHIPPING`, `ADMIN` | — |
| `DELIVERED` | `COMPLETED` | Delivery confirmed / return window passed | `ADMIN`, `SYSTEM` | Lifecycle complete |

Terminal states (`CANCELLED`, `EXPIRED`, `COMPLETED`) are immutable and reject outward state transitions.

## 3. Database Schema & Flyway Migration `V8__order_lifecycle_audit.sql`

```sql
ALTER TABLE orders ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS order_status_history (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    previous_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    actor_type VARCHAR(30) NOT NULL,
    actor_id VARCHAR(100) NULL,
    correlation_id VARCHAR(100) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

## 4. API Endpoints

### Customer Endpoints (`/api/v1/orders`)
- `POST /api/v1/orders`: Create order aggregate from active cart.
- `GET /api/v1/orders`: Paginated order history.
- `GET /api/v1/orders/{orderReference}`: Customer order detail (supports UUID or order number).
- `GET /api/v1/orders/{orderReference}/timeline`: Immutable audit history timeline.
- `POST /api/v1/orders/{orderReference}/cancel`: Customer cancellation.

### Admin Operational Endpoints (`/api/v1/admin/orders`)
- `GET /api/v1/admin/orders`: List orders (paginated, optional status filter).
- `GET /api/v1/admin/orders/{orderNumber}`: Admin inspect order.
- `GET /api/v1/admin/orders/{orderNumber}/timeline`: Admin audit timeline.
- `POST /api/v1/admin/orders/{orderId}/process`: Transition `CONFIRMED` -> `PROCESSING`.
- `POST /api/v1/admin/orders/{orderId}/ready-for-fulfilment`: Transition `PROCESSING` -> `READY_FOR_FULFILMENT`.
- `POST /api/v1/admin/orders/{orderId}/shipped`: Transition `READY_FOR_FULFILMENT` -> `SHIPPED`.
- `POST /api/v1/admin/orders/{orderId}/out-for-delivery`: Transition `SHIPPED` -> `OUT_FOR_DELIVERY`.
- `POST /api/v1/admin/orders/{orderId}/delivered`: Transition `OUT_FOR_DELIVERY` -> `DELIVERED`.
- `POST /api/v1/admin/orders/{orderId}/complete`: Transition `DELIVERED` -> `COMPLETED`.
- `POST /api/v1/admin/orders/{orderId}/cancel`: Administrative cancellation override.
