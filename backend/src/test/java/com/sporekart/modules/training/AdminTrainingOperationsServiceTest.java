package com.sporekart.modules.training;

import com.sporekart.modules.training.application.AdminTrainingOperationsService;
import com.sporekart.modules.training.controller.dto.AdminTrainingDashboardResponse;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.DemandStatus;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingDemandRequest;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingDemandRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentPaymentRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import com.sporekart.modules.training.infrastructure.persistence.SpringDataTrainingBatchRepository;
import com.sporekart.modules.training.infrastructure.persistence.SpringDataTrainingEnrollmentPaymentRepository;
import com.sporekart.modules.training.infrastructure.persistence.SpringDataTrainingProgramRepository;
import com.sporekart.modules.training.infrastructure.persistence.TrainingBatchEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTrainingOperationsServiceTest {

    @Mock
    private TrainingProgramRepository programRepository;
    @Mock
    private TrainingBatchRepository batchRepository;
    @Mock
    private TrainingEnrollmentRepository enrollmentRepository;
    @Mock
    private TrainingDemandRepository demandRepository;
    @Mock
    private TrainingEnrollmentPaymentRepository paymentRepository;
    @Mock
    private SpringDataTrainingProgramRepository springDataProgramRepository;
    @Mock
    private SpringDataTrainingBatchRepository springDataBatchRepository;
    @Mock
    private SpringDataTrainingEnrollmentPaymentRepository springDataPaymentRepository;

    private AdminTrainingOperationsService operationsService;

    @BeforeEach
    void setUp() {
        operationsService = new AdminTrainingOperationsService(
                programRepository,
                batchRepository,
                enrollmentRepository,
                demandRepository,
                paymentRepository,
                springDataProgramRepository,
                springDataBatchRepository,
                springDataPaymentRepository
        );
    }

    @Test
    @DisplayName("getDashboardMetrics should calculate aggregated training statistics correctly")
    void testGetDashboardMetrics() {
        when(springDataProgramRepository.count()).thenReturn(3L);

        TrainingBatch batch1 = new TrainingBatch(
                "batch-1", "prog-1", "B-001", Instant.now(), Instant.now().plus(5, ChronoUnit.DAYS),
                new com.sporekart.modules.training.domain.Capacity(20, 18),
                BatchStatus.ACTIVE, com.sporekart.modules.training.domain.DeliveryMode.ONLINE, null, null, "UTC", "admin", "admin", List.of(), Instant.now(), Instant.now()
        );

        TrainingBatch batch2 = new TrainingBatch(
                "batch-2", "prog-1", "B-002", Instant.now(), Instant.now().plus(5, ChronoUnit.DAYS),
                new com.sporekart.modules.training.domain.Capacity(10, 2),
                BatchStatus.SCHEDULED, com.sporekart.modules.training.domain.DeliveryMode.ONLINE, null, null, "UTC", "admin", "admin", List.of(), Instant.now(), Instant.now()
        );

        TrainingBatchEntity b1 = TrainingBatchEntity.fromDomain(batch1);
        TrainingBatchEntity b2 = TrainingBatchEntity.fromDomain(batch2);
        when(springDataBatchRepository.findAll()).thenReturn(List.of(b1, b2));



        when(demandRepository.countByStatus(DemandStatus.ACTIVE)).thenReturn(4L);
        when(enrollmentRepository.searchEnrollments(null, null, null, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));
        when(enrollmentRepository.countByStatus(EnrollmentStatus.PAYMENT_PENDING)).thenReturn(2L);
        when(enrollmentRepository.countByStatus(EnrollmentStatus.REJECTED)).thenReturn(1L);
        when(springDataPaymentRepository.findAll()).thenReturn(List.of());

        AdminTrainingDashboardResponse dashboard = operationsService.getDashboardMetrics();

        assertThat(dashboard.getActiveProgramsCount()).isEqualTo(3L);
        assertThat(dashboard.getUpcomingBatchesCount()).isEqualTo(2L);
        assertThat(dashboard.getTotalConfiguredCapacity()).isEqualTo(30L);
        assertThat(dashboard.getTotalOccupiedSeats()).isEqualTo(20L);
        assertThat(dashboard.getTotalRemainingSeats()).isEqualTo(10L);
        assertThat(dashboard.getActiveDemandCount()).isEqualTo(4L);
        assertThat(dashboard.getBatchesApproachingFullCount()).isEqualTo(1L);
    }
}
