# SPOREKART v3.0 — Final Production API Inventory

**Date**: 2026-08-15
**Total Endpoints**: 48 REST API Endpoints

---

## 1. Catalog & Public APIs
- `GET /api/v1/health` (Public, 200 OK)
- `GET /api/v1/version` (Public, 200 OK)
- `GET /api/v1/catalog/products` (Public, Paginated)
- `GET /api/v1/catalog/products/{sku}` (Public)
- `GET /api/v1/catalog/categories` (Public)
- `GET /api/v1/products/{productId}/reviews` (Public)
- `GET /api/v1/products/{productId}/rating-summary` (Public)

## 2. Customer Cart & Checkout APIs
- `GET /api/v1/cart` (Authenticated Customer)
- `POST /api/v1/cart/items` (Authenticated Customer)
- `DELETE /api/v1/cart/items/{sku}` (Authenticated Customer)
- `POST /api/v1/checkout/sessions` (Authenticated Customer)

## 3. Order & Payment APIs
- `POST /api/v1/orders` (Authenticated Customer, Idempotent)
- `GET /api/v1/orders/{orderReference}` (Authenticated Customer)
- `GET /api/v1/orders/{orderReference}/timeline` (Authenticated Customer)
- `POST /api/v1/orders/{orderReference}/cancel` (Authenticated Customer)
- `POST /api/v1/payments` (Authenticated Customer)
- `POST /api/v1/payments/verify` (Authenticated Customer)
- `GET /api/v1/payments/{paymentReference}` (Authenticated Customer)
- `POST /api/v1/payments/webhooks/razorpay` (Public, HMAC-SHA256 Signed)

## 4. Shipping & Logistics APIs
- `GET /api/v1/orders/{orderReference}/shipment` (Authenticated Customer)
- `GET /api/v1/orders/{orderReference}/tracking` (Authenticated Customer)
- `POST /api/v1/webhooks/shiprocket` (Public, Token Signed)

## 5. Post-Purchase Support & Returns APIs
- `GET /api/v1/orders/{orderReference}/return-eligibility` (Authenticated Customer)
- `POST /api/v1/orders/{orderReference}/returns` (Authenticated Customer)
- `GET /api/v1/returns/{returnReference}` (Authenticated Customer)
- `GET /api/v1/customer/returns` (Authenticated Customer)
- `POST /api/v1/returns/{returnReference}/cancel` (Authenticated Customer)
- `POST /api/v1/customer/support/tickets` (Authenticated Customer)
- `GET /api/v1/customer/support/tickets` (Authenticated Customer)
- `GET /api/v1/customer/support/tickets/{ticketNumber}` (Authenticated Customer)
- `POST /api/v1/customer/support/tickets/{ticketNumber}/messages` (Authenticated Customer)
- `POST /api/v1/customer/support/tickets/{ticketNumber}/reopen` (Authenticated Customer)
- `POST /api/v1/customer/support/tickets/{ticketNumber}/replacements` (Authenticated Customer)
- `POST /api/v1/reviews` (Authenticated Customer)
- `POST /api/v1/reviews/{reviewReference}/vote` (Authenticated Customer)

## 6. Admin Management APIs (All require `ROLE_ADMIN`)
- `GET /api/v1/admin/orders`
- `POST /api/v1/admin/orders/{orderReference}/cancel`
- `GET /api/v1/admin/payments`
- `POST /api/v1/admin/payments/{paymentRef}/reconcile`
- `GET /api/v1/admin/inventory`
- `POST /api/v1/admin/inventory/skus/{sku}/adjust`
- `GET /api/v1/admin/shipments`
- `POST /api/v1/admin/shipments/{ref}/cancel`
- `GET /api/v1/admin/returns`
- `POST /api/v1/admin/returns/{returnRef}/inspect`
- `POST /api/v1/admin/returns/{returnRef}/refund`
- `GET /api/v1/admin/support/tickets`
- `POST /api/v1/admin/support/tickets/{ticketNumber}/assign`
- `POST /api/v1/admin/support/replacements/{replacementRef}/approve`
- `GET /api/v1/admin/reviews`
- `POST /api/v1/admin/reviews/{reviewRef}/moderate`