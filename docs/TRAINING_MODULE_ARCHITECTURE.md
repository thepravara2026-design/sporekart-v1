# SPOREKART v3.0 — Training Module Architecture & Foundation (Training 0)

## 1. Module Responsibility
The Training Module (`com.sporekart.modules.training`) is responsible for managing training programs, batch capacity, scheduling, trainee enrollment lifecycle, administrative operations, and cancellation/rescheduling policies within SPOREKART v3.0.

## 2. Architecture Diagram

```
+-------------------------------------------------------------------+
|                            CLIENT API                             |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|               Presentation / Controller Layer                     |
|  - TrainingHealthController (/api/v1/training/health)             |
|  - TrainingConfigController (/api/v1/admin/training/config)       |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                  Application / Service Layer                      |
|  - TrainingApplicationService                                     |
|  - TrainingPolicyProperties (@ConfigurationProperties)            |
+-------------------------------------------------------------------+
                                  |
               +------------------+------------------+
               |                                     |
               v                                     v
+-------------------------------+   +-------------------------------+
|        Domain Layer           |   |    Infrastructure Layer       |
|  - TrainingProgram            |   |  - JPA Entities & Repositories|
|  - TrainingBatch & Capacity   |   |  - Atomic Seat Allocation     |
|  - TrainingEnrollment         |   |  - Event Publisher Adapter    |
|  - CancellationPolicy         |   +-------------------------------+
|  - CapacityPolicy             |
|  - Domain Events & Exceptions |
+-------------------------------+
               |
               +----------------------------------------------------+
               |                   |                |               |
               v                   v                v               v
       Existing Database   Existing Events  Existing Auth  Existing Audit
           (Flyway V31)     (Spring Events)    (Security)     (SecurityAudit)
```

## 3. Core Domain Entities & Relationships

```
TrainingProgram (1)
     │
     └──> TrainingBatch (N)
               │
               ├──> BatchSchedule (N)
               ├──> Capacity (Value Object: totalCapacity, occupiedSeats)
               └──> TrainingEnrollment (N) ──> Trainee (UserAccount.id)
```

- **`TrainingProgram`**: Aggregate root representing a course offering. Identifiers are UUID string values.
- **`TrainingBatch`**: Represents a scheduled instance of a program with total capacity, occupied seats, and state (`PLANNED`, `ACTIVE`, `FULL`, `COMPLETED`, `CANCELLED`).
- **`BatchSchedule`**: Represents scheduled sessions, dates/times, duration, and locations for a batch.
- **`TrainingEnrollment`**: Represents a trainee's enrollment record referencing `UserAccount.id` with a database unique constraint `uq_batch_trainee(batch_id, trainee_id)`.

## 4. Data Ownership

| Domain/Module | Owned Data Assets & Entities |
| :--- | :--- |
| **Security / User** | User identity, authentication credentials, `UserAccount`, roles (`ROLE_ADMIN`, `ROLE_CUSTOMER`). |
| **Training** | `TrainingProgram`, `TrainingBatch`, `BatchSchedule`, `TrainingEnrollment`, capacity states, cancellation/rescheduling policies. |
| **Payment** | Payment transactions, callbacks, payment status (`PaymentAttempt`). Referenced via `paymentReference`. |
| **Notification** | Notification delivery, templates, outbox dispatch (`Notification`). Triggered by domain events. |
| **Audit** | Operational and security audit records (`SecurityAuditEvent`). |

## 5. State Model Architecture

### Batch State Transitions
```
PLANNED ──> ACTIVE ──> FULL ──> COMPLETED
   │          │          │
   └──────────┴──────────┴────> CANCELLED
```
- A batch automatically transitions to `FULL` when `occupiedSeats >= totalCapacity`.
- If capacity is expanded by an administrator or a seat is released, a `FULL` batch automatically transitions back to `ACTIVE`.

### Enrollment State Transitions
```
PENDING ──> CONFIRMED ──> COMPLETED
   │           │
   └───────────┴───────> CANCELLED / WAITLISTED
```

