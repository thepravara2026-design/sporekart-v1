# SPOREKART v3.0 — Production Environment Matrix

**Date**: 2026-08-15
**Target Environment**: Production (Spring Profile: `prod`)

---

## 1. Environment Variable Inventory

> **SECURITY MANDATE**: Never commit actual secret values. All secrets must be injected at runtime via secret managers (AWS Secrets Manager, GCP Secret Manager, Vault) or secure environment variables.

| Variable Name | Purpose | Required? | Secret? | Default in Code | Validation Method |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | YES | NO | `dev` | Startup check (must be `prod`) |
| `PORT` | Server HTTP port | NO | NO | `8080` | Port binding |
| `DATABASE_URL` | PostgreSQL JDBC connection URL | YES | NO | `jdbc:postgresql://localhost:5432/sporekart_prod` | JDBC connection test |
| `DATABASE_USERNAME` | PostgreSQL database user | YES | YES | None | DB Auth test |
| `DATABASE_PASSWORD` | PostgreSQL database password | YES | YES | None | DB Auth test |
| `DB_POOL_SIZE` | HikariCP maximum pool size | NO | NO | `20` | Hikari MBean |
| `RAZORPAY_KEY_ID` | Razorpay API key ID | YES (in Prod) | NO | `rzp_test_mock` | Gateway initialization |
| `RAZORPAY_KEY_SECRET` | Razorpay API key secret | YES (in Prod) | YES | `mock_secret` | Webhook signature test |
| `SHIPROCKET_API_EMAIL` | Shiprocket account email | YES (in Prod) | NO | `mock@shiprocket` | Auth token request |
| `SHIPROCKET_API_PASSWORD` | Shiprocket account password | YES (in Prod) | YES | `mock_password` | Auth token request |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins (comma separated) | YES | NO | `http://localhost:5173` | Spring Security CORS check |
| `RATE_LIMIT_ENABLED` | Enable rate limiting filter | NO | NO | `true` | Filter execution |
| `RATE_LIMIT_RPM` | Requests per minute per IP | NO | NO | `120` | Filter evaluation |

---

## 2. Public vs. Secret Classification Matrix

- **Public / Server Configuration**: `PORT`, `SPRING_PROFILES_ACTIVE`, `DB_POOL_SIZE`, `CORS_ALLOWED_ORIGINS`, `RATE_LIMIT_ENABLED`, `RATE_LIMIT_RPM`, `RAZORPAY_KEY_ID`, `SHIPROCKET_API_EMAIL`.
- **Private Server-Only Secrets (NEVER expose to frontend)**: `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `RAZORPAY_KEY_SECRET`, `SHIPROCKET_API_PASSWORD`.