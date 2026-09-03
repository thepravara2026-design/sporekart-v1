# SQ-01 Deliverable E — Remediation Backlog

## 1. Overview
This backlog categorizes code quality, test, complexity, security, and coverage findings identified during **SQ-01 Baseline Analysis**. Findings are assigned to subsequent quality sprints (SQ-02 through SQ-09) for targeted resolution.

---

## 2. Sprint Backlog Mapping

### 2.1 SQ-02 — Architecture & Core Framework Remediation
- **ITEM-02-01**: Fix pre-existing package declaration mismatches in `CategoryDomainTest.java` and `ProductDomainTest.java` (`com.sporekart.modules.catalog.domain` vs `com.sporekart.modules.catalog`).
- **ITEM-02-02**: Standardize global exception handling and correlation ID filter propagation across all application entry points.
- **ITEM-02-03**: Clean up unused imports and deprecated Spring Boot config properties in `application/config`.

### 2.2 SQ-03 — Commerce Domains Remediation (Catalog, Cart, Inventory, Customer)
- **ITEM-03-01**: Improve unit test coverage for `CatalogApplicationService` and `InventoryApplicationService`.
- **ITEM-03-02**: Address duplicate validation logic in `CartItem` and `CheckoutSession` initialization.
- **ITEM-03-03**: Refactor entity DTO mapping methods in `CustomerController` to eliminate redundant field mapping.

### 2.3 SQ-04 — Orders & Payment Gateway Hardening
- **ITEM-04-01**: Reduce cognitive complexity in `OrderStateMachine` state transition handler methods.
- **ITEM-04-02**: Add branch test coverage for Razorpay signature verification failure and edge-case webhook scenarios.
- **ITEM-04-03**: Audit transactional outbox boundaries during payment state changes.

### 2.4 SQ-05 — Training Module Quality Maintenance
- **ITEM-05-01**: Maintain accepted Training functionality (Training 0–14).
- **ITEM-05-02**: Refactor internal DTO duplication across `TrainingProgramController` and `TrainingBatchController`.
- **ITEM-05-03**: Add javadoc documentation for key domain exceptions in `com.sporekart.modules.training.domain.exception`.

### 2.5 SQ-06 — Notification & Outbox Pattern Hardening
- **ITEM-06-01**: Add retry and circuit breaker fallback coverage for `MockEmailProvider` and `InAppNotificationProvider`.
- **ITEM-06-02**: Refactor `OutboxScheduler` polling query to optimize database indexing.
- **ITEM-06-03**: Ensure sensitive notification payload fields (tokens, passwords) are masked in application logs.

### 2.6 SQ-07 — Security, Dependencies & Hotspot Remediation
- **ITEM-07-01**: Review and resolve SonarQube Security Hotspots in `JwtTokenProvider` (weak key generation warnings in test profile).
- **ITEM-07-02**: Remediate logging of sensitive information in `DevUserSeeder` and authentication endpoints.
- **ITEM-07-03**: Address Flyway and H2 dialect deprecation warnings in test configurations.

### 2.7 SQ-08 — Coverage & Duplication Remediation
- **ITEM-08-01**: Eliminate structural duplication across DTOs and Mappers across all modules (target duplication `< 3.0%`).
- **ITEM-08-02**: Raise overall backend line coverage from current baseline to `>= 85%`.
- **ITEM-08-03**: Raise overall backend branch coverage to `>= 80%`.

### 2.8 SQ-09 — Complexity & Final Quality Gate Certification
- **ITEM-09-01**: Refactor top 10 highest cognitive complexity methods across all backend domains to `<= 15`.
- **ITEM-09-02**: Execute full SonarQube scan and certify Quality Gate `PASS` status prior to Sprint 7X release verification.
