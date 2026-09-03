package com.sporekart.modules.training.application;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.event.BatchBecameFullEvent;
import com.sporekart.modules.training.domain.event.TrainingEnrollmentCreatedEvent;
import com.sporekart.modules.training.domain.exception.*;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentHistoryRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
public class EnrollmentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentApplicationService.class);

    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingBatchRepository batchRepository;
    private final TrainingProgramRepository programRepository;
    private final TrainingEnrollmentHistoryRepository historyRepository;
    private final CapacityApplicationService capacityService;
    private final ApplicationEventPublisher eventPublisher;
    private final SecurityAuditService auditService;
    private final com.sporekart.modules.training.domain.port.TrainingDemandRepository demandRepository;

    @Autowired
    public EnrollmentApplicationService(
            TrainingEnrollmentRepository enrollmentRepository,
            TrainingBatchRepository batchRepository,
            TrainingProgramRepository programRepository,
            TrainingEnrollmentHistoryRepository historyRepository,
            CapacityApplicationService capacityService,
            ApplicationEventPublisher eventPublisher,
            SecurityAuditService auditService,
            @Autowired(required = false) com.sporekart.modules.training.domain.port.TrainingDemandRepository demandRepository) {
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository must not be null");
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository must not be null");
        this.programRepository = Objects.requireNonNull(programRepository, "programRepository must not be null");
        this.historyRepository = Objects.requireNonNull(historyRepository, "historyRepository must not be null");
        this.capacityService = Objects.requireNonNull(capacityService, "capacityService must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
        this.demandRepository = demandRepository;
    }

    public EnrollmentApplicationService(
            TrainingEnrollmentRepository enrollmentRepository,
            TrainingBatchRepository batchRepository,
            TrainingProgramRepository programRepository,
            TrainingEnrollmentHistoryRepository historyRepository,
            CapacityApplicationService capacityService,
            ApplicationEventPublisher eventPublisher,
            SecurityAuditService auditService) {
        this(enrollmentRepository, batchRepository, programRepository, historyRepository, capacityService, eventPublisher, auditService, null);
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

        // 5. Look up program for authoritative price snapshot
        TrainingProgram program = programRepository.findById(batch.getProgramId())
                .orElseThrow(() -> new TrainingNotFoundException("TrainingProgram not found for id: " + batch.getProgramId()));

        BigDecimal priceAmount = program.getPriceAmount() != null ? program.getPriceAmount() : BigDecimal.ZERO;
        String currency = program.getCurrency() != null ? program.getCurrency() : "INR";

        // 6. Execute atomic capacity slot allocation if free program, else check capacity without consuming until payment verified
        boolean isFreeProgram = priceAmount.compareTo(BigDecimal.ZERO) == 0;
        if (isFreeProgram) {
            boolean slotAllocated = batchRepository.tryAllocateSeatAtomic(batchId);
            if (!slotAllocated) {
                throw new BatchFullException("Capacity allocation failed: Batch " + batch.getBatchCode() + " is FULL");
            }
        }

        // 7. Create enrollment entity with snapshot attributes
        TrainingEnrollment enrollment = TrainingEnrollment.create(batchId, traineeId, priceAmount, currency, idempotencyKey, traineeId);
        if (isFreeProgram) {
            enrollment.confirm("FREE_PROGRAM");
        }
        TrainingEnrollment saved = enrollmentRepository.save(enrollment);

        // Record initial history
        historyRepository.save(TrainingEnrollmentHistory.record(saved.getId(), null, saved.getStatus(), "ENROLLMENT_CREATED", traineeId));

        // Resolve active demand if present and confirmed
        if (saved.getStatus() == EnrollmentStatus.CONFIRMED && demandRepository != null) {
            demandRepository.findByBatchIdAndTraineeIdAndStatus(batchId, traineeId, DemandStatus.ACTIVE)
                    .ifPresent(demand -> {
                        demand.resolve(traineeId);
                        demandRepository.save(demand);
                        log.info("Resolved active demand id={} for trainee={} upon successful enrollment in batch={}", demand.getId(), traineeId, batchId);
                    });
        }

        log.info("Successfully created TrainingEnrollment id={}, code={} for trainee={} in batch={}", saved.getId(), saved.getEnrollmentCode(), traineeId, batchId);

        // 8. Publish domain events & record security audit
        eventPublisher.publishEvent(new TrainingEnrollmentCreatedEvent(saved.getId(), batchId, traineeId));

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
                AuditStatus.SUCCESS,
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
