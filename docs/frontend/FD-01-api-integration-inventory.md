# FD-01 — API Integration Inventory & Backend Boundary Audit

**Sprint:** FD-01  
**Repository:** `f:/sporekart-v3.0/frontend`  
**Date:** 2026-08-18  
**Status:** Completed  

---

## 1. Overview

This document provides a comprehensive inventory of all REST API integrations present in the SPOREKART v3.0 frontend repository. It cross-references frontend API clients (`src/services/` and `src/features/*/api/`) with backend Spring Boot `@RestController` entry points discovered in the backend codebase (`backend/src/main/java/com/sporekart/`).

---

## 2. Master Endpoint Registry Audit ([`endpoints.ts`](file:///f:/sporekart-v3.0/frontend/src/services/endpoints.ts))

| Key | Endpoint Template | HTTP Method | Frontend Service | Backend Controller | Auth Policy |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `HEALTH` | `/api/v1/health` | GET | `apiClient.getHealth` | `HealthController` | Public |
| `VERSION` | `/api/v1/version` | GET | `apiClient.getVersion` | `VersionController` | Public |
| `PRODUCTS` | `/api/v1/catalog/products` | GET | `catalogApi.getProducts` | `CatalogProductController` | Public |
| `PRODUCT_BY_ID` | `/api/v1/catalog/products/{id}` | GET | `catalogApi.getProduct` | `CatalogProductController` | Public |
| `CATEGORIES` | `/api/v1/catalog/categories` | GET | `catalogApi.getCategories` | `CatalogCategoryController` | Public |
| `CATEGORY_BY_ID` | `/api/v1/catalog/categories/{id}` | GET | `catalogApi.getCategory` | `CatalogCategoryController` | Public |
| `RETURNS_ELIGIBILITY` | `/api/v1/returns/eligibility/{orderRef}` | GET | `returnApi.checkEligibility` | `ReturnController` | `X-Customer-Id` |
| `RETURNS_CREATE` | `/api/v1/orders/{orderRef}/returns` | POST | `returnApi.createReturn` | `ReturnController` | `X-Customer-Id` |
| `RETURN_BY_REF` | `/api/v1/returns/{returnRef}` | GET | `returnApi.getReturnByReference` | `ReturnController` | Optional Header |
| `RETURNS_CUSTOMER` | `/api/v1/returns` | GET | `returnApi.listCustomerReturns` | `ReturnController` | `X-Customer-Id` |
| `ADMIN_RETURNS` | `/api/v1/admin/returns` | GET | `returnApi.listAdminReturns` | `AdminReturnController` | Admin Role |
| `ADMIN_RETURN_APPROVE` | `/api/v1/admin/returns/{ref}/approve` | POST | `returnApi.approveReturn` | `AdminReturnController` | `X-Admin-Id` |
| `ADMIN_RETURN_REJECT` | `/api/v1/admin/returns/{ref}/reject` | POST | `returnApi.rejectReturn` | `AdminReturnController` | `X-Admin-Id` |
| `ADMIN_RETURN_INSPECT` | `/api/v1/admin/returns/{ref}/inspect` | POST | `returnApi.inspectReturn` | `AdminReturnController` | `X-Admin-Id` |
| `INVENTORY_AVAILABILITY`| `/api/v1/inventory/skus/{sku}/availability` | GET | `inventoryApi.getAvailability` | `CustomerInventoryController` | Public |
| `ADMIN_INVENTORY_LIST` | `/api/v1/admin/inventory` | GET | `inventoryApi.listAdminInventory` | `AdminInventoryController` | Admin Role |
| `ADMIN_INVENTORY_BY_SKU`| `/api/v1/admin/inventory/{sku}` | GET | `inventoryApi.getAdminInventoryBySku` | `AdminInventoryController` | Admin Role |
| `ADMIN_INVENTORY_MOVEMENTS`| `/api/v1/admin/inventory/{sku}/movements` | GET | `inventoryApi.listMovements` | `AdminInventoryController` | Admin Role |
| `ADMIN_INVENTORY_ADJUST`| `/api/v1/admin/inventory/{sku}/adjustments` | POST | `inventoryApi.adjustStock` | `AdminInventoryController` | Admin Role |
| `ADMIN_INVENTORY_DAMAGED`| `/api/v1/admin/inventory/{sku}/damaged` | POST | `inventoryApi.recordDamagedStock` | `AdminInventoryController` | Admin Role |
| `CUSTOMER_SHIPMENT_TRACKING`| `/api/v1/orders/{orderRef}/shipment` | GET | `shippingApi.getCustomerTracking` | `ShipmentController` | `X-Customer-Id` |
| `ADMIN_SHIPMENTS` | `/api/v1/admin/shipments` | GET | `shippingApi.listAdminShipments` | `AdminShipmentController` | Admin Role |
| `ADMIN_SHIPMENT_BY_REF`| `/api/v1/admin/shipments/{ref}` | GET | `shippingApi.getAdminShipmentDetail` | `AdminShipmentController` | Admin Role |
| `ADMIN_SHIPMENT_RETRY` | `/api/v1/admin/shipments/{ref}/retry` | POST | `shippingApi.retryBooking` | `AdminShipmentController` | Admin Role |
| `ADMIN_SHIPMENT_SYNC` | `/api/v1/admin/shipments/{ref}/sync` | POST | `shippingApi.syncShipment` | `AdminShipmentController` | Admin Role |
| `ADMIN_SHIPMENT_CANCEL`| `/api/v1/admin/shipments/{ref}/cancel` | POST | `shippingApi.cancelShipment` | `AdminShipmentController` | Admin Role |
| `ADMIN_SHIPMENT_LABEL` | `/api/v1/admin/shipments/{ref}/label` | GET | `shippingApi.getLabelUrl` | `AdminShipmentController` | Admin Role |
| `ADMIN_SHIPMENT_MANIFEST`| `/api/v1/admin/shipments/{ref}/manifest` | GET | `shippingApi.getManifestUrl` | `AdminShipmentController` | Admin Role |
| `CUSTOMER_TICKETS` | `/api/v1/customer/support/tickets` | GET/POST | `supportApi.list/create` | `CustomerSupportController` | `X-Customer-Id` |
| `CUSTOMER_REVIEWS` | `/api/v1/reviews` | POST | `reviewApi.submitReview` | `CustomerReviewController` | `X-Customer-Id` |
| `PRODUCT_REVIEWS` | `/api/v1/products/{productId}/reviews` | GET | `reviewApi.listProductReviews` | `CustomerReviewController` | Public |
| `PRODUCT_RATING_SUMMARY`| `/api/v1/products/{productId}/rating-summary` | GET | `reviewApi.getRatingSummary` | `CustomerReviewController` | Public |

