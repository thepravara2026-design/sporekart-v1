# FD-01 — Master Frontend Migration Plan (FD-01 → FD-25)

**Sprint:** FD-01  
**Repository:** `f:/sporekart-v3.0/frontend`  
**Date:** 2026-08-18  
**Status:** Completed  

---

## 1. Executive Summary

This master migration plan outlines the exact roadmap for transitioning the SPOREKART v3.0 React/Vite frontend from its current baseline state to the full production architecture defined by the authoritative **SPOREKART Sprint 7A–7X / FD-01–FD-25 Frontend Roadmap**.

Every existing feature, page, component, and API client has been assigned a migration classification: **KEEP**, **REFACTOR**, **MERGE**, **REPLACE**, **DEPRECATE**, or **NEW**.

---

## 2. Component Migration Classification Summary

```text
Existing Codebase Elements
├── Shared & Core Infrastructure
│   ├── ErrorBoundary.tsx                      → KEEP / REFACTOR (FD-04)
│   ├── MainLayout.tsx                         → REFACTOR (FD-04 / FD-08)
│   └── apiClient.ts / endpoints.ts            → REFACTOR (FD-02)
│
├── Public Catalog & Commerce
│   ├── HomePage.tsx                           → REFACTOR (FD-08)
│   ├── ProductListPage.tsx                    → REFACTOR (FD-09)
│   ├── ProductDetailPage.tsx                  → REFACTOR (FD-10)
│   ├── CategoryListPage.tsx                   → REFACTOR (FD-09)
│   ├── ProductCard.tsx                        → REFACTOR (FD-07 / FD-09)
│   ├── ProductGrid.tsx                        → REFACTOR (FD-07 / FD-09)
│   ├── CatalogFilterBar.tsx                   → REFACTOR (FD-09 / FD-17)
│   └── PaginationControls.tsx                 → REPLACE / MERGE (FD-05 / FD-17)
│
├── Unrouted Account & Returns
│   ├── ReturnRequestPage.tsx                  → REFACTOR & ROUTE (FD-12)
│   └── ReturnDetailPage.tsx                   → REFACTOR & ROUTE (FD-12)
│
├── Unrouted Training & Grower Consoles
│   └── TraineeTrainingConsole.tsx             → REFACTOR & ROUTE (FD-13 / FD-14)
│
├── Unrouted Admin Consoles
│   ├── AdminReturnListPage.tsx                → REFACTOR & ROUTE (FD-16 / FD-17)
│   ├── AdminTrainingOperationsConsole.tsx     → REFACTOR & ROUTE (FD-13 / FD-16)
│   ├── BatchManagementConsole.tsx             → REFACTOR & ROUTE (FD-13 / FD-16)
│   ├── NotificationOperationsConsole.tsx      → REFACTOR & ROUTE (FD-16 / FD-17)
│   ├── TrainingProgramManagement.tsx          → REFACTOR & ROUTE (FD-13 / FD-16)
│   └── TrainingReportingConsole.tsx           → REFACTOR & ROUTE (FD-17)
│
└── System Observability
    ├── HealthPage.tsx                         → KEEP (FD-04)
    └── NotFoundPage.tsx                       → KEEP (FD-04)
```

---

## 3. Sequential FD-01 → FD-25 Execution Roadmap Matrix

