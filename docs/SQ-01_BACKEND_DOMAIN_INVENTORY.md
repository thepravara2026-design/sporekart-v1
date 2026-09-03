# SQ-01 Deliverable A — Backend Domain Inventory

## Overview
This document provides a comprehensive, repository-verified inventory of all logical and physical backend domains within SPOREKART v3.0 (`backend/src/main/java/com/sporekart`).

---

## 1. Domain Summary Table

| Domain ID | Domain Name | Package Path | Key Controllers | Key Services | Key Repositories | Key Entities |
|---|---|---|---|---|---|---|
| DOM-01 | Application / Bootstrap | `com.sporekart` | — | — | — | — |
| DOM-02 | Application Config | `com.sporekart.application.config`, `configuration` | — | `JacksonConfig`, `SecurityConfig`, `WebConfig` | — | — |
| DOM-03 | Exception Handling | `com.sporekart.application.exception` | `GlobalExceptionHandler` | `ErrorResponse` | — | — |
| DOM-04 | Idempotency | `com.sporekart.application.idempotency` | — | `IdempotencyAspect`, `IdempotencyService` | `IdempotentRecordRepository` | `IdempotentRecord` |
| DOM-05 | Observability | `com.sporekart.application.observability` | — | `CustomActuatorHealthIndicator` | — | — |
| DOM-06 | Transactional Outbox | `com.sporekart.application.outbox` | — | `OutboxPublisher`, `OutboxScheduler` | `OutboxEventRepository` | `OutboxEvent` |
| DOM-07 | Resilience | `com.sporekart.application.resilience` | — | `CircuitBreakerAspect`, `RetryAspect` | — | — |
| DOM-08 | Web Core | `com.sporekart.application.web` | — | `CorrelationIdFilter` | — | — |
| DOM-09 | Cart | `com.sporekart.modules.cart` | `CartController` | `CartApplicationService` | `CartRepository` | `Cart`, `CartItem` |
| DOM-10 | Catalog & Product Search | `com.sporekart.modules.catalog` | `CatalogController`, `SearchController` | `CatalogApplicationService` | `ProductRepository`, `CategoryRepository` | `Product`, `Category` |
| DOM-11 | Checkout | `com.sporekart.modules.checkout` | `CheckoutController` | `CheckoutApplicationService` | — | `CheckoutSession` |
| DOM-12 | Customer & Users | `com.sporekart.modules.customer` | `CustomerController` | `CustomerApplicationService` | `CustomerRepository` | `Customer`, `Address` |
| DOM-13 | Inventory | `com.sporekart.modules.inventory` | `InventoryController` | `InventoryApplicationService` | `InventoryRepository` | `InventoryItem`, `StockReservation` |
| DOM-14 | Notifications & Preferences | `com.sporekart.modules.notification` | `NotificationController` | `NotificationApplicationService` | `NotificationRepository` | `Notification`, `NotificationPreference` |
| DOM-15 | Order | `com.sporekart.modules.order` | `OrderController`, `AdminOrderController` | `OrderApplicationService` | `OrderRepository` | `Order`, `OrderItem`, `OrderStatusHistory` |
| DOM-16 | Payment | `com.sporekart.modules.payment` | `PaymentController` | `PaymentApplicationService` | `PaymentRepository` | `Payment`, `PaymentMethod` |
| DOM-17 | Returns & Refunds | `com.sporekart.modules.returns` | `ReturnController` | `ReturnApplicationService` | `ReturnRequestRepository` | `ReturnRequest`, `ReturnItem` |
| DOM-18 | Review & Rating | `com.sporekart.modules.review` | `ReviewController` | `ReviewApplicationService` | `ProductReviewRepository` | `ProductReview` |
| DOM-19 | Security & Identity | `com.sporekart.modules.security` | `AuthController` | `AuthApplicationService`, `JwtTokenProvider` | `UserRepository`, `RoleRepository` | `User`, `Role` |
| DOM-20 | Shipment & Logistics | `com.sporekart.modules.shipment` | `ShipmentController` | `ShipmentApplicationService` | `ShipmentRepository` | `Shipment`, `Carrier` |
| DOM-21 | Customer Support | `com.sporekart.modules.support` | `SupportController` | `SupportApplicationService` | `TicketRepository` | `SupportTicket`, `TicketMessage` |
| DOM-22 | Training Module | `com.sporekart.modules.training` | `TrainingProgramController`, `TrainingBatchController`, `TrainingEnrollmentController`, `TrainingAttendanceController`, `TrainingCertificateController` | `TrainingProgramService`, `TrainingEnrollmentService`, `TrainingBatchService` | `TrainingProgramRepository`, `TrainingEnrollmentRepository`, `TrainingBatchRepository` | `TrainingProgram`, `TrainingBatch`, `TrainingEnrollment`, `TrainingAttendance`, `TrainingCertificate` |

