package com.sporekart.modules.training;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.training.application.CapacityApplicationService;
import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.event.TrainingEnrollmentCreatedEvent;
import com.sporekart.modules.training.domain.exception.*;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EnrollmentApplicationServiceTest {

    private TrainingEnrollmentRepository enrollmentRepository;
    private TrainingBatchRepository batchRepository;
    private CapacityApplicationService capacityService;
    private ApplicationEventPublisher eventPublisher;
    private SecurityAuditService auditService;
    private EnrollmentApplicationService service;

    @BeforeEach
    void setUp() {
        enrollmentRepository = mock(TrainingEnrollmentRepository.class);
        batchRepository = mock(TrainingBatchRepository.class);
        capacityService = mock(CapacityApplicationService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        auditService = mock(SecurityAuditService.class);

        service = new EnrollmentApplicationService(
                enrollmentRepository,
                batchRepository,
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
        assertEquals(EnrollmentStatus.PENDING, created.getStatus());

        verify(batchRepository).tryAllocateSeatAtomic(batchId);
        verify(enrollmentRepository).save(any(TrainingEnrollment.class));
        verify(eventPublisher).publishEvent(any(TrainingEnrollmentCreatedEvent.class));
        verify(auditService).logEvent(eq(AuditEventType.SECURITY_SYSTEM_ALERT), eq(traineeId), eq(created.getId()), anyString(), anyString(), eq(com.sporekart.modules.security.domain.AuditStatus.SUCCESS), anyString());
    }

    @Test
    @DisplayName("enrollTrainee() returns previous enrollment idempotently when idempotencyKey matches")
    void testEnrollTraineeIdempotentReplay() {
        String key = "key-repeat-1";
        TrainingEnrollment existing = TrainingEnrollment.create("batch-1", "trainee-1", key, "trainee-1");

        when(enrollmentRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        TrainingEnrollment replayed = service.enrollTrainee("batch-1", "trainee-1", key);

        assertSame(existing, replayed);
        verify(batchRepository, never()).tryAllocateSeatAtomic(anyString());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enrollTrainee() throws DuplicateEnrollmentException if trainee already enrolled in batch")
    void testEnrollTraineeDuplicateRejection() {
        String batchId = "batch-1";
        String traineeId = "trainee-1";
        TrainingBatch batch = TrainingBatch.create("prog-1", "B-1", Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS), 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");

        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(enrollmentRepository.existsByBatchIdAndTraineeId(batchId, traineeId)).thenReturn(true);

        assertThrows(DuplicateEnrollmentException.class, () -> service.enrollTrainee(batchId, traineeId, null));
        verify(batchRepository, never()).tryAllocateSeatAtomic(anyString());
    }

    @Test
    @DisplayName("enrollTrainee() throws BatchFullException when atomic capacity allocation fails")
    void testEnrollTraineeCapacityFullRejection() {
        String batchId = "batch-1";
        String traineeId = "trainee-1";
        TrainingBatch batch = TrainingBatch.create("prog-1", "B-1", Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS), 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");

        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(enrollmentRepository.existsByBatchIdAndTraineeId(batchId, traineeId)).thenReturn(false);
        when(batchRepository.tryAllocateSeatAtomic(batchId)).thenReturn(false);

        assertThrows(BatchFullException.class, () -> service.enrollTrainee(batchId, traineeId, null));
    }

    @Test
    @DisplayName("getEnrollmentById() throws UnauthorizedEnrollmentAccessException on IDOR attempt")
    void testGetEnrollmentByIdIdorProtection() {
        TrainingEnrollment enrollment = TrainingEnrollment.create("batch-1", "trainee-alice@example.com");

        when(enrollmentRepository.findById("enr-123")).thenReturn(Optional.of(enrollment));

        // Alice accessing her own enrollment succeeds
        assertNotNull(service.getEnrollmentById("enr-123", "trainee-alice@example.com", false));

        // Bob accessing Alice's enrollment throws UnauthorizedEnrollmentAccessException
        assertThrows(UnauthorizedEnrollmentAccessException.class, () ->
                service.getEnrollmentById("enr-123", "trainee-bob@example.com", false));

        // Admin accessing Alice's enrollment succeeds
        assertNotNull(service.getEnrollmentById("enr-123", "trainee-bob@example.com", true));
    }
}
