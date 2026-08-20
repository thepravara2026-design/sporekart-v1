# Customer Cart / Checkout / Payment / Order Authorization Fix

**Branch:** `feature/fix-customer-cart-checkout-authorization`
**Date:** 2026-08-20
**Severity:** High — entire customer commerce pipeline was broken for authenticated users

## Problem Statement

Authenticated customers were unable to complete the Product → Add to Cart → Cart → Checkout → Payment → Order pipeline. The self-gating auth check in page components used a different auth source than the React AuthContext, causing a race condition during auth hydration that displayed "Sign in required" on page refresh.

## Root Causes

### 1. Dual auth state (Primary)
- `CartPage`, `CheckoutPage`, `OrdersPage`, and `OrderConfirmationPage` each called `isAuthenticated()` from `cartUtils.ts`, which reads directly from `localStorage.getItem('accessToken')`.
- `AuthContext` (React state) hydrates asynchronously via `useEffect` — `isLoading` is `true` during mount, then `false` after hydration.
- On page refresh, `localStorage` has the token but `AuthContext` hasn't hydrated yet. The self-gating in each page checked `isAuthenticated()` from `cartUtils` (raw localStorage) **before** `AuthContext` was ready, causing a flash of "Sign in required".

### 2. No route-level protection
- Customer-only routes (`/cart`, `/checkout`, `/orders`, etc.) had no `ProtectedRoute` wrapper. Any visitor could navigate to them.

### 3. No auth gate on Add to Cart
- `ProductActions` called `cartApi.addItem()` directly on click without checking auth state. Unauthenticated clicks fired API calls that failed with 401.

### 4. No Customer preset on LoginPage
- The LoginPage had presets for admin, grower, trainee, and dual — but not customer. Customers had to type credentials manually.

## Changes

### Frontend

| File | Change |
|------|--------|
| `App.tsx` | Wrapped `/cart`, `/checkout`, `/checkout/confirmation`, `/orders`, `/orders/:orderReference`, `/orders/:orderReference/return-request` with `<ProtectedRoute><RequireRole roles={['ROLE_CUSTOMER']}>` |
| `CartPage.tsx` | Removed self-gating `isAuthenticated()` from cartUtils; removed inline "Sign in required" section; removed unused `Link` import |
| `CheckoutPage.tsx` | Same pattern — removed self-gating; removed inline auth warning |
| `OrdersPage.tsx` | Replaced `isAuthenticated` from cartUtils with `useAuth()` from AuthContext; removed inline auth block |
| `OrderConfirmationPage.tsx` | Removed self-gating `isAuthenticated()` block |
| `ProductActions.tsx` | Added `useAuth` + `useNavigate`; checks `isAuthenticated` before `handleAddToCart`, redirects to `/login` with return URL if unauthenticated |
| `LoginPage.tsx` | Added Customer preset button (green, between Admin and Grower in the 2-column grid) |

### Backend

No backend changes required. The security chain is correct:
- Spring Security requires `.authenticated()` for all non-catalog endpoints
- JWT filter extracts roles into `Authentication` object
- Controllers use `Principal`/`Authentication` for identity resolution
- IDOR protection via scoped queries (e.g., `findByCustomerId`)

### Tests

| File | Change |
|------|--------|
| `ProductDetailPage.test.tsx` | Added `AuthProvider` wrapper + localStorage seeding |
| `ProductPurchaseIntegration.test.tsx` | Added `AuthProvider` wrapper |
| `CatalogEndToEnd.test.tsx` | Added `AuthProvider` wrapper |
| `ProductActions.test.tsx` | Added `AuthProvider` + `MemoryRouter` wrappers |
| `ProductDetailDiscoveryComponents.test.tsx` | Added `AuthProvider` + `MemoryRouter` wrappers |
| `CheckoutPage.test.tsx` | Added `AuthProvider` wrapper; updated unauthenticated test |
| `OrderConfirmationPage.test.tsx` | Updated to test API failure instead of removed self-gating |
| `OrdersPage.test.tsx` | Added `AuthProvider` wrapper + localStorage seeding |
| `CartPage.test.tsx` | Added `AuthProvider` wrapper + localStorage seeding; updated unauthenticated test |
| `CartIntegration.test.tsx` | Added `AuthProvider` wrapper + localStorage seeding |

### Backend Tests

| File | Description |
|------|-------------|
| `CustomerAuthorizationAcceptanceTest.java` | 16 tests verifying: unauthenticated→401, wrong role→403, valid customer→200, IDOR→404, public catalog→200 |

## Verification

| Check | Result |
|-------|--------|
| TypeScript (`npx tsc --noEmit`) | Clean |
| ESLint (`npm run lint`) | Clean (0 warnings) |
| Frontend tests (`npx vitest run`) | 463/463 pass (75 test files) |
| Backend auth tests (`mvn test -Dtest=CustomerAuthorizationAcceptanceTest`) | 16/16 pass |

## Security Properties Verified

1. **Unauthenticated users** → 401 on all cart/checkout/payment/order endpoints
2. **Wrong role** → 403 on role-restricted endpoints (admin endpoints)
3. **Valid customer** → 200 on their own cart and orders
4. **IDOR** → 404 when accessing another customer's order
5. **Public catalog** → 200 without authentication
6. **No security weakening** — all existing protections remain intact
