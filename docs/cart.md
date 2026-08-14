# Sporekart v3.0 — Cart Module Specification & Handoff Guide

## Module Overview
The **Cart Module** (`com.sporekart.modules.cart`) manages customer shopping cart aggregates, persistent line items, price snapshots, quantity rules, cross-user isolation, and concurrency protection.

---

## 1. Domain Model Architecture
- **`Cart` (Aggregate Root)**:
  - `id` (`UUID`): Primary key.
  - `customerId` (`String`): Customer owner identity.
  - `status` (`CartStatus`): `ACTIVE`, `CHECKED_OUT`, `ABANDONED`.
  - `currency` (`String`): Currency code (default `USD`).
  - `subtotal` (`BigDecimal`): Calculated sum of line item totals.
  - `itemCount` (`int`): Calculated sum of line item quantities.
  - `version` (`Long`): Optimistic locking version for concurrency safety.
  - `items` (`List<CartItem>`): Collection of cart line items.
- **`CartItem`**:
  - `id` (`UUID`): Line item ID.
  - `cartId` (`UUID`): Associated cart ID.
  - `productId` (`UUID`): Catalog product reference.
  - `variantId` (`UUID`): Optional product variant reference.
  - `sku` (`String`): Product SKU snapshot.
  - `productNameSnapshot` (`String`): Display product name.
  - `variantNameSnapshot` (`String`): Display variant description.
  - `unitPriceSnapshot` (`BigDecimal`): Current unit price snapshot at time of addition.
  - `quantity` (`int`): Quantity (must be `> 0`).
  - `lineTotal` (`BigDecimal`): `unitPriceSnapshot * quantity`.

---

## 2. API Contract Specification
Base Endpoint: `/api/v1/cart`

| Endpoint | Method | Request Body | Response Payload | Description |
|---|---|---|---|---|
| `/api/v1/cart` | `GET` | None | `ApiResponse<CartDto>` | Retrieves or creates active cart for authenticated customer. |
| `/api/v1/cart/items` | `POST` | `AddCartItemCommand` | `ApiResponse<CartDto>` | Adds catalog product to active cart (merges quantity if duplicate). |
| `/api/v1/cart/items/{itemId}` | `PATCH` | `UpdateCartItemCommand` | `ApiResponse<CartDto>` | Updates quantity for line item. |
| `/api/v1/cart/items/{itemId}` | `DELETE` | None | `ApiResponse<CartDto>` | Removes specific line item from active cart. |
| `/api/v1/cart/items` | `DELETE` | None | `ApiResponse<CartDto>` | Clears all line items from active cart. |

---

## 3. Database Schema (`V4__cart_domain.sql`)
- **`carts` Table**:
  - `id UUID PRIMARY KEY`
  - `customer_id VARCHAR(100) NOT NULL`
  - `status VARCHAR(20) NOT NULL`
  - `currency VARCHAR(3) NOT NULL`
  - `subtotal DECIMAL(12, 2) NOT NULL`
  - `item_count INT NOT NULL`
  - `version BIGINT NOT NULL DEFAULT 0`
  - `created_at TIMESTAMP WITH TIME ZONE NOT NULL`
  - `updated_at TIMESTAMP WITH TIME ZONE NOT NULL`
  - `CONSTRAINT uq_active_customer_cart UNIQUE (customer_id, status)`
- **`cart_items` Table**:
  - `id UUID PRIMARY KEY`
  - `cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE`
  - `product_id UUID NOT NULL`
  - `variant_id UUID`
  - `sku VARCHAR(50) NOT NULL`
  - `product_name_snapshot VARCHAR(200) NOT NULL`
  - `variant_name_snapshot VARCHAR(100)`
  - `unit_price_snapshot DECIMAL(12, 2) NOT NULL`
  - `quantity INT NOT NULL CHECK (quantity > 0)`
  - `line_total DECIMAL(12, 2) NOT NULL`
  - `created_at TIMESTAMP WITH TIME ZONE NOT NULL`
  - `updated_at TIMESTAMP WITH TIME ZONE NOT NULL`

---

## 4. Cross-Module Handoff Contract for Sprint 3B (Checkout)

> [!IMPORTANT]
> Sprint 3B (Pricing & Checkout) MUST consume Cart data exclusively through the `CartApplicationService` or `CartDto` response model.

Rules for Checkout:
1. **Cart Retrieval**: Checkout queries `CartApplicationService.getOrCreateActiveCart(customerId)` to obtain active cart state.
2. **Item Revalidation**: Checkout is responsible for re-validating catalog stock and final authoritative order pricing before order placement.
3. **Cart Status Lifecycle**: Upon successful checkout, Checkout transitions cart status from `ACTIVE` to `CHECKED_OUT` via `cart.markAsCheckedOut()`.
4. **No Direct Entity Access**: Checkout MUST NOT directly import `CartEntity` or execute SQL joins against `carts` or `cart_items` tables.
