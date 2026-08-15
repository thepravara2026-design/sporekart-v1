package com.sporekart.application.idempotency;

import com.sporekart.application.exception.InvalidIdempotencyKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private static final int MAX_KEY_LENGTH = 128;

    private final IdempotencyRecordRepository repository;
    private final com.sporekart.application.observability.metrics.CommerceMetricsService metricsService;

    @Autowired
    public IdempotencyService(@Autowired(required = false) IdempotencyRecordRepository repository,
                              @Autowired(required = false) com.sporekart.application.observability.metrics.CommerceMetricsService metricsService) {
        this.repository = repository;
        this.metricsService = metricsService;
    }

    @Transactional
    public Optional<IdempotencyRecord> checkOrStartProcessing(String actorId, String idempotencyKey, String requestPath, String payload) {
        if (repository == null) {
            return Optional.empty();
        }

        validateKey(idempotencyKey);
        String requestHash = computeHash(payload);

        Optional<IdempotencyRecord> existingOpt = repository.findByActorIdAndIdempotencyKey(actorId, idempotencyKey);

        if (existingOpt.isPresent()) {
            IdempotencyRecord existing = existingOpt.get();

            if (!existing.getRequestHash().equals(requestHash)) {
                log.warn("Idempotency payload mismatch for actor: {} and key: {}", actorId, idempotencyKey);
                if (metricsService != null) {
                    metricsService.recordIdempotencyConflict();
                }
                throw new InvalidIdempotencyKeyException("Idempotency key reused with a different request payload");
            }

            log.info("Idempotency match found for actor: {} key: {} status: {}", actorId, idempotencyKey, existing.getStatus());
            if (metricsService != null) {
                metricsService.recordIdempotencyReplayed();
            }
            return Optional.of(existing);
        }

        // Register new PROCESSING record
        IdempotencyRecord record = new IdempotencyRecord(idempotencyKey, actorId, requestPath, requestHash);
        repository.save(record);
        return Optional.of(record);
    }

    @Transactional
    public void recordResponse(String actorId, String idempotencyKey, int statusCode, String responseBody) {
        if (repository == null) return;
        repository.findByActorIdAndIdempotencyKey(actorId, idempotencyKey).ifPresent(record -> {
            record.markCompleted(statusCode, responseBody);
            repository.save(record);
        });
    }

    public void validateKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidIdempotencyKeyException("Idempotency-Key header cannot be blank");
        }
        if (idempotencyKey.length() > MAX_KEY_LENGTH) {
            throw new InvalidIdempotencyKeyException("Idempotency-Key exceeds maximum length of " + MAX_KEY_LENGTH + " characters");
        }
    }

    private String computeHash(String payload) {
        if (payload == null) payload = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return String.valueOf(payload.hashCode());
        }
    }
}
