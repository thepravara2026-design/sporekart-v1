# SPOREKART v3.0 — SPRINT 5F RELEASE ENGINEERING GAP ANALYSIS

## Release Engineering & Deployment Automation Gap Matrix

| Area | Sprint 5E Baseline | Risk / Operational Gap | Required Sprint 5F Action | Status |
|:---|:---|:---|:---|:---|
| **Build Pipeline** | Maven and npm commands documented in runbooks. | LOW: Manual invocation could lead to environment variation across dev machines. | Standardize `scripts/build.sh` executing `mvn clean verify` and `npm ci && npm run build` deterministically. | COMPLETED |
| **CI Quality Gates** | Separate workflow files (`backend-ci.yml`, `frontend-ci.yml`, `quality.yml`). | LOW: Disconnected workflows could allow image build without backend test validation. | Create unified `release-ci.yml` with strict zero-suppression quality gates covering build, test, Docker build, and manifest generation. | COMPLETED |
| **Artifact Traceability** | App version string in `application.yml`. | MEDIUM: Operators cannot inspect exact Git SHA or build timestamp of running release. | Implement `/api/v1/version` identity metadata and `generate-release-manifest.sh` producing `release-manifest.json`. | COMPLETED |
| **Migration Release Check** | Flyway migrations run on Spring Boot startup. | LOW: Deployment could proceed even if Flyway migration fails or table checksum diverges. | Create `scripts/verify-migrations.sh` to validate Flyway migration chain V1–V19 prior to container promotion. | COMPLETED |
| **Container Image Verification** | Multi-stage Dockerfiles and `docker-compose.prod.yml` created in 5E. | LOW: Containers not validated for non-root execution, readiness, or log redaction prior to deployment. | Create `scripts/verify-containers.sh` to perform runtime image validation and security checks. | COMPLETED |
| **Deployment Automation** | Manual deployment runbook steps documented in 5E. | MEDIUM: Manual steps risk operator error during production releases. | Create `scripts/deploy-release.sh` automating pre-flight checks, config validation, container rollout, and health polling. | COMPLETED |
| **Post-Deployment Verification** | Smoke test specification documented in 5E (`production-smoke-test.md`). | LOW: Smoke tests require manual cURL execution. | Create `scripts/post-deployment-verify.sh` to automatically run non-destructive smoke tests post-deploy. | COMPLETED |
| **Rollback Verification** | Container tag rollback and database restore procedures documented in 5E. | LOW: Rollback steps unverified by automated script. | Create `scripts/verify-rollback.sh` to execute controlled container tag rollback and health re-verification. | COMPLETED |
| **Master Release Gate** | Readiness checklist verified manually in 5E. | HIGH: Deployment might proceed without executing all 12 quality gate checks. | Create `scripts/release-gate.sh` executing 12-point release gate and outputting `RELEASE READY` or `RELEASE BLOCKED`. | COMPLETED |
