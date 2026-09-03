# SPOREKART v3.0 — PAC-05 Training Module Domain Map

**Document ID:** `PAC-05-TRAINING-DOMAIN-MAP`  
**Sprint:** `PAC-05 — Training Module End-to-End Acceptance`  
**Author:** Antigravity FAANG Master Execution Agent  
**Date:** 2026-08-19  
**Status:** Certified Domain Map  

---

## 1. Executive Summary

This document maps all Training Module REST endpoints, frontend consoles, application services, DTO contracts, and security rules certified under PAC-05.

---

## 2. Admin Training Management Endpoints

| Capability | Component / Console | REST Endpoint | HTTP | Auth Rule | Domain Action |
|------------|---------------------|---------------|------|-----------|---------------|
| **Program Mgmt** | `TrainingProgramManagement` | `GET /api/v1/admin/training/programs` | GET | `ROLE_ADMIN` | Lists all training programs |
| **Create Program** | `TrainingProgramManagement` | `POST /api/v1/admin/training/programs` | POST | `ROLE_ADMIN` | Saves new `TrainingProgram` |
| **Batch Ops** | `BatchManagementConsole` | `POST /api/v1/admin/training/batches` | POST | `ROLE_ADMIN` | Configures batch capacity, dates & price |
| **Announce Batch**| `BatchManagementConsole` | `PUT /api/v1/admin/training/batches/{id}/announce` | PUT | `ROLE_ADMIN` | Transitions status to `ANNOUNCED` / `OPEN` |
| **Admin Cancel** | `AdminTrainingOperationsConsole` | `POST /api/v1/admin/training/batches/{id}/cancel` | POST | `ROLE_ADMIN` | Cancels batch (Min 7 days prior) & triggers refunds |
| **Admin Reschedule**| `AdminTrainingOperationsConsole` | `POST /api/v1/admin/training/batches/{id}/reschedule` | POST | `ROLE_ADMIN` | Transfers batch enrollments to new dates |

---

## 3. Trainee Training Endpoints

| Capability | Component / Console | REST Endpoint | HTTP | Auth Rule | Domain Action |
|------------|---------------------|---------------|------|-----------|---------------|
| **Browse Courses** | `TraineeTrainingConsole` | `GET /api/v1/training/programs/public` | GET | Public / Trainee | Lists open training batches & available seats |
| **Enroll** | `TraineeTrainingConsole` | `POST /api/v1/trainee/training/enroll` | POST | `ROLE_TRAINEE` | Checks capacity & creates `TrainingEnrollment` |
| **Verify Payment** | `TraineeTrainingConsole` | `POST /api/v1/trainee/training/payments/verify` | POST | `ROLE_TRAINEE` | Verifies mock payment, marks enrollment `CONFIRMED` |
| **My Enrollments** | `TraineeTrainingConsole` | `GET /api/v1/trainee/training/enrollments` | GET | `ROLE_TRAINEE` | Lists enrollments for authenticated trainee |
| **Trainee Cancel** | `TraineeTrainingConsole` | `POST /api/v1/trainee/training/enrollments/{id}/cancel` | POST | `ROLE_TRAINEE` | Cancels enrollment (Min 2 days prior) & releases seat |
| **Reschedule Req** | `TraineeTrainingConsole` | `POST /api/v1/trainee/training/enrollments/{id}/reschedule` | POST | `ROLE_TRAINEE` | Requests transfer to eligible open batch |

---

## 4. Domain Map Verdict

**VERDICT: PASS** — 100% of implemented Training Module controllers, services, and frontend consoles are accurately mapped and verified.
