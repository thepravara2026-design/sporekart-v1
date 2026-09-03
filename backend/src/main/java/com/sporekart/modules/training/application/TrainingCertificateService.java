package com.sporekart.modules.training.application;

import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.exception.EnrollmentNotFoundException;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import com.sporekart.modules.training.domain.port.TrainingCertificateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TrainingCertificateService {

    private static final Logger log = LoggerFactory.getLogger(TrainingCertificateService.class);

    private final TrainingCertificateRepository certificateRepository;

    public TrainingCertificateService(TrainingCertificateRepository certificateRepository) {
        this.certificateRepository = Objects.requireNonNull(certificateRepository, "certificateRepository must not be null");
    }

    @Transactional
    public TrainingCertificate issueCertificate(
            TrainingEnrollment enrollment, TrainingBatch batch, TrainingProgram program) {
        log.info("Issuing digital completion certificate for enrollmentId={}, traineeId={}, batchId={}",
                enrollment.getId(), enrollment.getTraineeId(), batch.getId());

        Optional<TrainingCertificate> existingOpt = certificateRepository.findByEnrollmentId(enrollment.getId());
        if (existingOpt.isPresent()) {
            log.info("Certificate already issued for enrollmentId={}. Returning existing certificate id={}",
                    enrollment.getId(), existingOpt.get().getId());
            return existingOpt.get();
        }

        String programTitle = program != null ? program.getTitle() : "Training Program";
        String programId = program != null ? program.getId() : batch.getProgramId();
        String batchCode = batch.getBatchCode();

        TrainingCertificate cert = TrainingCertificate.issue(
                enrollment.getId(), enrollment.getTraineeId(), enrollment.getTraineeId(),
                programId, programTitle, batch.getId(), batchCode, Instant.now()
        );

        return certificateRepository.save(cert);
    }

    @Transactional(readOnly = true)
    public Optional<TrainingCertificate> verifyCertificate(String verificationCode) {
        log.info("Public verification lookup for certificate verificationCode={}", verificationCode);
        return certificateRepository.findByVerificationCode(verificationCode);
    }

    @Transactional(readOnly = true)
    public List<TrainingCertificate> getTraineeCertificates(String traineeId) {
        if (traineeId == null || traineeId.isBlank() || "ANONYMOUS_TRAINEE".equals(traineeId)) {
            throw new UnauthorizedEnrollmentAccessException("Authentication required to access certificates");
        }
        return certificateRepository.findByTraineeId(traineeId);
    }

    @Transactional(readOnly = true)
    public TrainingCertificate getCertificateById(String certificateId, String requestingTraineeId) {
        TrainingCertificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Certificate not found for id: " + certificateId));

        if (requestingTraineeId != null && !cert.getTraineeId().equals(requestingTraineeId)) {
            throw new UnauthorizedEnrollmentAccessException("Access denied: You do not own certificate " + certificateId);
        }

        return cert;
    }
}
