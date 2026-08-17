package com.sporekart.modules.training.application;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.event.BatchBecameFullEvent;
import com.sporekart.modules.training.domain.event.TrainingEnrollmentCreatedEvent;
import com.sporekart.modules.training.domain.exception.*;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
public class EnrollmentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentApplicationService.class);

    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingBatchRepository batchRepository;
    private final CapacityApplicationService capacityService;
    private final ApplicationEventPublisher eventPublisher;
    private final SecurityAuditService auditService;
    private final com.sporekart.modules.training.domain.port.TrainingDemandRepository demandRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public EnrollmentApplicationService(
            TrainingEnrollmentRepository enrollmentRepository,
            TrainingBatchRepository batchRepository,
            CapacityApplicationService capacityService,
            ApplicationEventPublisher eventPublisher,
            SecurityAuditService auditService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) com.sporekart.modules.training.domain.port.TrainingDemandRepository demandRepository) {
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository must not be null");
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository must not be null");
        this.capacityService = Objects.requireNonNull(capacityService, "capacityService must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
        this.demandRepository = demandRepository;
    }

    public EnrollmentApplicationService(
            TrainingEnrollmentRepository enrollmentRepository,
            TrainingBatchRepository batchRepository,
            CapacityApplicationService capacityService,
            ApplicationEventPublisher eventPublisher,
            SecurityAuditService auditService) {
        this(enrollmentRepository, batchRepository, capacityService, eventPublisher, auditService, null);
    }

    @Transactional(noRollbackFor = {BatchFullException.class, InvalidBatchStateException.class, DuplicateEnrollmentException.class})
    public TrainingEnrollment enrollTrainee(String batchId, String traineeId, String idempotencyKey) {
        Objects.requireNonNull(batchId, "batchId must not be null");
        Objects.requireNonNull(traineeId, "traineeId must not be null");

        // 1. Check idempotency: if key provided and already exists, return previous result idempotently
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<TrainingEnrollment> existingIdempotent = enrollmentRepository.findByIdempotencyKey(idempotencyKey);
            if (existingIdempotent.isPresent()) {
                log.info("Idempotent enrollment replay for key={} returning enrollment id={}", idempotencyKey, existingIdempotent.get().getId());
                return existingIdempotent.get();
            }
        }

        // 2. Validate batch exists and is in an enrollable state
        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

        if (batch.getStatus() == BatchStatus.FULL) {
            throw new BatchFullException("Cannot enroll trainee: Batch " + batch.getBatchCode() + " is FULL");
        }
        if (batch.getStatus() == BatchStatus.CANCELLED || batch.getStatus() == BatchStatus.COMPLETED) {
            throw new InvalidBatchStateException("Batch " + batch.getBatchCode() + " is not accepting enrollments (Status: " + batch.getStatus() + ")");
        }

        // 3. Validate duplicate enrollment rule (One trainee + One batch = At most 1 active enrollment)
        if (enrollmentRepository.existsByBatchIdAndTraineeId(batchId, traineeId)) {
            throw new DuplicateEnrollmentException("Trainee " + traineeId + " is already enrolled in batch " + batchId);
        }

        // 4. Pre-check capacity status
        if (batch.getStatus() == BatchStatus.FULL || batch.getCapacity().isFull()) {
            throw new BatchFullException("Cannot enroll trainee: Batch " + batch.getBatchCode() + " is FULL");
        }

        // 5. Execute atomic capacity slot allocation
        boolean slotAllocated = batchRepository.tryAllocateSeatAtomic(batchId);
        if (!slotAllocated) {
            throw new BatchFullException("Capacity allocation failed: Batch " + batch.getBatchCode() + " is FULL");
        }

        // 6. Create enrollment entity
        TrainingEnrollment enrollment = TrainingEnrollment.create(batchId, traineeId, idempotencyKey, traineeId);
        TrainingEnrollment saved = enrollmentRepository.save(enrollment);

        // Resolve active demand if present for this trainee & batch
        if (demandRepository != null) {
            demandRepository.findByBatchIdAndTraineeIdAndStatus(batchId, traineeId, com.sporekart.modules.training.domain.DemandStatus.ACTIVE)
                    .ifPresent(demand -> {
                        demand.resolve(traineeId);
                        demandRepository.save(demand);
                        log.info("Resolved active demand id={} for trainee={} upon successful enrollment in batch={}", demand.getId(), traineeId, batchId);
                    });
        }

        log.info("Successfully created TrainingEnrollment id={} for trainee={} in batch={}", saved.getId(), traineeId, batchId);

        // 7. Publish domain events & record security audit
        eventPublisher.publishEvent(new TrainingEnrollmentCreatedEvent(saved.getId(), batchId, traineeId));

        // If batch became full after this allocation, publish BatchBecameFullEvent
        TrainingBatch reloaded = batchRepository.findById(batchId).orElse(batch);
        if (reloaded.getStatus() == BatchStatus.FULL) {
            eventPublisher.publishEvent(new BatchBecameFullEvent(reloaded.getId(), reloaded.getBatchCode(), reloaded.getCapacity().getTotalCapacity()));
        }

        auditService.logEvent(
                AuditEventType.SECURITY_SYSTEM_ALERT,
                traineeId,
                saved.getId(),
                "127.0.0.1",
                "SYSTEM",
                com.sporekart.modules.security.domain.AuditStatus.SUCCESS,
                "Enrolled trainee " + traineeId + " into batch " + batch.getBatchCode()
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public TrainingEnrollment getEnrollmentById(String enrollmentId, String requesterId, boolean isAdmin) {
        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("TrainingEnrollment not found for id: " + enrollmentId));

        if (!isAdmin && !enrollment.getTraineeId().equalsIgnoreCase(requesterId)) {
            throw new UnauthorizedEnrollmentAccessException("Access denied: Trainee " + requesterId + " cannot view enrollment " + enrollmentId);
        }

        return enrollment;
    }

    @Transactional(readOnly = true)
    public Page<TrainingEnrollment> getTraineeEnrollments(String traineeId, Pageable pageable) {
        return enrollmentRepository.findByTraineeId(traineeId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<TrainingEnrollment> getBatchEnrollmentsForAdmin(String batchId, Pageable pageable) {
        batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));
        return enrollmentRepository.findByBatchId(batchId, pageable);
    }
}
