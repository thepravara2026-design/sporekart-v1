# SPOREKART v3.0 — FD-FINAL-VERIFICATION-01 Baseline Architecture Audit

**Document ID:** `FD-FINAL-VERIFICATION-BASELINE-01`  
**Date:** August 19, 2026  
**Branch:** `feature/fd-final-verification-01`  
**Commit:** `85bbe6d`  
**Repository State:** Clean  

---

## 1. Structure & Module Architecture Baseline

The SPOREKART v3.0 frontend is structured under `frontend/src/` with clear feature boundaries and strict separation of concerns:

- `frontend/src/app/`: Core application setup, routing tree (`App.tsx`), global error boundary.
- `frontend/src/components/`: Reusable design system primitives (`Button`, `Card`, `Modal`, `Table`, `Badge`, `Toast`), layout shells (`Header`, `Footer`, `MobileBottomNav`), security guards (`ProtectedRoute`, `RequireRole`), and dynamic SEO head (`SEOHead`).
- `frontend/src/context/`: Authentication context provider (`AuthContext`).
- `frontend/src/config/`: Localization dictionaries (`i18n.ts`).
- `frontend/src/features/`: Domain-driven feature modules:
  - `catalog/`: Catalog listing, product detail, category browsing, filters.
  - `cart/`: Shopping cart state management, line items, summary drawer.
  - `checkout/`: Multi-step checkout pipeline, address validation, mock payment gateway integration.
  - `orders/`: Order tracking, shipment status, return request handoff.
  - `grower/`: Grower portal dashboard, product management, inventory controls, shipment management.
  - `seller/`: Seller marketplace dashboard, listing management, order management.
  - `admin/`: Master admin layout shell, returns audit console, training program management, batch scheduling console, operations console, reporting console, notification ops.
  - `trainee/`: Trainee Academy layout shell, training console, enrollment management, demand requests, certificates, notification drawer.
- `frontend/src/pages/`: Page containers (`HomePage`, `LoginPage`, `UnauthorizedPage`, `HealthPage`, `NotFoundPage`, `DesignSystemShowcase`).
- `frontend/src/services/`: REST API clients (`apiClient`, `authApi`, `catalogApi`, `cartApi`, `checkoutApi`, `orderApi`, `returnApi`, `batchApi`).
- `frontend/src/test/`: Global test configuration, accessibility audit helpers, visual regression tests, live E2E REST API integration test suite.

---

## 2. Route & Persona Access Control Architecture

- **Public Routes:** `/`, `/products`, `/products/:productId`, `/categories`, `/cart`, `/checkout`, `/checkout/confirmation`, `/login`, `/unauthorized`, `/health`, `/design-system-showcase`.
- **Protected Master Admin Routes (`ROLE_ADMIN`):** `/admin`, `/admin/dashboard`, `/admin/returns`, `/admin/training`, `/admin/training/batches`, `/admin/training/operations`, `/admin/training/reports`, `/admin/notifications`.
- **Protected Grower Routes (`ROLE_GROWER`, `ROLE_ADMIN`):** `/grower`, `/grower/profile`, `/grower/products`, `/grower/inventory`, `/grower/orders`, `/grower/shipments`, `/grower/reports`, `/grower/settings`.
- **Protected Seller Routes (`ROLE_SELLER`, `ROLE_GROWER`, `ROLE_ADMIN`):** `/seller`, `/seller/dashboard`, `/seller/products`, `/seller/inventory`, `/seller/orders`.
- **Protected Trainee Routes (`ROLE_TRAINEE`, `ROLE_GROWER`, `ROLE_ADMIN`):** `/training`, `/training/console`.
- **Persona Presets:** Developer persona switchers for `admin`, `grower`, `trainee`, `customer`, and `dual` (`ROLE_TRAINEE + ROLE_GROWER`).

---

## 3. Test Infrastructure Baseline

- **Unit & Component Testing:** Vitest + React Testing Library + jsdom environment.
- **Accessibility Automation:** `src/test/accessibilityTestHelper.ts` performing ARIA baseline audits.
- **Visual Regression Suite:** `src/test/visual/visualRegression.test.tsx` checking layout stability.
- **Live E2E REST Integration Suite:** `src/test/e2e/apiIntegration.test.ts` testing HTTP DTO contract envelopes.
