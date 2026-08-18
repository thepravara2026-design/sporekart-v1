# SPOREKART v3.0 — GROWER SECURITY AUDIT & REMEDIATION REPORT

**Sprint:** GB-02
**Date:** August 18, 2026
**Security Status:** HARDENED & VERIFIED

---

## 1. Security Findings & Remediation

| Finding ID | Severity | Affected Component | Description | Remediation | Verification Test | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `SEC-GB02-001` | **HIGH** | `GrowerController.java` | `resolveUserId` fallback to `"grower-1"` on `principal == null` | Throws `AccessDeniedException` (HTTP 401/403) when principal is missing or invalid | `testUnauthenticatedAccessDenied` | **RESOLVED** |
| `SEC-GB02-002` | **MEDIUM** | `GrowerController.java` | Parameter type strictness `@AuthenticationPrincipal UserPrincipal` causing 500 when non-`UserPrincipal` present | Parameter updated to `@AuthenticationPrincipal Object principal` supporting `UserPrincipal`, `UserDetails`, and principal names | `GrowerContractIntegrationTest` (10/10 passed) | **RESOLVED** |
| `SEC-GB02-003` | **MEDIUM** | `GrowerApplicationService.java` | Unsupported order status transition returned unhandled order without error | Added `default -> throw new IllegalArgumentException("Unsupported order status transition to: " + newStatus)` | `scenario8_invalid_order_transition_throws` | **RESOLVED** |
| `SEC-GB02-004` | **LOW** | `GrowerApplicationService.java` | Report summary aggregation risk of un-scoped calculation | Verified `orderRepository.findAllByGrowerId(userId)` strictly scopes sales metrics to authenticated grower | `scenario6_reporting_data_isolation` | **RESOLVED** |

---

## 2. Authorization Security Matrix

- **Grower -> Own Resources**: ALLOWED (HTTP 200 OK)
- **Grower -> Other Grower Resources**: DENIED (HTTP 403 Forbidden / 404 Not Found)
- **Grower -> Admin APIs (`/api/v1/admin/*`)**: DENIED (HTTP 403 Forbidden)
- **Unauthenticated -> Grower APIs (`/api/v1/grower/*`)**: DENIED (HTTP 401 Unauthorized)
- **Trainee + Grower -> Grower APIs (`/api/v1/grower/*`)**: ALLOWED (HTTP 200 OK)
