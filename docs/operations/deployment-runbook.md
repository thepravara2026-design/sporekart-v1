# Deployment Runbook — SPOREKART v3.0

This runbook provides step-by-step instructions for operators deploying SPOREKART v3.0 into staging or production environments.

---

## 1. Environment & Prerequisites

### Minimum System Requirements
- JDK 21 / JRE 21 (Temurin)
- PostgreSQL 16+
- Docker & Docker Compose 3.8+ (for containerized deployments)
- Network access to Payment (Razorpay) and Shipping (Shiprocket) API gateways

### Required Environment Variables

| Variable Name | Description | Secret? | Profile |
| :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (`prod`) | No | System |
| `DATABASE_URL` | JDBC Connection string | No | System |
| `DATABASE_USERNAME` | PostgreSQL database user | Yes | Secret Manager |
| `DATABASE_PASSWORD` | PostgreSQL database password | Yes | Secret Manager |
| `JWT_SECRET_KEY` | HMAC-SHA512 signing key (min 512 bits) | Yes | Secret Manager |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed CORS origins | No | Environment |
| `PAYMENT_PROVIDER_KEY` | Razorpay / Provider Key ID | Yes | Secret Manager |
| `PAYMENT_PROVIDER_SECRET` | Razorpay / Provider Key Secret | Yes | Secret Manager |
| `PAYMENT_WEBHOOK_SECRET` | Webhook verification secret | Yes | Secret Manager |
| `SHIPPING_PROVIDER_KEY` | Shiprocket / Provider API Key | Yes | Secret Manager |
| `SHIPPING_PROVIDER_SECRET`| Shiprocket / Provider API Secret | Yes | Secret Manager |

---

## 2. Deployment Steps

### Step 1: Database Setup & Migration
1. Provision target PostgreSQL database instance (`sporekart_prod`).
2. Verify database connection string.
3. Flyway migrations apply automatically on Spring Boot application startup when `spring.flyway.enabled=true`.

### Step 2: Build Application Artifacts
```bash
# Build Backend Executable JAR
cd backend
mvn clean package -DskipTests

# Build Frontend Static Bundle
cd ../frontend
npm ci
npm run build
```

### Step 3: Container Deployment (Docker Compose)
```bash
# Export environment variables or populate production secret manager
export DATABASE_PASSWORD="<STRONG_PASSWORD>"
export JWT_SECRET_KEY="<SUPER_SECURE_512_BIT_KEY>"
export CORS_ALLOWED_ORIGINS="https://sporekart.com"

# Launch containerized platform
docker-compose -f docker-compose.prod.yml up -d --build
```

### Step 4: Verification
1. Check backend readiness:
   ```bash
   curl -f http://localhost:8080/actuator/health/readiness
   ```
2. Verify Prometheus metrics endpoint:
   ```bash
   curl -f http://localhost:8080/actuator/prometheus
   ```
3. Open storefront in browser and confirm catalog loads cleanly.

---

## 3. Graceful Restart & Shutdown
To perform zero-downtime rolling restart or maintenance:
```bash
# Trigger graceful shutdown (allows in-flight requests 20s completion buffer)
docker-compose -f docker-compose.prod.yml stop backend

# Start backend container
docker-compose -f docker-compose.prod.yml start backend
```

---

## 4. Emergency Rollback Procedure
If post-deployment health check fails:
1. Revert to previous stable container tag:
   ```bash
   docker pull sporekart-backend:<PREVIOUS_STABLE_TAG>
   docker-compose -f docker-compose.prod.yml up -d backend
   ```
2. Verify application health:
   ```bash
   curl -f http://localhost:8080/actuator/health
   ```
3. Document incident in `docs/operations/production-incident-register.md`.
