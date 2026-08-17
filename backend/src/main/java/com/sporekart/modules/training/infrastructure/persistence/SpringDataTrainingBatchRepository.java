package com.sporekart.modules.training.infrastructure.persistence;

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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TrainingBatchEntity b SET b.occupiedSeats = b.occupiedSeats + 1, b.updatedAt = :now, b.status = CASE WHEN (b.occupiedSeats + 1) >= b.totalCapacity THEN com.sporekart.modules.training.domain.BatchStatus.FULL ELSE b.status END WHERE b.id = :batchId AND b.occupiedSeats < b.totalCapacity AND b.status NOT IN (com.sporekart.modules.training.domain.BatchStatus.CANCELLED, com.sporekart.modules.training.domain.BatchStatus.COMPLETED)")
    int tryAllocateSeatAtomic(@Param("batchId") String batchId, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TrainingBatchEntity b SET b.occupiedSeats = CASE WHEN b.occupiedSeats > 0 THEN b.occupiedSeats - 1 ELSE 0 END, b.updatedAt = :now, b.status = CASE WHEN b.status = com.sporekart.modules.training.domain.BatchStatus.FULL THEN com.sporekart.modules.training.domain.BatchStatus.ACTIVE ELSE b.status END WHERE b.id = :batchId")
    int releaseSeatAtomic(@Param("batchId") String batchId, @Param("now") Instant now);
}
