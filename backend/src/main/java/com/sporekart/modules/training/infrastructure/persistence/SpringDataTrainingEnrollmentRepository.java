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

    long countByStatus(com.sporekart.modules.training.domain.EnrollmentStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM TrainingEnrollmentEntity e WHERE " +
           "(:batchId IS NULL OR e.batchId = :batchId) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:search IS NULL OR LOWER(e.traineeId) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.enrollmentCode) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.id) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TrainingEnrollmentEntity> searchEnrollments(
            @org.springframework.data.repository.query.Param("batchId") String batchId,
            @org.springframework.data.repository.query.Param("status") com.sporekart.modules.training.domain.EnrollmentStatus status,
            @org.springframework.data.repository.query.Param("search") String search,
            Pageable pageable);
}