| Sprint | Title | Primary Objectives & Target Deliverables | Key Dependencies / Input | Output Artifacts / Verification |
| :--- | :--- | :--- | :--- | :--- |
| **FD-01** | Frontend Architecture & Existing UI Audit | Repository audit, component/route/API inventories, baseline test/build/browser verification, gap analysis, migration plan | Clean repository baseline | `docs/frontend/FD-01-*.md`, Final Audit Report |
| **FD-02** | Backend ↔ Frontend Contract Alignment | Align frontend DTO interfaces with Spring Boot REST contracts, standardize Axios interceptors, add Zod schema validation | FD-01 API inventory, Spring Boot controllers | Zod DTO schemas, aligned service API clients |
| **FD-03** | Design Tokens & Visual Foundation | Configure Tailwind CSS, Sporekart Forest color palette, typography tokens, border radii, shadows, and Lucide icons | Sporekart Design System spec | `tailwind.config.js`, design tokens CSS |
| **FD-04** | Core Layout & Accessibility Foundation | Rebuild App shell, header, sidebar, footer, main container, ARIA landmarks, focus indicators, keyboard nav | FD-03 tokens, `MainLayout.tsx` | `AppLayout`, `Header`, `Sidebar`, `Footer` |
| **FD-05** | Primitive UI Component Library | Implement shadcn/Radix primitive components (`Button`, `Input`, `Select`, `Badge`, `Card`, `Checkbox`) | FD-03 tokens, FD-04 layout | `src/components/ui/*` primitives |
| **FD-06** | Overlay, Feedback & Data Components | Implement accessible `Dialog`, `Drawer`, `Toast`, `DropdownMenu`, `Tooltip`, `Table`, `Tabs` | FD-05 primitives, Radix UI | Overlay component library |
| **FD-07** | Commerce Design System | Build specialized commerce primitives (`PriceTag`, `StockBadge`, `ProductCard`, `QuantitySelector`) | FD-05 primitives, `ProductCard.tsx` | Commerce component library |
| **FD-08** | Global Navigation & Marketing Sections | Build responsive global navigation bar, hero banner, category pills, marketing footer | FD-04 layout, `HomePage.tsx` | Redesigned `HomePage` & Nav Header |
| **FD-09** | Product Discovery & Search UX | Implement search bar, category filter sidebar, price range slider, paginated product grid | FD-07 commerce primitives, `ProductListPage.tsx` | Redesigned catalog search & discovery |
| **FD-10** | Product Detail Experience | Single product gallery, variant selector, stock status, reviews list, rating summary, add-to-cart | FD-07 primitives, `ProductDetailPage.tsx` | Redesigned `ProductDetailPage` |
| **FD-11** | Cart & Checkout UX | Slide-over cart drawer, cart page, checkout multi-step flow, payment form, order summary | FD-07 primitives, backend checkout/cart APIs | `CartPage`, `CheckoutPage`, `OrderConfirmation` |
| **FD-12** | Orders & Account Experience | User profile management, address book, order history, order tracking timeline, return request flow | `ReturnRequestPage.tsx`, `ReturnDetailPage.tsx` | Account portal routes (`/account/*`) |
| **FD-13** | Training UI/UX | Public training catalog, program details, batch selector, seat availability bar, trainee dashboard | `TraineeTrainingConsole.tsx`, `batchApi.ts` | Training experience routes (`/training/*`) |
| **FD-14** | Grower Experience | Dedicated grower support portal, resource library, advisory guides, ticket submission | `supportApi.ts`, `TraineeTrainingConsole.tsx` | Grower portal routes (`/grower/*`) |
| **FD-15** | Seller Marketplace UI Foundation | Seller dashboard, product listing manager, inventory sync status, sales metrics cards | Backend seller APIs | Seller portal routes (`/seller/*`) |
| **FD-16** | Admin UI Foundation | Master admin sidebar navigation, dashboard overview cards, role-based navigation guards | Admin consoles, backend security | Admin portal shell (`/admin/*`) |
| **FD-17** | Tables, Filters & Operational UX | Standardize data tables, multi-column sorting, advanced filter toolbars, CSV export across admin | Unrouted admin consoles (`AdminReturnListPage`, etc.) | Operational tables across all admin routes |
| **FD-18** | Permission-Based UI & Security UX | Role-based route guards (`RequireRole`), permission checks, session timeout modal, unauthorized 403 page | Backend Spring Security roles | Auth context, `ProtectedRoute` wrapper |
| **FD-19** | SEO & Content Experience | Meta tags, OpenGraph tags, dynamic page titles, Kannada localized content, FAQ & static pages | Static content, React Helmet / Meta tags | Static pages, SEO metadata helpers |
| **FD-20** | Responsive & Mobile UX Hardening | Mobile bottom navigation, touch target optimization, table horizontal scrolling, mobile drawer nav | All pages & viewports (360px – 1536px+) | Responsive audit PASS |
| **FD-21** | Accessibility Hardening | Automated axe-core accessibility testing, screen reader verification, high-contrast mode, reduced motion | WCAG 2.2 AA guidelines | 0 Accessibility violations report |
| **FD-22** | Frontend ↔ Backend Integration Verification | End-to-end integration testing of all frontend flows against live backend Spring Boot REST services | Live backend service instance | Integration test suite PASS |
| **FD-23** | Visual Regression & UX QA | Playwright / Vitest visual screenshot comparison, cross-browser verification (Chrome, Firefox, Edge, Safari) | Approved Design System visual spec | Visual QA acceptance report |
| **FD-24** | Performance & Frontend Production Hardening | Code-splitting optimization, image lazy loading, TanStack Query cache tuning, bundle size minimization | Production build output | Lighthouse score > 90, bundle analysis |
| **FD-25** | Final Frontend Acceptance | End-to-end acceptance certification across all 25 frontend sprints, sign-off checklist verification | All FD-01–FD-24 deliverables | Final Acceptance Certificate PASS |

---

## 4. FD-02 Hand-off Prerequisites Checklist

Before commencing **FD-02 (Backend ↔ Frontend Contract Alignment)**, verify that:

- [x] Repository audit completed and clean branch `sprint-fd-01-frontend-architecture-audit` created.
- [x] All existing frontend routes, components, and API integrations cataloged in `docs/frontend/`.
- [x] Empirical browser baseline and test/build baseline established and recorded.
- [x] Master migration classification matrix finalized.
- [x] No backend API contracts modified and no unauthorized dependencies installed during audit.
