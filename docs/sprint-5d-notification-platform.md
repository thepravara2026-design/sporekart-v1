# Sporekart v3.0 — Sprint 5D: Notification & Communication Platform Architecture

## Executive Overview

The Notification & Communication Platform provides a provider-neutral, multi-channel messaging infrastructure supporting **Email**, **SMS**, **WhatsApp**, and **In-App** delivery across Sporekart v3.0 modular monolith domains.

It is designed with strict transactional decoupling: notification delivery failures **never** cause commerce transactions (Order, Payment, Shipping, Return) to roll back.

---

## Key Architectural Principles

1. **Provider Neutrality via SPI**:
   `NotificationProvider` SPI decouples domain logic from external communication vendors (Mock email, Mock SMS, Mock WhatsApp, In-App).

2. **Template Engine & Versioning**:
   - `NotificationTemplate` & `NotificationTemplateVersion` manage localized, versioned notification templates.
   - `TemplatePlaceholderSubstitutor` ensures safe placeholder substitution (`{{key}}`) preventing code injection.

3. **User Notification Preferences & Opt-Outs**:
   - `NotificationPreferenceService` enforces user channel preferences per category.
   - Mandatory categories (`SECURITY`, `TRANSACTIONAL`, `SYSTEM_ALERT`) bypass non-critical opt-outs to ensure critical customer alerts are delivered.

4. **Transactional Decoupling & Asynchronous Processing**:
   - Commerce domains publish events (`OrderLifecycleEvent`, `ShipmentLifecycleEvent`, `ReturnRequestedEvent`, `SupportTicketCreatedEvent`).
   - `NotificationEventProcessor` listens via `@EventListener` + `@Async`, executing outside the primary commerce database transaction.

5. **Resilience, Retryability & Auditability**:
   - Integrates with `ResilientExecutor` for exponential backoff retries on transient errors.
   - `NotificationDeliveryAttempt` logs duration, status, and provider message IDs for 100% auditability.
   - `Notification` entities support optimistic locking (`@Version`) and idempotency key deduplication.

---

## Core Domain Entities & Database Schema (Flyway V20)

- `notifications` table (Aggregate root with lifecycle status: `PENDING` -> `PROCESSING` -> `SENT` -> `DELIVERED` / `FAILED`)
- `notification_delivery_attempts` table (Log of provider dispatch attempts)
- `notification_templates` & `notification_template_versions` tables (Versioned template engine)
- `notification_preferences` table (Customer opt-in/opt-out settings)

---

## Security & Customer Data Isolation

- REST endpoints (`GET /api/v1/notifications`, `POST /api/v1/notifications/{id}/read`, `GET /api/v1/notification-preferences`) validate JWT authentication and restrict user access strictly to their own notifications (`userId` match).
- Admin endpoints (`/api/v1/admin/notifications/**`, `/api/v1/admin/notification-templates/**`) require `@PreAuthorize("hasRole('ROLE_ADMIN')")`.

---

## Verification & Metrics

- Operational metrics tracked via `CommerceMetricsService`:
  - `sporekart.notifications.sent`
  - `sporekart.notifications.delivered`
  - `sporekart.notifications.failed`
- Security audits emitted to `SecurityAuditService` for template modifications and dispatch logs.
