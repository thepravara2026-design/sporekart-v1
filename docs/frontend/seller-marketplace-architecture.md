# SPOREKART v3.0 — Seller Marketplace Architecture (FD-15)

**Sprint:** FD-15  
**Module:** `frontend/src/features/seller/`  
**Status:** PASS  
**Date:** 2026-08-19  

---

## 1. Overview

FD-15 establishes the **Seller Marketplace UI Foundation** for SPOREKART v3.0. It provides a dedicated seller portal shell (`/seller/*`) enabling third-party merchants and vendors to manage their product catalog listings, monitor real-time inventory synchronization across facilities, process customer order fulfillment, and track financial payout settlements.

---

## 2. Route Architecture

Registered routes in [`App.tsx`](file:///f:/sporekart-v3.0/frontend/src/app/App.tsx):

- `/seller` & `/seller/dashboard` -> `SellerDashboardPage` (Sales metrics, lifetime revenue, payout summary)
- `/seller/products` -> `SellerProductManagementPage` (Listing creation modal, category filter, active listing table)
- `/seller/inventory` -> `SellerInventoryPage` (On-hand & reserved stock, warehouse location, stock adjustment modal)
- `/seller/orders` -> `SellerOrderManagementPage` (Order fulfillment status transitions, customer dispatch control)

---

## 3. Domain Components & UI Primitives

1. **`SellerLayout`**: Shared portal header banner with tabbed navigation and responsive layout.
2. **`SellerMetricsCard`**: Highlights lifetime sales, monthly revenue, active listings count, and on-hand stock.
3. **`InventoryStatusBadge`**: Visual status badge (`Synced`, `Syncing`, `Sync Error`, `Out of Sync`).
4. **`PayoutSummaryTable`**: Historical payout settlements table with status indicators (`Completed`, `Pending`, `Processing`).
5. **`SellerProductTable`**: Paginated seller catalog table with price, SKU, category, and listing status.
6. **`SellerInventoryTable`**: Warehouse stock table with available quantity calculation and inline adjustment triggers.
7. **`SellerOrderTable`**: Order fulfillment processing table with status transition action buttons.

---

## 4. API Client & Server State Management

- **Endpoint Registry**: Added `SELLER_DASHBOARD`, `SELLER_PRODUCTS`, `SELLER_INVENTORY`, `SELLER_ORDERS`, `SELLER_PAYOUTS` to `ENDPOINTS` in [`endpoints.ts`](file:///f:/sporekart-v3.0/frontend/src/services/endpoints.ts).
- **Service**: Implemented `sellerApi` in `frontend/src/features/seller/api/sellerApi.ts` using `axiosInstance`.
- **Query Hooks**: Custom TanStack Query hooks in `useSeller.ts`:
  - `useSellerMetrics`
  - `useSellerProducts`
  - `useCreateSellerProduct` (invalidates `SELLER_QUERY_KEYS.PRODUCTS` & `METRICS`)
  - `useSellerInventory`
  - `useAdjustSellerStock` (invalidates `SELLER_QUERY_KEYS.INVENTORY` & `PRODUCTS`)
  - `useSellerOrders`
  - `useTransitionSellerOrder` (invalidates `SELLER_QUERY_KEYS.ORDERS` & `METRICS`)
  - `useSellerPayouts`

---

## 5. Verification & Acceptance

- **Vitest Suite**: 417 / 417 tests passed (67 test files).
- **TypeScript (`npx tsc --noEmit`)**: 0 errors.
- **Production Build (`npm run build`)**: PASS (Bundled cleanly in 5.02s).
