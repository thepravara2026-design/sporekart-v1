package com.sporekart.modules.training.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataTrainingEnrollmentRepository extends JpaRepository<TrainingEnrollmentEntity, String> {

    Optional<TrainingEnrollmentEntity> findByBatchIdAndTraineeId(String batchId, String traineeId);

    List<TrainingEnrollmentEntity> findByBatchId(String batchId);

    List<TrainingEnrollmentEntity> findByTraineeId(String traineeId);

    boolean existsByBatchIdAndTraineeId(String batchId, String traineeId);
}
