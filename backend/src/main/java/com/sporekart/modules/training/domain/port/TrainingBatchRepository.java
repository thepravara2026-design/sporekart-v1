package com.sporekart.modules.training.domain.port;

import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.TrainingBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TrainingBatchRepository {
    TrainingBatch save(TrainingBatch batch);
    Optional<TrainingBatch> findById(String id);
    Optional<TrainingBatch> findByBatchCode(String batchCode);
    List<TrainingBatch> findByProgramId(String programId);
    List<TrainingBatch> findAll();
    Page<TrainingBatch> findAll(Pageable pageable);
    Page<TrainingBatch> searchBatches(String programId, BatchStatus status, DeliveryMode deliveryMode, Instant fromDate, Instant toDate, Pageable pageable);
    Page<TrainingBatch> findPublicActiveBatches(Pageable pageable);
    boolean tryAllocateSeatAtomic(String batchId);
    boolean releaseSeatAtomic(String batchId);
}
