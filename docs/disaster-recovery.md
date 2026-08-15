# SPOREKART v3.0 — DISASTER RECOVERY PLAN

## 1. RECOVERY TARGETS (RPO & RTO)

- **Recovery Point Objective (RPO)**: **15 Minutes**  
  Maximum permissible data loss in the event of major infrastructure or cloud failure.
- **Recovery Time Objective (RTO)**: **1 Hour**  
  Target duration to restore application availability, database connectivity, and transaction processing following a total outage.

---

## 2. BACKUP STRATEGY & RETENTION

- **Automated Database Snapshots**: Managed PostgreSQL continuous WAL archiving with daily full backups at 02:00 UTC.
- **Retention Period**: Daily backups retained for 30 days; weekly backups retained for 90 days.
- **Storage Location**: Multi-region encrypted object storage (GCS / AWS S3) separate from production compute host.
- **Backup Verification**: Weekly automated restore test into isolated QAT environment.

---

## 3. DISASTER RECOVERY RESTORE PROCEDURE

### Phase 1: Infrastructure & Secret Restoration
1. Provision target host or cloud instance.
2. Restore production secrets (`.env`) from encrypted secrets vault (e.g. HashiCorp Vault / AWS Secrets Manager).
3. Verify DNS entry / Reverse Proxy routing to new host IP.

### Phase 2: Database Restoration
1. Pull latest verified database dump from backup storage.
2. Restore database schema and data:
   ```bash
   pg_restore -h <NEW_DB_HOST> -U <DB_USER> -d sporekart_prod --clean /backups/sporekart_prod_latest.dump
   ```
3. Run Flyway repair & migrate to ensure migration table checksum consistency:
   ```bash
   mvn flyway:repair -Dflyway.configFiles=flyway.conf
   ```

### Phase 3: Application Container Redeployment
1. Pull production Docker images:
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```
2. Monitor startup logs and confirm liveness & readiness health checks:
   ```bash
   curl -f http://localhost:8080/actuator/health/readiness
   ```

### Phase 4: Post-Recovery State Reconciliation
1. Execute `PaymentReconciliationService` to reconcile any in-flight payments recorded during the outage window.
2. Execute `ShipmentReconciliationService` to sync shipment status with shipping delivery providers.
3. Perform post-deployment smoke test suite.