## 6. Authorization & Security Boundaries
- **Admin Boundary**: Operations modifying programs, batches, total capacity, and administrative cancellations require `@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN')")`.
- **Trainee Boundary**: Trainee operations (viewing programs, initializing enrollment, trainee cancellation) require authenticated context (`@PreAuthorize("isAuthenticated()")` or role-based check). Authoritative user identity is always extracted from the authenticated SecurityContext.

## 7. Policy & Configuration Strategy
Domain policies are encapsulated in dedicated domain policy classes:
- **`CancellationPolicy`**:
  - Admin cancellation/rescheduling window = 7 days before scheduled start date (`scheduledDate.minusDays(7)`).
  - Trainee cancellation/rescheduling window = 2 days before scheduled start date (`scheduledDate.minusDays(2)`).
- **`CapacityPolicy`**:
  - Enforces capacity non-overbooking invariants, seat allocation, and total capacity expansion/reduction.
- Policy parameters are injected via `TrainingPolicyProperties` bound to `sporekart.training.*` in `application.yml`.

## 8. Transaction & Concurrency Strategy
- **Transaction Boundaries**: Declared at the application service level using `@Transactional`.
- **Atomic Concurrency Protection**: High-concurrency seat allocation executes atomic database update queries:
  ```sql
  UPDATE training_batches
  SET occupied_seats = occupied_seats + 1,
      status = CASE WHEN (occupied_seats + 1) >= total_capacity THEN 'FULL' ELSE status END
  WHERE id = :batchId AND occupied_seats < total_capacity AND status NOT IN ('CANCELLED', 'COMPLETED');
  ```
  This eliminates race conditions and guarantees zero overbooking under concurrent enrollment spikes.

## 9. Event Integration Architecture
Domain events implement `TrainingEvent` and are published via Spring's `ApplicationEventPublisher`:
- `TrainingProgramCreatedEvent`
- `TrainingBatchCreatedEvent`
- `BatchCapacityChangedEvent`
- `BatchFullEvent`
- `TrainingEnrollmentCreatedEvent`
- `TrainingEnrollmentCancelledEvent`
- `TrainingEnrollmentRescheduledEvent`

Events naturally integrate with the existing Transactional Outbox and Notification processing pipelines.

## 10. Error Handling Strategy
Domain exceptions are handled centrally in `GlobalExceptionHandler`:
- `TrainingNotFoundException` (404 NOT_FOUND)
- `BatchNotFoundException` (404 NOT_FOUND)
- `BatchFullException` (400 BAD_REQUEST)
- `EnrollmentNotFoundException` (404 NOT_FOUND)
- `DuplicateEnrollmentException` (409 CONFLICT)
- `CapacityExceededException` (400 BAD_REQUEST)
- `CancellationWindowExpiredException` (400 BAD_REQUEST)
- `RescheduleWindowExpiredException` (400 BAD_REQUEST)
- `UnauthorizedTrainingOperationException` (403 FORBIDDEN)

## 11. Extension Points for Training 1–14
1. **Training 1 (Program Management)**: Extend `TrainingProgram` attributes and administrative CRUD use cases.
2. **Training 2 (Batch & Schedule)**: Expand `BatchSchedule` location links, instructor assignments, and recurrence.
3. **Training 3 (Capacity & Slot)**: Integrate real-time slot lock reservations and waitlist queues.
4. **Training 4 (Trainee Enrollment)**: Implement complete enrollment checkout flow.
5. **Training 5 (Full-Batch & Demand Handling)**: Implement automated waitlist notifications and admin demand dashboards.
6. **Training 6 (Payment Integration)**: Bind `TrainingEnrollment.paymentReference` to `PaymentApplicationService`.
7. **Training 7 (Enrollment Lifecycle)**: Implement status state machine listener hooks.
8. **Training 8-9 (Admin/Trainee Operations)**: Expand operational dashboard endpoints.
9. **Training 10 (Cancellation & Rescheduling)**: Implement full refund workflow and seat re-allocation.
10. **Training 11 (Notifications)**: Wire training domain event listeners to outbox notification triggers.
11. **Training 12 (Attendance & Certificate)**: Add attendance records and PDF certificate generation engine.
12. **Training 13 (Reporting & Audit)**: Wire training actions to `SecurityAuditService`.
13. **Training 14 (Production Hardening & Final Acceptance)**: Load testing, rate limiting, and final verification.
