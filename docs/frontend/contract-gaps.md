# FD-02 — Contract Gaps & Resolution Log

**Sprint:** FD-02  
**Repository:** `f:/sporekart-v3.0`  
**Date:** 2026-08-18  
**Status:** Resolved / Audited  

---

## 1. Executive Summary

This document records every contract mismatch, payload ambiguity, missing interface, header discrepancy, and pagination inconsistency discovered during **FD-02 (Frontend ↔ Backend Contract Alignment)**, along with its resolution status, risk level, and verification method.

---

## 2. Master Contract Gap Inventory & Resolution Log

| Issue ID | Classification | Contract Area | Description & Evidence | Resolution / Action Taken | Verification Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **GAP-01** | **P1 (High)** | Pagination Contract | [`batchApi.ts`](file:///f:/sporekart-v3.0/frontend/src/features/admin/api/batchApi.ts#L64) and [`trainingProgramApi.ts`](file:///f:/sporekart-v3.0/frontend/src/features/admin/api/trainingProgramApi.ts#L37) defined `PageResponse` with `number: number` instead of Spring Data `page: number`, `first: boolean`, `last: boolean`. | Aligned `batchApi.ts` and `trainingProgramApi.ts` to import central `PageResponse<T>` from `src/types/api.ts`. | **RESOLVED & VERIFIED** |
| **GAP-02** | **P1 (High)** | Auth Interceptor | Frontend API calls injected manual headers (`X-Customer-Id: CUST-001`) without supporting standard OAuth 2.0 Bearer tokens in Axios interceptors. | Updated [`apiClient.ts`](file:///f:/sporekart-v3.0/frontend/src/services/apiClient.ts) to automatically extract token from `localStorage` (`accessToken` or `token`) and inject `Authorization: Bearer <token>`. | **RESOLVED & VERIFIED** |
| **GAP-03** | **P1 (High)** | Missing Auth / Cart Services | Backend `AuthController`, `CartController`, and `CheckoutController` existed on Spring Boot backend but lacked frontend service wrappers. | Created [`authApi.ts`](file:///f:/sporekart-v3.0/frontend/src/services/authApi.ts) and [`cartApi.ts`](file:///f:/sporekart-v3.0/frontend/src/services/cartApi.ts) with full DTO typings and endpoint mappings. | **RESOLVED & VERIFIED** |
| **GAP-04** | **P2 (Medium)**| Error Details Schema | Backend `ApiErrorDetails.java` returns `requestId` in MDC error payloads, but frontend `ApiErrorDetails` in [`types/api.ts`](file:///f:/sporekart-v3.0/frontend/src/types/api.ts) lacked `requestId`. | Added `requestId?: string;` to `ApiErrorDetails` in `types/api.ts` and `ApiError.ts`. | **RESOLVED & VERIFIED** |
| **GAP-05** | **P2 (Medium)**| Endpoint Registry | Master [`endpoints.ts`](file:///f:/sporekart-v3.0/frontend/src/services/endpoints.ts) registry was missing Auth, Cart, Checkout, and Admin Training routes. | Expanded `ENDPOINTS` registry in `endpoints.ts` to cover all active backend REST endpoints. | **RESOLVED & VERIFIED** |
| **GAP-06** | **P2 (Medium)**| Runtime Validation | Raw Axios responses were cast to TypeScript interfaces without runtime schema validation. | Installed `zod` (`v3.24.2`) and created [`contractSchemas.ts`](file:///f:/sporekart-v3.0/frontend/src/types/schemas/contractSchemas.ts) for runtime verification of `ApiResponse`, `PageResponse`, `Product`, `AuthTokenResponse`, `Cart`, and `CheckoutPreview`. | **RESOLVED & VERIFIED** |
| **GAP-07** | **P3 (Low)** | Contract Tests | Frontend test suite lacked automated validation of Zod contract schemas and API error response handling. | Created [`contractSchemas.test.ts`](file:///f:/sporekart-v3.0/frontend/src/services/__tests__/contractSchemas.test.ts) unit test suite. | **RESOLVED & VERIFIED** |

---

## 3. Residual Non-Blocking Ambiguities & Future Phase Recommendations

1. **JWT Session Restoration:** While [`authApi.ts`](file:///f:/sporekart-v3.0/frontend/src/services/authApi.ts) and `apiClient.ts` now support Bearer tokens, React Context session management and automatic token refresh loops will be formally integrated into the UI shell during **FD-18 (Permission-Based UI & Security UX)**.
2. **Offline Mock Data Cleanup:** Unused mock fixtures in test suites should be progressively replaced with Zod-validated DTO fixtures during feature implementation sprints (FD-07 through FD-17).
