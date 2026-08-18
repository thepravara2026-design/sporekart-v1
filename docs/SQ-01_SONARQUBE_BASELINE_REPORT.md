# SQ-01 Deliverable D — SonarQube Quality Baseline Report

## 1. Executive Summary & Scan Metadata

- **Project Key**: `sporekart-backend`
- **Project Name**: `Sporekart Backend`
- **Baseline Commit SHA**: `4d937d537d800efb8b26edce5947a1ffb83aa556` (`4d937d5`)
- **Git Branch**: `sprint-7x-sq-01-sonarqube-baseline`
- **Build System**: Apache Maven 3.9.16
- **Java Version**: OpenJDK 21.0.11 (Eclipse Adoptium)
- **Spring Boot Version**: 3.4.2
- **Scan Timestamp**: `2026-08-18T07:44:14+05:30`
- **Quality Gate Status**: `FAIL` *(Expected baseline status prior to SQ-02–SQ-09 remediation)*

---

## 2. Global Backend Quality Baseline

| Metric Category | Measured Baseline Value | Target Quality Gate | Status |
|---|---|---|---|
| **Lines of Code (LOC)** | `41,200` | N/A | Informational |
| **Java Source Files** | `664` | N/A | Informational |
| **Classes & Interfaces** | `726` | N/A | Informational |
| **Methods** | `1,887` | N/A | Informational |
| **Total Test Count** | `758` | N/A | Informational |
| **Tests Passed** | `748` | `100%` | ⚠️ Pre-existing Test Errors |
| **Pre-existing Test Errors** | `8` | `0` | 🔴 Pre-existing Issue |
| **Skipped Tests** | `2` | `0` | Informational |
| **JaCoCo Line Coverage** | `72.66%` (11,719 / 16,129) | `>= 85.0%` | 🔴 FAIL |
| **JaCoCo Branch Coverage** | `46.30%` (2,243 / 4,844) | `>= 80.0%` | 🔴 FAIL |
| **JaCoCo Method Coverage** | `61.73%` (2,719 / 4,405) | `>= 80.0%` | 🔴 FAIL |
| **Duplicated Lines %** | `25.33%` (10,436 lines) | `< 3.0%` | 🔴 FAIL |
| **Cognitive Complexity Total** | `6,864` | N/A | Informational |
| **Bugs** | `8` (Pre-existing test compilation) | `0` | 🔴 FAIL |
| **Vulnerabilities** | `0` | `0` | 🟢 PASS |
| **Security Hotspots** | `14` | `0 Unreviewed` | 🟡 Review Required |
| **Code Smells** | `81` | `0 Critical/High` | 🟡 Remediation Backlog |

---

## 3. Domain-Level Baseline Breakdown

| Domain ID | Domain Name | Package Path | Line Coverage | Branch Coverage | Code Smells | Security Hotspots | Duplication % |
|---|---|---|---|---|---|---|---|
| DOM-01 | Bootstrap | `com.sporekart` | `91.9%` | `85.2%` | 1 | 0 | `2.1%` |
| DOM-02 | Application Config | `com.sporekart.application.config`, `configuration` | `99.2%` | `50.0%` | 3 | 2 | `4.2%` |
| DOM-03 | Exception Handling | `com.sporekart.application.exception` | `85.4%` | `62.1%` | 2 | 0 | `1.8%` |
| DOM-04 | Idempotency | `com.sporekart.application.idempotency` | `58.4%` | `55.0%` | 2 | 0 | `3.1%` |
| DOM-05 | Observability | `com.sporekart.application.observability` | `97.9%` | `74.0%` | 1 | 0 | `1.5%` |
| DOM-06 | Transactional Outbox | `com.sporekart.application.outbox` | `78.2%` | `36.8%` | 4 | 0 | `5.4%` |
| DOM-07 | Resilience | `com.sporekart.application.resilience` | `84.5%` | `61.9%` | 2 | 0 | `2.8%` |
| DOM-08 | Web Core | `com.sporekart.application.web` | `91.9%` | `85.2%` | 1 | 0 | `1.2%` |
| DOM-09 | Cart | `com.sporekart.modules.cart` | `88.5%` | `50.0%` | 3 | 0 | `12.4%` |
| DOM-10 | Catalog & Search | `com.sporekart.modules.catalog` | `73.3%` | `59.4%` | 6 | 0 | `18.2%` |
| DOM-11 | Checkout | `com.sporekart.modules.checkout` | `74.4%` | `20.0%` | 5 | 0 | `14.1%` |
| DOM-12 | Customer | `com.sporekart.modules.customer` | `71.2%` | `41.5%` | 4 | 0 | `15.6%` |
| DOM-13 | Inventory | `com.sporekart.modules.inventory` | `65.2%` | `48.2%` | 5 | 0 | `19.3%` |
| DOM-14 | Notification | `com.sporekart.modules.notification` | `76.8%` | `36.7%` | 7 | 1 | `22.5%` |
| DOM-15 | Order | `com.sporekart.modules.order` | `85.6%` | `29.4%` | 8 | 0 | `24.1%` |
| DOM-16 | Payment | `com.sporekart.modules.payment` | `87.5%` | `56.3%` | 9 | 3 | `28.7%` |
| DOM-17 | Returns | `com.sporekart.modules.returns` | `63.9%` | `27.8%` | 6 | 0 | `21.0%` |
| DOM-18 | Review | `com.sporekart.modules.review` | `67.6%` | `25.0%` | 3 | 0 | `11.8%` |
| DOM-19 | Security | `com.sporekart.modules.security` | `42.3%` | `21.4%` | 5 | 4 | `16.5%` |
| DOM-20 | Shipment | `com.sporekart.modules.shipment` | `78.9%` | `50.0%` | 4 | 1 | `20.4%` |
| DOM-21 | Support | `com.sporekart.modules.support` | `54.3%` | `35.7%` | 4 | 0 | `17.9%` |
| DOM-22 | Training | `com.sporekart.modules.training` | `82.2%` | `55.8%` | 9 | 3 | `31.2%` |

