# Sporekart v3.0 — Catalog Domain Handoff Guide

## Purpose
This document provides a concise engineering handoff for developers and teams building subsequent Sporekart domains (starting with **Sprint 3 — Cart & Customer Experience**).

---

## 1. Catalog Responsibilities & Scope
The Catalog domain strictly owns:
- Product discovery & taxonomy listing.
- Keyword search, price bounds (`minPrice`, `maxPrice`), status, and category filtering.
- Whitelisted product & category sorting.
- Product detail presentation.

Catalog DOES NOT own:
- Cart management
- Wishlists
- Orders or Checkout
- Payments or Razorpay
- Shipping or Shiprocket
- Reviews or Customer Accounts

---

## 2. Stable Public APIs & Endpoints
| Endpoint | Method | Description |
|---|---|---|
| `/api/v1/catalog/products` | `GET` | Paginated product search & filtering |
| `/api/v1/catalog/products/{productId}` | `GET` | Single product details lookup |
| `/api/v1/catalog/categories` | `GET` | Paginated category listing |
| `/api/v1/catalog/categories/{categoryId}` | `GET` | Single category details lookup |

---

## 3. Rules of Engagement for Sprint 3 (Cart)

> [!IMPORTANT]
> Follow these strict architectural boundaries when integrating Cart with Catalog.

1. **Product Referencing**:
   Cart items MUST store products by `product_id` (UUID).
2. **Entity Isolation**:
   Cart services MUST NOT import `ProductEntity` or execute SQL joins against `products` table directly.
3. **Price Snapshotting**:
   Cart items MUST snapshot product prices at the time of addition/checkout to prevent unexpected price changes during order fulfillment.
4. **UI Integration**:
   Add-to-Cart buttons can be integrated directly into `ProductCard.tsx` and `ProductDetailPage.tsx` by consuming a Cart Context or State Store.

---

## 4. Verification & Testing Commands
- **Backend Tests**: `mvn clean test` (50 tests passing)
- **Frontend Tests**: `npm run test` (451 tests passing)
- **Frontend Build**: `npm run build` (`tsc && vite build`)
