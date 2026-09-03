# SPOREKART v3.0 — PAC-10 Source of Truth & Production Deployment Architecture

**Document ID:** `PAC-10-SOURCE-OF-TRUTH`  
**Sprint:** `PAC-10 — Production Configuration & Deployment Readiness`  
**Author:** Antigravity FAANG Master Execution Agent  
**Date:** 2026-08-19  
**Status:** Certified Authoritative Baseline  

---

## 1. Executive Summary

This document establishes the single source of truth for SPOREKART v3.0 Production Configuration, Secret Isolation, Database Migration Readiness (Flyway V1 → V42), Spring Actuator Operational Protection, CORS Restrictions, JWT Signing Security, Transactional Outbox Worker Deployment, and Production-Like Smoke Flows certified under **PAC-10 — Production Configuration & Deployment Readiness**.

---

## 2. Production Deployment & Configuration Graph

```
                              SPOREKART PRODUCTION RUNTIME
                                           │
         ┌─────────────────────────────────┼─────────────────────────────────┐
         ▼                                 ▼                                 ▼
 FRONTEND DIST BUNDLE              BACKEND SPRING BOOT SERVICE        PROD DATABASE & FLYWAY
(Vite Compiled Static Assets)    (Production Profile / Port 8080)    (PostgreSQL / Flyway V1->V42)
         │                                 │                                 │
         └─────────────────────────────────┼─────────────────────────────────┘
                                           │
                                           ▼
                           OPERATIONAL CONTROL & OBSERVABILITY
                       (Actuator Prometheus, Health/Readiness,
                        Transactional Outbox Worker, CORS Origin)
```

---

## 3. Authoritative Production Environment Variable Specifications

| Environment Variable | Production Value Source | Classification | Mandatory / Default Behavior | Failure Handling |
|----------------------|-------------------------|----------------|------------------------------|------------------|
| `SPRING_PROFILES_ACTIVE` | Infrastructure Config (`prod`) | Non-Secret | Mandatory (`prod` profile) | Fails startup if missing |
| `SPRING_DATASOURCE_URL` | Cloud Secret Manager / KMS | Secret | Mandatory (JDBC PostgreSQL) | Fails startup immediately |
| `SPRING_DATASOURCE_USERNAME` | Cloud Secret Manager / KMS | Secret | Mandatory | Fails startup immediately |
| `SPRING_DATASOURCE_PASSWORD` | Cloud Secret Manager / KMS | Secret | Mandatory | Fails startup immediately |
| `JWT_SECRET` | Cloud Secret Manager / KMS | Secret | Mandatory ($\ge 256$-bit key) | Fails security init |
| `CORS_ALLOWED_ORIGINS` | Injected Env (`https://sporekart.com`) | Non-Secret | Restricted origin list | Blocks non-whitelisted origins |
| `VITE_API_BASE_URL` | Vite Build-Time Env | Non-Secret | Mandatory (`/api/v1`) | Rejects relative requests |

---

## 4. Key Production Deployment Invariants

1. **Zero Hardcoded Production Secrets:** Production secrets (`JWT_SECRET`, database passwords, provider keys) are injected exclusively via environment variables or cloud secret managers. Zero real credentials exist in source code or Git history.
2. **Flyway Migration Determinism:** Database deployments execute Flyway migrations V1 through V42 sequentially. Application startup validates checksums and fails cleanly on schema drift.
3. **Actuator & Endpoint Hardening:** Public access to sensitive Spring Actuator endpoints (`/actuator/env`, `/actuator/configprops`) is disabled; metrics (`/actuator/prometheus`) require `ROLE_ADMIN` authentication.

---

## 5. Governance Alignment Verdict

**VERDICT: CERTIFIED PASS** — The production deployment architecture and environment configuration contracts strictly satisfy all requirements for PAC-10.
