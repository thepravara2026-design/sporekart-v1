# SPOREKART v3.0 — GROWER OPERATIONAL HARDENING

**Sprint ID:** GB-03  
**Sprint Name:** Grower Production Readiness, Observability & Operational Hardening  
**Date:** August 19, 2026  
**Status:** COMPLETE / VERIFIED  

---

## 1. Operational Overview

This document details the operational hardening mechanisms implemented for the **Grower Bounded Context** (`com.sporekart.modules.grower`) in SPOREKART v3.0.

---

## 2. Key Operational Mechanisms

### 2.1 Transaction Boundaries
- Service write paths (`updateProfile`, `updateSettings`, `createProduct`, `updateProduct`, `adjustStock`, `transitionOrder`) execute within standard read-write Spring `@Transactional` boundaries.
- Service read operations (`getProfile`, `getSettings`, `getDashboard`, `getProducts`, `getProductById`, `getInventory`, `getInventoryBySku`, `getOrders`, `getOrderById`) execute with `@Transactional(readOnly = true)` to optimize connection allocation and prevent unintentional mutations.

### 2.2 Security & Multi-Tenant Audit Trail
- All Grower mutations invoke `SecurityAuditService.logEvent` recording structured event records in `security_audit_events`.
- Audit records capture event type, actor ID, resource/target ID, status, and descriptive metadata without exposing PII or credentials.

### 2.3 Concurrency & Inventory Protection
- Inventory updates enforce quantity adjustments in `InventoryItem.adjustOnHand()` and persist via atomic repository saves.
- JPA optimistic locking and versioning prevent lost updates during concurrent stock adjustments.

### 2.4 API Error & Exception Standardization
- All Grower endpoints use `ApiErrorDetails` and `ApiResponse` envelopes.
- Unauthorized access, cross-grower IDOR, or illegal status transitions throw deterministic runtime exceptions (`AccessDeniedException`, `NoSuchElementException`, `IllegalArgumentException`) handled by global `@RestControllerAdvice`.
