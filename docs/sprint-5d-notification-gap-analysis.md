# Sporekart v3.0 — Sprint 5D Notification & Communication Platform Gap Analysis

## Executive Summary
This document provides a comprehensive architectural audit and gap analysis for **Sprint 5D: Notification & Communication Platform** in Sporekart v3.0. The audit inspects all completed foundations (Sprints 0–4, 5A–5C, 5D Reliability/Resilience, 5E Production Readiness, 5F Release Engineering) and details the reuse strategy, modular-monolith boundaries, domain event integrations, and database design for Sprint 5D.

---

## 1. Capability Gap Matrix

| Domain / Dimension | Existing Capability (Sprint 0–5F) | Required Capability (Sprint 5D) | Identified Gap | Reuse Strategy |
| :--- | :--- | :--- | :--- | :--- |
| **Notification Domain Model** | None (only `com.sporekart.modules.notification.package-info`) | Durable `Notification` aggregate root & `NotificationDeliveryAttempt` record | Need full domain entities, enums (`NotificationChannel`, `NotificationStatus`, `NotificationPriority`), repositories & DTOs | Implement within `com.sporekart.modules.notification` following Spring Data JPA entity patterns |
| **Provider Abstraction** | None | Provider-independent `NotificationProvider` SPI supporting Email, SMS, WhatsApp, In-App | Need clean provider interface, channel-specific provider contracts, and mock providers for dev/test | Define `NotificationProvider` SPI with mock implementations (`MockEmailProvider`, `MockSmsProvider`, `MockWhatsAppProvider`, `InAppNotificationProvider`) |
| **Channel Abstraction** | None | `EMAIL`, `SMS`, `WHATSAPP`, `IN_APP` channel routing | Need channel router/orchestrator to dispatch notifications dynamically based on channel & user preferences | Implement `NotificationOrchestrator` to coordinate provider discovery & delivery |
| **Domain Event Integration** | Domain events published via Spring `ApplicationEventPublisher` in Order, Shipment, Returns, Review, Support | Integration for Customer (`USER_REGISTERED`), Order (`ORDER_CREATED`, `ORDER_CONFIRMED`, `ORDER_CANCELLED`, `ORDER_COMPLETED`), Payment (`PAYMENT_SUCCEEDED`, `PAYMENT_FAILED`, `REFUND_INITIATED`, `REFUND_COMPLETED`, `REFUND_FAILED`), Shipping (`SHIPMENT_CREATED`, `SHIPMENT_DISPATCHED`, `SHIPMENT_DELIVERED`, `SHIPMENT_FAILED`), Returns (`RETURN_REQUESTED`, `RETURN_APPROVED`, `RETURN_REJECTED`), Security (`ACCOUNT_LOCKED`, `PASSWORD_CHANGED`), Inventory/Admin (`LOW_STOCK_ALERT`, `INVENTORY_OPERATION_FAILURE`) | Need `NotificationEventProcessor` listening asynchronously to application events without corrupting source domain transactions | Use `@EventListener` and `@TransactionalEventListener(phase = AFTER_COMMIT)` to ensure notification failure never rolls back commerce state |
| **Idempotency** | `IdempotencyService` in `com.sporekart.application.idempotency` & database unique constraints | Notification delivery idempotency preventing duplicate messages for the same event + channel + recipient | Need domain-specific notification idempotency checks (`eventId + channel + recipient + templateVersion`) | Enforce database unique constraint on `(event_id, channel, recipient, template_version)` & check existing notification before dispatch |
| **Delivery State Machine** | None for notifications | Lifecycle: `CREATED` → `QUEUED` → `PROCESSING` → `SENT` → `DELIVERED`, failure path `FAILED` → `RETRY_SCHEDULED`, terminal `FAILED_PERMANENTLY` | Need status enum, valid transition guard methods, and optimistic locking (`@Version`) | Implement strict state transition validation methods on `Notification` entity |
| **Retry & Resilience** | `ResilientExecutor` in `com.sporekart.application.resilience` with backoff retries & exception classification | Bounded, observable retries for transient provider failures/timeouts vs permanent failures | Need classification of notification provider errors (`TransientFailureException` vs permanent error) and retry scheduling | Reuse `ResilientExecutor` for retry execution & record attempt details in `notification_delivery_attempts` |
| **Template System** | None | Versioned, locale-aware notification template management with fields `templateCode`, `channel`, `version`, `locale`, `subject`, `body`, `status` (`DRAFT`, `ACTIVE`, `ARCHIVED`) | Need versioned template domain model, active version selection logic, and safe placeholder substitution engine | Implement `NotificationTemplate` & `NotificationTemplateVersion` entities; create injection-safe placeholder substitutor |
| **Customer Preferences** | None | Channel-aware customer notification preferences for categories: `ORDER_UPDATES`, `PROMOTIONAL`, `PAYMENT`, `SHIPPING`, `RETURN_REFUND` | Need `NotificationPreference` model with channel flags and mandatory transactional notification bypass logic | Create `NotificationPreference` domain entity; enforce rule that mandatory security/transactional alerts bypass opt-out preferences |
| **In-App Notifications** | None | Persistent user in-app notification inbox with read/unread tracking and pagination | Need in-app persistent notification storage, unread count endpoint, and mark-read APIs | Model `IN_APP` channel deliveries to be queryable by authenticated users via `NotificationController` |
| **REST APIs & RBAC** | `SecurityContextHolder`, `JwtAuthenticationFilter`, `@PreAuthorize` in `com.sporekart.modules.security` | Customer APIs (`/api/v1/notifications`, `/api/v1/notification-preferences`) and Admin APIs (`/api/v1/admin/notifications`, `/api/v1/admin/notification-templates`) | Need customer notification controllers and admin notification controllers | Leverage existing JWT security filters, customer identity propagation, and `@PreAuthorize("hasRole('ROLE_ADMIN')")` |
| **Observability & Metrics** | `CommerceMetricsService` in `com.sporekart.application.observability.metrics` | Notification metrics (`sporekart.notifications.created`, `sent`, `delivered`, `failed`, `retried`, `duplicates`) | Need meter calls with low-cardinality tags | Extend `CommerceMetricsService` to record notification metrics |
| **Audit Logging** | `SecurityAuditService` in `com.sporekart.modules.security.application` | Audit records for `NOTIFICATION_CREATED`, `NOTIFICATION_SENT`, `NOTIFICATION_FAILED`, `NOTIFICATION_RETRIED`, `TEMPLATE_CREATED`, `TEMPLATE_UPDATED`, `PREFERENCE_CHANGED` | Need security audit logging for sensitive template and preference changes | Reuse `SecurityAuditService` for administrative and preference audit events |
| **Database Schema** | Flyway migrations V1–V19 complete | Flyway migration V20 for notification schema | Need Flyway migration script `V20__notification_platform_schema.sql` | Create `V20__notification_platform_schema.sql` adding `notifications`, `notification_delivery_attempts`, `notification_templates`, `notification_template_versions`, `notification_preferences` |

