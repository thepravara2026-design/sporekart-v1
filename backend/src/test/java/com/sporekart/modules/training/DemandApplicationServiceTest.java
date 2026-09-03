package com.sporekart.modules.training;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.training.application.DemandApplicationService;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.Capacity;
import com.sporekart.modules.training.domain.DemandStatus;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingDemandRequest;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.exception.DuplicateDemandException;
import com.sporekart.modules.training.domain.exception.DuplicateEnrollmentException;
import com.sporekart.modules.training.domain.exception.InvalidDemandStateException;
import com.sporekart.modules.training.domain.exception.UnauthorizedDemandAccessException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingDemandRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DemandApplicationServiceTest {

    private TrainingDemandRepository demandRepository;
    private TrainingBatchRepository batchRepository;
    private TrainingEnrollmentRepository enrollmentRepository;
    private SecurityAuditService auditService;
    private ApplicationEventPublisher eventPublisher;
    private DemandApplicationService demandService;

    @BeforeEach
    void setUp() {
        demandRepository = mock(TrainingDemandRepository.class);
        batchRepository = mock(TrainingBatchRepository.class);
        enrollmentRepository = mock(TrainingEnrollmentRepository.class);
        auditService = mock(SecurityAuditService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        demandService = new DemandApplicationService(
                demandRepository,
                batchRepository,
                enrollmentRepository,
                auditService,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Should successfully create demand request when batch is full")
    void testCreateDemandSuccess() {
        TrainingBatch fullBatch = mock(TrainingBatch.class);
        Capacity capacity = mock(Capacity.class);
        when(capacity.isFull()).thenReturn(true);
        when(fullBatch.getCapacity()).thenReturn(capacity);
        when(fullBatch.getStatus()).thenReturn(BatchStatus.ACTIVE);
        when(batchRepository.findById("batch-full")).thenReturn(Optional.of(fullBatch));

        when(enrollmentRepository.findByBatchIdAndTraineeId("batch-full", "trainee-john@sporekart.com"))
                .thenReturn(Optional.empty());
        when(demandRepository.existsByBatchIdAndTraineeIdAndStatus("batch-full", "trainee-john@sporekart.com", DemandStatus.ACTIVE))
                .thenReturn(false);

        when(demandRepository.save(any(TrainingDemandRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TrainingDemandRequest result = demandService.createDemand("batch-full", "trainee-john@sporekart.com");

        assertNotNull(result);
        assertEquals("batch-full", result.getBatchId());
        assertEquals("trainee-john@sporekart.com", result.getTraineeId());
        assertEquals(DemandStatus.ACTIVE, result.getStatus());
    }

    @Test
    @DisplayName("Should reject demand creation if batch has available capacity (Redirect to enrollment)")
    void testRejectDemandWhenSlotsAvailable() {
        TrainingBatch openBatch = mock(TrainingBatch.class);
        Capacity capacity = mock(Capacity.class);
        when(capacity.isFull()).thenReturn(false); // Slots are available!
        when(openBatch.getCapacity()).thenReturn(capacity);
        when(openBatch.getStatus()).thenReturn(BatchStatus.ACTIVE);
        when(batchRepository.findById("batch-open")).thenReturn(Optional.of(openBatch));

        assertThrows(InvalidDemandStateException.class, () ->
                demandService.createDemand("batch-open", "trainee-john@sporekart.com")
        );
    }

    @Test
    @DisplayName("Should reject demand creation if active demand already exists for trainee")
    void testRejectDuplicateActiveDemand() {
        TrainingBatch fullBatch = mock(TrainingBatch.class);
        Capacity capacity = mock(Capacity.class);
        when(capacity.isFull()).thenReturn(true);
        when(fullBatch.getCapacity()).thenReturn(capacity);
        when(fullBatch.getStatus()).thenReturn(BatchStatus.ACTIVE);
        when(batchRepository.findById("batch-full")).thenReturn(Optional.of(fullBatch));

        when(enrollmentRepository.findByBatchIdAndTraineeId("batch-full", "trainee-john@sporekart.com"))
                .thenReturn(Optional.empty());
        when(demandRepository.existsByBatchIdAndTraineeIdAndStatus("batch-full", "trainee-john@sporekart.com", DemandStatus.ACTIVE))
                .thenReturn(true);

        assertThrows(DuplicateDemandException.class, () ->
                demandService.createDemand("batch-full", "trainee-john@sporekart.com")
        );
    }

    @Test
    @DisplayName("Should reject demand creation if trainee already has an active enrollment")
    void testRejectDemandWhenAlreadyEnrolled() {
        TrainingBatch fullBatch = mock(TrainingBatch.class);
        Capacity capacity = mock(Capacity.class);
        when(capacity.isFull()).thenReturn(true);
        when(fullBatch.getCapacity()).thenReturn(capacity);
        when(fullBatch.getStatus()).thenReturn(BatchStatus.ACTIVE);
        when(batchRepository.findById("batch-full")).thenReturn(Optional.of(fullBatch));

        TrainingEnrollment activeEnrollment = mock(TrainingEnrollment.class);
        when(activeEnrollment.getStatus()).thenReturn(EnrollmentStatus.CONFIRMED);
        when(enrollmentRepository.findByBatchIdAndTraineeId("batch-full", "trainee-john@sporekart.com"))
                .thenReturn(Optional.of(activeEnrollment));

        assertThrows(DuplicateEnrollmentException.class, () ->
                demandService.createDemand("batch-full", "trainee-john@sporekart.com")
        );
    }

    @Test
    @DisplayName("Should throw UnauthorizedDemandAccessException when non-owner attempts to withdraw demand")
    void testWithdrawDemandUnauthorized() {
        TrainingDemandRequest demand = TrainingDemandRequest.create("batch-101", "trainee-alice@sporekart.com", "trainee-alice@sporekart.com");
        when(demandRepository.findById(demand.getId())).thenReturn(Optional.of(demand));

        assertThrows(UnauthorizedDemandAccessException.class, () ->
                demandService.withdrawDemand(demand.getId(), "trainee-bob@sporekart.com", false)
        );
    }
}
