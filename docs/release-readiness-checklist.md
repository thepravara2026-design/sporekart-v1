# SPOREKART v3.0 — RELEASE READINESS CHECKLIST

## MASTER RELEASE SIGN-OFF MATRIX

### 1. CODE & BUILD REPRODUCIBILITY
- [x] **No Debug Code**: Verbose debug statements, `System.out.println`, and test shortcuts removed from production classes.
- [x] **Deterministic Build**: Clean checkout builds identically with `mvn clean verify` and `npm run build`.
- [x] **Dependency Lock**: Frontend dependencies locked in `package-lock.json`; backend dependencies locked in `pom.xml`.
- [x] **Java 21 Alignment**: Backend targets Java 21 LTS (`<java.version>21</java.version>`).

### 2. SECURITY & SECRETS MANAGEMENT
- [x] **Zero Hardcoded Secrets**: All passwords, API keys, JWT secrets, and provider credentials externalized via environment variables.
- [x] **Secret Scanner Audit**: `.env.example` verified with placeholders only; repository & git history clean.
- [x] **Startup Fail-Fast Validator**: `ProductionConfigurationValidator` verified for `prod` and `staging` profiles.
- [x] **CORS Restricted**: Production CORS restricted to explicit origin configuration.
- [x] **Actuator Hardened**: Management endpoints restricted to `/actuator/health`, `/actuator/metrics`, and `/actuator/prometheus`.
- [x] **Admin RBAC Guard**: `/api/v1/admin/**` protected by mandatory `ROLE_ADMIN` authentication.

### 3. DATABASE & MIGRATIONS
- [x] **Flyway Migration Audit**: 19 Flyway migrations verified forward-compatible.
- [x] **Index Hardening**: Database indexes applied for audit events, refresh tokens, idempotency, and SKU lookups.
- [x] **Backup Procedure**: Automated database snapshot and restore runbook published.

### 4. CONTAINER & INFRASTRUCTURE
- [x] **Non-Root Execution**: Backend container running under unprivileged `sporekart` user.
- [x] **JVM Memory Limits**: JVM memory tuned via `-XX:MaxRAMPercentage=75.0`.
- [x] **Health Probes**: Liveness and readiness probes verified.
- [x] **Graceful Shutdown**: Spring Boot graceful shutdown enabled (`server.shutdown=graceful`).

### 5. TESTING & VERIFICATION
- [x] **Full Regression Baseline**: 292 / 292 backend integration and unit tests passing 100% clean.
- [x] **Frontend Build Verification**: `npm run build` succeeds cleanly.
- [x] **Smoke Test Matrix**: Production smoke test script published and verified.

---

## RELEASE APPROVAL SIGN-OFF

- **Principal Platform Engineer**: APPROVED (Sprint 5E Complete)
- **Application Security Engineer**: APPROVED (Zero Secrets Tracked, Validation Active)
- **Production SRE Engineer**: APPROVED (Container Hardening & Runbooks Complete)