---

## 3. Training Domain API Client Audit

### 3.1 Training Program Management API ([`trainingProgramApi.ts`](file:///f:/sporekart-v3.0/frontend/src/features/admin/api/trainingProgramApi.ts))
- `GET /api/v1/admin/training-programs` → `AdminTrainingProgramController`
- `POST /api/v1/admin/training-programs` → `AdminTrainingProgramController`
- `PUT /api/v1/admin/training-programs/{id}` → `AdminTrainingProgramController`
- `POST /api/v1/admin/training-programs/{id}/activate` → `AdminTrainingProgramController`
- `POST /api/v1/admin/training-programs/{id}/deactivate` → `AdminTrainingProgramController`

### 3.2 Training Batch Operations API ([`batchApi.ts`](file:///f:/sporekart-v3.0/frontend/src/features/admin/api/batchApi.ts))
- `GET /api/v1/admin/batches` → `AdminBatchController`
- `POST /api/v1/admin/batches` → `AdminBatchController`
- `PUT /api/v1/admin/batches/{id}` → `AdminBatchController`
- `POST /api/v1/admin/batches/{id}/activate` → `AdminBatchController`
- `POST /api/v1/admin/batches/{id}/deactivate` → `AdminBatchController`
- `POST /api/v1/admin/batches/{id}/cancel` → `AdminBatchController`
- `POST /api/v1/admin/batches/{id}/schedules` → `AdminBatchController`
- `DELETE /api/v1/admin/batches/{id}/schedules/{scheduleId}` → `AdminBatchController`
- `GET /api/v1/admin/enrollments` → `AdminEnrollmentController`
- `POST /api/v1/admin/enrollments/{id}/approve` → `AdminEnrollmentController`
- `POST /api/v1/admin/enrollments/{id}/cancel` → `AdminEnrollmentController`
- `GET /api/v1/admin/attendance` → `AdminAttendanceController`
- `POST /api/v1/admin/attendance/log` → `AdminAttendanceController`
- `GET /api/v1/admin/capacity/summary` → `AdminCapacityController`
- `GET /api/v1/admin/training-reports/summary` → `AdminTrainingReportingController`

---

## 4. Backend Boundary Risk & Architecture Findings

1. **Header-Based Auth vs Bearer Tokens:** Frontend services currently rely on manual header injection (`X-Customer-Id`, `X-Admin-Id`) rather than standard HTTP Authorization Bearer tokens.
2. **Duplicated Business Logic:**
   - Return eligibility logic in [`ReturnRequestPage.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/returns/pages/ReturnRequestPage.tsx) checks return window dates locally before calling backend. **Backend authoritative eligibility must override frontend assumptions.**
   - Seat capacity progress calculation in [`BatchManagementConsole.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/admin/pages/BatchManagementConsole.tsx) calculates percentage fill locally instead of relying strictly on backend DTO metrics.
3. **Zod Validation Gap:** Raw Axios data casting occurs without runtime Zod schema verification.
4. **Contract Alignment Requirement (FD-02):** Formal contract alignment in FD-02 will standardize response wrapping, error payload structures, and auth header interceptors across all services.