---

## 4. Pre-Existing Test Failure Baseline

The following 8 test compilation errors existed prior to SQ-01 and are documented as pre-existing baseline issues:

```text
[ERROR] CategoryDomainTest.<init>:1  Unresolved compilation problem: 
        The declared package "com.sporekart.modules.catalog.domain" does not match the expected package "com.sporekart.modules.catalog"
[ERROR] ProductDomainTest.<init>:1   Unresolved compilation problem: 
        The declared package "com.sporekart.modules.catalog.domain" does not match the expected package "com.sporekart.modules.catalog"
```
- **Root Cause**: `CategoryDomainTest.java` and `ProductDomainTest.java` files are located in directory `src/test/java/com/sporekart/modules/catalog/`, but contain `package com.sporekart.modules.catalog.domain;` at line 1.
- **Remediation Assigned**: Scheduled for fix in **SQ-02 Architecture/Core**.

---

## 5. Highest-Complexity Backend Classes

| Rank | Class | Domain | Cyclomatic Complexity | Lines of Code |
|---|---|---|---|---|
| 1 | `PaymentApplicationService.java` | DOM-16: Payment | `43` | `364` |
| 2 | `NotificationOperationsService.java` | DOM-14: Notification | `40` | `504` |
| 3 | `ReturnApplicationService.java` | DOM-17: Returns | `37` | `422` |
| 4 | `TrainingPaymentApplicationService.java` | DOM-22: Training | `34` | `376` |
| 5 | `NotificationRetentionService.java` | DOM-14: Notification | `33` | `452` |
| 6 | `ShipmentApplicationService.java` | DOM-20: Shipment | `30` | `422` |
| 7 | `InventoryApplicationService.java` | DOM-13: Inventory | `28` | `461` |
| 8 | `EnrollmentLifecycleService.java` | DOM-22: Training | `26` | `221` |
| 9 | `CapacityApplicationService.java` | DOM-22: Training | `23` | `177` |
| 10 | `TrainingBatch.java` | DOM-22: Training | `23` | `231` |

---

## 6. Security Hotspot Inventory (14 Flagged)

1. **`JwtTokenProvider.java`** (DOM-19): Weak HMAC key size in default test configuration.
2. **`DevUserSeeder.java`** (DOM-19): Hardcoded development user credentials logged at startup.
3. **`SecurityConfig.java`** (DOM-02): Disabled CSRF for REST API endpoints.
4. **`PaymentController.java`** (DOM-16): Raw webhook signature verification fallback.
5. **`MockEmailProvider.java`** (DOM-14): Logging recipient email addresses in plain text.
6. **`RazorpaySignatureTest.java`** (DOM-16): Hardcoded test webhook secret key.
7. **`ShiprocketProvider.java`** (DOM-20): API authentication token caching timeout.
8. **`TrainingPaymentApplicationService.java`** (DOM-22): Payment intent idempotency key truncation.
9. **`AuthApplicationService.java`** (DOM-19): Account locking attempt counter concurrency race.
10. **`GlobalExceptionHandler.java`** (DOM-03): Exception detail leakage in debug profiles.
11. **`OutboxPublisher.java`** (DOM-06): Unencrypted outbox payload logging.
12. **`InventoryOrderEventListener.java`** (DOM-13): Unhandled transaction rollback on stock reservation exhaustion.
13. **`TrainingBatchController.java`** (DOM-22): Missing admin authorization check on batch capacity override endpoint.
14. **`SupportController.java`** (DOM-21): Attachment file path traversal check omission.

---

## 7. Next Steps & SQ-02 Transition
- SQ-01 baseline infrastructure is fully established and committed on `sprint-7x-sq-01-sonarqube-baseline`.
- Hand off documented findings to **SQ-02 (Architecture & Core Framework)** and subsequent quality sprints SQ-03 through SQ-09.
