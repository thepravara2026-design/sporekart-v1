package com.sporekart.modules.training.application;

import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.exception.BatchNotFoundException;
import com.sporekart.modules.training.domain.exception.EnrollmentNotFoundException;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import com.sporekart.modules.training.domain.port.TrainingAttendanceRepository;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TrainingAttendanceService {

    private static final Logger log = LoggerFactory.getLogger(TrainingAttendanceService.class);

    private final TrainingAttendanceRepository attendanceRepository;
    private final TrainingBatchRepository batchRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;

    public TrainingAttendanceService(
            TrainingAttendanceRepository attendanceRepository,
            TrainingBatchRepository batchRepository,
            TrainingEnrollmentRepository enrollmentRepository) {
        this.attendanceRepository = Objects.requireNonNull(attendanceRepository, "attendanceRepository must not be null");
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository must not be null");
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository must not be null");
    }

    @Transactional
    public List<TrainingAttendance> markRosterAttendance(
            String batchId, String scheduleId, List<AttendanceItem> items, String actor) {
        log.info("Marking attendance roster for batchId={}, scheduleId={} by actor={}", batchId, scheduleId, actor);

        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("Training batch not found for id: " + batchId));

        boolean scheduleExists = batch.getSchedules().stream()
                .anyMatch(s -> s.getId().equals(scheduleId));
        if (!scheduleExists) {
            log.warn("Schedule id={} declared for batchId={} roster marking", scheduleId, batchId);
        }

        List<TrainingAttendance> updated = new ArrayList<>();

        for (AttendanceItem item : items) {
            TrainingEnrollment enrollment = enrollmentRepository.findById(item.enrollmentId())
                    .orElseThrow(() -> new EnrollmentNotFoundException("Enrollment not found for id: " + item.enrollmentId()));

            Optional<TrainingAttendance> existingOpt = attendanceRepository.findByEnrollmentIdAndScheduleId(item.enrollmentId(), scheduleId);

            TrainingAttendance record;
            if (existingOpt.isPresent()) {
                record = existingOpt.get();
                record.updateStatus(item.status(), item.notes(), actor);
            } else {
                record = TrainingAttendance.mark(
                        item.enrollmentId(), scheduleId, batchId, enrollment.getTraineeId(),
                        item.status(), item.notes(), actor
                );
            }
            updated.add(attendanceRepository.save(record));
        }

        return updated;
    }

    @Transactional(readOnly = true)
    public List<TrainingAttendance> getScheduleAttendanceRoster(String batchId, String scheduleId) {
        return attendanceRepository.findByBatchIdAndScheduleId(batchId, scheduleId);
    }

    @Transactional(readOnly = true)
    public AttendanceSummary getTraineeAttendanceSummary(String enrollmentId, String requestingTraineeId) {
        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Enrollment not found for id: " + enrollmentId));

        if (requestingTraineeId != null && !enrollment.getTraineeId().equals(requestingTraineeId)) {
            throw new UnauthorizedEnrollmentAccessException("Access denied: You do not own enrollment " + enrollmentId);
        }

        TrainingBatch batch = batchRepository.findById(enrollment.getBatchId()).orElse(null);
        int totalDeclaredSessions = batch != null && batch.getSchedules() != null ? batch.getSchedules().size() : 0;

        List<TrainingAttendance> records = attendanceRepository.findByEnrollmentId(enrollmentId);
        int totalMarked = records.size();

        long attendedCount = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE)
                .count();

        int totalEvaluated = Math.max(totalDeclaredSessions, totalMarked);
        double attendancePercentage = totalEvaluated > 0 ? ((double) attendedCount / totalEvaluated) * 100.0 : 0.0;

        return new AttendanceSummary(
                enrollmentId, enrollment.getBatchId(), enrollment.getTraineeId(),
                totalEvaluated, (int) attendedCount, Math.round(attendancePercentage * 100.0) / 100.0, records
        );
    }

    public record AttendanceItem(String enrollmentId, AttendanceStatus status, String notes) {}

    public record AttendanceSummary(
            String enrollmentId, String batchId, String traineeId,
            int totalSessions, int attendedSessions, double attendancePercentage,
            List<TrainingAttendance> sessionRecords
    ) {}
}
