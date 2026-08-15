# SPOREKART v3.0 — Production Smoke Test Guide

**Date**: 2026-08-15
**Purpose**: Post-deployment verification suite for live environment go-live.

---

## 1. Automated Health & Metadata Checks

```bash
# 1. System Health
curl -s -f https://api.sporekart.com/actuator/health | jq .
# Expected: {"status":"UP"}

# 2. Version Verification
curl -s -f https://api.sporekart.com/api/v1/version | jq .
# Expected: {"version":"0.1.0-SNAPSHOT","commitSha":"2aeb2d0"}
```

---

## 2. Public Read Verification

```bash
# 3. Catalog Browsing
curl -s -f https://api.sporekart.com/api/v1/catalog/products?page=0&size=5 | jq .

# 4. Product Details
curl -s -f https://api.sporekart.com/api/v1/catalog/products/PROD-101 | jq .

# 5. Product Rating Summary
curl -s -f https://api.sporekart.com/api/v1/products/PROD-101/rating-summary | jq .
```

---

## 3. Security & Access Control Rejection Tests

```bash
# 6. Unauthenticated Access Rejection (Expected: 401 Unauthorized)
curl -s -o /dev/null -w "%{http_code}\n" https://api.sporekart.com/api/v1/orders

# 7. Unauthenticated Admin Route Access Rejection (Expected: 401 Unauthorized)
curl -s -o /dev/null -w "%{http_code}\n" https://api.sporekart.com/api/v1/admin/orders

# 8. Unauthenticated Return Creation Rejection (Expected: 401 Unauthorized)
curl -s -o /dev/null -w "%{http_code}\n" -X POST https://api.sporekart.com/api/v1/orders/ORD-101/returns
```

---

## 4. Rate Limiting Verification

```bash
# 9. Rate Limit Headers Verification
curl -i -s https://api.sporekart.com/api/v1/health | grep -i "X-RateLimit"
```