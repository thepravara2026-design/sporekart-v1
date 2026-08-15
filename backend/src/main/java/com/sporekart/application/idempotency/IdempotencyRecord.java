package com.sporekart.application.idempotency;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "idempotency_key", length = 128, nullable = false)
    private String idempotencyKey;

    @Column(name = "actor_id", length = 100, nullable = false)
    private String actorId;

    @Column(name = "request_path", length = 255, nullable = false)
    private String requestPath;

    @Column(name = "request_hash", length = 64, nullable = false)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private IdempotencyStatus status;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum IdempotencyStatus {
        PROCESSING,
        COMPLETED,
        FAILED
    }

    public IdempotencyRecord() {}

    public IdempotencyRecord(String idempotencyKey, String actorId, String requestPath, String requestHash) {
        this.id = UUID.randomUUID().toString();
        this.idempotencyKey = idempotencyKey;
        this.actorId = actorId;
        this.requestPath = requestPath;
        this.requestHash = requestHash;
        this.status = IdempotencyStatus.PROCESSING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getActorId() { return actorId; }
    public String getRequestPath() { return requestPath; }
    public String getRequestHash() { return requestHash; }
    public IdempotencyStatus getStatus() { return status; }
    public Integer getResponseCode() { return responseCode; }
    public String getResponseBody() { return responseBody; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void markCompleted(int responseCode, String responseBody) {
        this.status = IdempotencyStatus.COMPLETED;
        this.responseCode = responseCode;
        this.responseBody = responseBody;
        this.updatedAt = Instant.now();
    }

    public void markFailed(int responseCode, String responseBody) {
        this.status = IdempotencyStatus.FAILED;
        this.responseCode = responseCode;
        this.responseBody = responseBody;
        this.updatedAt = Instant.now();
    }
}
