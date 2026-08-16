# Production Deployment Runbook — SPOREKART v3.0

This runbook defines the repeatable, traceable, operator-executable production deployment execution path for SPOREKART v3.0.

---

## 1. Production Topology & Deployment Target

```
                  INTERNET
                     |
                     v
              HTTPS / TLS
                     |
         +-----------+-----------+
         |                       |
         v                       v
    FRONTEND                 API GATEWAY
    (Vite / React)          (Spring Boot)
                                 |
         +-----------------------+-----------------------+
         |                       |                       |
         v                       v                       v
     PostgreSQL              Razorpay                Shiprocket
     / Supabase              Provider                Provider
         |
         v
      Flyway

Monitoring & Observability:
    Prometheus (/actuator/prometheus) -> Grafana Dashboards
    SLF4J MDC Logging (correlationId, requestId, traceId)
```

---

## 2. Deployment Contract & Environment Variables

| Variable | Type | Category | Description |
| :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | System | REQUIRED | Must be set to `prod` |
| `DATABASE_URL` | Config | REQUIRED | `jdbc:postgresql://<HOST>:<PORT>/<DB_NAME>?sslmode=require` |
| `DATABASE_USERNAME` | Secret | REQUIRED | Managed via Vault / Cloud Secret Manager |
| `DATABASE_PASSWORD` | Secret | REQUIRED | Managed via Vault / Cloud Secret Manager |
| `JWT_SECRET_KEY` | Secret | REQUIRED | Min 512-bit HMAC-SHA512 signing key |
| `CORS_ALLOWED_ORIGINS` | Config | REQUIRED | Production domain (`https://sporekart.com`) |
| `PAYMENT_PROVIDER_KEY` | Secret | REQUIRED | Razorpay Production Key ID |
| `PAYMENT_PROVIDER_SECRET`| Secret | REQUIRED | Razorpay Production Key Secret |
| `SHIPPING_PROVIDER_KEY` | Secret | REQUIRED | Shiprocket Production API Key |
| `SHIPPING_PROVIDER_SECRET`| Secret | REQUIRED | Shiprocket Production API Secret |
| `VITE_API_BASE_URL` | Public | REQUIRED | Public API Gateway URL (`https://api.sporekart.com`) |

---

## 3. Mandatory Production Deployment Execution Order

```
1. Validate Release & Build Artifacts (JAR & static assets)
2. Preflight Environment & Secret Verification
3. Verify Target PostgreSQL / Supabase Database Availability
4. Execute Flyway Migrations (V1 through V23) & Schema Validation
5. Start Spring Boot Backend (`SPRING_PROFILES_ACTIVE=prod`)
6. Verify Health (`/actuator/health`) & Readiness (`/actuator/health/readiness`) Probes
7. Deploy Frontend Static Assets to Nginx / CDN
8. Verify Observability Metrics (`/actuator/prometheus`)
9. Execute Production Release Smoke Test
10. Final Release Acceptance Sign-Off
```

---

## 4. Execution Commands

### Step 1: Preflight Verification
```bash
# Verify Git release commit
git status
git log -1 --oneline

# Run backend pre-deployment tests
cd backend
mvn test -Dtest=ProductionDeploymentTestSuite
```

### Step 2: Database Migration Execution
```bash
# Flyway executes automatically on backend container startup when configured
java -jar -Dspring.profiles.active=prod \
          -Dspring.flyway.enabled=true \
          -Dspring.jpa.hibernate.ddl-auto=validate \
          target/sporekart-backend-0.1.0-SNAPSHOT.jar
```

### Step 3: Readiness Probe & Health Check
```bash
# Check process readiness
curl -f https://api.sporekart.com/actuator/health/readiness

# Verify telemetry scrape endpoint
curl -f https://api.sporekart.com/actuator/prometheus
```

---

## 5. Emergency Rollback & Failure Recovery Procedure

If post-deployment readiness check fails or critical 5xx errors spike:
1. **Stop Release Container:** Terminate active release instance (`docker stop sporekart-backend-prod`).
2. **Revert Artifact:** Launch previous verified container SHA (`sporekart-backend:<PREVIOUS_STABLE_SHA>`).
3. **Verify Readiness:** Confirm `/actuator/health/readiness` returns HTTP 200 `UP`.
4. **Log Analysis:** Query centralized logs using `X-Correlation-ID` to isolate root cause.
5. **Database Safety Note:** Database migrations (Flyway V1-V23) are additive and backward-compatible. Do NOT attempt destructive Flyway migration rollbacks manually.
