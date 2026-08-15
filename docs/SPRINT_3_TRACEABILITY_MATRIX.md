# SPOREKART v3.0 — Sprint 3 Requirement Traceability Matrix

| Requirement | Domain Component | API / Module Entry Point | Database Schema / Constraints | Automated Test Suite | Validation Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Cart Operations** | Cart Domain | `/api/v1/cart/items` | `carts`, `cart_items` | `CartControllerTest`, `CartCheckoutIntegrationTest` | **VERIFIED** |
| **Authoritative Pricing** | Catalog / Checkout | `/api/v1/checkout/preview` | `products`, `product_variants` | `CheckoutControllerTest`, `CartCheckoutIntegrationTest` | **VERIFIED** |
| **Order Creation** | Order Domain | `/api/v1/checkout/orders` | `orders`, `order_items` | `OrderControllerTest`, `TransactionIntegrityAndOutboxTest` | **VERIFIED** |
| **Stock Reservation** | Inventory Domain | `InventoryApplicationService` | `inventory_items`, `stock_reservations` | `InventoryReservationIntegrationTest`, `CommerceConcurrencyIntegrationTest` | **VERIFIED** |
| **Payment Orchestration** | Payment Domain | `/api/v1/payments/verify` | `payments`, `payment_attempts`, `payment_webhook_events` | `PaymentOrchestrationIntegrationTest`, `PaymentWebhookSecurityTest` | **VERIFIED** |
| **Order State Machine** | Order Lifecycle | `OrderApplicationService` | `order_status_history` | `OrderStateMachineIntegrationTest` | **VERIFIED** |
| **Fulfilment & Shipping** | Shipping Domain | `/api/v1/admin/shipments` | `shipments`, `shipment_tracking_events` | `ShippingHandoffTest`, `ShippingProviderContractTest` | **VERIFIED** |
| **Returns & Refunds** | Returns Domain | `/api/v1/returns` | `returns`, `refund_records` | `CommerceEndToEndLifecycleTest` | **VERIFIED** |
| **Transaction Rollback** | Infrastructure | `@Transactional` Boundary | Flyway Migrations V1-V11 | `TransactionIntegrityAndOutboxTest` | **VERIFIED** |
| **IDOR Protection** | Security | Filter / Application Service | Customer ID ownership check | `CommerceSecurityAndAuthorizationTest` | **VERIFIED** |
| **Failure Recovery** | Compensation | Webhook Event Handler | Automated reservation release | `CommerceFailureMatrixAndRecoveryTest` | **VERIFIED** |
