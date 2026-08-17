package com.sporekart.modules.training.application.reporting;

import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationStatus;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class TrainingReportingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingReportingService.class);

    private final TrainingProgramRepository programRepository;
    private final TrainingBatchRepository batchRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingEnrollmentPaymentRepository paymentRepository;
    private final TrainingAttendanceRepository attendanceRepository;
    private final TrainingCertificateRepository certificateRepository;
    private final SpringDataJpaNotificationRepository notificationRepository;

    public TrainingReportingService(
            @Autowired(required = false) TrainingProgramRepository programRepository,
            TrainingBatchRepository batchRepository,
            TrainingEnrollmentRepository enrollmentRepository,
            TrainingEnrollmentPaymentRepository paymentRepository,
            TrainingAttendanceRepository attendanceRepository,
            TrainingCertificateRepository certificateRepository,
            @Autowired(required = false) SpringDataJpaNotificationRepository notificationRepository) {
        this.programRepository = programRepository;
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository must not be null");
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository must not be null");
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "paymentRepository must not be null");
        this.attendanceRepository = Objects.requireNonNull(attendanceRepository, "attendanceRepository must not be null");
        this.certificateRepository = Objects.requireNonNull(certificateRepository, "certificateRepository must not be null");
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public ExecutiveOverview getExecutiveOverview() {
        log.info("Computing Executive Training Overview metrics from authoritative domain data");

        List<TrainingBatch> allBatches = batchRepository.findAll();
        long activeProgramsCount = programRepository != null ? programRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProgramStatus.ACTIVE).count() : 0;

        int totalCapacitySeats = 0;
        int totalOccupiedSeats = 0;
        int activeBatchesCount = 0;
        int fullBatchesCount = 0;

        for (TrainingBatch b : allBatches) {
            if (b.getStatus() == BatchStatus.ACTIVE || b.getStatus() == BatchStatus.SCHEDULED) {
                activeBatchesCount++;
            }
            if (b.getCapacity() != null) {
                totalCapacitySeats += b.getCapacity().getTotalCapacity();
                totalOccupiedSeats += b.getCapacity().getOccupiedSeats();
                if (b.getCapacity().isFull()) {
                    fullBatchesCount++;
                }
            }
        }

        int totalAvailableSeats = Math.max(0, totalCapacitySeats - totalOccupiedSeats);
        double seatUtilizationPercentage = totalCapacitySeats > 0 ?
                ((double) totalOccupiedSeats / totalCapacitySeats) * 100.0 : 0.0;

        List<TrainingEnrollment> allEnrollments = enrollmentRepository.findAll();
        long totalEnrollments = allEnrollments.size();
        long confirmedEnrollments = allEnrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.CONFIRMED).count();
        long pendingPaymentEnrollments = allEnrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.PAYMENT_PENDING || e.getStatus() == EnrollmentStatus.PAYMENT_FAILED).count();
        long cancelledEnrollments = allEnrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.CANCELLED).count();
        long completedEnrollments = allEnrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED).count();

        // Calculate Revenue from Payments using BigDecimal
        List<TrainingEnrollmentPayment> payments = paymentRepository.findAll();
        BigDecimal grossRevenue = BigDecimal.ZERO;
        BigDecimal totalRefunds = BigDecimal.ZERO;

        for (TrainingEnrollmentPayment p : payments) {
            if (p.getStatus() == TrainingPaymentStatus.VERIFIED || p.getStatus() == TrainingPaymentStatus.ENROLLMENT_CONFIRMED) {
                grossRevenue = grossRevenue.add(p.getAmount());
            } else if (p.getStatus() == TrainingPaymentStatus.FAILED || p.getStatus() == TrainingPaymentStatus.EXPIRED) {
                totalRefunds = totalRefunds.add(p.getAmount());
            }
        }

        BigDecimal netRevenue = grossRevenue.subtract(totalRefunds);

        // Certificate Metrics
        List<TrainingCertificate> certs = certificateRepository.findByTraineeId("") != null ?
                certificateRepository.findByBatchId("") : List.of();
        int certsIssuedCount = certs.size();

        return new ExecutiveOverview(
                activeProgramsCount, allBatches.size(), activeBatchesCount, fullBatchesCount,
                totalCapacitySeats, totalOccupiedSeats, totalAvailableSeats,
                Math.round(seatUtilizationPercentage * 100.0) / 100.0,
                totalEnrollments, confirmedEnrollments, pendingPaymentEnrollments, cancelledEnrollments, completedEnrollments,
                grossRevenue, totalRefunds, netRevenue, certsIssuedCount
        );
    }

    @Transactional(readOnly = true)
    public List<BatchUtilizationReportItem> getBatchUtilizationReport(String programIdFilter) {
        List<TrainingBatch> batches = batchRepository.findAll();
        List<BatchUtilizationReportItem> report = new ArrayList<>();

        for (TrainingBatch b : batches) {
            if (programIdFilter != null && !programIdFilter.isBlank() && !b.getProgramId().equals(programIdFilter)) {
                continue;
            }
            int total = b.getCapacity() != null ? b.getCapacity().getTotalCapacity() : 0;
            int occupied = b.getCapacity() != null ? b.getCapacity().getOccupiedSeats() : 0;
            int available = b.getCapacity() != null ? b.getCapacity().getAvailableSeats() : 0;
            double utilPct = total > 0 ? ((double) occupied / total) * 100.0 : 0.0;

            report.add(new BatchUtilizationReportItem(
                    b.getId(), b.getProgramId(), b.getBatchCode(), b.getStatus().name(),
                    total, occupied, available, Math.round(utilPct * 100.0) / 100.0
            ));
        }

        return report;
    }

    @Transactional(readOnly = true)
    public NotificationOperationalReport getNotificationReport() {
        if (notificationRepository == null) {
            return new NotificationOperationalReport(0, 0, 0, 0, List.of());
        }
        List<Notification> all = notificationRepository.findAll();
        long total = all.size();
        long delivered = all.stream().filter(n -> n.getStatus() == NotificationStatus.DELIVERED).count();
        long failed = all.stream().filter(n -> n.getStatus() == NotificationStatus.FAILED || n.getStatus() == NotificationStatus.FAILED_PERMANENTLY).count();
        long pending = all.stream().filter(n -> n.getStatus() == NotificationStatus.QUEUED || n.getStatus() == NotificationStatus.CREATED).count();

        List<FailedNotificationItem> failedItems = all.stream()
                .filter(n -> n.getStatus() == NotificationStatus.FAILED || n.getStatus() == NotificationStatus.FAILED_PERMANENTLY)
                .map(n -> new FailedNotificationItem(n.getId(), n.getTemplateCode(), n.getChannel().name(), n.getRecipient(), n.getBody(), n.getCreatedAt().toString()))
                .toList();

        return new NotificationOperationalReport(total, delivered, failed, pending, failedItems);
    }

    public record ExecutiveOverview(
            long activeProgramsCount, int totalBatches, int activeBatches, int fullBatches,
            int totalCapacitySeats, int totalOccupiedSeats, int totalAvailableSeats, double seatUtilizationPercentage,
            long totalEnrollments, long confirmedEnrollments, long pendingPaymentEnrollments, long cancelledEnrollments, long completedEnrollments,
            BigDecimal grossRevenue, BigDecimal totalRefunds, BigDecimal netRevenue, int certificatesIssuedCount
    ) {}

    public record BatchUtilizationReportItem(
            String batchId, String programId, String batchCode, String status,
            int totalSeats, int occupiedSeats, int availableSeats, double utilizationPercentage
    ) {}

    public record FailedNotificationItem(
            String id, String eventType, String channel, String recipient, String errorMessage, String createdAt
    ) {}

    public record NotificationOperationalReport(
            long totalNotifications, long delivered, long failed, long pending, List<FailedNotificationItem> failedItems
    ) {}
}
