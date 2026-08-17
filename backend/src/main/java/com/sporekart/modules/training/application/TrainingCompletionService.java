package com.sporekart.modules.training.application;

import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.event.TrainingEnrollmentCompletedEvent;
import com.sporekart.modules.training.domain.exception.BatchNotFoundException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TrainingCompletionService {

    private static final Logger log = LoggerFactory.getLogger(TrainingCompletionService.class);

    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingBatchRepository batchRepository;
    private final TrainingProgramRepository programRepository;
    private final TrainingAttendanceService attendanceService;
    private final TrainingCertificateService certificateService;
    private final EnrollmentLifecycleService lifecycleService;
    private final ApplicationEventPublisher eventPublisher;

    public TrainingCompletionService(
            TrainingEnrollmentRepository enrollmentRepository,
            TrainingBatchRepository batchRepository,
            @Autowired(required = false) TrainingProgramRepository programRepository,
            TrainingAttendanceService attendanceService,
            TrainingCertificateService certificateService,
            EnrollmentLifecycleService lifecycleService,
            ApplicationEventPublisher eventPublisher) {
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository must not be null");
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository must not be null");
        this.programRepository = programRepository;
        this.attendanceService = Objects.requireNonNull(attendanceService, "attendanceService must not be null");
        this.certificateService = Objects.requireNonNull(certificateService, "certificateService must not be null");
        this.lifecycleService = Objects.requireNonNull(lifecycleService, "lifecycleService must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
    }

    @Transactional
    public BatchCompletionResult evaluateBatchCompletion(String batchId, Double minAttendancePercentage, String actor) {
        double threshold = minAttendancePercentage != null ? minAttendancePercentage : 80.0;
        log.info("Evaluating completion for batchId={}, minAttendanceThreshold={}%, actor={}", batchId, threshold, actor);

        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("Training batch not found for id: " + batchId));

        TrainingProgram program = null;
        if (programRepository != null) {
            program = programRepository.findById(batch.getProgramId()).orElse(null);
        }

        List<TrainingEnrollment> enrollments = enrollmentRepository.findByBatchId(batchId);
        List<EnrollmentCompletionStatus> results = new ArrayList<>();

        int completedCount = 0;

        for (TrainingEnrollment enrollment : enrollments) {
            if (enrollment.getStatus() == EnrollmentStatus.CANCELLED || enrollment.getStatus() == EnrollmentStatus.REJECTED) {
                continue;
            }

            TrainingAttendanceService.AttendanceSummary summary =
                    attendanceService.getTraineeAttendanceSummary(enrollment.getId(), null);

            boolean eligible = summary.attendancePercentage() >= threshold;

            if (eligible && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
                TrainingEnrollment completed = lifecycleService.transitionStatus(
                        enrollment.getId(), EnrollmentStatus.COMPLETED,
                        "Graduated with " + summary.attendancePercentage() + "% attendance", actor
                );

                TrainingCertificate cert = certificateService.issueCertificate(completed, batch, program);

                eventPublisher.publishEvent(new TrainingEnrollmentCompletedEvent(
                        completed.getId(), batchId, completed.getTraineeId(), cert.getId()
                ));

                completedCount++;
                results.add(new EnrollmentCompletionStatus(
                        enrollment.getId(), enrollment.getTraineeId(), true, summary.attendancePercentage(), cert.getCertificateNumber()
                ));
            } else if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
                TrainingCertificate cert = certificateService.issueCertificate(enrollment, batch, program);
                results.add(new EnrollmentCompletionStatus(
                        enrollment.getId(), enrollment.getTraineeId(), true, summary.attendancePercentage(), cert.getCertificateNumber()
                ));
            } else {
                results.add(new EnrollmentCompletionStatus(
                        enrollment.getId(), enrollment.getTraineeId(), false, summary.attendancePercentage(), null
                ));
            }
        }

        return new BatchCompletionResult(batchId, batch.getBatchCode(), threshold, completedCount, results);
    }

    public record EnrollmentCompletionStatus(
            String enrollmentId, String traineeId, boolean completed, double attendancePercentage, String certificateNumber
    ) {}

    public record BatchCompletionResult(
            String batchId, String batchCode, double minAttendanceThreshold, int newlyCompletedCount, List<EnrollmentCompletionStatus> enrollments
    ) {}
}
