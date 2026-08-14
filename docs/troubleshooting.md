# SPOREKART v3.0 — TROUBLESHOOTING & DIAGNOSTICS GUIDE

## 1. Common Development & Runtime Issues

### 1.1 Backend Failures

#### Problem: `Port 8080 already in use`
- **Symptom**: `WebServerStartException: Port 8080 was already in use.`
- **Cause**: Another process or previous Spring Boot instance is bound to 8080.
- **Solution**: Terminate the process or override port via command line:
  ```bash
  mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
  ```

#### Problem: H2 Database Migration Failure
- **Symptom**: `FlywayException: Migration V3__catalog_query_indexes.sql failed`
- **Cause**: Schema state mismatch or dirty H2 in-memory DB cache.
- **Solution**: Execute `mvn clean test` to reset in-memory state.

---

### 1.2 Frontend Failures

#### Problem: `AxiosError: Network Error` or `Connection Refused`
- **Symptom**: Frontend shows error banner *"Unable to load catalog products. A network error occurred."*
- **Cause**: Backend Spring Boot service is not running or CORS is misconfigured.
- **Solution**:
  1. Verify backend health endpoint at `http://localhost:8080/api/v1/health`.
  2. Ensure `VITE_API_BASE_URL` in `.env` points to `http://localhost:8080`.

#### Problem: Vite HMR / Dev Server Port Conflict
- **Symptom**: Vite starts on port 5174 instead of 5173.
- **Cause**: Port 5173 is occupied.
- **Solution**: Kill running Vite processes or accept port 5174 (API client relies on relative proxy or environment variable).

---

## 2. API Error Diagnosis Checklist

1. **400 Bad Request (`CATALOG_INVALID_SORT`)**:
   - Check requested sort parameter against allowed whitelist: `name`, `price`, `createdAt`, `updatedAt`, `sku`, `status`.
2. **400 Bad Request (`CATALOG_INVALID_PRICE_RANGE`)**:
   - Verify `minPrice >= 0`, `maxPrice >= 0`, and `minPrice <= maxPrice`.
3. **404 Not Found (`CATALOG_PRODUCT_NOT_FOUND`)**:
   - Verify requested UUID or SKU exists in database.
