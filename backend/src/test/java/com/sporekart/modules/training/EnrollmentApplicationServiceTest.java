package com.sporekart.modules.training;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.training.application.CapacityApplicationService;
import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.event.TrainingEnrollmentCreatedEvent;
import com.sporekart.modules.training.domain.exception.*;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentHistoryRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EnrollmentApplicationServiceTest {

    private TrainingEnrollmentRepository enrollmentRepository;
    private TrainingBatchRepository batchRepository;
    private TrainingProgramRepository programRepository;
    private TrainingEnrollmentHistoryRepository historyRepository;
    private CapacityApplicationService capacityService;
    private ApplicationEventPublisher eventPublisher;
    private SecurityAuditService auditService;
    private EnrollmentApplicationService service;

    @BeforeEach
    void setUp() {
        enrollmentRepository = mock(TrainingEnrollmentRepository.class);
        batchRepository = mock(TrainingBatchRepository.class);
        programRepository = mock(TrainingProgramRepository.class);
        historyRepository = mock(TrainingEnrollmentHistoryRepository.class);
        capacityService = mock(CapacityApplicationService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        auditService = mock(SecurityAuditService.class);

        TrainingProgram defaultProgram = TrainingProgram.create("Default Title", "Desc", BigDecimal.ZERO, "INR");
        when(programRepository.findById(anyString())).thenReturn(Optional.of(defaultProgram));
        when(historyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service = new EnrollmentApplicationService(
                enrollmentRepository,
                batchRepository,
                programRepository,
                historyRepository,
                capacityService,
                eventPublisher,
                auditService
        );
    }

    @Test
    @DisplayName("enrollTrainee() successfully allocates slot, saves enrollment, emits event, and logs audit")
    void testEnrollTraineeSuccess() {
        String batchId = "batch-100";
        String traineeId = "trainee-john@example.com";
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);

        TrainingBatch batch = TrainingBatch.create("prog-1", "BCODE-1", start, end, 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        batch.activate();

        when(enrollmentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(enrollmentRepository.existsByBatchIdAndTraineeId(batchId, traineeId)).thenReturn(false);
        when(batchRepository.tryAllocateSeatAtomic(batchId)).thenReturn(true);
        when(enrollmentRepository.save(any(TrainingEnrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        TrainingEnrollment created = service.enrollTrainee(batchId, traineeId, "idempotency-key-1");

        assertNotNull(created);
        assertEquals(batchId, created.getBatchId());
        assertEquals(traineeId, created.getTraineeId());
        verify(batchRepository).tryAllocateSeatAtomic(batchId);
        verify(enrollmentRepository).save(any(TrainingEnrollment.class));
        verify(eventPublisher).publishEvent(any(TrainingEnrollmentCreatedEvent.class));
    }

    @Test
    @DisplayName("enrollTrainee() throws BatchFullException when batch capacity is FULL")
    void testEnrollTraineeBatchFull() {
        String batchId = "batch-full";
        String traineeId = "trainee-john@example.com";
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);

        TrainingBatch batch = TrainingBatch.create("prog-1", "BCODE-FULL", start, end, 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        batch.activate();
        for (int i = 0; i < 10; i++) {
            batch.allocateSeat();
        }

        when(enrollmentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

        assertThrows(BatchFullException.class, () -> service.enrollTrainee(batchId, traineeId, null));
    }

    @Test
    @DisplayName("enrollTrainee() throws DuplicateEnrollmentException when trainee already enrolled")
    void testEnrollTraineeDuplicate() {
        String batchId = "batch-100";
        String traineeId = "trainee-john@example.com";
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);

        TrainingBatch batch = TrainingBatch.create("prog-1", "BCODE-1", start, end, 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        batch.activate();

        when(enrollmentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(enrollmentRepository.existsByBatchIdAndTraineeId(batchId, traineeId)).thenReturn(true);

        assertThrows(DuplicateEnrollmentException.class, () -> service.enrollTrainee(batchId, traineeId, null));
    }

    @Test
    @DisplayName("getEnrollmentById() returns enrollment for owner trainee")
    void testGetEnrollmentByIdSuccess() {
        String enrollmentId = "enr-1";
        TrainingEnrollment enrollment = TrainingEnrollment.create("batch-1", "trainee-1");

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        TrainingEnrollment result = service.getEnrollmentById(enrollmentId, "trainee-1", false);
        assertEquals(enrollment, result);
    }

    @Test
    @DisplayName("getEnrollmentById() throws UnauthorizedEnrollmentAccessException when non-admin accesses another trainee's enrollment")
    void testGetEnrollmentByIdUnauthorized() {
        String enrollmentId = "enr-1";
        TrainingEnrollment enrollment = TrainingEnrollment.create("batch-1", "trainee-1");

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        assertThrows(UnauthorizedEnrollmentAccessException.class, () -> service.getEnrollmentById(enrollmentId, "trainee-2", false));
    }
}
