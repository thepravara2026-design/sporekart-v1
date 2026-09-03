package com.sporekart.modules.training.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataTrainingCertificateRepository extends JpaRepository<TrainingCertificateEntity, String> {
    Optional<TrainingCertificateEntity> findByEnrollmentId(String enrollmentId);
    Optional<TrainingCertificateEntity> findByVerificationCode(String verificationCode);
    List<TrainingCertificateEntity> findByTraineeId(String traineeId);
    List<TrainingCertificateEntity> findByBatchId(String batchId);
}
