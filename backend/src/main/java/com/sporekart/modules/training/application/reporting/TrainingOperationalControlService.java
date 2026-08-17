package com.sporekart.modules.training.application.reporting;

import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationStatus;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.training.application.TrainingCertificateService;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TrainingOperationalControlService {

    private static final Logger log = LoggerFactory.getLogger(TrainingOperationalControlService.class);

    private final TrainingBatchRepository batchRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingDemandRepository demandRepository;
    private final TrainingCertificateService certificateService;
    private final SpringDataJpaNotificationRepository notificationRepository;

    public TrainingOperationalControlService(
            TrainingBatchRepository batchRepository,
            TrainingEnrollmentRepository enrollmentRepository,
            @Autowired(required = false) TrainingDemandRepository demandRepository,
            TrainingCertificateService certificateService,
            @Autowired(required = false) SpringDataJpaNotificationRepository notificationRepository) {
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository must not be null");
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository must not be null");
        this.demandRepository = demandRepository;
        this.certificateService = Objects.requireNonNull(certificateService, "certificateService must not be null");
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<OperationalExceptionItem> getOperationalExceptions() {
        log.info("Scanning system for training operational exceptions and anomalies");
        List<OperationalExceptionItem> list = new ArrayList<>();

        // 1. Full batches with pending demand
        List<TrainingBatch> batches = batchRepository.findAll();
        for (TrainingBatch b : batches) {
            if (b.getCapacity() != null && b.getCapacity().isFull()) {
                long demandCount = demandRepository != null ? demandRepository.countByBatchIdAndStatus(b.getId(), DemandStatus.ACTIVE) : 0;
                if (demandCount > 0) {
                    list.add(new OperationalExceptionItem(
                            "EXC-FULL-BATCH-" + b.getId(), "FULL_BATCH_PENDING_DEMAND",
                            "Batch " + b.getBatchCode() + " is at 100% capacity with " + demandCount + " pending demand requests.",
                            "HIGH", b.getId(), "BATCH"
                    ));
                }
            }
        }

        // 2. Failed notifications
        if (notificationRepository != null) {
            List<Notification> failedList = notificationRepository.findAll().stream()
                    .filter(n -> n.getStatus() == NotificationStatus.FAILED || n.getStatus() == NotificationStatus.FAILED_PERMANENTLY)
                    .toList();
            for (Notification n : failedList) {
                list.add(new OperationalExceptionItem(
                        "EXC-NOTIF-" + n.getId(), "FAILED_NOTIFICATION",
                        "Notification [" + n.getTemplateCode() + "] to " + n.getRecipient() + " failed.",
                        "MEDIUM", n.getId(), "NOTIFICATION"
                ));
            }
        }

        return list;
    }

    @Transactional
    public boolean retryFailedNotification(String notificationId, String actor) {
        log.info("Executing administrative operational retry for notificationId={} by actor={}", notificationId, actor);
        if (notificationRepository != null) {
            Optional<Notification> entityOpt = notificationRepository.findById(notificationId);
            if (entityOpt.isPresent()) {
                Notification entity = entityOpt.get();
                entity.markDelivered("RETRIED-MANUAL");
                notificationRepository.save(entity);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public TrainingCertificate retryCertificateGeneration(String enrollmentId, String actor) {
        log.info("Executing administrative operational retry for certificate generation on enrollmentId={} by actor={}", enrollmentId, actor);
        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found for id: " + enrollmentId));

        TrainingBatch batch = batchRepository.findById(enrollment.getBatchId())
                .orElseThrow(() -> new IllegalArgumentException("Batch not found for id: " + enrollment.getBatchId()));

        return certificateService.issueCertificate(enrollment, batch, null);
    }

    public record OperationalExceptionItem(
            String exceptionId, String type, String description, String severity, String resourceId, String resourceType
    ) {}
}
