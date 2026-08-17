package com.sporekart.modules.training.domain.port;

import com.sporekart.modules.training.domain.TrainingEnrollment;

import java.util.List;
import java.util.Optional;

public interface TrainingEnrollmentRepository {
    TrainingEnrollment save(TrainingEnrollment enrollment);
    Optional<TrainingEnrollment> findById(String id);
    Optional<TrainingEnrollment> findByBatchIdAndTraineeId(String batchId, String traineeId);
    List<TrainingEnrollment> findByBatchId(String batchId);
    List<TrainingEnrollment> findByTraineeId(String traineeId);
    boolean existsByBatchIdAndTraineeId(String batchId, String traineeId);
}
