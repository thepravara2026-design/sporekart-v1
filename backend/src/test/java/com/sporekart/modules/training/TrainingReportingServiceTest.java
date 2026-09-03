package com.sporekart.modules.training;

import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.training.application.reporting.TrainingReportingService;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingReportingServiceTest {

    @Mock private TrainingProgramRepository programRepository;
    @Mock private TrainingBatchRepository batchRepository;
    @Mock private TrainingEnrollmentRepository enrollmentRepository;
    @Mock private TrainingEnrollmentPaymentRepository paymentRepository;
    @Mock private TrainingAttendanceRepository attendanceRepository;
    @Mock private TrainingCertificateRepository certificateRepository;
    @Mock private SpringDataJpaNotificationRepository notificationRepository;

    private TrainingReportingService reportingService;

    @BeforeEach
    void setUp() {
        reportingService = new TrainingReportingService(
                programRepository, batchRepository, enrollmentRepository,
                paymentRepository, attendanceRepository, certificateRepository, notificationRepository
        );
    }

    @Test
    @DisplayName("getExecutiveOverview calculates correct metrics from domain repositories")
    void getExecutiveOverviewCalculatesMetrics() {
        TrainingBatch b1 = createTestBatch("b-1", 20, 15);
        TrainingBatch b2 = createTestBatch("b-2", 10, 10);

        TrainingEnrollment e1 = createTestEnrollment("e-1", "b-1", EnrollmentStatus.CONFIRMED);
        TrainingEnrollment e2 = createTestEnrollment("e-2", "b-2", EnrollmentStatus.COMPLETED);

        TrainingEnrollmentPayment p1 = new TrainingEnrollmentPayment("p-1", "TRN-PAY-1", "b-1", "trainee-1", "e-1", BigDecimal.valueOf(500), "INR", TrainingPaymentStatus.VERIFIED, Instant.now(), Instant.now(), "TRAINEE", "TRAINEE");
        TrainingEnrollmentPayment p2 = new TrainingEnrollmentPayment("p-2", "TRN-PAY-2", "b-2", "trainee-1", "e-2", BigDecimal.valueOf(100), "INR", TrainingPaymentStatus.FAILED, Instant.now(), Instant.now(), "TRAINEE", "TRAINEE");

        when(batchRepository.findAll()).thenReturn(List.of(b1, b2));
        when(enrollmentRepository.findAll()).thenReturn(List.of(e1, e2));
        when(paymentRepository.findAll()).thenReturn(List.of(p1, p2));

        TrainingReportingService.ExecutiveOverview overview = reportingService.getExecutiveOverview();

        assertThat(overview.totalBatches()).isEqualTo(2);
        assertThat(overview.fullBatches()).isEqualTo(1);
        assertThat(overview.totalCapacitySeats()).isEqualTo(30);
        assertThat(overview.totalOccupiedSeats()).isEqualTo(25);
        assertThat(overview.seatUtilizationPercentage()).isEqualTo(83.33);
        assertThat(overview.grossRevenue()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(overview.totalRefunds()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(overview.netRevenue()).isEqualByComparingTo(BigDecimal.valueOf(400));
    }

    private TrainingBatch createTestBatch(String id, int totalSeats, int occupiedSeats) {
        return new TrainingBatch(
                id, "PROG-1", "BATCH-" + id, Instant.now(), Instant.now().plus(Duration.ofDays(5)),
                new Capacity(totalSeats, occupiedSeats), BatchStatus.ACTIVE, DeliveryMode.ONLINE,
                "Zoom Link", "https://meeting.com", "UTC", "ADMIN", "ADMIN", List.of(), Instant.now(), Instant.now()
        );
    }

    private TrainingEnrollment createTestEnrollment(String id, String batchId, EnrollmentStatus status) {
        return new TrainingEnrollment(
                id, "ENR-" + id, batchId, "trainee-1", status, "PAY-REF-100",
                BigDecimal.valueOf(499), "INR", Instant.now(), Instant.now(), null, null,
                Instant.now(), Instant.now(), "TRAINEE", "TRAINEE", "IDEMP-1"
        );
    }
}
