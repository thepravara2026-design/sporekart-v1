package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.DemandStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataTrainingDemandRepository extends JpaRepository<TrainingDemandEntity, String> {

    Optional<TrainingDemandEntity> findByBatchIdAndTraineeIdAndStatus(String batchId, String traineeId, DemandStatus status);

    boolean existsByBatchIdAndTraineeIdAndStatus(String batchId, String traineeId, DemandStatus status);

    Page<TrainingDemandEntity> findByTraineeId(String traineeId, Pageable pageable);

    Page<TrainingDemandEntity> findByBatchId(String batchId, Pageable pageable);

    Page<TrainingDemandEntity> findByBatchIdAndStatus(String batchId, DemandStatus status, Pageable pageable);

    long countByBatchIdAndStatus(String batchId, DemandStatus status);
}
