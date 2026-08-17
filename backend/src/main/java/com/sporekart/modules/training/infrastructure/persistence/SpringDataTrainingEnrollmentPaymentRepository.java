package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.TrainingPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataTrainingEnrollmentPaymentRepository extends JpaRepository<TrainingEnrollmentPaymentEntity, String> {

    Optional<TrainingEnrollmentPaymentEntity> findByPaymentId(String paymentId);

    Optional<TrainingEnrollmentPaymentEntity> findByBatchIdAndTraineeIdAndStatus(String batchId, String traineeId, TrainingPaymentStatus status);

    List<TrainingEnrollmentPaymentEntity> findByTraineeId(String traineeId);
}
