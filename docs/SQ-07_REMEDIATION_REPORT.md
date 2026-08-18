# SQ-07 — Remediation Report

## Security Remediation Log

### 1. `SecurityConfig.java` (`com.sporekart.application.configuration`)
- **Finding**: Missing HSTS (HTTP Strict Transport Security) header directive in Spring Security headers configuration.
- **Severity**: Low / Security Best Practice
- **Fix**: Added `.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))` to security filter chain.
- **Commit**: `97c7c18`

### 2. `BackendSecurityHardeningRegressionTest.java` (`com.sporekart.application.security`)
- **Finding**: Need dedicated regression test suite covering security headers, IDOR, RBAC, and public vs admin authorization.
- **Severity**: Medium / Regression Protection
- **Fix**: Implemented `BackendSecurityHardeningRegressionTest.java` with 6 integration tests.
- **Commit**: `86db3ff`

---

## Dependency Vulnerability Audit Log
- **Spring Boot Version**: `3.4.2` (Latest stable release)
- **Spring Security Version**: `6.4.2` (Latest stable release)
- **Jackson Databind**: `2.18.2` (Patched against deserialization vulnerabilities)
- **JJWT API**: `0.12.6` (Configured with HMAC-SHA512 512-bit key requirements)
- **Audit Outcome**: 0 High/Critical vulnerability risks identified.
