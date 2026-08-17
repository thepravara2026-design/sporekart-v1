package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.DeliveryMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataTrainingBatchRepository extends JpaRepository<TrainingBatchEntity, String> {

    Optional<TrainingBatchEntity> findByBatchCode(String batchCode);

    List<TrainingBatchEntity> findByProgramId(String programId);

    @Query("SELECT b FROM TrainingBatchEntity b WHERE " +
           "(:programId IS NULL OR b.programId = :programId) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:deliveryMode IS NULL OR b.deliveryMode = :deliveryMode) AND " +
           "(:fromDate IS NULL OR b.startDate >= :fromDate) AND " +
           "(:toDate IS NULL OR b.startDate <= :toDate)")
    Page<TrainingBatchEntity> searchBatches(
            @Param("programId") String programId,
            @Param("status") BatchStatus status,
            @Param("deliveryMode") DeliveryMode deliveryMode,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable);

    @Query("SELECT b FROM TrainingBatchEntity b WHERE b.status IN (com.sporekart.modules.training.domain.BatchStatus.SCHEDULED, com.sporekart.modules.training.domain.BatchStatus.ACTIVE, com.sporekart.modules.training.domain.BatchStatus.FULL) ORDER BY b.startDate ASC")
    Page<TrainingBatchEntity> findPublicActiveBatches(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TrainingBatchEntity b SET b.occupiedSeats = b.occupiedSeats + 1, b.updatedAt = :now, b.status = CASE WHEN (b.occupiedSeats + 1) >= b.totalCapacity THEN com.sporekart.modules.training.domain.BatchStatus.FULL ELSE b.status END WHERE b.id = :batchId AND b.occupiedSeats < b.totalCapacity AND b.status NOT IN (com.sporekart.modules.training.domain.BatchStatus.CANCELLED, com.sporekart.modules.training.domain.BatchStatus.COMPLETED)")
    int tryAllocateSeatAtomic(@Param("batchId") String batchId, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TrainingBatchEntity b SET b.occupiedSeats = CASE WHEN b.occupiedSeats > 0 THEN b.occupiedSeats - 1 ELSE 0 END, b.updatedAt = :now, b.status = CASE WHEN b.status = com.sporekart.modules.training.domain.BatchStatus.FULL THEN com.sporekart.modules.training.domain.BatchStatus.ACTIVE ELSE b.status END WHERE b.id = :batchId")
    int releaseSeatAtomic(@Param("batchId") String batchId, @Param("now") Instant now);
}
