# SPOREKART v3.0 — Secret Rotation Plan

**Date**: 2026-08-15
**Security Mandate**: Zero zero-day credentials in code or deployment bundles.

---

## 1. Secret Inventory & Rotation Schedules

| Secret Name | Category | Rotation Trigger / Schedule | Dependent Systems | Zero-Downtime Rotation Procedure |
| :--- | :--- | :--- | :--- | :--- |
| `DATABASE_PASSWORD` | Infrastructure | 90 Days / Compromise Event | Backend Spring Boot Service | 1. Create secondary DB user with same privileges.<br>2. Update `DATABASE_PASSWORD` in secret manager.<br>3. Trigger rolling restart of application instances.<br>4. Revoke old DB password. |
| `RAZORPAY_KEY_SECRET` | Third-party API | 180 Days / Security Alert | Payment Module | 1. Generate new API secret in Razorpay Dashboard.<br>2. Support dual-key verification during 24h grace period.<br>3. Update `RAZORPAY_KEY_SECRET` in production env.<br>4. Revoke old key. |
| `SHIPROCKET_API_PASSWORD` | Third-party API | 180 Days / Staff Exit | Shipping Module | 1. Reset password in Shiprocket portal.<br>2. Update `SHIPROCKET_API_PASSWORD` in env.<br>3. Restart backend service to re-authenticate JWT. |
| `JWT_SECRET_KEY` | Application Security | 90 Days | Auth / Gateway | 1. Configure backend to accept signatures signed with old or new key.<br>2. Issue new tokens with new key.<br>3. Retire old key after max token expiry (24h). |

---

## 2. Emergency Compromise Response Procedure

If any production secret is suspected to be exposed:
1. Immediately log security incident in `docs/security/incidents/`.
2. Rotate secret in provider portal / secret manager.
3. Perform emergency application deployment with updated secret.
4. Revoke all active sessions if `JWT_SECRET_KEY` or admin credential was affected.