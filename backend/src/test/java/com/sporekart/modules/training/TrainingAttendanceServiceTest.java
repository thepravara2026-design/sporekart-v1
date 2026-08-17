package com.sporekart.modules.training;

import com.sporekart.modules.training.application.TrainingAttendanceService;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.port.TrainingAttendanceRepository;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingAttendanceServiceTest {

    @Mock private TrainingAttendanceRepository attendanceRepository;
    @Mock private TrainingBatchRepository batchRepository;
    @Mock private TrainingEnrollmentRepository enrollmentRepository;

    private TrainingAttendanceService attendanceService;

    private static final String BATCH_ID = "batch-101";
    private static final String SCHEDULE_ID = "sched-1";
    private static final String ENROLLMENT_ID = "enr-777";
    private static final String TRAINEE_ID = "trainee-123";

    @BeforeEach
    void setUp() {
        attendanceService = new TrainingAttendanceService(attendanceRepository, batchRepository, enrollmentRepository);
    }

    @Test
    @DisplayName("markRosterAttendance creates or updates attendance for trainees")
    void markRosterAttendanceSavesAttendance() {
        TrainingBatch batch = createTestBatch(BATCH_ID);
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_ID);

        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(enrollmentRepository.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
        when(attendanceRepository.findByEnrollmentIdAndScheduleId(ENROLLMENT_ID, SCHEDULE_ID)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<TrainingAttendanceService.AttendanceItem> items = List.of(
                new TrainingAttendanceService.AttendanceItem(ENROLLMENT_ID, AttendanceStatus.PRESENT, "Good participation")
        );

        List<TrainingAttendance> result = attendanceService.markRosterAttendance(BATCH_ID, SCHEDULE_ID, items, "ADMIN");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(result.get(0).getMarkedBy()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("getTraineeAttendanceSummary calculates correct attendance percentage")
    void getTraineeAttendanceSummaryCalculatesPercentage() {
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_ID);
        TrainingBatch batch = createTestBatch(BATCH_ID);
        TrainingAttendance att1 = TrainingAttendance.mark(ENROLLMENT_ID, SCHEDULE_ID, BATCH_ID, TRAINEE_ID, AttendanceStatus.PRESENT, null, "ADMIN");
        TrainingAttendance att2 = TrainingAttendance.mark(ENROLLMENT_ID, "sched-2", BATCH_ID, TRAINEE_ID, AttendanceStatus.ABSENT, null, "ADMIN");

        when(enrollmentRepository.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(attendanceRepository.findByEnrollmentId(ENROLLMENT_ID)).thenReturn(List.of(att1, att2));

        TrainingAttendanceService.AttendanceSummary summary =
                attendanceService.getTraineeAttendanceSummary(ENROLLMENT_ID, TRAINEE_ID);

        assertThat(summary.attendedSessions()).isEqualTo(1);
        assertThat(summary.attendancePercentage()).isEqualTo(50.0);
    }

    private TrainingBatch createTestBatch(String id) {
        BatchSchedule s1 = new BatchSchedule(SCHEDULE_ID, id, "Day 1", Instant.now(), 60, "Room 1", Instant.now(), Instant.now());
        BatchSchedule s2 = new BatchSchedule("sched-2", id, "Day 2", Instant.now().plus(Duration.ofDays(1)), 60, "Room 1", Instant.now(), Instant.now());
        return new TrainingBatch(
                id, "PROG-1", "BATCH-001", Instant.now(), Instant.now().plus(Duration.ofDays(5)),
                new Capacity(10, 5), BatchStatus.ACTIVE, DeliveryMode.ONLINE,
                "Zoom Link", "https://meeting.com", "UTC", "ADMIN", "ADMIN", List.of(s1, s2), Instant.now(), Instant.now()
        );
    }

    private TrainingEnrollment createTestEnrollment(String id, String batchId, String traineeId) {
        return new TrainingEnrollment(
                id, "ENR-001", batchId, traineeId, EnrollmentStatus.CONFIRMED, "PAY-REF-100",
                BigDecimal.valueOf(499), "USD", Instant.now(), Instant.now(), null, null,
                Instant.now(), Instant.now(), "TRAINEE", "TRAINEE", "IDEMP-1"
        );
    }
}
