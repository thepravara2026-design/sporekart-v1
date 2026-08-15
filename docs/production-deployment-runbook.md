# SPOREKART v3.0 — PRODUCTION DEPLOYMENT RUNBOOK

## 1. PRE-DEPLOYMENT PREREQUISITES

Ensure the following operational prerequisites are met before deploying Sporekart v3.0:

1. **Target Environment**: Docker Engine 24+ and Docker Compose 2.20+ installed on target host.
2. **Database Provisioning**: PostgreSQL 16+ instance provisioned (Supabase or managed RDS/Cloud SQL) with database `sporekart_prod` created.
3. **Environment Secrets**: Secure `.env` file generated from `.env.example` containing strong production values:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DATABASE_URL=jdbc:postgresql://<DB_HOST>:5432/sporekart_prod`
   - `DATABASE_USERNAME=<DB_USER>`
   - `DATABASE_PASSWORD=<STRONG_RANDOM_PASSWORD>`
   - `JWT_SECRET_KEY=<MIN_64_CHAR_RANDOM_SECRET>`
   - `CORS_ALLOWED_ORIGINS=https://sporekart.com,https://admin.sporekart.com`
   - `PAYMENT_PROVIDER_KEY=<RAZORPAY_LIVE_KEY>`
   - `PAYMENT_PROVIDER_SECRET=<RAZORPAY_LIVE_SECRET>`
   - `PAYMENT_WEBHOOK_SECRET=<RAZORPAY_LIVE_WEBHOOK_SECRET>`

---

## 2. STEP-BY-STEP DEPLOYMENT PROCEDURE

### Step 1: Pre-Deployment Database Backup
Execute a manual database backup before running deployment migrations:
```bash
pg_dump -h <DB_HOST> -U <DB_USER> -d sporekart_prod -F c -b -v -f /backups/sporekart_prod_pre_deploy_$(date +%Y%m%d_%H%M%S).dump
```

### Step 2: Build Production Docker Images
```bash
# Build Backend Container Image
docker build -t sporekart-backend:3.0.0 -f infrastructure/docker/backend.Dockerfile backend

# Build Frontend Container Image
docker build -t sporekart-frontend:3.0.0 -f infrastructure/docker/frontend.Dockerfile frontend
```

### Step 3: Deploy Production Stack via Docker Compose
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Step 4: Verify Container Startup & Flyway Migrations
Monitor backend logs to confirm successful Flyway migration execution and startup:
```bash
docker logs -f sporekart-backend
```
Expected output:
```text
INFO  o.f.core.internal.command.DbMigrate : Successfully applied 19 migrations to schema "public", now at version v19
INFO  com.sporekart.SporekartApplication : Started SporekartApplication in 4.5 seconds
```

### Step 5: Verify Health Probes
```bash
# Liveness Probe
curl -f http://localhost:8080/actuator/health/liveness

# Readiness Probe
curl -f http://localhost:8080/actuator/health/readiness
```

---

## 3. POST-DEPLOYMENT SMOKE TEST & VERIFICATION

Execute the automated post-deployment smoke test script:
```bash
bash scripts/production-smoke-test.sh
```
Verify that:
- `/actuator/health/readiness` returns `{"status":"UP"}`
- `/api/v1/catalog/products` returns HTTP 200
- Protected `/api/v1/orders` returns HTTP 401 Unauthorized for unauthenticated calls
- Rate limiting header `X-RateLimit-Limit` is present on HTTP responses

---

## 4. EMERGENCY CONTACTS & ESCALATION

- **Platform Lead**: platform-oncall@sporekart.example.com
- **Database Administrator**: dba-team@sporekart.example.com
- **Security Incident Response**: security@sporekart.example.com
