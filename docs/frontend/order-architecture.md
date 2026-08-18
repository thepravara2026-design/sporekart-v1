# SPOREKART v3.0 — Order Management & History Architecture (FD-13)

**Sprint:** FD-13 — Order Management History Experience  
**Status:** Production / Backend-Contract Aligned  
**Location:** `frontend/src/features/orders/`

---

## 1. Overview

The orders feature delivers the customer's order-management surfaces: a paginated order history list (`/orders`), a full order detail view (`/orders/:orderReference`), and the return-request entry flow (`/orders/:orderReference/return-request`). The backend is the single authority for order state, pricing, shipment, tracking, and return eligibility — the frontend renders server-computed values and never fabricates domain data.

### Route Map

```text
/orders                                OrdersPage        — paginated history + status filter
/orders/:orderReference                OrderDetailPage   — items, totals, timeline, shipment/tracking,
                                                           cancellation, return/support handoffs
/orders/:orderReference/return-request ReturnRequestPage  — eligibility check + return submission
```

Order status domain (labels, badge variants, cancellable set) lives in the orders feature and is re-exported for backward compatibility with checkout surfaces (see §5).

---

## 2. Backend Contract Constraints (from the FD-13 audit)

The implementation is deliberately limited to what the backend exposes:

| Surface | Backend truth |
|---|---|
| History | `GET /api/v1/orders?page=&size=` returns a zero-indexed `PageResponse<OrderSummaryDto>` (newest-first) |
| Order detail | `GET /api/v1/orders/{orderReference}` returns `OrderDto` (items, totals, shipping address) |
| Timeline | `GET /api/v1/orders/{orderReference}/timeline` returns `OrderTimelineDto` (`currentStatus` + `history[]`) |
| Cancellation | `POST /api/v1/orders/{orderReference}/cancel` — backend `OrderStatus.isCancellable()` is the enforcement authority |
| Shipment | `GET /api/v1/orders/{orderReference}/shipment` returns `ShipmentDto` (404 until a shipment exists) |
| Tracking | `GET /api/v1/orders/{orderReference}/tracking` returns `ShipmentTrackingResponseDto` (`timeline[]`) |
| Return eligibility | `GET /api/v1/orders/{orderReference}/return-eligibility` — backend decides eligibility + deadline |
| Return submission | `POST /api/v1/orders/{orderReference}/returns` (`CreateReturnCommand`) |

**Filters:** the status filter is applied client-side over the fetched page (the backend history endpoint does not accept a status query param). `ALL` shows the raw page; a specific status filters `page.content` in the query layer.

---

## 3. Directory Layout

```text
src/features/orders/
├── constants/orderConstants.ts      # ORDER_PAGE_SIZE, ORDER_STATUS_FILTERS, CANCELABLE_ORDER_STATUSES
├── utils/
│   ├── orderStatus.ts               # OrderStatus enum, labels, badge variants (canonical domain)
│   └── orderUtils.ts                # Safe error messages, shipment status labels/variants, formatOrderDate
├── hooks/
│   ├── useOrder.ts                  # ORDER_KEYS, useOrders, useOrderByReference, useOrderTimeline, useCancelOrder
│   ├── useShipment.ts               # useOrderShipment, useShipmentTracking
│   └── useReturnEligibility.ts      # useReturnEligibility (handoff gate only)
├── components/
│   ├── OrderStatusBadge.tsx         # Human label + visual variant (backend label only)
│   ├── OrderCard.tsx                # History summary card (link, date, count, status, total)
│   ├── OrderList.tsx                # Cards list or empty state
│   ├── OrderFilters.tsx             # Status filter Select
│   ├── OrderPagination.tsx          # Zero-indexed page navigation
│   ├── OrderSkeleton.tsx / OrderEmptyState.tsx / OrderErrorState.tsx
│   ├── OrderItem.tsx / OrderItems.tsx / OrderSummary.tsx   # Detail lines, totals
│   ├── OrderTimeline.tsx            # Status-event timeline
│   ├── ShipmentStatus.tsx / ShipmentTracking.tsx           # Shipment + courier scans
│   ├── OrderActions.tsx             # Cancel action + confirmation Dialog
│   ├── OrderReturnHandoff.tsx       # Return handoff (eligibility-gated) + support handoff
│   └── __tests__/
├── pages/
│   ├── OrdersPage.tsx               # History list + filter + pagination (+ __tests__)
│   └── OrderDetailPage.tsx          # Full order detail (+ __tests__)
└── index.ts                         # Feature barrel export
```

---

## 4. Page Orchestration

### OrdersPage (`/orders`)

