# SPOREKART v3.0 — PAC-06 Source of Truth & Admin Architecture

**Document ID:** `PAC-06-SOURCE-OF-TRUTH`  
**Sprint:** `PAC-06 — Admin & Platform Operations Acceptance`  
**Author:** Antigravity FAANG Master Execution Agent  
**Date:** 2026-08-19  
**Status:** Certified Authoritative Baseline  

---

## 1. Executive Summary

This document establishes the single source of truth for SPOREKART v3.0 Admin & Platform Operations architecture, control plane boundaries, role privilege enforcement, multi-domain management contracts, and operational monitoring certified under **PAC-06 — Admin & Platform Operations Acceptance**.

---

## 2. Admin Control Plane Architecture

```
Admin SPA Consoles (AdminDashboardPage, AdminLayout, Consoles)
        │
        ▼ Spring Security Filter Chain (/api/v1/admin/** -> hasAnyAuthority("ADMIN", "ROLE_ADMIN"))
Admin REST Controllers Layer
   ├── AdminOrderController
   ├── AdminShipmentController
   ├── AdminReturnController
   ├── AdminTrainingProgramController / AdminBatchController / AdminEnrollmentController
   ├── AdminTrainingOperationsConsole & Reporting
   └── NotificationOperationsConsole & Actuator Endpoint Protection
        │
        ▼ Business Domain Services Layer (Server-Side Domain Validation & Audit Logging)
   ├── OrderApplicationService
   ├── InventoryApplicationService
   ├── TrainingProgramApplicationService / TrainingCancellationService
   ├── ReturnApplicationService / PaymentApplicationService
   └── OutboxEventProcessor & NotificationTemplateInitializer
        │
        ▼ Database & Persistence Layer (Atomic Commit / Safe Rollback)
   ├── orders / order_items / shipments
   ├── catalog_products / inventory_items
   ├── training_programs / training_batches / training_enrollments
   ├── payments / refunds
   └── outbox_events / notification_logs
```

---

## 3. Authoritative Control Plane Capabilities

| Operational Subsystem | Primary Admin Component / Controller | Controlled Domain Entities | Primary Authority Rule |
|-----------------------|--------------------------------------|----------------------------|------------------------|
| **Order Management** | `AdminOrderController`, `AdminShipmentController` | `orders`, `shipments` | `hasAnyAuthority("ADMIN", "ROLE_ADMIN")` |
| **Returns & Refunds** | `AdminReturnController`, `ReturnApplicationService` | `return_requests`, `refunds` | `hasAnyAuthority("ADMIN", "ROLE_ADMIN")` |
| **Catalog & Products** | `CatalogProductController`, `ProductApplicationService` | `product_entities`, `categories` | Admin overwrite & status management |
| **Training Programs** | `AdminTrainingProgramController`, `AdminBatchController` | `training_programs`, `training_batches` | `hasAnyAuthority("ADMIN", "ROLE_ADMIN")` |
| **Training Operations** | `AdminTrainingOperationsService`, `AdminEnrollmentController` | `training_enrollments`, `training_payments` | `hasAnyAuthority("ADMIN", "ROLE_ADMIN")` |
| **Notifications & Outbox**| `NotificationOperationsConsole`, `OutboxEventProcessor` | `outbox_events`, `notification_templates` | `hasAnyAuthority("ADMIN", "ROLE_ADMIN")` |
| **System Health & Metrics**| Actuator `/actuator/health`, `/actuator/prometheus` | In-memory metrics & database health | Public health status; Prometheus admin-only |

---

## 4. Key Admin Governance Safeguards

1. **Control Plane Invariant Preservation:** Admin operations execute as a privileged control plane, enforcing all domain state machine rules (e.g. order state machine, training cancellation windows, capacity limits) without bypassing transactional integrity or creating corrupted state.
2. **Privilege Boundary Security:** All endpoints matching `/api/v1/admin/**` and sensitive Actuator metrics endpoints (`/actuator/prometheus`, `/actuator/metrics`) require `hasAnyAuthority("ADMIN", "ROLE_ADMIN")`. Unauthorized requests return `HTTP 403 FORBIDDEN`.
3. **Data Integrity & Auditability:** Admin state modifications (e.g. return approval, shipment creation, batch rescheduling) write atomic domain updates and outbox events for operational auditing.

---

## 5. Governance Alignment Verdict

**VERDICT: CERTIFIED PASS** — The Admin control plane architecture strictly satisfies all security, multi-domain control, and transactional integrity rules required for PAC-06.