---

## 2. Architecture & Reuse Strategy

1. **Modular Monolith Boundaries**:
   - Everything related to notifications resides strictly within `com.sporekart.modules.notification`.
   - Business domains (Order, Payment, Shipping, Returns, Security) publish domain events via Spring's `ApplicationEventPublisher`. They have zero dependency on notification provider SDKs or template engines.

2. **Transactional Decoupling**:
   - Source transactions (e.g. order creation, payment capture) commit first.
   - `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` or async event listeners process notification creation.
   - A failure during notification delivery will never trigger a rollback of a completed order, payment, shipment, or refund transaction.

3. **Resilience & Idempotency Reuse**:
   - `ResilientExecutor` handles retry execution with exponential backoff and jitter.
   - Idempotency is enforced at database level (`event_id + channel + recipient + template_version` unique index) and checked prior to provider dispatch.

4. **Security & Identity Alignment**:
   - APIs use existing `JwtAuthenticationFilter`, `UserAccount`, and `@PreAuthorize` controls.
   - Customer endpoints verify `userId` against `SecurityContextHolder` to prevent notification enumeration and cross-user data exposure.

---

## 3. Database Schema Plan (`V20__notification_platform_schema.sql`)

The migration introduces 5 dedicated tables:
- `notifications`: Core notification records tracking status, recipient, template, correlation IDs, and delivery outcome.
- `notification_delivery_attempts`: Audit trail of each provider attempt (attempt number, provider message ID, duration, status, error details).
- `notification_templates`: Header record for versioned notification templates.
- `notification_template_versions`: Detailed body, subject, locale, version, and status (`DRAFT`, `ACTIVE`, `ARCHIVED`) per template code and channel.
- `notification_preferences`: Per-user preference settings controlling opt-ins/opt-outs for non-mandatory notification categories across channels (`EMAIL`, `SMS`, `WHATSAPP`, `IN_APP`).

---

## 4. Test Strategy

- **Unit Tests**: Domain model state machine transitions, template substitutor placeholder validation, preference calculation logic, error classification.
- **Integration Tests**: Provider abstractions (`MockEmailProvider`, `MockSmsProvider`, `MockWhatsAppProvider`, `InAppNotificationProvider`), idempotency duplicate suppression, retry exhaustion, concurrency tests, and REST API controller security/customer isolation tests.
- **Regression Suite**: Run `mvn clean test` across all 295+ existing tests to guarantee 0 regressions.
