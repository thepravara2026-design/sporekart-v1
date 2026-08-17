package com.sporekart.modules.training;

import com.sporekart.modules.training.application.TraineeTrainingOperationsService;
import com.sporekart.modules.training.controller.dto.TraineeEnrollmentDetailResponse;
import com.sporekart.modules.training.controller.dto.TraineeTrainingDashboardResponse;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingDemandRepository;
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
class TraineeTrainingOperationsServiceTest {

    @Mock
    private TrainingEnrollmentRepository enrollmentRepository;

    @Mock
    private TrainingBatchRepository batchRepository;

    @Mock
    private TrainingProgramRepository programRepository;

    @Mock
    private TrainingDemandRepository demandRepository;

    @Mock
    private TrainingEnrollmentPaymentRepository paymentRepository;

    @Mock
    private SpringDataTrainingDemandRepository springDataDemandRepository;

    private TraineeTrainingOperationsService operationsService;

    @BeforeEach
    void setUp() {
        operationsService = new TraineeTrainingOperationsService(
                enrollmentRepository,
                batchRepository,
                programRepository,
                demandRepository,
                paymentRepository,
                springDataDemandRepository
        );
    }

    @Test
    @DisplayName("Should return accurate dashboard metrics for authenticated trainee")
    void shouldReturnDashboardMetricsForTrainee() {
        String traineeId = "trainee-123";

        TrainingEnrollment enrollment1 = new TrainingEnrollment(
                "enr-1", "ENR-001", "batch-1", traineeId, EnrollmentStatus.CONFIRMED,
                "pay-1", new BigDecimal("500.00"), "INR", Instant.now(), Instant.now(), null, null, Instant.now(), Instant.now(), traineeId, traineeId, null
        );

        TrainingEnrollment enrollment2 = new TrainingEnrollment(
                "enr-2", "ENR-002", "batch-2", traineeId, EnrollmentStatus.PENDING,
                null, new BigDecimal("500.00"), "INR", Instant.now(), null, null, null, Instant.now(), Instant.now(), traineeId, traineeId, null
        );

        when(enrollmentRepository.findByTraineeId(traineeId)).thenReturn(List.of(enrollment1, enrollment2));
        when(springDataDemandRepository.findByTraineeId(eq(traineeId), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        TrainingBatch batch1 = new TrainingBatch(
                "batch-1", "prog-1", "BATCH-001", Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                new Capacity(20, 5), BatchStatus.ACTIVE, DeliveryMode.ONLINE, "Online Venue", "https://zoom.us/j/123", "UTC", traineeId, traineeId, List.of(), Instant.now(), Instant.now()
        );
        when(batchRepository.findById("batch-1")).thenReturn(Optional.of(batch1));

        TrainingProgram program1 = new TrainingProgram(
                "prog-1", "spring-boot-advanced", "Spring Boot Advanced", "Advanced Spring Boot", "Backend",
                40, ProgramStatus.ACTIVE, new BigDecimal("500.00"), "INR", traineeId, traineeId, Instant.now(), Instant.now()
        );
        when(programRepository.findById("prog-1")).thenReturn(Optional.of(program1));

        TraineeTrainingDashboardResponse response = operationsService.getTraineeDashboardMetrics(traineeId);

        assertNotNull(response);
        assertEquals(1, response.getUpcomingEnrollmentsCount());
        assertEquals(1, response.getPendingEnrollmentsCount());
        assertEquals(0, response.getActiveDemandRequestsCount());
        assertEquals("Spring Boot Advanced", response.getNextUpcomingSessionTitle());
        assertEquals("BATCH-001", response.getNextUpcomingBatchCode());
    }

    @Test
    @DisplayName("Should throw UnauthorizedEnrollmentAccessException if trainee tries to access another trainee's enrollment detail")
    void shouldPreventIdorOnEnrollmentDetail() {
        String authenticatedTrainee = "trainee-A";
        String victimTrainee = "trainee-B";

        TrainingEnrollment victimEnrollment = new TrainingEnrollment(
                "enr-victim", "ENR-999", "batch-1", victimTrainee, EnrollmentStatus.CONFIRMED,
                "pay-99", new BigDecimal("500.00"), "INR", Instant.now(), Instant.now(), null, null, Instant.now(), Instant.now(), victimTrainee, victimTrainee, null
        );

        when(enrollmentRepository.findById("enr-victim")).thenReturn(Optional.of(victimEnrollment));

        assertThrows(UnauthorizedEnrollmentAccessException.class, () ->
                operationsService.getTraineeEnrollmentDetail("enr-victim", authenticatedTrainee)
        );
    }

    @Test
    @DisplayName("Should successfully return enrollment detail when owned by authenticated trainee")
    void shouldReturnDetailForOwnedEnrollment() {
        String traineeId = "trainee-123";

        TrainingEnrollment enrollment = new TrainingEnrollment(
                "enr-1", "ENR-001", "batch-1", traineeId, EnrollmentStatus.CONFIRMED,
                "pay-1", new BigDecimal("500.00"), "INR", Instant.now(), Instant.now(), null, null, Instant.now(), Instant.now(), traineeId, traineeId, null
        );

        TrainingBatch batch = new TrainingBatch(
                "batch-1", "prog-1", "BATCH-001", Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                new Capacity(20, 5), BatchStatus.ACTIVE, DeliveryMode.ONLINE, "Online Venue", "https://zoom.us/j/123", "UTC", traineeId, traineeId, List.of(), Instant.now(), Instant.now()
        );

        TrainingProgram program = new TrainingProgram(
                "prog-1", "spring-boot-advanced", "Spring Boot Advanced", "Advanced Spring Boot", "Backend",
                40, ProgramStatus.ACTIVE, new BigDecimal("500.00"), "INR", traineeId, traineeId, Instant.now(), Instant.now()
        );

        when(enrollmentRepository.findById("enr-1")).thenReturn(Optional.of(enrollment));
        when(batchRepository.findById("batch-1")).thenReturn(Optional.of(batch));
        when(programRepository.findById("prog-1")).thenReturn(Optional.of(program));

        TraineeEnrollmentDetailResponse response = operationsService.getTraineeEnrollmentDetail("enr-1", traineeId);

        assertNotNull(response);
        assertEquals("enr-1", response.getId());
        assertEquals("Spring Boot Advanced", response.getProgramTitle());
        assertEquals("BATCH-001", response.getBatchCode());
        assertEquals("CONFIRMED", response.getEnrollmentStatus());
    }
}
