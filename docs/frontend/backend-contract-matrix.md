# FD-02 — Backend Contract Matrix

**Sprint:** FD-02  
**Repository:** `f:/sporekart-v3.0`  
**Date:** 2026-08-18  
**Status:** Aligned  

---

## 1. Executive Summary

This contract matrix provides a domain-by-domain mapping of backend REST controllers against frontend API service clients. It defines HTTP methods, URL paths, authentication policies, DTO payload structures, query parameters, error responses, and alignment status for all SPOREKART v3.0 modules.

---

## 2. Comprehensive Contract Alignment Matrix

| Domain | Feature Endpoint | HTTP Method | Path | Auth / Role Policy | Frontend Service | Request Payload | Response Data Payload | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **System** | Health Status | GET | `/api/v1/health` | Public | `apiClient.getHealth` | None | `ApiResponse<HealthStatusData>` | **PASS** |
| **System** | Version Info | GET | `/api/v1/version` | Public | `apiClient.getVersion` | None | `ApiResponse<VersionInfoData>` | **PASS** |
| **Auth** | Customer Register | POST | `/api/v1/auth/register` | Public | `authApi.register` | `RegisterRequestDto` | `UserProfileDto` (201 Created) | **ALIGNED** |
| **Auth** | Login | POST | `/api/v1/auth/login` | Public | `authApi.login` | `LoginRequestDto` | `AuthTokenResponseDto` | **ALIGNED** |
| **Auth** | Refresh Token | POST | `/api/v1/auth/refresh` | Public (Refresh Token Body)| `authApi.refreshToken` | `RefreshTokenRequestDto` | `AuthTokenResponseDto` | **ALIGNED** |
| **Auth** | Logout | POST | `/api/v1/auth/logout` | Bearer Token | `authApi.logout` | None | `204 No Content` | **ALIGNED** |
| **Auth** | Logout All | POST | `/api/v1/auth/logout-all` | Bearer Token | `authApi.logoutAll` | None | `204 No Content` | **ALIGNED** |
| **Auth** | Current User Profile | GET | `/api/v1/auth/me` | Bearer Token | `authApi.getCurrentUser` | None | `UserProfileDto` | **ALIGNED** |
| **Auth** | Change Password | POST | `/api/v1/auth/change-password`| Bearer Token | `authApi.changePassword` | `ChangePasswordRequestDto` | `204 No Content` | **ALIGNED** |
| **Auth** | Active Sessions | GET | `/api/v1/auth/sessions` | Bearer Token | `authApi.getActiveSessions` | None | `List<UserSessionDto>` | **ALIGNED** |
| **Catalog**| Product List | GET | `/api/v1/catalog/products` | Public | `catalogApi.getProducts` | Query: `page,size,sort,categoryId,status,search,minPrice,maxPrice` | `ApiResponse<PageResponse<ProductDto>>` | **PASS** |
| **Catalog**| Product Detail | GET | `/api/v1/catalog/products/{id}`| Public | `catalogApi.getProduct` | Path: `productId` (UUID / SKU) | `ApiResponse<ProductDto>` | **PASS** |
| **Catalog**| Category List | GET | `/api/v1/catalog/categories` | Public | `catalogApi.getCategories` | Query: `page,size,sort,status,search` | `ApiResponse<PageResponse<CategoryDto>>` | **PASS** |
| **Cart** | Active Cart | GET | `/api/v1/cart` | Bearer / Principal | `cartApi.getCart` | None | `ApiResponse<CartDto>` | **ALIGNED** |
| **Cart** | Add Cart Item | POST | `/api/v1/cart/items` | Bearer / Principal | `cartApi.addItem` | `AddCartItemCommand` | `ApiResponse<CartDto>` (201 Created) | **ALIGNED** |
| **Cart** | Update Quantity | PATCH | `/api/v1/cart/items/{itemId}` | Bearer / Principal | `cartApi.updateItemQuantity` | `UpdateCartItemCommand` | `ApiResponse<CartDto>` | **ALIGNED** |
| **Cart** | Remove Item | DELETE| `/api/v1/cart/items/{itemId}` | Bearer / Principal | `cartApi.removeItem` | Path: `itemId` | `ApiResponse<CartDto>` | **ALIGNED** |
| **Cart** | Clear Cart | DELETE| `/api/v1/cart/items` | Bearer / Principal | `cartApi.clearCart` | None | `ApiResponse<CartDto>` | **ALIGNED** |
| **Checkout**| Preview Calculation| POST| `/api/v1/checkout/preview` | Bearer / Principal | `cartApi.generateCheckoutPreview` | `CheckoutPreviewRequest` | `ApiResponse<CheckoutPreviewResponse>` | **ALIGNED** |
| **Returns**| Eligibility Check | GET | `/api/v1/returns/eligibility/{orderRef}`| `X-Customer-Id` | `returnApi.checkEligibility` | Path: `orderRef` | `ReturnEligibilityDto` | **ALIGNED** |
| **Returns**| Create Return | POST | `/api/v1/orders/{orderRef}/returns` | `X-Customer-Id` | `returnApi.createReturn` | Payload: `reasonCode,description,items` | `ReturnDto` | **ALIGNED** |
| **Returns**| Admin Return List | GET | `/api/v1/admin/returns` | Admin Role | `returnApi.listAdminReturns` | Query: `status,searchKey,customerId` | `List<ReturnDto>` | **ALIGNED** |
| **Returns**| Approve Return | POST | `/api/v1/admin/returns/{ref}/approve`| Admin / `X-Admin-Id` | `returnApi.approveReturn` | Body: `{ notes }` | `ReturnDto` | **ALIGNED** |
| **Returns**| Inspect Return | POST | `/api/v1/admin/returns/{ref}/inspect`| Admin / `X-Admin-Id` | `returnApi.inspectReturn` | Payload: `{ outcome, notes, inspectorId }` | `ReturnDto` | **ALIGNED** |
| **Inventory**| SKU Availability | GET | `/api/v1/inventory/skus/{sku}/availability` | Public | `inventoryApi.getAvailability` | Path: `sku` | `{ data: StockAvailabilityDto }` | **ALIGNED** |
| **Inventory**| Admin Stock Adjust | POST | `/api/v1/admin/inventory/{sku}/adjustments` | Admin Role | `inventoryApi.adjustStock` | Body: `{ sku, newOnHandQuantity, reason }` | `{ data: InventoryItemDto }` | **ALIGNED** |
| **Shipping**| Track Shipment | GET | `/api/v1/orders/{orderRef}/shipment`| `X-Customer-Id` | `shippingApi.getCustomerTracking` | Path: `orderRef` | `{ data: ShipmentTrackingResponseDto }` | **ALIGNED** |
| **Shipping**| Retry Booking | POST | `/api/v1/admin/shipments/{ref}/retry` | Admin Role | `shippingApi.retryBooking` | Path: `shipmentRef` | `ShipmentDto` | **ALIGNED** |
| **Support**| List Tickets | GET | `/api/v1/customer/support/tickets`| `X-Customer-Id` | `supportApi.listCustomerTickets` | None | `List<CustomerTicketDto>` | **ALIGNED** |
| **Reviews**| Submit Review | POST | `/api/v1/reviews` | `X-Customer-Id` | `reviewApi.submitReview` | `SubmitReviewPayload` | `ProductReviewDto` | **ALIGNED** |
| **Training**| Admin Programs | GET | `/api/v1/admin/training-programs` | Admin Role | `fetchAdminTrainingPrograms` | Query: `search,status,category,page,size` | `ApiResponse<PageResponse<TrainingProgramDto>>` | **ALIGNED** |
| **Training**| Admin Batches | GET | `/api/v1/admin/batches` | Admin Role | `fetchAdminBatches` | Query: `programId,status,deliveryMode,page,size` | `ApiResponse<PageResponse<BatchDto>>` | **ALIGNED** |
