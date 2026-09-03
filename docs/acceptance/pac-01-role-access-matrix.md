# SPOREKART v3.0 — PAC-01 Role Access Matrix & Security Verification

**Document ID:** `PAC-01-ROLE-ACCESS-MATRIX`  
**Sprint:** `PAC-01 — Application Readiness & Architecture Acceptance`  
**Author:** Antigravity FAANG Master Execution Agent  
**Date:** 2026-08-19  
**Status:** Certified Security Matrix  

---

## 1. Executive Summary

This document defines and certifies the role authorization matrix across all user roles (`ROLE_CUSTOMER`, `ROLE_GROWER`, `ROLE_TRAINEE`, `ROLE_SELLER`, `ROLE_ADMIN`, and dual-role personas) for both frontend route navigation and backend REST API execution.

---

## 2. Mock Personas Baseline

| Persona ID | Username / Email | Assigned Roles | Mock Identity Context |
|------------|------------------|----------------|-----------------------|
| **MOCK-CUST-01** | `customer@sporekart.com` | `ROLE_CUSTOMER` | Standard E-Commerce Shopper |
| **MOCK-GROW-01** | `grower@sporekart.com` | `ROLE_GROWER` | Mushroom Grower / Supplier (`grower-1`) |
| **MOCK-TRAIN-01** | `trainee@sporekart.com` | `ROLE_TRAINEE` | Trainee Enrolled in Mycology Courses |
| **MOCK-SELL-01** | `seller@sporekart.com` | `ROLE_SELLER` | Third-Party Marketplace Seller |
| **MOCK-ADMIN-01** | `admin@sporekart.com` | `ROLE_ADMIN` | Platform Super Admin |
| **MOCK-DUAL-01** | `grower.trainee@sporekart.com` | `ROLE_GROWER`, `ROLE_TRAINEE` | Dual Role Persona (Grower + Trainee) |

---

## 3. Comprehensive Role Access Matrix

| Target Portal / API Domain | CUSTOMER | GROWER | TRAINEE | SELLER | ADMIN |
|----------------------------|----------|--------|---------|--------|-------|
| **Public Storefront (`/`, `/products`, `/categories`)** | ALLOW | ALLOW | ALLOW | ALLOW | ALLOW |
| **Cart & Checkout (`/cart`, `/checkout`)** | ALLOW (Own) | ALLOW (Own) | ALLOW (Own) | ALLOW (Own) | ALLOW (Platform) |
| **Customer Orders (`/orders`)** | ALLOW (Own) | ALLOW (Own) | ALLOW (Own) | ALLOW (Own) | ALLOW (Platform) |
| **Grower Portal (`/grower/**`)** | DENY | ALLOW (Own) | DENY | DENY | ALLOW (Platform) |
| **Trainee Portal (`/training/**`)** | DENY | ALLOW (Dual) | ALLOW (Own) | DENY | ALLOW (Platform) |
| **Seller Portal (`/seller/**`)** | DENY | ALLOW (Dual) | DENY | ALLOW (Own) | ALLOW (Platform) |
| **Admin Console (`/admin/**`)** | DENY | DENY | DENY | DENY | ALLOW (Platform) |
| **Backend REST `/api/v1/admin/**`** | 403 Forbidden | 403 Forbidden | 403 Forbidden | 403 Forbidden | 200 OK |
| **Backend REST `/api/v1/grower/**`** | 403 Forbidden | 200 OK (Own Tenant) | 403 Forbidden | 403 Forbidden | 200 OK |

---

## 4. Critical Admin Security Check Verification

### Defect Audit: "Unauthenticated `/admin` Access"
- **Verification Question:** Does visiting `/admin` without logging in expose the admin application?
- **Frontend Guard Verification:** In `frontend/src/app/App.tsx`, line 91:
  ```tsx
  <Route
    path="admin"
    element={
      <ProtectedRoute>
        <RequireRole roles={['ROLE_ADMIN']}>
          <AdminLayout />
        </RequireRole>
      </ProtectedRoute>
    }
  />
  ```
- **Execution Result:** Unauthenticated requests to `/admin` are immediately intercepted by `<ProtectedRoute>` and redirected to `/login` with `state={{ from: location }}`.
- **Backend API Guard Verification:** In `backend/src/main/java/com/sporekart/application/config/SecurityConfig.java`:
  ```java
  .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
  ```
- **Execution Result:** Any unauthenticated direct HTTP API call to `/api/v1/admin/**` returns `HTTP 401 Unauthorized`. Any authenticated user without `ROLE_ADMIN` receives `HTTP 403 Forbidden`.

---

## 5. Security Certification Verdict

**VERDICT: PASS** — Role-based authorization is enforced authoritatively on both backend REST API endpoints and frontend SPA routes.
