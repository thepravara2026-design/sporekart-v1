package com.sporekart.modules.training.domain.port;

import com.sporekart.modules.training.domain.TrainingEnrollmentPayment;
import com.sporekart.modules.training.domain.TrainingPaymentStatus;

import java.util.List;
import java.util.Optional;

public interface TrainingEnrollmentPaymentRepository {

    TrainingEnrollmentPayment save(TrainingEnrollmentPayment payment);

    Optional<TrainingEnrollmentPayment> findById(String id);

    Optional<TrainingEnrollmentPayment> findByPaymentId(String paymentId);

    Optional<TrainingEnrollmentPayment> findByBatchIdAndTraineeIdAndStatus(String batchId, String traineeId, TrainingPaymentStatus status);

    List<TrainingEnrollmentPayment> findByTraineeId(String traineeId);
}
