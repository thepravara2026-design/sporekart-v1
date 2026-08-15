# SPOREKART v3.0 — SPRINT 5E PRODUCTION READINESS GAP ANALYSIS

## Operational & Infrastructure Gap Matrix

| Area | Current State | Risk | Required Action | Status |
|:---|:---|:---|:---|:---|
| **Config** | Externalized placeholders in `application.yml` and `application-prod.yml`. Defaults provided. | MEDIUM: Default values could leak into production environment if unassigned. | Implement `ProductionConfigurationValidator` to fail fast on startup if production secrets rely on dev fallbacks. | COMPLETED |
| **Secrets** | `.env.example` created with placeholders. Secrets passed via env vars. | LOW: Accidental inclusion of real credentials in repository. | Scan repository & git history; enforce `.env.example` placeholder-only rules. | COMPLETED |
| **Docker** | Multi-stage Dockerfile present for backend and frontend. | LOW: Container running with default user or unconstrained memory. | Harden Dockerfiles with non-root user `sporekart`, JVM `-XX:MaxRAMPercentage=75.0`, and readiness health check probes. | COMPLETED |
| **Database** | Flyway migrations V1–V19 forward-only schema management. | LOW: Schema divergence or unvalidated migrations during deployment. | Validate all 19 Flyway migrations on startup; document restore-from-backup procedures for failed migrations. | COMPLETED |
| **Health** | `/actuator/health/liveness` and `/actuator/health/readiness` enabled in Spring Boot Actuator. | LOW: Sensitive health details exposed to unauthorized callers. | Restrict `show-details: when_authorized` in production profile while exposing liveness/readiness probes to container engine. | COMPLETED |
| **Logging** | LogRedactor in place for passwords, JWTs, card CVVs. Correlated with requestId and traceId. | LOW: Log volume spikes or debug noise during provider outages. | Set production root logging to `WARN` and application logging to `INFO`. Bounded stack trace logging. | COMPLETED |
| **Metrics** | Micrometer + `/actuator/prometheus` endpoint enabled. | LOW: Public exposure of internal application metrics. | Restrict Actuator web exposure to `health,info,metrics,prometheus` and protect management endpoints via network policy. | COMPLETED |
| **Security** | Spring Security JWT authentication, RBAC, input sanitization, rate limiting (Sprint 5A–5C). | LOW: Debug controllers or unauthenticated endpoints exposed. | Audit all controllers and verify customer to admin authorization restrictions. | COMPLETED |
| **CORS** | Configurable `CORS_ALLOWED_ORIGINS` via environment variable. | MEDIUM: Wildcard `*` or localhost origins leaking into production. | Restrict CORS in production to explicit domain origins; reject wildcards with credentials. | COMPLETED |
| **TLS** | Handled via reverse proxy (Nginx / ALB) termination. | LOW: Missing forwarded headers or unsecure cookie attributes. | Configure Nginx reverse proxy headers (`X-Forwarded-For`, `X-Forwarded-Proto`, `X-Forwarded-Host`). | COMPLETED |
| **Backup** | PostgreSQL WAL & scheduled full daily backups. | LOW: Unverified restore process causing extended downtime. | Document exact automated backup schedule and database restore runbook. | COMPLETED |
| **Recovery** | Cold standby / database restore procedures. | LOW: Out-of-sync payment or shipment reconciliation following disaster recovery. | Document RPO (15 min) and RTO (1 hr) targets and post-recovery reconciliation runbook. | COMPLETED |
| **CI/CD** | GitHub Actions workflows (`backend-ci.yml`, `frontend-ci.yml`, `quality.yml`). | LOW: Unvalidated builds deploying to production without smoke tests. | Configure automated build, unit test, integration test, lint, and security quality gates. | COMPLETED |
| **Rollback** | Container image rollback to prior tag (`sporekart-backend:<commit>`). | LOW: Incomplete application rollback leaving database in inconsistent state. | Document container tag rollback and forward-fix Flyway migration recovery steps. | COMPLETED |
| **Monitoring** | Health, Micrometer metrics, and Prometheus exporter. | LOW: Silent failures without operational alerting. | Define alert conditions for 5xx spikes, DB connection exhaustion, and reconciliation backlogs. | COMPLETED |
