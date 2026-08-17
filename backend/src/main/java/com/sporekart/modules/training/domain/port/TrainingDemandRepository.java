package com.sporekart.modules.training.domain.port;

import com.sporekart.modules.training.domain.DemandStatus;
import com.sporekart.modules.training.domain.TrainingDemandRequest;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrainingDemandRepository {

    TrainingDemandRequest save(TrainingDemandRequest demandRequest);

    Optional<TrainingDemandRequest> findById(String id);

    Optional<TrainingDemandRequest> findByBatchIdAndTraineeIdAndStatus(String batchId, String traineeId, DemandStatus status);

    boolean existsByBatchIdAndTraineeIdAndStatus(String batchId, String traineeId, DemandStatus status);

    Page<TrainingDemandRequest> findByTraineeId(String traineeId, Pageable pageable);

    Page<TrainingDemandRequest> findByBatchId(String batchId, Pageable pageable);

    Page<TrainingDemandRequest> findByBatchIdAndStatus(String batchId, DemandStatus status, Pageable pageable);

    long countByBatchIdAndStatus(String batchId, DemandStatus status);
}
