# 3. Server-Authoritative Pricing Engine & Stateless Checkout Preview

* **Status**: Accepted
* **Date**: 2026-08-15
* **Deciders**: Sporekart Architecture Review Board

## Context

In e-commerce platforms, prices fluctuate due to promotions, inventory updates, or catalog management. Frontend clients or cart snapshots may contain stale unit prices. Relying on client-provided monetary subtotals or stale cart snapshots introduces financial risks, price tampering vulnerabilities, and inconsistent totals. Furthermore, checkout calculation must remain decoupled from Order creation to support previewing totals before order placement.

## Decision

We decided to build a server-authoritative, 7-stage pricing engine (`CheckoutPricingService`) in the Checkout module (`com.sporekart.modules.checkout`):

1. **Server Price Authority**: The backend re-queries the Catalog for authoritative item prices during checkout calculation. Cart price snapshots are retained for price change detection (`priceChanged = true`), but live Catalog prices are used for subtotals and grand totals.
2. **Monetary Precision**: All financial calculations use the `Money` value object (`BigDecimal` with 2 decimal places and `RoundingMode.HALF_UP`). Floating-point numbers are strictly forbidden for monetary operations.
3. **Stateless Checkout Preview**: `POST /api/v1/checkout/preview` is stateless and read-oriented. It does NOT create Order entities, reserve inventory, process payments, or generate shipments.
4. **Reusable Domain Service**: `CheckoutPricingService` is designed as a reusable domain service so Sprint 3C (Order Management) can consume the exact same pricing engine without duplicating calculation logic.

## Consequences

* **Positive**:
  - Completely eliminates client-side price tampering.
  - Transparent price-change warnings alert customers if a product price changed while in the cart.
  - Zero duplicate pricing code between Checkout Preview and Order creation.
  - Decoupled ports (`TaxCalculatorPort`, `ShippingRateProviderPort`, `DiscountCalculatorPort`) allow future promotion engines or third-party tax/shipping integrations without modifying the core checkout engine.
* **Negative**:
  - Requires live Catalog resolution for each item in the cart during preview calculation.
