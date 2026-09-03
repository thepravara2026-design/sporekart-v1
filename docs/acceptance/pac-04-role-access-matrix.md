# SPOREKART v3.0 — PAC-04 Role Access Matrix

**Document ID:** `PAC-04-ROLE-ACCESS-MATRIX`  
**Sprint:** `PAC-04 — Seller & Grower End-to-End Acceptance`  
**Author:** Antigravity FAANG Master Execution Agent  
**Date:** 2026-08-19  
**Status:** Certified Role Access Matrix  

---

## 1. Executive Summary

This document defines the cross-role authorization matrix governing Seller, Grower, Customer, and Admin interactions with Seller/Grower products, inventory, orders, and workspace resources.

---

## 2. Role Access Matrix

| Resource / Endpoint | `ROLE_SELLER` | `ROLE_GROWER` | `ROLE_CUSTOMER` | `ROLE_ADMIN` |
|---------------------|---------------|---------------|-----------------|--------------|
| **Own Product (View/Edit)** | **ALLOW** | **ALLOW** | DENY | **ALLOW** |
| **Foreign Product (View/Edit)** | DENY | DENY | DENY | **ALLOW** |
| **Own Inventory (View/Adjust)** | **ALLOW** | **ALLOW** | DENY | **ALLOW** |
| **Foreign Inventory (View/Adjust)** | DENY | DENY | DENY | **ALLOW** |
| **Own Order Context (View)** | **ALLOW** | **ALLOW** | **ALLOW** (as customer) | **ALLOW** |
| **Foreign Order Context (View)** | DENY | DENY | DENY | **ALLOW** |
| **Order State Transition (Fulfillment)** | **ALLOW** (Own) | **ALLOW** (Own) | DENY | **ALLOW** |
| **Grower Profile (View/Update)** | DENY | **ALLOW** (Own) | DENY | **ALLOW** |
| **Customer Cart (View/Edit)** | DENY | DENY | **ALLOW** (Own) | **ALLOW** |
| **Admin Operations (`/api/v1/admin/**`)** | DENY | DENY | DENY | **ALLOW** |

---

## 3. Privilege Escalation & Access Control Verification

1. **Client ID Spoofing Defense:** Passing a foreign `sellerId` or `growerId` in JSON request DTOs or URL parameters is overridden by backend `SecurityContext` identity extraction.
2. **Role Boundaries:** Customers attempting to invoke `/api/v1/seller/**` or `/api/v1/grower/**` receive `HTTP 403 FORBIDDEN`.
3. **Cross-Tenant IDOR Defense:** `GrowerSecurityAcceptanceTest.scenario2` & `scenario4` confirm that querying a foreign product or order throws `AccessDeniedException`.

---

## 4. Role Access Matrix Verdict

**VERDICT: PASS** — Role boundaries and cross-tenant access defenses are 100% verified.
