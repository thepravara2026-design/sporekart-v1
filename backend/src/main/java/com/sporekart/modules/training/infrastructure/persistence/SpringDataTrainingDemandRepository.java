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

    long countByStatus(DemandStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM TrainingDemandEntity d WHERE " +
           "(:batchId IS NULL OR d.batchId = :batchId) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:search IS NULL OR LOWER(d.traineeId) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.id) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TrainingDemandEntity> searchDemands(
            @org.springframework.data.repository.query.Param("batchId") String batchId,
            @org.springframework.data.repository.query.Param("status") DemandStatus status,
            @org.springframework.data.repository.query.Param("search") String search,
            Pageable pageable);
}

