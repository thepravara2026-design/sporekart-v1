# SPOREKART v3.0 — Checkout & Order Review Architecture (FD-12)

**Sprint:** FD-12 — Checkout & Order Review Experience  
**Status:** Production / Backend-Contract Aligned  
**Location:** `frontend/src/features/checkout/`

---

## 1. Overview

The checkout feature is an authenticated, backend-authoritative multi-step flow that carries a customer from delivery details through order review to payment. Every price, total, stock condition, and payment decision originates from the backend; the frontend orchestrates the required backend pipeline (create order → reserve inventory → initiate payment → verify payment) and renders only server-computed values.

### Flow Shape

```text
Step 0  Customer & Delivery    AddressForm → POST /api/v1/checkout/preview (on submit)
Step 1  Review & Confirm       Products, address, delivery method, backend totals + warnings
Step 2  Payment                Payment method + Place Order → revalidate preview → pipeline
```

The step order is defined once in `constants/checkoutConstants.ts` (`CHECKOUT_STEP_LABELS`, `CheckoutStepIndex`) and rendered by `CheckoutStepper`.

---

## 2. Backend Contract Constraints (from the FD-12 audit)

The implementation is deliberately limited to what the backend exposes. The following are **not fabricated client-side**:

| Surface | Backend truth |
|---|---|
| Pricing / totals / currency | `POST /api/v1/checkout/preview` returns `breakdown` (`subtotal`, `discountTotal`, `taxTotal`, `shippingFee`, `grandTotal`) and `warnings` (`PRICE_CHANGED`, `ITEM_UNAVAILABLE`, `STOCK_LIMITED`, `GENERAL`) |
| Inventory | `POST /api/v1/inventory/reserve/{orderId}` (≈15 min TTL) is **required before payment initiation**; `POST /api/v1/inventory/reservations/{id}/release?reason=` releases stock |
| Orders | `POST /api/v1/orders` (`CreateOrderCommand`), `GET /api/v1/orders/{ref}` |
| Payment | `POST /api/v1/payments` (`{ orderId }`) and `POST /api/v1/payments/verify`; verification is backend-authoritative |
| Customer identity | `GET /api/v1/auth/me` (`UserProfileDto`) — no editable phone/address; prefill only |
| Shipping options | No selection endpoint exists → exactly **one** backend-computed "Standard Delivery" option derived from the preview fee. No delivery dates are shown |
| Saved addresses / address book | No backend address-book API → a single inline `AddressForm` per order. No select/create/edit/delete UI |

The mock payment provider completes instantly; the frontend supplies stable mock provider values (`providerPaymentId: pay_<providerOrderId>`, `providerSignature: 'mock_provider_signature'`) before calling the backend verification endpoint. No secret is ever handled client-side.

---

## 3. Directory Layout

```text
src/features/checkout/
├── constants/checkoutConstants.ts      # Steps, payment methods, backend error codes
├── types/checkout.ts                    # ShippingOption, CheckoutRevalidation
├── utils/checkoutUtils.ts               # Safe error messages, warning mapping, preview-change detection
├── hooks/
│   ├── useCheckoutPreview.ts            # POST /checkout/preview (single mutation)
│   ├── useCheckoutValidation.ts         # Revalidate before submit; detects total/availability drift
│   ├── useCustomerProfile.ts            # GET /auth/me (prefill)
│   ├── useShippingOptions.ts            # One backend-derived delivery option
│   ├── useOrder.ts / usePayment.ts      # Order & payment queries (order confirmation)
│   └── usePlaceOrder.ts                 # Order → reserve → initiate → verify pipeline
├── components/
│   ├── CheckoutStepper.tsx              # Accessible 3-step progress indicator
│   ├── CheckoutPageHeader.tsx           # Customer greeting / intro
│   ├── CustomerInformation.tsx          # Read-only identity (prefill source)
│   ├── AddressForm.tsx / AddressSection.tsx / CheckoutShippingForm.tsx
│   │                                    # Inline delivery address entry
│   ├── AddressCard.tsx                  # Read-only address on review step
│   ├── ShippingSection.tsx              # Backend-computed delivery method + fee
│   ├── CheckoutItem.tsx / CheckoutSummary.tsx
│   │                                    # Server-calculated lines and totals
│   ├── OrderReview.tsx                  # Review & Confirm step
│   ├── CheckoutValidationAlert.tsx      # Warnings + revalidation notice
│   ├── CheckoutSubmit.tsx               # Payment method + Place Order & Pay CTA
│   ├── CheckoutPaymentForm.tsx          # Payment step composition
│   ├── CheckoutErrorState.tsx / CheckoutSkeleton.tsx
│   └── __tests__/
├── pages/CheckoutPage.tsx               # Step orchestration + guards
├── pages/OrderConfirmationPage.tsx      # Post-payment confirmation
└── index.ts                             # Feature barrel export
```

