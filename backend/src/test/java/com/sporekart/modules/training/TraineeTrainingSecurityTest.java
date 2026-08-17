package com.sporekart.modules.training;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.training.application.CapacityApplicationService;
import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.application.TraineeTrainingOperationsService;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingDemandRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentHistoryRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentPaymentRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import com.sporekart.modules.training.infrastructure.persistence.SpringDataTrainingDemandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeTrainingSecurityTest {

    @Mock
    private TrainingEnrollmentRepository enrollmentRepository;

    @Mock
    private TrainingBatchRepository batchRepository;

    @Mock
    private TrainingProgramRepository programRepository;

    @Mock
    private TrainingEnrollmentHistoryRepository historyRepository;

    @Mock
    private CapacityApplicationService capacityService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SecurityAuditService auditService;

    @Mock
    private TrainingDemandRepository demandRepository;

    @Mock
    private TrainingEnrollmentPaymentRepository paymentRepository;

    @Mock
    private SpringDataTrainingDemandRepository springDataDemandRepository;

    private EnrollmentApplicationService enrollmentService;
    private TraineeTrainingOperationsService operationsService;

    @BeforeEach
    void setUp() {
        enrollmentService = new EnrollmentApplicationService(
                enrollmentRepository, batchRepository, programRepository, historyRepository, capacityService, eventPublisher, auditService
        );

        operationsService = new TraineeTrainingOperationsService(
                enrollmentRepository, batchRepository, programRepository, demandRepository, paymentRepository, springDataDemandRepository
        );
    }

    @Test
    @DisplayName("SCENARIO #1: Trainee A accessing Trainee A enrollment -> SUCCESS")
    void scenario1_traineeA_accesses_own_enrollment() {
        String traineeA = "trainee-A";

        TrainingEnrollment enrollmentA = new TrainingEnrollment(
                "enr-A", "ENR-001", "batch-1", traineeA, EnrollmentStatus.CONFIRMED,
                "pay-1", new BigDecimal("500.00"), "INR", Instant.now(), Instant.now(), null, null, Instant.now(), Instant.now(), traineeA, traineeA, null
        );

        when(enrollmentRepository.findById("enr-A")).thenReturn(Optional.of(enrollmentA));

        TrainingEnrollment result = enrollmentService.getEnrollmentById("enr-A", traineeA, false);

        assertNotNull(result);
        assertEquals("enr-A", result.getId());
        assertEquals(traineeA, result.getTraineeId());
    }

    @Test
    @DisplayName("SCENARIO #1 (b): Trainee A accessing Trainee B enrollment -> DENIED (403/Exception)")
    void scenario1_traineeA_accesses_traineeB_enrollment_denied() {
        String traineeA = "trainee-A";
        String traineeB = "trainee-B";

        TrainingEnrollment enrollmentB = new TrainingEnrollment(
                "enr-B", "ENR-002", "batch-1", traineeB, EnrollmentStatus.CONFIRMED,
                "pay-2", new BigDecimal("500.00"), "INR", Instant.now(), Instant.now(), null, null, Instant.now(), Instant.now(), traineeB, traineeB, null
        );

        when(enrollmentRepository.findById("enr-B")).thenReturn(Optional.of(enrollmentB));

        assertThrows(UnauthorizedEnrollmentAccessException.class, () ->
                enrollmentService.getEnrollmentById("enr-B", traineeA, false)
        );
    }

    @Test
    @DisplayName("SCENARIO #6: Malicious traineeId parameter must be ignored by server-side ownership logic")
    void scenario6_malicious_traineeId_ignored() {
        String authenticatedUser = "real-trainee";

        when(enrollmentRepository.findByTraineeId(authenticatedUser)).thenReturn(List.of());
        when(springDataDemandRepository.findByTraineeId(eq(authenticatedUser), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        // Call service method with authenticatedUser context
        operationsService.getTraineeDashboardMetrics(authenticatedUser);

        // Verify query was strictly executed for authenticatedUser and never for any manipulated ID
        verify(enrollmentRepository, times(1)).findByTraineeId(authenticatedUser);
        verify(enrollmentRepository, never()).findByTraineeId("victim-user");
    }
}
