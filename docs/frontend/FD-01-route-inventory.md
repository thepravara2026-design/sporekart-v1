# FD-01 — Route Inventory & Target Mapping

**Sprint:** FD-01  
**Repository:** `f:/sporekart-v3.0/frontend`  
**Date:** 2026-08-18  
**Status:** Completed  

---

## 1. Overview

This document presents the complete route inventory of the SPOREKART v3.0 frontend. It catalogs all currently registered routes in [`App.tsx`](file:///f:/sporekart-v3.0/frontend/src/app/App.tsx), compares them against unrouted standalone feature components, and maps every target route across the 5 authoritative role domains: **Public**, **Buyer**, **Grower**, **Seller**, and **Admin**.

---

## 2. Currently Registered Routes Matrix

| Route Path | Component Name | Layout Wrapper | Auth / Role Requirement | API Dependencies | Current State | Target FD Sprint |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `/` | `HomePage` | `MainLayout` | None (Public) | None | Active / Synchronous | FD-08 |
| `/products` | `ProductListPage` | `MainLayout` | None (Public) | `catalogApi.getProducts`, `catalogApi.getCategories` | Active / Synchronous | FD-09 |
| `/products/:productId` | `ProductDetailPage` | `MainLayout` | None (Public) | `catalogApi.getProduct` | Active / Synchronous | FD-10 |
| `/categories` | `CategoryListPage` | `MainLayout` | None (Public) | `catalogApi.getCategories` | Active / Synchronous | FD-09 |
| `/health` | `HealthPage` | `MainLayout` | None (Public) | `apiClient.getHealth`, `apiClient.getVersion` | Active / Code-split (React.lazy) | FD-04 |
| `*` | `NotFoundPage` | `MainLayout` | None (Public) | None | Active / Code-split (React.lazy) | FD-04 |

---

## 3. Unrouted Feature Components Matrix

The following feature components exist in `src/features/` but are **not currently wired into `App.tsx` routes**:

| Component Name | File Path | Intended Domain | Intended Route Path | Required Role | Required Backend API | Target FD Sprint |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `ReturnRequestPage` | `features/returns/pages/ReturnRequestPage.tsx` | Buyer | `/account/orders/:orderRef/return` | `ROLE_BUYER` | `returnApi.checkEligibility`, `returnApi.createReturn` | FD-12 |
| `ReturnDetailPage` | `features/returns/pages/ReturnDetailPage.tsx` | Buyer | `/account/returns/:returnRef` | `ROLE_BUYER` | `returnApi.getReturnByReference`, `returnApi.cancelReturn` | FD-12 |
| `TraineeTrainingConsole` | `features/trainee/pages/TraineeTrainingConsole.tsx` | Grower / Trainee | `/training/dashboard` | `ROLE_GROWER` / `ROLE_TRAINEE` | `batchApi` (list batches, enrollments, certificates, demand) | FD-13 / FD-14 |
| `AdminReturnListPage` | `features/admin/pages/AdminReturnListPage.tsx` | Admin | `/admin/returns` | `ROLE_ADMIN` | `returnApi.listAdminReturns`, `approveReturn`, `rejectReturn`, `inspectReturn` | FD-16 / FD-17 |
| `AdminTrainingOperationsConsole` | `features/admin/pages/AdminTrainingOperationsConsole.tsx` | Admin | `/admin/training/operations` | `ROLE_ADMIN` | `batchApi` (capacity, demand, attendance, enrollment approval) | FD-13 / FD-16 |
| `BatchManagementConsole` | `features/admin/pages/BatchManagementConsole.tsx` | Admin | `/admin/training/batches` | `ROLE_ADMIN` | `batchApi` (fetchAdminBatches, createBatch, updateBatch, activateBatch) | FD-13 / FD-16 |
| `NotificationOperationsConsole` | `features/admin/pages/NotificationOperationsConsole.tsx` | Admin | `/admin/notifications` | `ROLE_ADMIN` | `batchApi` / `notificationApi` (templates, outbox logs, test send) | FD-16 / FD-17 |
| `TrainingProgramManagement` | `features/admin/pages/TrainingProgramManagement.tsx` | Admin | `/admin/training/programs` | `ROLE_ADMIN` | `trainingProgramApi` (fetchAdminTrainingPrograms, create, update, activate) | FD-13 / FD-16 |
| `TrainingReportingConsole` | `features/admin/pages/TrainingReportingConsole.tsx` | Admin | `/admin/training/reports` | `ROLE_ADMIN` | `batchApi` (fetchTrainingMetrics, revenue, attendance metrics) | FD-13 / FD-17 |

---

## 4. Target Architecture Route Map (FD-01 → FD-25 Target State)

```text
/ (Public Shell)
├── /                                   [HomePage]                     (FD-08)
├── /products                           [ProductListPage]              (FD-09)
├── /products/:productId                [ProductDetailPage]            (FD-10)
├── /categories                         [CategoryListPage]             (FD-09)
├── /search                             [ProductSearchPage]            (FD-09)
├── /training                           [PublicTrainingCatalogPage]    (FD-13)
├── /training/:programSlug              [TrainingDetailPage]           (FD-13)
├── /grower-support                     [GrowerSupportPage]            (FD-14)
├── /about                              [AboutPage]                    (FD-19)
├── /contact                            [ContactPage]                  (FD-19)
├── /faq                                [FAQPage]                      (FD-19)
├── /health                             [HealthPage]                   (FD-04)
│
├── /cart                               [CartPage]                     (FD-11)
├── /checkout                           [CheckoutPage]                 (FD-11)
├── /checkout/confirmation              [OrderConfirmationPage]        (FD-11)
│
├── /account (Buyer Shell - Auth Required)                             (FD-12 / FD-18)
│   ├── /account/profile                [UserProfilePage]              (FD-12)
│   ├── /account/addresses              [UserAddressesPage]            (FD-12)
│   ├── /account/orders                 [UserOrderHistoryPage]         (FD-12)
│   ├── /account/orders/:orderRef       [UserOrderDetailPage]          (FD-12)
│   ├── /account/orders/:orderRef/return[ReturnRequestPage]            (FD-12)
│   └── /account/returns/:returnRef     [ReturnDetailPage]             (FD-12)
│
├── /grower (Grower Shell - Auth Required)                             (FD-14 / FD-18)
│   ├── /grower/dashboard               [GrowerDashboardPage]          (FD-14)
│   ├── /grower/training                [TraineeTrainingConsole]       (FD-13 / FD-14)
│   ├── /grower/resources               [GrowerResourcesPage]          (FD-14)
│   └── /grower/support                 [GrowerSupportTicketPage]      (FD-14)
│
├── /seller (Seller Marketplace Shell - Auth Required)                 (FD-15 / FD-18)
│   ├── /seller/dashboard               [SellerDashboardPage]          (FD-15)
│   ├── /seller/products                [SellerProductManagementPage]  (FD-15)
│   ├── /seller/inventory               [SellerInventoryPage]          (FD-15)
│   └── /seller/orders                  [SellerOrderManagementPage]    (FD-15)
│
└── /admin (Admin Shell - Role Admin Required)                         (FD-16 / FD-18)
    ├── /admin/dashboard                [AdminDashboardPage]           (FD-16)
    ├── /admin/products                 [AdminProductPage]             (FD-16)
    ├── /admin/orders                   [AdminOrderListPage]           (FD-16)
    ├── /admin/returns                  [AdminReturnListPage]          (FD-16 / FD-17)
    ├── /admin/inventory                [AdminInventoryPage]           (FD-17)
    ├── /admin/shipments                [AdminShipmentPage]            (FD-17)
    ├── /admin/training/programs        [TrainingProgramManagement]    (FD-13 / FD-16)
    ├── /admin/training/batches         [BatchManagementConsole]       (FD-13 / FD-16)
    ├── /admin/training/operations      [AdminTrainingOperationsConsole](FD-13 / FD-16)
    ├── /admin/training/reports         [TrainingReportingConsole]     (FD-13 / FD-17)
    └── /admin/notifications            [NotificationOperationsConsole](FD-16 / FD-17)
```
