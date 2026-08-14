# ADR 0002: Cart Domain Architecture, Price Snapshotting & Checkout Handoff

## Status
APPROVED

## Context
Sporekart v3.0 requires a persistent shopping cart domain (Sprint 3A) that allows authenticated customers to manage items, quantities, and price snapshots while enforcing strict modular boundaries with the Catalog domain and preparing a clean input contract for Sprint 3B (Pricing & Checkout).

## Decisions
1. **Catalog Domain Isolation**:
   - The Cart module depends on Catalog via `CatalogPort` (`CatalogAdapter`). Cart never directly imports or queries Catalog `ProductEntity` repositories.
2. **Authenticated Customer Ownership & Identity**:
   - Cart ownership is enforced server-side via Spring Security principal identity (`Principal.getName()`). Request body payload customer IDs are strictly ignored.
3. **Database Uniqueness & Concurrency**:
   - Unique constraint `uq_active_customer_cart` on `(customer_id, status)` prevents race conditions from creating duplicate active carts.
   - Optimistic locking `@Version` field on `CartEntity` prevents concurrent update conflicts.
4. **Monetary Precision & Snapshots**:
   - All prices, line totals, and subtotals use `BigDecimal`. Unit prices are snapshot at the time of cart item addition for display stability, while Sprint 3B performs authoritative order price revalidation.

## Consequences
- **Positive**: Modular monolith boundaries preserved; zero JPA leakage; complete concurrency and cross-customer security isolation.
- **Negative**: Minor database overhead for snapshotting line item attributes.
