package com.sporekart.modules.training;

import com.sporekart.modules.training.application.reporting.TrainingAuditService;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.infrastructure.persistence.SpringDataTrainingEnrollmentHistoryRepository;
import com.sporekart.modules.training.infrastructure.persistence.TrainingEnrollmentHistoryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingAuditServiceTest {

    @Mock private SpringDataTrainingEnrollmentHistoryRepository historyRepository;

    private TrainingAuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new TrainingAuditService(historyRepository);
    }

    @Test
    @DisplayName("getAuditLogs returns filtered audit items")
    void getAuditLogsReturnsFilteredItems() {
        TrainingEnrollmentHistoryEntity h1 = new TrainingEnrollmentHistoryEntity(
                "h-1", "enr-100", EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED,
                "Payment completed", "admin@sporekart.com", Instant.now()
        );

        when(historyRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(h1));

        List<TrainingAuditService.AuditLogItem> logs = auditService.getAuditLogs("admin@sporekart.com", null);

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).actor()).isEqualTo("admin@sporekart.com");
        assertThat(logs.get(0).toStatus()).isEqualTo("CONFIRMED");
    }
}
