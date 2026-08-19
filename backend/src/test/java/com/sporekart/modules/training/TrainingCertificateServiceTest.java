package com.sporekart.modules.training;

import com.sporekart.modules.training.application.TrainingCertificateService;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.port.TrainingCertificateRepository;
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
class TrainingCertificateServiceTest {

    @Mock private TrainingCertificateRepository certificateRepository;

    private TrainingCertificateService certificateService;

    private static final String ENROLLMENT_ID = "enr-777";
    private static final String TRAINEE_ID = "trainee-123";
    private static final String BATCH_ID = "batch-101";

    @BeforeEach
    void setUp() {
        certificateService = new TrainingCertificateService(certificateRepository);
    }

    @Test
    @DisplayName("issueCertificate creates a new certificate if none exists for enrollment")
    void issueCertificateCreatesNewCertificate() {
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_ID);
        TrainingBatch batch = createTestBatch(BATCH_ID);
        TrainingProgram program = new TrainingProgram(
                "PROG-1", "advanced-mycology", "Advanced Mycology", "Desc", "CAT", 5,
                com.sporekart.modules.training.domain.ProgramStatus.ACTIVE, BigDecimal.valueOf(499), "INR", "ADMIN", "ADMIN", Instant.now(), Instant.now()
        );

        when(certificateRepository.findByEnrollmentId(ENROLLMENT_ID)).thenReturn(Optional.empty());
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrainingCertificate cert = certificateService.issueCertificate(enrollment, batch, program);

        assertThat(cert).isNotNull();
        assertThat(cert.getEnrollmentId()).isEqualTo(ENROLLMENT_ID);
        assertThat(cert.getProgramTitle()).isEqualTo("Advanced Mycology");
        assertThat(cert.getVerificationCode()).isNotNull();
    }

    @Test
    @DisplayName("verifyCertificate finds certificate by verification code")
    void verifyCertificateFindsByCode() {
        TrainingCertificate cert = TrainingCertificate.issue(ENROLLMENT_ID, TRAINEE_ID, TRAINEE_ID, "PROG-1", "Program 1", BATCH_ID, "BATCH-001", Instant.now());
        when(certificateRepository.findByVerificationCode(cert.getVerificationCode())).thenReturn(Optional.of(cert));

        Optional<TrainingCertificate> found = certificateService.verifyCertificate(cert.getVerificationCode());

        assertThat(found).isPresent();
        assertThat(found.get().getCertificateNumber()).isEqualTo(cert.getCertificateNumber());
    }

    private TrainingBatch createTestBatch(String id) {
        return new TrainingBatch(
                id, "PROG-1", "BATCH-001", Instant.now(), Instant.now().plus(Duration.ofDays(5)),
                new Capacity(10, 5), BatchStatus.ACTIVE, DeliveryMode.ONLINE,
                "Zoom Link", "https://meeting.com", "UTC", "ADMIN", "ADMIN", List.of(), Instant.now(), Instant.now()
        );
    }

    private TrainingEnrollment createTestEnrollment(String id, String batchId, String traineeId) {
        return new TrainingEnrollment(
                id, "ENR-001", batchId, traineeId, EnrollmentStatus.CONFIRMED, "PAY-REF-100",
                BigDecimal.valueOf(499), "INR", Instant.now(), Instant.now(), null, null,
                Instant.now(), Instant.now(), "TRAINEE", "TRAINEE", "IDEMP-1"
        );
    }
}
