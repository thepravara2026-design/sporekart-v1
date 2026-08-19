# SPOREKART v3.0 — PAC-06 Admin Domain Map

**Document ID:** `PAC-06-ADMIN-DOMAIN-MAP`  
**Sprint:** `PAC-06 — Admin & Platform Operations Acceptance`  
**Author:** Antigravity FAANG Master Execution Agent  
**Date:** 2026-08-19  
**Status:** Certified Admin Domain Map  

---

## 1. Executive Summary

This document maps all Admin REST endpoints, frontend SPA consoles, application services, and database entities certified under PAC-06.

---

## 2. Admin Operational Endpoint Map

| Category | Component / Console | REST Endpoint | HTTP | Auth Requirement | Controlled Domain Action |
|----------|---------------------|---------------|------|------------------|--------------------------|
| **Dashboard** | `AdminDashboardPage` | `GET /api/v1/admin/dashboard/summary` | GET | `ROLE_ADMIN` | Executive metrics overview |
| **Order Ops** | `AdminOrderManagementPage` | `GET /api/v1/admin/orders` | GET | `ROLE_ADMIN` | Platform order inspection & filtering |
| **Order Details** | `AdminOrderManagementPage` | `GET /api/v1/admin/orders/{id}` | GET | `ROLE_ADMIN` | In-depth order inspection |
| **Shipment Ops**| `AdminShipmentController` | `POST /api/v1/admin/shipments` | POST | `ROLE_ADMIN` | Initiates shipment & updates order status |
| **Return Ops** | `AdminReturnListPage` | `GET /api/v1/admin/returns` | GET | `ROLE_ADMIN` | Views pending customer return requests |
| **Approve Return**| `AdminReturnListPage` | `POST /api/v1/admin/returns/{id}/approve` | POST | `ROLE_ADMIN` | Approves return & triggers refund outbox event |
| **Training Programs**| `TrainingProgramManagement` | `POST /api/v1/admin/training/programs` | POST | `ROLE_ADMIN` | Course definition & catalog management |
| **Batch Ops** | `BatchManagementConsole` | `POST /api/v1/admin/training/batches` | POST | `ROLE_ADMIN` | Schedules batch capacity, price & dates |
| **Admin Cancel Batch**| `AdminTrainingOperationsConsole`| `POST /api/v1/admin/training/batches/{id}/cancel` | POST | `ROLE_ADMIN` | Cancels batch (Min 7 days prior) & triggers refunds |
| **Notifications**| `NotificationOperationsConsole` | `GET /api/v1/admin/notifications/logs` | GET | `ROLE_ADMIN` | Audits notification delivery & retry status |
| **Metrics** | System Actuator | `GET /actuator/prometheus` | GET | `ROLE_ADMIN` | Exposes operational Prometheus metrics |

---

## 3. Domain Map Verdict

**VERDICT: PASS** — 100% of implemented Admin controllers, services, and frontend consoles are accurately mapped and verified.
