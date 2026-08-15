# SPOREKART v3.0 — Final Production Domain Map

**Date**: 2026-08-15
**Architecture Pattern**: Modular Monolith
**Target Runtime**: Spring Boot 3.4.2 / Java 21 / PostgreSQL 16

---

## 1. Domain Module Boundaries

```
com.sporekart.modules
├── catalog          # Product Catalog, Categories, Search & SKU Management
├── customer         # Customer Profile, Authentication Context & Addresses
├── cart             # Cart Lifecycle, Cart Items & Optimistic Locking
├── checkout         # Order Calculation, Discounts, Tax & Checkout Orchestration
├── order            # Order Aggregate, State Machine & Order History
├── payment          # Payment Checkout, Razorpay Integration & Webhook Reconciliation
├── inventory        # Stock Management, Reservations, Thresholds & Audit Logs
├── shipment         # Fulfillment, Shiprocket Integration, AWB & Tracking Timeline
├── returns          # Return Requests, Inspection & Reverse Logistics
├── refund           # Refund Records, Provider Refunds & Idempotent Processing
├── support          # Customer Support Tickets, SLA Calculation & Replacements
├── review           # Product Reviews, Ratings, Merchant Replies & Moderation
└── notification     # Email/SMS Event Listeners & Notification Logs
```

---

## 2. Module Responsibilities & Package Mapping

| Domain Module | Primary Entities | Application Service | Event Emitted |
| :--- | :--- | :--- | :--- |
| `catalog` | `Product`, `Category` | `CatalogApplicationService` | `ProductCreatedEvent` |
| `customer` | `Customer`, `Address` | `CustomerApplicationService` | `CustomerRegisteredEvent` |
| `cart` | `Cart`, `CartItem` | `CartApplicationService` | `CartClearedEvent` |
| `checkout` | `CheckoutSession` | `CheckoutApplicationService` | `CheckoutCompletedEvent` |
| `order` | `Order`, `OrderItem`, `OrderStatusHistory` | `OrderApplicationService` | `OrderCreatedEvent`, `OrderStatusChangedEvent` |
| `payment` | `Payment`, `PaymentAttempt`, `PaymentWebhookEvent` | `PaymentApplicationService` | `PaymentCompletedEvent`, `PaymentFailedEvent` |
| `inventory` | `InventoryItem`, `StockReservation` | `InventoryApplicationService` | `StockReservedEvent`, `StockReleasedEvent` |
| `shipment` | `Shipment`, `TrackingEvent` | `ShipmentApplicationService` | `ShipmentCreatedEvent`, `ShipmentDeliveredEvent` |
| `returns` | `Return`, `ReturnItem`, `ReturnInspection` | `ReturnApplicationService` | `ReturnRequestedEvent`, `ReturnApprovedEvent` |
| `refund` | `RefundRecord` | `RefundApplicationService` | `RefundProcessedEvent` |
| `support` | `SupportTicket`, `SupportMessage`, `ReplacementRequest` | `SupportApplicationService` | `TicketCreatedEvent`, `TicketResolvedEvent` |
| `review` | `ProductReview`, `ReviewHelpfulnessVote`, `ProductRatingSummary` | `ReviewApplicationService` | `ReviewSubmittedEvent` |
| `notification` | `NotificationLog` | `NotificationApplicationService` | N/A (Event Listener) |

---

## 3. Communication Rules

1. **In-process Method Invocations**: Direct cross-module Java method calls are allowed ONLY through `ApplicationService` interfaces.
2. **Asynchronous Events**: Cross-domain side-effects (e.g. Order Created → Reserve Inventory, Payment Completed → Mark Order Paid) MUST use Spring `@EventListener` or `@TransactionalEventListener`.
3. **No Direct Entity References**: Entities in one domain MUST NOT hold JPA `@ManyToOne` or `@OneToMany` relationships to entities in another domain. Cross-domain references use String/UUID identifiers.