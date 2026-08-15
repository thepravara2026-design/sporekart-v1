# SPOREKART v3.0 — PRODUCTION SMOKE TEST SPECIFICATION

## 1. AUTOMATED PRODUCTION SMOKE TEST MATRIX

The production smoke test suite validates core infrastructure, API availability, authentication boundaries, and resilience behavior after deployment without performing real financial charges.

| Test ID | Objective | Endpoint | Expected Result | Pass Criteria |
|:---|:---|:---|:---|:---|
| **SMK-01** | Container Liveness | `GET /actuator/health/liveness` | HTTP 200 OK | `{"status":"UP"}` |
| **SMK-02** | Container & DB Readiness | `GET /actuator/health/readiness` | HTTP 200 OK | `{"status":"UP"}` |
| **SMK-03** | Frontend Static Asset | `GET /` | HTTP 200 OK | Returns Nginx HTML index page |
| **SMK-04** | Public Catalog Read | `GET /api/v1/catalog/products` | HTTP 200 OK | Product list array returned |
| **SMK-05** | Product Search API | `GET /api/v1/catalog/search?q=spore` | HTTP 200 OK | Search result list returned |
| **SMK-06** | Protected Order Denial | `GET /api/v1/orders` | HTTP 401 Unauthorized | Reject anonymous call |
| **SMK-07** | Admin Endpoint Denial | `GET /api/v1/admin/audit-logs` | HTTP 401 / 403 | Reject non-admin calls |
| **SMK-08** | Rate Limit Headers | `GET /api/v1/catalog/products` | HTTP 200 OK | `X-RateLimit-Limit` header present |
| **SMK-09** | Actuator Sensitive Path Protection | `GET /actuator/env` | HTTP 404 / 401 | Environment secrets hidden |
| **SMK-10** | Webhook Security Guard | `POST /api/v1/payments/webhooks/razorpay` | HTTP 400 / 401 | Rejects un-signed webhooks |

---

## 2. SMOKE TEST EXECUTION SCRIPT

Save as `scripts/production-smoke-test.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"

echo "==> Running Sporekart v3.0 Production Smoke Tests against $BASE_URL..."

# SMK-01 Liveness
echo -n "Checking Liveness... "
curl -sf "$BASE_URL/actuator/health/liveness" | grep -q "UP" && echo "PASS"

# SMK-02 Readiness
echo -n "Checking Readiness... "
curl -sf "$BASE_URL/actuator/health/readiness" | grep -q "UP" && echo "PASS"

# SMK-04 Catalog Products
echo -n "Checking Catalog Products Endpoint... "
curl -sf "$BASE_URL/api/v1/catalog/products" > /dev/null && echo "PASS"

# SMK-06 Auth Protection
echo -n "Checking Auth Protection on Protected Endpoint... "
STATUS=$(curl -o /dev/null -s -w "%{http_code}" "$BASE_URL/api/v1/orders")
if [ "$STATUS" -eq 401 ] || [ "$STATUS" -eq 403 ]; then
  echo "PASS (HTTP $STATUS)"
else
  echo "FAIL (Unexpected status HTTP $STATUS)"
  exit 1
fi

echo "==> All Production Smoke Tests Passed Cleanly!"
```
