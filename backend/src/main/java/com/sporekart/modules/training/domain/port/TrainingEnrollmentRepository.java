package com.sporekart.modules.training.domain.port;

import com.sporekart.modules.training.domain.TrainingEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TrainingEnrollmentRepository {
    TrainingEnrollment save(TrainingEnrollment enrollment);
    Optional<TrainingEnrollment> findById(String id);
    Optional<TrainingEnrollment> findByBatchIdAndTraineeId(String batchId, String traineeId);
    Optional<TrainingEnrollment> findByIdempotencyKey(String idempotencyKey);
    List<TrainingEnrollment> findByBatchId(String batchId);
    Page<TrainingEnrollment> findByBatchId(String batchId, Pageable pageable);
    List<TrainingEnrollment> findByTraineeId(String traineeId);
    Page<TrainingEnrollment> findByTraineeId(String traineeId, Pageable pageable);
    boolean existsByBatchIdAndTraineeId(String batchId, String traineeId);
}
