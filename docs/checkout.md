# Sporekart v3.0 — Checkout Module & Pricing Engine Documentation

## 1. Domain Overview

The **Checkout Module** (`com.sporekart.modules.checkout`) provides a server-authoritative, deterministic calculation and validation engine for Sporekart v3.0. It accepts an authenticated customer's active shopping cart, resolves current live Catalog prices, calculates line subtotals, taxes, shipping estimates, and grand totals, and returns a comprehensive `CheckoutPreview`.

> [!IMPORTANT]
> **Server Price Authority**: Monetary calculations never trust frontend inputs. The backend re-validates item prices against the Catalog. If the live Catalog price differs from the Cart price snapshot, the calculation uses the live Catalog price and emits a `PRICE_CHANGED` warning.

> [!NOTE]
> **Order & Payment Isolation**: Checkout preview is 100% stateless and read-oriented. Calling `POST /api/v1/checkout/preview` does NOT create an Order entity in the database, reserve inventory, process payments, or generate shipments.

---

## 2. Architecture & 7-Stage Pricing Pipeline

```text
Authenticated Customer Request (POST /api/v1/checkout/preview)
                         │
                         ▼
             CheckoutController
                         │
                         ▼
         CheckoutApplicationService
                         │
                         ▼
            CheckoutPricingService
                         │
 ┌───────────────────────┴───────────────────────┐
 │ 1. Validate Cart (ACTIVE, Non-empty)          │
 │ 2. Resolve Catalog Prices & Detect Changes   │
 │ 3. Calculate Line Subtotals (Price * Qty)     │
 │ 4. Calculate Line & Order Discounts           │
 │ 5. Calculate Taxes (18% GST)                  │
 │ 6. Estimate Shipping (Free if >= ₹1000)      │
 │ 7. Compute Grand Total & Build Preview        │
 └───────────────────────┬───────────────────────┘
                         │
                         ▼
             CheckoutPreviewResponse
```

---

## 3. Core Domain Models

### `Money` Value Object
- Encapsulates `BigDecimal` value and `String` currency code.
- Enforces 2 decimal places with `RoundingMode.HALF_UP`.
- Prevents cross-currency arithmetic errors (`CurrencyMismatchException`).

### `CheckoutLineItem`
- Represents a single calculated line item.
- Exposes `cartUnitPrice` (snapshot), `authoritativeUnitPrice` (live catalog), `priceChanged` (boolean flag), `lineSubtotal`, `discountAmount`, `taxAmount`, and `lineTotal`.

### `CheckoutPreview`
- Point-in-time calculation aggregate.
- Exposes `previewId`, `cartId`, `customerId`, `currency`, `items`, `subtotal`, `discountTotal`, `taxTotal`, `shippingFee`, `grandTotal`, `warnings`, and `generatedAt`.

---

## 4. Port / Adapter Extensibility

- **`TaxCalculatorPort`** → Implemented by `ConfigurableTaxAdapter` (18% GST rate).
- **`ShippingRateProviderPort`** → Implemented by `FlatRateShippingAdapter` (₹50 flat rate; free for subtotals >= ₹1000).
- **`DiscountCalculatorPort`** → Implemented by `NoOpDiscountAdapter` (returns ₹0 by default; extensible for Sprint 4+ promotion engine).
- **`AvailabilityPort`** → Implemented by `CatalogAvailabilityAdapter` (interfaces `CatalogPort`).

---

## 5. REST API Specification

### `POST /api/v1/checkout/preview`

**Request Headers**:
- `Authorization`: Bearer token or authenticated session context.

**Request Body** (Optional):
```json
{
  "destinationAddress": "Mumbai, Maharashtra 400001",
  "couponCode": "WELCOME10"
}
```

**Response Body (HTTP 200 OK)**:
```json
{
  "success": true,
  "data": {
    "previewId": "8f2a9394-11b2-4d2c-a010-ef88231215b2",
    "cartId": "7383c172-968a-4ff5-89c3-e01d69ef5534",
    "customerId": "customer-1",
    "currency": "INR",
    "items": [
      {
        "cartItemId": "b59a4c10-21a4-4a21-8120-e19284bc7102",
        "productId": "3a009412-10fa-4001-9214-bb20109124a1",
        "sku": "SHROOM-001",
        "productName": "Oyster Spore Kit",
        "quantity": 2,
        "cartUnitPrice": 500.00,
        "authoritativeUnitPrice": 550.00,
        "priceChanged": true,
        "lineSubtotal": 1100.00,
        "discountAmount": 0.00,
        "taxAmount": 198.00,
        "lineTotal": 1298.00
      }
    ],
    "breakdown": {
      "subtotal": 1100.00,
      "discountTotal": 0.00,
      "taxTotal": 198.00,
      "shippingFee": 0.00,
      "grandTotal": 1298.00,
      "currency": "INR"
    },
    "warnings": [
      {
        "type": "PRICE_CHANGED",
        "productId": "3a009412-10fa-4001-9214-bb20109124a1",
        "message": "Price for 'Oyster Spore Kit' changed from INR 500.00 to INR 550.00"
      }
    ],
    "generatedAt": "2026-08-15T01:25:33Z"
  },
  "error": null,
  "meta": {
    "timestamp": "2026-08-15T01:25:33Z"
  }
}
```

---

## 6. Handoff Protocol for Sprint 3C (Order Management)

Sprint 3C must consume `CheckoutPricingService` directly to calculate the authoritative pricing snapshot when creating an `Order`:
- **Do NOT duplicate pricing logic in Order module.**
- Order Creation Service invokes `checkoutPricingService.calculateCheckoutPreview(cart, address, couponCode)` right before generating an `Order` aggregate.
- The resulting `CheckoutPreview` provides the exact prices, line totals, taxes, shipping, and grand total to persist in `orders` and `order_items` tables.