---

## 2. Detailed Domain Inventory

### 2.1 Core Infrastructure Domains (DOM-01 through DOM-08)
- **Bootstrap (`com.sporekart`)**: Main Spring Boot entry point (`SporekartApplication.java`).
- **Config (`com.sporekart.application.config`, `configuration`)**: Security filters, CORS rules, Jackson mappers, JPA auditing, Swagger/OpenAPI setup.
- **Exception (`com.sporekart.application.exception`)**: Central error handlers, `ErrorResponse` DTO, domain exception hierarchies.
- **Idempotency (`com.sporekart.application.idempotency`)**: Idempotency key tracking, standard header interceptors, DB persistence for idempotent operations.
- **Observability (`com.sporekart.application.observability`)**: Custom actuator health indicators, Micrometer Prometheus metrics setup.
- **Outbox (`com.sporekart.application.outbox`)**: Transactional outbox event publishing, event scheduling, event serialization/deserialization.
- **Resilience (`com.sporekart.application.resilience`)**: Circuit breaker, rate limiting, and retry abstractions.
- **Web (`com.sporekart.application.web`)**: Correlation ID tracking filters, base controller responses, web interceptors.

### 2.2 E-Commerce & Customer Domains (DOM-09 through DOM-13)
- **Cart (`com.sporekart.modules.cart`)**: Active cart management, item additions/updates/removals, subtotal calculations.
- **Catalog & Search (`com.sporekart.modules.catalog`)**: Product browsing, category hierarchies, search indexing and filtering.
- **Checkout (`com.sporekart.modules.checkout`)**: Order preparation, address selection, shipping/tax estimation, payment intent initialization.
- **Customer (`com.sporekart.modules.customer`)**: Profile management, address book, customer preference management.
- **Inventory (`com.sporekart.modules.inventory`)**: Stock level tracking, stock reservations during checkout, allocation reconciliation.

### 2.3 Operations, Payment & Logistics Domains (DOM-14 through DOM-21)
- **Notifications (`com.sporekart.modules.notification`)**: In-app notifications, email dispatch integration, notification template rendering.
- **Order (`com.sporekart.modules.order`)**: Order creation, state machine transitions, order cancellation, admin order management.
- **Payment (`com.sporekart.modules.payment`)**: Payment gateway integration (Razorpay/Mock), payment capture, webhook listeners, payment state machine.
- **Returns (`com.sporekart.modules.returns`)**: Return request filing, return approval workflow, refund initiation.
- **Review (`com.sporekart.modules.review`)**: Product ratings, customer review submission, review moderation.
- **Security (`com.sporekart.modules.security`)**: Authentication (login/register), JWT issuance/validation, RBAC role management.
- **Shipment (`com.sporekart.modules.shipment`)**: Shipment creation, tracking status updates, carrier integrations (Shiprocket/Mock).
- **Support (`com.sporekart.modules.support`)**: Customer support ticketing, agent assignment, message exchanges.

### 2.4 Training Domain (DOM-22)
- **Training (`com.sporekart.modules.training`)**: Complete training lifecycle (Programs, Batches, Enrollments, Attendance, Certificates, Payments, Reports, Cancellations, Reminders, Admin Operations).
- **Status**: Functionally accepted through Training 0–14 (Training Acceptance Gate = `ACCEPTED`).

---

## 3. Inventory Verification & Scope
- **Total Production Java Files**: ~250 files
- **Total Test Java Files**: ~120 files
- **All 22 Domains Verified**: Direct match against physical filesystem layout.
