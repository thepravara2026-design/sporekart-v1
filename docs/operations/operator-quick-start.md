# SPOREKART v3.0 — Operator Quick-Start Guide

**Target Audience**: DevOps, SRE & Platform Operators

---

## 1. Quick Startup & Deployment

```bash
# 1. Build Production Package
cd backend
mvn clean package -DskipTests

# 2. Run Application in Production Profile
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_USERNAME=sporekart_user
export DATABASE_PASSWORD=secret_password
export RAZORPAY_KEY_SECRET=live_razorpay_secret
export SHIPROCKET_API_PASSWORD=live_shiprocket_password

java -jar target/sporekart-backend-0.1.0-SNAPSHOT.jar
```

---

## 2. Quick Health & Log Diagnostic Commands

```bash
# Check Health Status
curl -s http://localhost:8080/actuator/health

# Tail Logs with MDC Request & User Context
tail -f logs/sporekart.log | grep -E "ERROR|WARN"

# Inspect Hikari Pool Status
curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.active
```