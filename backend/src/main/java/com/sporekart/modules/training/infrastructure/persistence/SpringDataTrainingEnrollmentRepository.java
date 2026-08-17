package com.sporekart.modules.training.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataTrainingEnrollmentRepository extends JpaRepository<TrainingEnrollmentEntity, String> {

    Optional<TrainingEnrollmentEntity> findByBatchIdAndTraineeId(String batchId, String traineeId);

    Optional<TrainingEnrollmentEntity> findByIdempotencyKey(String idempotencyKey);

    List<TrainingEnrollmentEntity> findByBatchId(String batchId);

    Page<TrainingEnrollmentEntity> findByBatchId(String batchId, Pageable pageable);

    List<TrainingEnrollmentEntity> findByTraineeId(String traineeId);

    Page<TrainingEnrollmentEntity> findByTraineeId(String traineeId, Pageable pageable);

    boolean existsByBatchIdAndTraineeId(String batchId, String traineeId);
}
