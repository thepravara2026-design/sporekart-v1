# FD-01 — Frontend Architecture Audit

**Sprint:** FD-01  
**Repository:** `f:/sporekart-v3.0/frontend`  
**Date:** 2026-08-18  
**Status:** Completed  

---

## 1. Executive Summary

This document presents the technical architecture audit of the SPOREKART v3.0 frontend repository. The audit evaluates the existing codebase, file layout, framework dependencies, routing architecture, state management patterns, and backend service integration boundaries prior to executing **FD-02 (Backend ↔ Frontend Contract Alignment)** through **FD-25 (Final Acceptance)**.

---

## 2. Repository & Technical Stack Verification

### 2.1 Configured Stack vs Intended Stack Baseline

| Technology Layer | Intended Baseline (Roadmap) | Configured State (Repository Verification) | Status / Action Required |
| :--- | :--- | :--- | :--- |
| **Framework** | React 18.x + TypeScript 5.x | `react` v18.3.1, `typescript` v5.7.3 | **VERIFIED** — Up-to-date baseline |
| **Build Tooling** | Vite 5.x | `vite` v5.4.14, `@vitejs/plugin-react` v4.3.4 | **VERIFIED** — Vite configured with dev proxy |
| **Routing** | React Router v6.x | `react-router-dom` v6.28.2 | **VERIFIED** — React Router v6 active |
| **State Management** | TanStack Query v5.x | `@tanstack/react-query` v5.66.0 | **VERIFIED** — Configured in `App.tsx` |
| **HTTP Client** | Axios | `axios` v1.7.9 | **VERIFIED** — Custom `apiClient` instance |
| **Styling Engine** | Tailwind CSS v3.x | Plain CSS (`index.css`) | **MISSING** — Must be introduced in FD-03 |
| **Primitive Component Library** | shadcn/ui / Radix UI | Custom raw HTML/CSS components | **MISSING** — Must be introduced in FD-05 |
| **Iconography** | Lucide Icons | Plain text / CSS shapes | **MISSING** — Must be introduced in FD-03/05 |
| **Form Handling** | React Hook Form | Standard unmanaged HTML form inputs / React `useState` | **MISSING** — Must be introduced in FD-05/06 |
| **Schema Validation** | Zod | Manual type checks / raw interface casting | **MISSING** — Must be introduced in FD-02/05 |
| **Unit / Component Testing**| Vitest + Testing Library | `vitest` v3.0.5, `@testing-library/react` v16.2.0 | **VERIFIED** — 14 test suites, 30 tests passing |

---

## 3. Current Directory Structure & Architectural Pattern

The existing frontend follows a hybrid **Feature + Layer** organization:

