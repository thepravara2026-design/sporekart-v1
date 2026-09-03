package com.sporekart.modules.training.application.reporting;

import com.sporekart.modules.training.infrastructure.persistence.SpringDataTrainingEnrollmentHistoryRepository;
import com.sporekart.modules.training.infrastructure.persistence.TrainingEnrollmentHistoryEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class TrainingAuditService {

    private static final Logger log = LoggerFactory.getLogger(TrainingAuditService.class);

    private final SpringDataTrainingEnrollmentHistoryRepository historyRepository;

    public TrainingAuditService(SpringDataTrainingEnrollmentHistoryRepository historyRepository) {
        this.historyRepository = Objects.requireNonNull(historyRepository, "historyRepository must not be null");
    }

    @Transactional(readOnly = true)
    public List<AuditLogItem> getAuditLogs(String actorFilter, String enrollmentIdFilter) {
        log.info("Querying training audit history log. actorFilter={}, enrollmentIdFilter={}", actorFilter, enrollmentIdFilter);

        List<TrainingEnrollmentHistoryEntity> history = historyRepository.findAllByOrderByCreatedAtDesc();

        return history.stream()
                .filter(h -> (actorFilter == null || actorFilter.isBlank() || h.getActor().equalsIgnoreCase(actorFilter)))
                .filter(h -> (enrollmentIdFilter == null || enrollmentIdFilter.isBlank() || h.getEnrollmentId().equals(enrollmentIdFilter)))
                .map(h -> new AuditLogItem(
                        h.getId(), h.getEnrollmentId(),
                        h.getFromStatus() != null ? h.getFromStatus().name() : "NONE",
                        h.getToStatus() != null ? h.getToStatus().name() : "UNKNOWN",
                        h.getReason(), h.getActor(), h.getCreatedAt().toString()
                ))
                .toList();
    }

    public record AuditLogItem(
            String id, String enrollmentId, String fromStatus, String toStatus,
            String reason, String actor, String timestamp
    ) {}
}
