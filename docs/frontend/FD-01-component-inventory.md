# FD-01 — Component Inventory & Refactoring Matrix

**Sprint:** FD-01  
**Repository:** `f:/sporekart-v3.0/frontend`  
**Date:** 2026-08-18  
**Status:** Completed  

---

## 1. Overview

This document presents the complete component inventory of the SPOREKART v3.0 React application. Every existing UI component, layout container, error boundary, feature page, and utility component has been cataloged, classified, and mapped to a recommended action:

- **KEEP:** Production-ready implementation; minor or no changes required.
- **REFACTOR:** Functional baseline present; restructuring, prop standardization, or styling refactor needed.
- **MERGE:** Functional duplication detected; should be consolidated into a single reusable component.
- **REPLACE:** To be superseded by the new Design System UI library (FD-05 / FD-06).
- **DEPRECATE:** Outdated or unused legacy artifact.
- **NEW:** Component required by target architecture but currently absent.

---

## 2. Shared & Layout Components Inventory

| Component Name | File Path | Category | Purpose & Description | Recommended Action | Target Sprint |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **ErrorBoundary** | [`ErrorBoundary.tsx`](file:///f:/sporekart-v3.0/frontend/src/components/ErrorBoundary.tsx) | Utility | Class component catching React render errors with fallback UI | **KEEP / REFACTOR** | FD-04 |
| **MainLayout** | [`MainLayout.tsx`](file:///f:/sporekart-v3.0/frontend/src/layouts/MainLayout.tsx) | Layout | Application shell containing Brand link, top nav, `<Outlet />`, and footer | **REFACTOR** | FD-04 / FD-08 |

---

## 3. Catalog Feature Components Inventory

| Component Name | File Path | Category | Purpose & Description | Recommended Action | Target Sprint |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **ProductCard** | [`ProductCard.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/catalog/components/ProductCard.tsx) | Commerce | Card displaying product image, title, category, price, stock status, and view details CTA | **REFACTOR** | FD-07 / FD-09 |
| **ProductGrid** | [`ProductGrid.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/catalog/components/ProductGrid.tsx) | Commerce | CSS Grid wrapper component for rendering arrays of ProductCards | **REFACTOR** | FD-07 / FD-09 |
| **CatalogFilterBar** | [`CatalogFilterBar.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/catalog/components/CatalogFilterBar.tsx) | Commerce | Search input, category dropdown, price filters, and sort selector connected to URL search params | **REFACTOR** | FD-09 / FD-17 |
| **PaginationControls**| [`PaginationControls.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/catalog/components/PaginationControls.tsx) | Primitive | Page numbers, Previous/Next navigation buttons, and page info display | **REPLACE / MERGE** | FD-05 / FD-17 |

---

## 4. Page Components Inventory

| Page Name | File Path | Category | Route Registered | Purpose & Key Features | Recommended Action | Target Sprint |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **HomePage** | [`HomePage.tsx`](file:///f:/sporekart-v3.0/frontend/src/pages/HomePage.tsx) | Public | `/` | Hero section, system status badge, tech stack overview, quick links | **REFACTOR** | FD-08 |
| **ProductListPage** | [`ProductListPage.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/catalog/pages/ProductListPage.tsx) | Public | `/products` | Filter bar, paginated product grid, loading skeletons, empty state handle | **REFACTOR** | FD-09 |
| **ProductDetailPage** | [`ProductDetailPage.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/catalog/pages/ProductDetailPage.tsx) | Public | `/products/:productId` | Single product details, price display, description, stock status, add to cart button | **REFACTOR** | FD-10 |
| **CategoryListPage** | [`CategoryListPage.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/catalog/pages/CategoryListPage.tsx) | Public | `/categories` | Category card grid with active product counts and description | **REFACTOR** | FD-09 |
| **HealthPage** | [`HealthPage.tsx`](file:///f:/sporekart-v3.0/frontend/src/pages/HealthPage.tsx) | Utility | `/health` | Live backend API ping, version info, service health breakdown grid | **KEEP** | FD-04 |
| **NotFoundPage** | [`NotFoundPage.tsx`](file:///f:/sporekart-v3.0/frontend/src/pages/NotFoundPage.tsx) | Utility | `*` | Clean 404 notice with CTA button back to home | **KEEP** | FD-04 |

---

## 5. Unrouted Standalone Feature Consoles Inventory

| Console / Page Name | File Path | Category | Purpose & Description | Recommended Action | Target Sprint |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **AdminReturnListPage** | [`AdminReturnListPage.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/admin/pages/AdminReturnListPage.tsx) | Admin | Returns list, inspection modal, approve/reject workflow, refund retry | **REFACTOR & ROUTE** | FD-16 / FD-17 |
| **AdminTrainingOperationsConsole** | [`AdminTrainingOperationsConsole.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/admin/pages/AdminTrainingOperationsConsole.tsx) | Admin | Training capacity, demand list, enrollment approval, attendance logging | **REFACTOR & ROUTE** | FD-13 / FD-16 |
| **BatchManagementConsole** | [`BatchManagementConsole.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/admin/pages/BatchManagementConsole.tsx) | Admin | Program batches management, schedule modal, capacity indicator, batch activation | **REFACTOR & ROUTE** | FD-13 / FD-16 |
| **NotificationOperationsConsole** | [`NotificationOperationsConsole.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/admin/pages/NotificationOperationsConsole.tsx) | Admin | Notification templates list, test send modal, dispatch log viewer, outbox event monitor | **REFACTOR & ROUTE** | FD-16 / FD-17 |
| **TrainingProgramManagement** | [`TrainingProgramManagement.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/admin/pages/TrainingProgramManagement.tsx) | Admin | Training course catalog management, create/edit course modal, status toggles | **REFACTOR & ROUTE** | FD-13 / FD-16 |
| **TrainingReportingConsole** | [`TrainingReportingConsole.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/admin/pages/TrainingReportingConsole.tsx) | Admin | Metrics summary cards, revenue/completion charts, CSV export button | **REFACTOR & ROUTE** | FD-13 / FD-17 |
| **ReturnRequestPage** | [`ReturnRequestPage.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/returns/pages/ReturnRequestPage.tsx) | Account | Return eligibility checker, item selector, reason description, evidence URLs upload | **REFACTOR & ROUTE** | FD-12 |
| **ReturnDetailPage** | [`ReturnDetailPage.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/returns/pages/ReturnDetailPage.tsx) | Account | Return tracking timeline, status history list, refund details card | **REFACTOR & ROUTE** | FD-12 |
| **TraineeTrainingConsole** | [`TraineeTrainingConsole.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/trainee/pages/TraineeTrainingConsole.tsx) | Training | Trainee dashboard, enrolled batches list, schedule calendar, certificate downloader, demand submission modal | **REFACTOR & ROUTE** | FD-13 / FD-14 |

---

## 6. Missing Target Primitive & Domain Components

The following components are **absent** in the baseline repository and must be created during primitive and domain design system sprints:

- **Primitive UI (FD-05):** `Button`, `Input`, `Select`, `Checkbox`, `RadioGroup`, `Badge`, `Card`, `Avatar`, `Spinner`, `Skeleton`, `Divider`.
- **Overlay & Data UI (FD-06):** `Dialog` (Modal), `Drawer`, `Toast`, `DropdownMenu`, `Tooltip`, `Table`, `Tabs`, `Accordion`.
- **Commerce UI (FD-07 / FD-11):** `PriceTag`, `StockBadge`, `QuantitySelector`, `CartItem`, `CartDrawer`, `CheckoutStepper`.
- **Training UI (FD-13):** `BatchCard`, `SeatProgressIndicator`, `AttendanceBadge`, `CertificateCard`, `TrainingScheduleList`.
- **Grower UI (FD-14):** `GrowerDashboardCard`, `ResourceDownloadList`, `AdvisoryCard`.
- **Seller UI (FD-15):** `SellerMetricsCard`, `InventoryStatusBadge`, `PayoutSummaryTable`.
- **Admin UI (FD-16 / FD-17):** `AdminSidebar`, `AdminHeader`, `DataTable`, `FilterToolbar`, `MetricsOverviewCard`.