1. **Unauthenticated** → warning Alert + Continue Shopping (the frontend has no login route; 401 handling is message-based).
2. **Loading** → `OrderSkeleton`.
3. **Error** → `OrderErrorState` (Retry) using `getOrderErrorMessage`.
4. **Empty** → `OrderEmptyState` with Start Shopping link.
5. **Populated** → `OrderFilters` + `OrderList` + `OrderPagination` (only when `totalPages > 1`).

Zero-indexed pages: `useOrders(page, ORDER_PAGE_SIZE, status, authenticated)`; changing the filter resets to page 0 via a `useEffect`.

### OrderDetailPage (`/orders/:orderReference`)

The page composes independent backend queries (each cached under `ORDER_KEYS`):

- `useOrderByReference` — detail (required)
- `useOrderTimeline` — status events
- `useOrderShipment` + `useShipmentTracking` — enabled only for `SHIPPED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `COMPLETED`

Layout: header card (date + status badge + `OrderActions`), items + timeline column, summary + address + shipment/tracking + return/support handoffs column.

### ReturnRequestPage (`/orders/:orderReference/return-request`)

Owned by the returns feature but routed from the orders domain. On mount it calls `checkEligibility`; only an eligible response renders the submission form (items, quantities capped at `returnableQuantity`, reason, description, evidence URLs). Successful submission shows the returned reference and links back to the order / history.

---

## 5. Domain Re-export Strategy

Order status was previously duplicated in `checkout/utils/orderStatus.ts`. FD-13 consolidates the canonical domain into `orders/utils/orderStatus.ts` and makes the checkout copy a thin re-export so existing checkout surfaces (confirmation page, review) keep their import paths:

```text
checkout/utils/orderStatus.ts   →  export * from '../../orders/utils/orderStatus'
checkout/hooks/useOrder.ts      →  re-exports ORDER_KEYS + useOrderByReference from orders feature
```

Cancellation mirroring: `CANCELABLE_ORDER_STATUSES` exists only to decide *when to surface* the Cancel action; the backend remains the enforcement authority and rejects disallowed cancellations.

---

## 6. Error Handling

- `getOrderErrorMessage` maps backend codes (`ORDER_NOT_FOUND`, `ORDER_ACCESS_DENIED`, `ORDER_NOT_CANCELLABLE`, `PAYMENT_NOT_FOUND`) plus 401/403/404 to safe user-facing messages with a generic fallback.
- `getShipmentErrorMessage` treats 404 as "no shipment yet" (expected during fulfilment) rather than an error.
- `useOrderShipment` / `useShipmentTracking` / `useReturnEligibility` set `retry: false` — a 404 shipment or ineligible order is a legitimate state, not a transient failure.
- `ApiError` preserves the backend error envelope (`code`, `message`, `timestamp`, `path`, `requestId`).

---

## 7. Accessibility

- `OrderFilters` uses a labelled `Select` (`aria-label="Filter orders by status"`).
- `OrderCard` links expose the order number; the whole card is a single column layout on narrow viewports (`flexWrap`).
- `OrderTimeline` / `ShipmentTracking` render semantic `<ol>/<li>` lists with `aria-hidden` decorative markers.
- `OrderActions` Cancel button sets `aria-haspopup="dialog"`; the confirmation `Dialog` is focus-trapped by the shared primitive; the submit button sets `aria-busy`/loading name while pending (duplicate-submission protection).
- Loading skeletons are `aria-hidden`; spinners carry `role="status"` with explicit labels.
- Error states render safe messages (never stack traces) with a Retry action.

---

## 8. Testing

- **Page tests** (`pages/__tests__/OrdersPage.test.tsx`): sign-in guard, paginated card rendering, empty state, error+retry, zero-indexed pagination, filter→page-0 reset.
- **Page test** (`pages/__tests__/OrderDetailPage.test.tsx`): detail render, sign-in guard, cancellation via dialog, shipment-section gating.
- **Component tests** (`components/__tests__/`): `OrderCard`, `OrderStatusBadge`, `OrderSummary`, `OrderTimeline`, `ShipmentStatus`, `ShipmentTracking`, `OrderReturnHandoff`.
- **Utils test** (`utils/__tests__/orderUtils.test.ts`): error mapping, status labels/variants, date formatting.
- **Contract schemas** (`types/schemas/contractSchemas.ts` + `services/__tests__/contractSchemas.test.ts`): Zod schemas for `OrderSummaryDto`, `OrderStatusHistoryDto`, `OrderTimelineDto`, `ShipmentTrackingResponseDto`, `ShipmentDto` with round-trip tests.
- **Regression:** 387 tests across 62 files; `tsc --noEmit`, `vite build`, and `eslint` (FD-13 files) all clean.