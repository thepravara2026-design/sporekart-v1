package com.sporekart.modules.training.application;

import com.sporekart.modules.training.controller.dto.ScheduleResponse;
import com.sporekart.modules.training.controller.dto.TraineeEnrollmentDetailResponse;
import com.sporekart.modules.training.controller.dto.TraineeTrainingDashboardResponse;
import com.sporekart.modules.training.domain.DemandStatus;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.TrainingEnrollmentPayment;
import com.sporekart.modules.training.domain.TrainingProgram;
import com.sporekart.modules.training.domain.exception.EnrollmentNotFoundException;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingDemandRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentPaymentRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import com.sporekart.modules.training.infrastructure.persistence.SpringDataTrainingDemandRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TraineeTrainingOperationsService {

    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingBatchRepository batchRepository;
    private final TrainingProgramRepository programRepository;
    private final TrainingDemandRepository demandRepository;
    private final TrainingEnrollmentPaymentRepository paymentRepository;
    private final SpringDataTrainingDemandRepository springDataDemandRepository;

    public TraineeTrainingOperationsService(
            TrainingEnrollmentRepository enrollmentRepository,
            TrainingBatchRepository batchRepository,
            TrainingProgramRepository programRepository,
            TrainingDemandRepository demandRepository,
            TrainingEnrollmentPaymentRepository paymentRepository,
            SpringDataTrainingDemandRepository springDataDemandRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.batchRepository = batchRepository;
        this.programRepository = programRepository;
        this.demandRepository = demandRepository;
        this.paymentRepository = paymentRepository;
        this.springDataDemandRepository = springDataDemandRepository;
    }

    public TraineeTrainingDashboardResponse getTraineeDashboardMetrics(String traineeId) {
        if (traineeId == null || traineeId.isBlank() || "ANONYMOUS_TRAINEE".equals(traineeId)) {
            throw new UnauthorizedEnrollmentAccessException("Authentication required to view trainee dashboard");
        }

        List<TrainingEnrollment> myEnrollments = enrollmentRepository.findByTraineeId(traineeId);

        long upcomingCount = myEnrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.CONFIRMED || e.getStatus() == EnrollmentStatus.ACTIVE)
                .count();

        long activeCount = myEnrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .count();

        long completedCount = myEnrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED)
                .count();

        long pendingCount = myEnrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.PENDING || e.getStatus() == EnrollmentStatus.PAYMENT_PENDING)
                .count();

        long activeDemandCount = springDataDemandRepository.findByTraineeId(traineeId, Pageable.unpaged()).getContent().stream()
                .filter(d -> d.getStatus() == DemandStatus.ACTIVE)
                .count();

        // Next upcoming session
        Optional<TrainingBatch> nextBatch = myEnrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.CONFIRMED || e.getStatus() == EnrollmentStatus.ACTIVE)
                .map(e -> batchRepository.findById(e.getBatchId()).orElse(null))
                .filter(b -> b != null && b.getStartDate().isAfter(Instant.now().minusSeconds(86400)))
                .min(Comparator.comparing(TrainingBatch::getStartDate));

        String sessionTitle = null;
        String batchCode = null;
        Instant startDate = null;
        String deliveryMode = null;
        String venueOrMeeting = null;

        if (nextBatch.isPresent()) {
            TrainingBatch b = nextBatch.get();
            batchCode = b.getBatchCode();
            startDate = b.getStartDate();
            deliveryMode = b.getDeliveryMode().name();
            venueOrMeeting = b.getMeetingUrl() != null && !b.getMeetingUrl().isBlank() ? b.getMeetingUrl() : b.getVenueInfo();

            Optional<TrainingProgram> prog = programRepository.findById(b.getProgramId());
            sessionTitle = prog.map(TrainingProgram::getTitle).orElse("Training Batch " + b.getBatchCode());
        }

        return new TraineeTrainingDashboardResponse(
                upcomingCount,
                activeCount,
                completedCount,
                pendingCount,
                activeDemandCount,
                sessionTitle,
                batchCode,
                startDate,
                deliveryMode,
                venueOrMeeting
        );
    }

    public Page<TrainingEnrollment> getUpcomingTrainingForTrainee(String traineeId, Pageable pageable) {
        if (traineeId == null || traineeId.isBlank() || "ANONYMOUS_TRAINEE".equals(traineeId)) {
            throw new UnauthorizedEnrollmentAccessException("Authentication required to view upcoming training");
        }

        List<TrainingEnrollment> upcoming = enrollmentRepository.findByTraineeId(traineeId).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.CONFIRMED || e.getStatus() == EnrollmentStatus.ACTIVE)
                .sorted(Comparator.comparing(TrainingEnrollment::getCreatedAt).reversed())
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), upcoming.size());
        if (start > upcoming.size()) {
            return new PageImpl<>(List.of(), pageable, upcoming.size());
        }
        return new PageImpl<>(upcoming.subList(start, end), pageable, upcoming.size());
    }

    public TraineeEnrollmentDetailResponse getTraineeEnrollmentDetail(String enrollmentId, String traineeId) {
        if (traineeId == null || traineeId.isBlank() || "ANONYMOUS_TRAINEE".equals(traineeId)) {
            throw new UnauthorizedEnrollmentAccessException("Authentication required to view enrollment details");
        }

        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Training enrollment not found for id: " + enrollmentId));

        if (!enrollment.getTraineeId().equals(traineeId)) {
            throw new UnauthorizedEnrollmentAccessException("Access denied: You do not own enrollment " + enrollmentId);
        }

        TrainingBatch batch = batchRepository.findById(enrollment.getBatchId())
                .orElseThrow(() -> new EnrollmentNotFoundException("Training batch not found for id: " + enrollment.getBatchId()));

        Optional<TrainingProgram> programOpt = programRepository.findById(batch.getProgramId());
        String programTitle = programOpt.map(TrainingProgram::getTitle).orElse("Training Program");
        String programCategory = programOpt.map(TrainingProgram::getCategory).orElse("General");

        List<ScheduleResponse> scheduleResponses = batch.getSchedules().stream()
                .map(ScheduleResponse::fromDomain)
                .collect(Collectors.toList());

        String paymentStatusSummary = "UNPAID";
        if (enrollment.getPaymentReference() != null) {
            Optional<TrainingEnrollmentPayment> paymentOpt = paymentRepository.findByPaymentId(enrollment.getPaymentReference());
            if (paymentOpt.isPresent()) {
                paymentStatusSummary = paymentOpt.get().getStatus().name();
            } else if (enrollment.getStatus() == EnrollmentStatus.CONFIRMED || enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                paymentStatusSummary = "VERIFIED";
            }
        } else if (enrollment.getStatus() == EnrollmentStatus.CONFIRMED) {
            paymentStatusSummary = "FREE / CONFIRMED";
        }

        return new TraineeEnrollmentDetailResponse(
                enrollment.getId(),
                enrollment.getEnrollmentCode(),
                batch.getId(),
                batch.getProgramId(),
                programTitle,
                programCategory,
                batch.getBatchCode(),
                batch.getDeliveryMode().name(),
                batch.getVenueInfo(),
                batch.getMeetingUrl(),
                batch.getTimezone(),
                scheduleResponses,
                enrollment.getStatus().name(),
                paymentStatusSummary,
                enrollment.getPriceAmount(),
                enrollment.getCurrency(),
                enrollment.getPaymentReference(),
                enrollment.getEnrolledAt(),
                enrollment.getConfirmedAt(),
                enrollment.getActivatedAt(),
                enrollment.getCompletedAt(),
                enrollment.getCreatedAt()
        );
    }
}
