package com.sporekart.modules.training.application;

import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.event.*;
import com.sporekart.modules.training.domain.exception.*;
import com.sporekart.modules.training.domain.policy.CancellationPolicy;
import com.sporekart.modules.training.domain.policy.CapacityPolicy;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingApplicationService {

    private static final Logger log = LoggerFactory.getLogger(TrainingApplicationService.class);

    private final TrainingProgramRepository programRepository;
    private final TrainingBatchRepository batchRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final CancellationPolicy cancellationPolicy;
    private final CapacityPolicy capacityPolicy;
    private final ApplicationEventPublisher eventPublisher;

    public TrainingApplicationService(
            TrainingProgramRepository programRepository,
            TrainingBatchRepository batchRepository,
            TrainingEnrollmentRepository enrollmentRepository,
            CancellationPolicy cancellationPolicy,
            CapacityPolicy capacityPolicy,
            ApplicationEventPublisher eventPublisher) {
        this.programRepository = programRepository;
        this.batchRepository = batchRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.cancellationPolicy = cancellationPolicy;
        this.capacityPolicy = capacityPolicy;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TrainingProgram createProgram(String title, String description, BigDecimal priceAmount, String currency) {
        TrainingProgram program = TrainingProgram.create(title, description, priceAmount, currency);
        TrainingProgram saved = programRepository.save(program);
        log.info("Created TrainingProgram id={}, title='{}'", saved.getId(), saved.getTitle());
        eventPublisher.publishEvent(new TrainingProgramCreatedEvent(saved.getId(), saved.getTitle()));
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<TrainingProgram> getProgramById(String id) {
        return programRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<TrainingProgram> getAllPrograms() {
        return programRepository.findAll();
    }

    @Transactional
    public TrainingBatch createBatch(String programId, String batchCode, Instant startDate, Instant endDate, int totalCapacity) {
        TrainingProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new TrainingNotFoundException("TrainingProgram not found for id: " + programId));

        if (batchRepository.findByBatchCode(batchCode).isPresent()) {
            throw new IllegalArgumentException("Batch code '" + batchCode + "' already exists");
        }

        TrainingBatch batch = TrainingBatch.create(program.getId(), batchCode, startDate, endDate, totalCapacity);
        TrainingBatch saved = batchRepository.save(batch);
        log.info("Created TrainingBatch id={}, batchCode='{}', capacity={}", saved.getId(), saved.getBatchCode(), totalCapacity);
        eventPublisher.publishEvent(new TrainingBatchCreatedEvent(saved.getId(), saved.getProgramId(), saved.getBatchCode(), totalCapacity));
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<TrainingBatch> getBatchById(String id) {
        return batchRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<TrainingBatch> getBatchesForProgram(String programId) {
        return batchRepository.findByProgramId(programId);
    }

    @Transactional
    public TrainingBatch updateBatchCapacity(String batchId, int newTotalCapacity) {
        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

        int previousCapacity = batch.getCapacity().getTotalCapacity();
        batch.updateTotalCapacity(newTotalCapacity);
        TrainingBatch saved = batchRepository.save(batch);

        log.info("Updated TrainingBatch id={} capacity from {} to {}", batchId, previousCapacity, newTotalCapacity);
        eventPublisher.publishEvent(new BatchCapacityChangedEvent(batchId, previousCapacity, newTotalCapacity));

        if (saved.getStatus() == BatchStatus.FULL) {
            eventPublisher.publishEvent(new BatchFullEvent(saved.getId(), saved.getBatchCode(), saved.getCapacity().getTotalCapacity()));
        }

        return saved;
    }

    @Transactional
    public TrainingEnrollment initializeEnrollment(String batchId, String traineeId) {
        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

        if (enrollmentRepository.existsByBatchIdAndTraineeId(batchId, traineeId)) {
            throw new DuplicateEnrollmentException("Trainee " + traineeId + " is already enrolled in batch " + batchId);
        }

        if (batch.getStatus() == BatchStatus.FULL || batch.getCapacity().isFull()) {
            throw new BatchFullException("Cannot enroll trainee: Batch " + batch.getBatchCode() + " is FULL");
        }

        boolean allocated = batchRepository.tryAllocateSeatAtomic(batchId);
        if (!allocated) {
            throw new BatchFullException("Concurrency error: Batch capacity allocation failed for batch " + batchId);
        }

        TrainingEnrollment enrollment = TrainingEnrollment.create(batchId, traineeId);
        TrainingEnrollment saved = enrollmentRepository.save(enrollment);

        log.info("Initialized TrainingEnrollment id={} for trainee={} in batch={}", saved.getId(), traineeId, batchId);
        eventPublisher.publishEvent(new TrainingEnrollmentCreatedEvent(saved.getId(), batchId, traineeId));

        // Re-check if batch became full after allocation
        TrainingBatch updatedBatch = batchRepository.findById(batchId).orElse(batch);
        if (updatedBatch.getStatus() == BatchStatus.FULL) {
            eventPublisher.publishEvent(new BatchFullEvent(updatedBatch.getId(), updatedBatch.getBatchCode(), updatedBatch.getCapacity().getTotalCapacity()));
        }

        return saved;
    }

    public void validateCancellationPolicy(String batchId, boolean isAdmin, Instant now) {
        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

        Instant scheduledDate = batch.getStartDate();
        if (isAdmin) {
            cancellationPolicy.validateAdminCancellation(scheduledDate, now);
        } else {
            cancellationPolicy.validateTraineeCancellation(scheduledDate, now);
        }
    }

    public void validateReschedulePolicy(String batchId, boolean isAdmin, Instant now) {
        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

        Instant scheduledDate = batch.getStartDate();
        if (isAdmin) {
            cancellationPolicy.validateAdminReschedule(scheduledDate, now);
        } else {
            cancellationPolicy.validateTraineeReschedule(scheduledDate, now);
        }
    }

    public CancellationPolicy getCancellationPolicy() {
        return cancellationPolicy;
    }

    public CapacityPolicy getCapacityPolicy() {
        return capacityPolicy;
    }
}
