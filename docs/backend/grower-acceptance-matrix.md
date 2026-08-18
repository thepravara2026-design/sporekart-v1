# SPOREKART v3.0 — GB-02 ACCEPTANCE MATRIX

**Sprint ID:** GB-02  
**Sprint Name:** Grower API Integration, Contract & End-to-End Hardening  
**Date:** August 18, 2026  
**Status:** ALL CRITERIA PASSED (24 / 24)  

---

| # | Requirement / Criterion | API / Component | Security Rule / Mechanism | Verification Test | Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | Grower API Audit | `/api/v1/grower/*` | All 15 Grower endpoints mapped & verified | `GrowerContractIntegrationTest` | PASS | COMPLETE |
| 2 | Contract Match | Frontend `growerApi.ts` | Aligned to `/api/v1/grower/*` | Vitest `GrowerComponents.test.tsx` | PASS | COMPLETE |
| 3 | Server Identity | `@AuthenticationPrincipal` | `resolveUserId` enforces principal identity | `scenario5_server_side_identity` | PASS | COMPLETE |
| 4 | No Client growerId | Request DTOs | Request body/query `growerId` ignored | `scenario5_server_side_identity` | PASS | COMPLETE |
| 5 | Cross-Grower Product Isolation | `/api/v1/grower/products/{id}` | Grower A blocked from Grower B product | `scenario2_growerA_accesses_growerB_product` | PASS | COMPLETE |
| 6 | Cross-Grower Inventory Isolation | `/api/v1/grower/inventory/{sku}` | Grower A blocked from Grower B inventory | `scenario3_growerA_accesses_growerB_inventory` | PASS | COMPLETE |
| 7 | Cross-Grower Order Isolation | `/api/v1/grower/orders/{id}` | Grower A blocked from Grower B order | `scenario4_growerA_accesses_growerB_order` | PASS | COMPLETE |
| 8 | Cross-Grower Shipment Isolation | `/api/v1/grower/shipments` | `findAllByGrowerId` scopes shipments | `GrowerSecurityAcceptanceTest` | PASS | COMPLETE |
| 9 | Cross-Grower Reporting Isolation | `/api/v1/grower/reports/summary` | Report aggregates strictly on authenticated `growerId` | `scenario6_reporting_data_isolation` | PASS | COMPLETE |
| 10 | IDOR Attempt Blocking | Product/Inventory/Order APIs | AccessDeniedException returned on IDOR | `GrowerSecurityAcceptanceTest` | PASS | COMPLETE |
| 11 | Vertical Privilege Escalation | `/api/v1/admin/*` | `ROLE_GROWER` blocked from admin APIs | `testRoleGrowerCannotAccessAdminUsers` | PASS | COMPLETE |
| 12 | Dual Role Compatibility | `ROLE_TRAINEE` + `ROLE_GROWER` | Combined authorities coexist without collision | `testDualRoleCanAccessGrowerProfile` | PASS | COMPLETE |
| 13 | Inventory Concurrency | `/inventory/{sku}/adjustments` | Version check & `adjustOnHand` validation | `scenario7_stock_adjustment_consistency` | PASS | COMPLETE |
| 14 | Order State Transitions | `/orders/{id}/process` etc. | Domain state machine & transition rules | `scenario8_invalid_order_transition_throws` | PASS | COMPLETE |
| 15 | Request Validation | Request DTOs | Server-side validation on DTO fields | `GrowerSecurityAcceptanceTest` | PASS | COMPLETE |
| 16 | Error Contract Format | `ApiError` / `ApiResponse` | Standardized JSON payload responses | `GrowerContractIntegrationTest` | PASS | COMPLETE |
| 17 | Database Scoping Indexes | `idx_*_grower_id` | Flyway V42 indexes for fast filtering | `V42__grower_backend_foundation.sql` | PASS | COMPLETE |
| 18 | No Unscoped Queries | Repositories | `findAllByGrowerId` used exclusively | `GrowerApplicationService.java` | PASS | COMPLETE |
| 19 | Transaction Boundaries | `@Transactional` | Service methods transactional | `GrowerApplicationService.java` | PASS | COMPLETE |
| 20 | Frontend FD-14 Integration | `frontend/src/features/grower/` | React Query & API integration verified | Vitest 407/407 passed | PASS | COMPLETE |
| 21 | Full Backend Suite | `mvn clean test` | 797 unit & integration tests pass | `mvn clean test` (797 passed) | PASS | COMPLETE |
| 22 | Full Frontend Suite | `npm test -- --run` | 407 Vitest tests pass across 65 files | `npm test` (407 passed) | PASS | COMPLETE |
| 23 | TypeScript & Build | `npx tsc` & `npm run build` | 0 type errors, Vite build in 4.84s | `npm run build` (0 errors) | PASS | COMPLETE |
| 24 | Clean Working Tree | Git | `git status` clean after commit | `git status` | PASS | COMPLETE |
