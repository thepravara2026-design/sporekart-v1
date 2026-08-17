package com.sporekart.modules.training.domain.port;

import com.sporekart.modules.training.domain.TrainingBatch;

import java.util.List;
import java.util.Optional;

public interface TrainingBatchRepository {
    TrainingBatch save(TrainingBatch batch);
    Optional<TrainingBatch> findById(String id);
    Optional<TrainingBatch> findByBatchCode(String batchCode);
    List<TrainingBatch> findByProgramId(String programId);
    List<TrainingBatch> findAll();
    boolean tryAllocateSeatAtomic(String batchId);
    boolean releaseSeatAtomic(String batchId);
}