---

## 4. Page Orchestration (`CheckoutPage.tsx`)

### Guarded states
1. **Unauthenticated** → sign-in alert + Continue Shopping link (the frontend has no login route; 401 handling is message-based).
2. **Cart loading** → `CheckoutSkeleton`.
3. **Cart error** → `CheckoutErrorState` (retry / return to cart).
4. **Empty cart** → `CartEmptyState`.
5. **Populated cart** → the 3-step flow.

### Preview fetch
The server preview is fetched once per distinct submitted address (guarded by a `requestedAddress` ref so re-renders never refire the mutation — TanStack v5 returns a new mutation object per render).

### Duplicate-submission protection
- The Place Order CTA disables while `placeOrder.isPending` and the handler no-ops if pending.
- `usePlaceOrder` holds a session-scoped idempotency key (`??=` ref) reused across retries so a mid-pipeline retry replays the same order instead of creating a duplicate.

### Sticky summary
On step 0 the right-hand column shows a sticky `CheckoutSummary` once a preview exists. Steps 1 and 2 render their own summaries inside `OrderReview` / `CheckoutPaymentForm` to avoid duplication.

---

## 5. Price Revalidation & Submission Guard (Phase 13)

Before the final `placeOrder`, the page re-runs the preview via `useCheckoutValidation.revalidate(address, currentPreview)`:

- `hasPreviewChanged` compares the reviewed preview with the refreshed one on **grand total**, **currency**, and **presence of any blocking warning** (advisory `PRICE_CHANGED` warnings are surfaced but do not block).
- If totals changed → submission is blocked and the notice *"The order total has changed. Please review your order."* is shown; the updated totals are already displayed.
- If a blocking warning (`ITEM_UNAVAILABLE`) appeared → submission is blocked with an actionable message.
- Only an unchanged, non-blocking preview proceeds to `placeOrder.mutate`.

---

## 6. Order Pipeline (`usePlaceOrder`)

```text
orderApi.createOrder({ shippingAddress, idempotencyKey, customerNotes? })
  → inventoryApi.reserveInventory(order.id)            // REQUIRED before payment
    → paymentApi.initiatePayment(order.id)
      → verifyPayment({ paymentReference, providerOrderId,
                        providerPaymentId, providerSignature })   // backend-authoritative
  on payment failure → releaseReservation(res.id, 'PAYMENT_FAILED')   // best-effort
on success → success toast + navigate('/checkout/confirmation?orderNumber=...')
```

The `ORDER_NOT_PAYABLE` failure mode is prevented because payment initiation only happens after an active reservation exists.

---

## 7. Error Handling & Warnings

- `getCheckoutErrorMessage` maps backend error codes (`CHECKOUT_CART_EMPTY`, `PAYMENT_VERIFICATION_FAILED`, `ORDER_NOT_PAYABLE`, `401/409`, ...) to safe user-facing messages with a generic fallback.
- `CheckoutValidationAlert` renders backend warnings with appropriate variants (`error` for unavailable items, `warning` for price/stock, `info` otherwise), an actionable hint for blocking conditions, and the revalidation notice.
- `CheckoutErrorState` surfaces a safe message (never stack traces) and the backend `requestId` as a diagnostic reference.
- `ApiError` preserves the backend error envelope (`code`, `message`, `timestamp`, `path`, `requestId`).

---

## 8. Testing

- **Page test** (`pages/__tests__/CheckoutPage.test.tsx`): sign-in guard, empty cart, profile prefill, full 3-step flow (with `inventoryApi` and `authApi` mocked), warning surfacing, and the totals-changed blocking guard.
- **Component tests**: `OrderReview`, `CheckoutSummary`, `CheckoutItem`, `AddressCard`, `CustomerInformation`, `CheckoutValidationAlert`, `CheckoutSubmit`, `CheckoutErrorState`, `CheckoutSkeleton`, `CheckoutPageHeader`, `CheckoutStepper`, `CheckoutShippingForm`, `CheckoutPaymentForm`.
- **Hook test** (`hooks/__tests__/usePlaceOrder.test.tsx`): full pipeline ordering, idempotency-key reuse across retries, and best-effort reservation release on payment failure.
- **Utils test** (`__tests__/checkoutUtils.test.ts`): error mapping, warning classification, `hasPreviewChanged`, `formatAddressSummary`.
- **Regression**: 333 tests across 52 files; `tsc --noEmit`, `vite build`, and `eslint` (FD-12 files) all clean.
