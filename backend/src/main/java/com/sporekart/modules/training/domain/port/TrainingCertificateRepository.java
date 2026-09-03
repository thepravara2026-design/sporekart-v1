package com.sporekart.modules.training.domain.port;

import com.sporekart.modules.training.domain.TrainingCertificate;

import java.util.List;
import java.util.Optional;

public interface TrainingCertificateRepository {
    TrainingCertificate save(TrainingCertificate certificate);
    Optional<TrainingCertificate> findById(String id);
    Optional<TrainingCertificate> findByEnrollmentId(String enrollmentId);
    Optional<TrainingCertificate> findByVerificationCode(String verificationCode);
    List<TrainingCertificate> findByTraineeId(String traineeId);
    List<TrainingCertificate> findByBatchId(String batchId);
}