```text
frontend/
├── dist/                          # Production build output
├── public/                        # Static assets (favicon, images)
├── src/
│   ├── app/                       # Application root & providers
│   │   └── App.tsx                # Main router & QueryClientProvider setup
│   ├── components/                # Shared layout & global components
│   │   └── ErrorBoundary.tsx      # React error boundary component
│   ├── features/                  # Domain-specific feature modules
│   │   ├── admin/                 # Admin consoles (Batches, Programs, Reports, Notifications, Returns)
│   │   │   ├── api/               # Feature-specific API clients (batchApi.ts, trainingProgramApi.ts)
│   │   │   ├── pages/             # Admin console pages
│   │   │   └── __tests__/         # Unit tests for admin pages
│   │   ├── catalog/               # Product & Category domain
│   │   │   ├── components/        # ProductCard, ProductGrid, CatalogFilterBar, PaginationControls
│   │   │   ├── hooks/             # useCatalog.ts custom hook
│   │   │   ├── pages/             # ProductListPage, ProductDetailPage, CategoryListPage
│   │   │   └── __tests__/         # Unit & End-to-end catalog test suites
│   │   ├── returns/               # Customer & Admin returns domain
│   │   │   └── pages/             # ReturnRequestPage, ReturnDetailPage
│   │   └── trainee/               # Trainee experience domain
│   │       └── pages/             # TraineeTrainingConsole
│   ├── layouts/                   # Shell & page wrappers
│   │   └── MainLayout.tsx         # Global navigation header & container wrapper
│   ├── pages/                     # Top-level standalone routes
│   │   ├── HealthPage.tsx         # Observability & system health page
│   │   ├── HomePage.tsx           # Home landing hero & tech stack summary
│   │   ├── NotFoundPage.tsx       # 404 Error page
│   │   └── __tests__/             # Unit tests for core pages
│   ├── services/                  # Global API service abstractions
│   │   ├── apiClient.ts           # Axios instance configuration & interceptors
│   │   ├── apiError.ts            # Standardized API error wrapper
│   │   ├── catalogApi.ts          # Catalog endpoints
│   │   ├── endpoints.ts           # Master URL constant registry
│   │   ├── inventoryApi.ts        # Inventory management API
│   │   ├── returnApi.ts           # Order returns API
│   │   ├── reviewApi.ts           # Product reviews API
│   │   ├── shippingApi.ts         # Shipping & tracking API
│   │   ├── supportApi.ts          # Support tickets API
│   │   └── __tests__/             # API unit tests
│   ├── test/                      # Vitest setup
│   │   └── setup.ts               # Jest-DOM matchers setup
│   ├── types/                     # TypeScript type definitions
│   │   ├── api.ts                 # Base ApiResponse & page wrappers
│   │   └── catalog.ts             # Product & category domain types
│   ├── index.css                  # Global CSS styles & dark theme CSS variables
│   └── main.tsx                   # React root mount point
├── .eslintrc.cjs                  # ESLint configuration
├── index.html                     # HTML document entry point
├── package.json                   # Project manifest & script declarations
├── tsconfig.json                  # TypeScript compiler settings
└── vite.config.ts                 # Vite build & proxy settings
```

---

## 4. State Management Audit

1. **Server State:** Managed exclusively by **TanStack Query (`@tanstack/react-query`)**.
   - `QueryClient` initialized in [`App.tsx`](file:///f:/sporekart-v3.0/frontend/src/app/App.tsx#L16-L24) with 5-minute stale time for catalog data and 1 retry.
   - Used effectively in [`useCatalog.ts`](file:///f:/sporekart-v3.0/frontend/src/features/catalog/hooks/useCatalog.ts) for product and category fetching.
2. **Local Form & UI State:** Managed via standard React `useState` hooks.
   - No global form library (React Hook Form) currently used.
3. **URL / Route State:** Managed via `react-router-dom` `useSearchParams` in [`CatalogFilterBar.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/catalog/components/CatalogFilterBar.tsx).
4. **Client Auth & User Session State:** **Unimplemented**.
   - Current pages use dummy user IDs or hardcoded headers (`X-Customer-Id: CUST-001`, `X-Admin-Id: ADM-001`) passed directly to API services.
   - Global auth state context or token storage is missing.

---

## 5. Backend Architectural Integration Boundaries

- **Proxy Configuration:** [`vite.config.ts`](file:///f:/sporekart-v3.0/frontend/src/vite.config.ts#L16-L22) routes all `/api/*` traffic to `http://localhost:8080`.
- **API Client Layer:** All services funnel through [`apiClient.ts`](file:///f:/sporekart-v3.0/frontend/src/services/apiClient.ts), which wraps Axios errors into standard `ApiError` instances.
- **Header Policies:** Headers such as `X-Customer-Id` and `X-Admin-Id` are attached ad-hoc inside service method calls rather than via an authentication interceptor.
- **DTO Mappings:** DTOs in `services/*.ts` match backend Spring Boot API responses, but lack formal Zod validation schemas.

---

## 6. Architecture Key Findings & Recommendations

1. **Keep Component-Feature Layout:** The current `src/features/` structure aligns closely with the target architecture and should be expanded rather than replaced.
2. **Standardize API Client Interceptors:** Replace ad-hoc request headers with standard OAuth/JWT auth interceptors in FD-02/FD-18.
3. **Configure Tailwind & UI Primitives:** Introduce Tailwind CSS, Radix primitives, and Lucide icons in FD-03/FD-05 without disturbing existing service logic.
