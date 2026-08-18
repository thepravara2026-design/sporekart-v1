# SPOREKART v3.0 — Frontend Feature Architecture Guide

## 1. Overview

SPOREKART v3.0 enforces a strict Feature-Driven Domain Architecture (`src/features/*`). Each domain feature acts as a self-contained module containing its API bindings, domain TypeScript definitions, custom hooks, reusable feature components, pages, constants, utilities, and tests.

```text
Application Shell (App.tsx / Router)
       │
       ▼
Feature Route Pages (src/features/{feature}/pages/*)
       │
       ▼
Feature Components (src/features/{feature}/components/*)
       │
       ├─────────────────────────────────┐
       ▼                                 ▼
Feature Hooks (hooks/*)        Shared UI System (src/components/ui/*)
       │                                 │
       ▼                                 ▼
Domain API Service (api/*)     Design System Tokens (src/design-system/*)
       │
       ▼
Shared API Client (src/services/apiClient.ts)
       │
       ▼
Backend Contract Envelopes (Java 21 / Spring Boot REST APIs)
```

---

## 2. Directory Architecture & Layer Rules

Each feature module under `src/features/{feature_name}/` must strictly adhere to the following layout:

```text
src/features/catalog/
├── api/                   # Domain API client methods & endpoints wrapper
│   └── catalogApi.ts
├── types/                 # Domain interfaces, enums, & query parameter models
│   └── catalog.ts
├── constants/             # Feature constants (default filters, sort options)
│   └── catalogConstants.ts
├── utils/                 # Pure domain utility functions (formatting, badge mapping)
│   └── catalogUtils.ts
├── hooks/                 # TanStack Query & local filter state hooks
│   ├── useProducts.ts
│   ├── useProduct.ts
│   ├── useCategories.ts
│   └── useCatalogFilters.ts
├── components/            # Domain-specific UI components
│   ├── ProductCard.tsx
│   ├── ProductGrid.tsx
│   ├── ProductList.tsx
│   ├── ProductImage.tsx
│   ├── ProductPrice.tsx
│   ├── ProductAvailability.tsx
│   ├── ProductCardSkeleton.tsx
│   ├── CategoryCard.tsx
│   ├── CategoryGrid.tsx
│   ├── CatalogFilters.tsx
│   ├── CatalogFilterBar.tsx
│   ├── CatalogSearch.tsx
│   ├── CatalogSort.tsx
│   ├── CatalogToolbar.tsx
│   ├── CatalogPagination.tsx
│   ├── CatalogEmptyState.tsx
│   ├── CatalogErrorState.tsx
│   └── __tests__/
│       ├── CatalogFilterBar.test.tsx
│       ├── ProductCard.test.tsx
│       └── CatalogFeatureComponents.test.tsx
├── pages/                 # Full feature route views
│   ├── ProductListPage.tsx
│   ├── ProductDetailPage.tsx
│   ├── CategoryListPage.tsx
│   └── __tests__/
│       ├── ProductListPage.test.tsx
│       └── ProductDetailPage.test.tsx
└── index.ts               # Public feature module export index
```

### Checkout Feature Layout (FD-12)

The checkout feature follows the same layered layout as catalog, with an extra constraint: **the feature never invents backend data**. It mirrors backend endpoints 1:1 (`/checkout/preview`, `/orders`, `/inventory/reserve`, `/payments`, `/auth/me`).

```text
src/features/checkout/
├── constants/checkoutConstants.ts      # Step order, payment methods, backend error codes
├── types/checkout.ts                    # ShippingOption, CheckoutRevalidation
├── utils/checkoutUtils.ts               # Safe error mapping, warning classification, preview-change detection
├── hooks/                               # useCheckoutPreview, useCheckoutValidation, useCustomerProfile,
│                                        # useShippingOptions, usePlaceOrder, useOrder, usePayment
├── components/                          # AddressForm, OrderReview, CheckoutSummary, CheckoutSubmit,
│                                        # CheckoutValidationAlert, CheckoutPaymentForm, ... (+ __tests__)
├── pages/                               # CheckoutPage, OrderConfirmationPage (+ __tests__)
└── index.ts                             # Public feature module export index
```

The order pipeline (create order → reserve inventory → initiate payment → verify payment) lives in `hooks/usePlaceOrder.ts`, keeping business orchestration out of the page and components. See [`checkout-architecture.md`](./checkout-architecture.md) for the full contract-driven design.

---

## 3. Strict Boundary Rules1. **Domain Isolation**: Feature-specific components (`ProductCard.tsx`) MUST reside in `src/features/{feature}/components/`. They must NEVER be placed in `src/components/ui/`.
2. **Generic UI Primitives**: `src/components/ui/` primitives (`Button`, `Card`, `Badge`, `Select`, `Input`) MUST remain domain-neutral. They must never import from `src/features/`.
3. **API Contracts**: Feature API calls MUST consume centralized envelopes (`ApiResponse<T>`, `PageResponse<T>`, `ApiError`) aligned with FD-02 backend specifications.
4. **Public Barrel Index**: Cross-feature imports should consume from `src/features/{feature}/index.ts`.
