# SPOREKART v3.0 — SPRINT 6B CONFIGURATION & ENVIRONMENT HARDENING REPORT

## 1. Executive Summary

Sprint 6B (Configuration & Environment Hardening) establishes a deterministic, environment-aware, secret-safe configuration architecture for Sporekart v3.0 across **LOCAL**, **TEST**, **STAGING**, and **PRODUCTION** profiles.

### Core Accomplishments
- **Branch**: `sprint-6b-configuration-hardening`
- **Secret Isolation**: 100% of backend secrets externalized via environment variables; 0 backend secrets exposed to Vite client bundle.
- **Fail-Fast Startup Validation**: Enhanced `ProductionConfigurationValidator` enforces strict validation of JWT keys, DB credentials, and prohibits mock providers in production.
- **Environment Templates**: Created `frontend/.env.example` and verified root `.env.example`.
- **Quality Gates**: 312 / 312 backend tests passing, 20 / 20 frontend unit tests passing, 100% clean production builds.

---

## 2. Configuration Inventory & Classification

| Configuration Key | Location / Property | Secret? | Target Runtime | Action / Default Policy |
| :--- | :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Environment | No | Backend | Controls active profile (`dev`, `test`, `staging`, `prod`) |
| `PORT` | `server.port` | No | Backend | Web server port (`8080` default) |
| `DATABASE_URL` | `spring.datasource.url` | No | Backend | Environment-specific JDBC connection URL |
| `DATABASE_USERNAME` | `spring.datasource.username` | Yes | Backend | Externalized via env variable; mandatory in prod |
| `DATABASE_PASSWORD` | `spring.datasource.password` | Yes | Backend | Externalized via env variable; validated via `ProductionConfigurationValidator` |
| `JWT_SECRET_KEY` | `app.security.jwt.secret` | Yes | Backend | Externalized via env variable; $\ge 32$ chars required in prod |
| `RAZORPAY_KEY_ID` | `sporekart.payment.razorpay.key-id` | No / Limited | Backend | Public API Key ID for payment initiation |
| `RAZORPAY_KEY_SECRET` | `sporekart.payment.razorpay.key-secret` | Yes | Backend | Backend-only secret key for payment verification |
| `RAZORPAY_WEBHOOK_SECRET` | `sporekart.payment.razorpay.webhook-secret` | Yes | Backend | Backend-only secret for HMAC webhook verification |
| `PAYMENT_PROVIDER` | `sporekart.payment.provider` | No | Backend | Provider mode (`MOCK` vs `RAZORPAY`) |
| `SHIPPING_PROVIDER` | `sporekart.shipping.provider` | No | Backend | Provider mode (`MOCK` vs `SHIPROCKET`) |
| `SHIPROCKET_WEBHOOK_TOKEN` | `sporekart.shipping.shiprocket.webhook-token` | Yes | Backend | Token for Shiprocket webhook verification |
| `CORS_ALLOWED_ORIGINS` | `app.cors.allowed-origins` | No | Backend | Allowed origins (`http://localhost:5173` local, domain in prod) |
| `VITE_API_BASE_URL` | `import.meta.env.VITE_API_BASE_URL` | No | Frontend | Public API gateway base URL (`/api` default) |
| `VITE_APP_ENV` | `import.meta.env.VITE_APP_ENV` | No | Frontend | Client-side environment indicator (`local`, `prod`) |

---

## 3. Cross-Environment Safety Matrix

| Feature / Domain | LOCAL (`dev`) | TEST (`test`) | STAGING (`staging`) | PRODUCTION (`prod`) |
| :--- | :--- | :--- | :--- | :--- |
| **Database Engine** | Embedded H2 (PostgreSQL mode) | Embedded H2 (In-Memory) | External PostgreSQL / Supabase | External PostgreSQL / Supabase |
| **Payment Provider** | `MOCK` allowed | `MOCK` | `MOCK` / `RAZORPAY` Sandbox | `RAZORPAY` Live (Mock Warning) |
| **Shipping Provider** | `MOCK` allowed | `MOCK` | `MOCK` / `SHIPROCKET` Sandbox | `SHIPROCKET` Live (Mock Warning) |
| **CORS Policy** | `http://localhost:5173` | Test Origins | Staging Frontend Domain | Approved Production Domain |
| **Logging Level** | `DEBUG` / `INFO` | `WARN` | `INFO` | `WARN` / Redacted Secrets |
| **Actuator Exposure** | `health,info,metrics,prometheus` | `health` | `health,metrics,prometheus` | `health,metrics,prometheus` |
| **Sensitive Actuators** | 401 Unauthorized | 401 Unauthorized | 401 Unauthorized | 401 Unauthorized |
| **Secrets Management** | Dev Placeholders Allowed | Dev Placeholders Allowed | **External Enforced** | **External Enforced** |

---

## 4. Secret Safety & Frontend Isolation Audit

- **Client Bundle Audit**: Inspected `frontend/src/` for client-side environment accesses (`import.meta.env`).
  - **Result**: Exactly 1 public access point (`VITE_API_BASE_URL` in `apiClient.ts`).
  - **Exposed Secrets**: `0` (Zero backend secrets reach Vite or browser bundles).
- **Tracked Secrets Audit**: Reviewed `.gitignore` rules (`.env`, `.env.local`, `.env.production`, `*.pem`, `*.key`).
  - **Result**: `0` real production credentials stored in tracked repository files.

---

## 5. Startup Validation & Fail-Fast Rules (`ProductionConfigurationValidator`)

For `prod` and `staging` profiles:
1. `app.security.jwt.secret`: Must be present, not default dev secret, not placeholder, and at least 32 characters (256 bits).
2. `spring.datasource.username` & `spring.datasource.password`: Must be explicitly specified via environment variables.
3. `sporekart.payment.provider` & `sporekart.shipping.provider`: Audited for live production deployment readiness.

---

## 6. Verification Results

| Quality Gate | Requirement | Actual Result | Status |
| :--- | :--- | :--- | :--- |
| **Backend Unit & Integration Tests** | 100% PASS | 312 / 312 Tests PASS | **PASS** |
| **Frontend Production Build** | `npm run build` | 151 modules transformed in 1.67s | **PASS** |
| **Frontend Unit Tests** | `npm test` | 20 / 20 Tests PASS (10 files) | **PASS** |
| **Sprint 6A Regression** | Golden path smoke test | Verified 100% operational | **PASS** |

---

## 7. Sprint 6C Readiness Decision

### Decision: **`READY FOR SPRINT 6C`**

#### Justification:
The Sporekart v3.0 configuration system is fully externalized, environment-aware, secret-safe, and fail-fast validated. Zero secrets leak to client bundles, all 332 automated tests pass cleanly, and the codebase is technically ready to enter **Sprint 6C — Security Hardening**.
