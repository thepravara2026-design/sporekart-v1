package com.sporekart.modules.training.application;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.training.controller.dto.CapacityResponse;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.event.BatchBecameFullEvent;
import com.sporekart.modules.training.domain.event.BatchCapacityAvailableEvent;
import com.sporekart.modules.training.domain.event.BatchCapacityChangedEvent;
import com.sporekart.modules.training.domain.exception.BatchFullException;
import com.sporekart.modules.training.domain.exception.BatchNotFoundException;
import com.sporekart.modules.training.domain.exception.CapacityBelowOccupancyException;
import com.sporekart.modules.training.domain.exception.InsufficientCapacityException;
import com.sporekart.modules.training.domain.exception.InvalidBatchStateException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class CapacityApplicationService {

    private static final Logger log = LoggerFactory.getLogger(CapacityApplicationService.class);

    private final TrainingBatchRepository batchRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SecurityAuditService auditService;

    public CapacityApplicationService(
            TrainingBatchRepository batchRepository,
            ApplicationEventPublisher eventPublisher,
            SecurityAuditService auditService) {
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
    }

    @Transactional
    public CapacityResponse allocateSlot(String batchId) {
        log.info("Attempting atomic slot allocation for batchId={}", batchId);

        boolean success = batchRepository.tryAllocateSeatAtomic(batchId);
        if (!success) {
            TrainingBatch batch = batchRepository.findById(batchId)
                    .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

            if (batch.getStatus() == BatchStatus.CANCELLED || batch.getStatus() == BatchStatus.COMPLETED) {
                throw new InvalidBatchStateException("Cannot allocate slot: Batch " + batch.getBatchCode() + " is in status " + batch.getStatus());
            }

            if (batch.getCapacity().isFull() || batch.getStatus() == BatchStatus.FULL) {
                log.warn("Slot allocation rejected: Batch {} is FULL ({}/{})", batch.getBatchCode(), batch.getCapacity().getOccupiedSeats(), batch.getCapacity().getTotalCapacity());
                throw new InsufficientCapacityException("Cannot allocate slot: Training batch " + batch.getBatchCode() + " is FULL (" + batch.getCapacity().getOccupiedSeats() + "/" + batch.getCapacity().getTotalCapacity() + ")");
            }

            throw new InsufficientCapacityException("Slot allocation failed for batch " + batch.getBatchCode());
        }

        TrainingBatch updatedBatch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found post allocation for id: " + batchId));

        log.info("Successfully allocated slot for batchId={}, code={}, newOccupied={}/{}",
                batchId, updatedBatch.getBatchCode(), updatedBatch.getCapacity().getOccupiedSeats(), updatedBatch.getCapacity().getTotalCapacity());

        if (updatedBatch.getCapacity().isFull() || updatedBatch.getStatus() == BatchStatus.FULL) {
            eventPublisher.publishEvent(new BatchBecameFullEvent(
                    updatedBatch.getId(),
                    updatedBatch.getBatchCode(),
                    updatedBatch.getCapacity().getTotalCapacity()
            ));
        }

        return CapacityResponse.fromDomain(updatedBatch);
    }

    @Transactional
    public CapacityResponse releaseSlot(String batchId) {
        log.info("Attempting atomic slot release for batchId={}", batchId);

        TrainingBatch batchBefore = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

        boolean wasFull = batchBefore.getCapacity().isFull() || batchBefore.getStatus() == BatchStatus.FULL;

        boolean success = batchRepository.releaseSeatAtomic(batchId);
        if (!success) {
            log.warn("Release seat atomic operation returned false for batchId={}", batchId);
        }

        TrainingBatch updatedBatch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found post release for id: " + batchId));

        if (wasFull && !updatedBatch.getCapacity().isFull()) {
            eventPublisher.publishEvent(new BatchCapacityAvailableEvent(
                    updatedBatch.getId(),
                    updatedBatch.getBatchCode(),
                    updatedBatch.getCapacity().getTotalCapacity(),
                    updatedBatch.getCapacity().getOccupiedSeats()
            ));
        }

        log.info("Released slot for batchId={}, code={}, newOccupied={}/{}",
                batchId, updatedBatch.getBatchCode(), updatedBatch.getCapacity().getOccupiedSeats(), updatedBatch.getCapacity().getTotalCapacity());

        return CapacityResponse.fromDomain(updatedBatch);
    }

    @Transactional
    public CapacityResponse updateBatchCapacity(String batchId, int newTotalCapacity, String adminUser) {
        if (newTotalCapacity < 0) {
            throw new IllegalArgumentException("Total capacity must not be negative");
        }

        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

        int currentOccupied = batch.getCapacity().getOccupiedSeats();
        if (newTotalCapacity < currentOccupied) {
            throw new CapacityBelowOccupancyException("New capacity (" + newTotalCapacity + ") cannot be less than currently occupied seats (" + currentOccupied + ")");
        }

        int previousCapacity = batch.getCapacity().getTotalCapacity();
        boolean wasFull = batch.getCapacity().isFull() || batch.getStatus() == BatchStatus.FULL;

        batch.updateTotalCapacity(newTotalCapacity);
        TrainingBatch saved = batchRepository.save(batch);

        log.info("Updated batch capacity for batchId={}, code={}, prevCapacity={}, newCapacity={}, occupied={}",
                batchId, saved.getBatchCode(), previousCapacity, newTotalCapacity, currentOccupied);

        auditService.logEvent(
                com.sporekart.modules.security.domain.AuditEventType.SECURITY_SYSTEM_ALERT,
                adminUser != null ? adminUser : "ADMIN",
                "BATCH_CAPACITY_UPDATED",
                "TrainingBatch",
                saved.getId(),
                null,
                "Updated capacity from " + previousCapacity + " to " + newTotalCapacity
        );

        eventPublisher.publishEvent(new BatchCapacityChangedEvent(
                saved.getId(),
                saved.getBatchCode(),
                previousCapacity,
                newTotalCapacity,
                currentOccupied
        ));

        if (wasFull && !saved.getCapacity().isFull()) {
            eventPublisher.publishEvent(new BatchCapacityAvailableEvent(
                    saved.getId(),
                    saved.getBatchCode(),
                    saved.getCapacity().getTotalCapacity(),
                    saved.getCapacity().getOccupiedSeats()
            ));
        } else if (!wasFull && saved.getCapacity().isFull()) {
            eventPublisher.publishEvent(new BatchBecameFullEvent(
                    saved.getId(),
                    saved.getBatchCode(),
                    saved.getCapacity().getTotalCapacity()
            ));
        }

        return CapacityResponse.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public CapacityResponse getBatchCapacity(String batchId) {
        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));
        return CapacityResponse.fromDomain(batch);
    }
}
