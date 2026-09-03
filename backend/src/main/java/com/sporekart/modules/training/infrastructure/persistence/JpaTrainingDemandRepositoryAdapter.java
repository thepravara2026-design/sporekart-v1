package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.DemandStatus;
import com.sporekart.modules.training.domain.TrainingDemandRequest;
import com.sporekart.modules.training.domain.port.TrainingDemandRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class JpaTrainingDemandRepositoryAdapter implements TrainingDemandRepository {

    private final SpringDataTrainingDemandRepository springDataRepository;

    public JpaTrainingDemandRepositoryAdapter(SpringDataTrainingDemandRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public TrainingDemandRequest save(TrainingDemandRequest demandRequest) {
        TrainingDemandEntity entity = TrainingDemandEntity.fromDomain(demandRequest);
        TrainingDemandEntity saved = springDataRepository.saveAndFlush(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<TrainingDemandRequest> findById(String id) {
        return springDataRepository.findById(id).map(TrainingDemandEntity::toDomain);
    }

    @Override
    public Optional<TrainingDemandRequest> findByBatchIdAndTraineeIdAndStatus(String batchId, String traineeId, DemandStatus status) {
        return springDataRepository.findByBatchIdAndTraineeIdAndStatus(batchId, traineeId, status)
                .map(TrainingDemandEntity::toDomain);
    }

    @Override
    public boolean existsByBatchIdAndTraineeIdAndStatus(String batchId, String traineeId, DemandStatus status) {
        return springDataRepository.existsByBatchIdAndTraineeIdAndStatus(batchId, traineeId, status);
    }

    @Override
    public Page<TrainingDemandRequest> findByTraineeId(String traineeId, Pageable pageable) {
        return springDataRepository.findByTraineeId(traineeId, pageable)
                .map(TrainingDemandEntity::toDomain);
    }

    @Override
    public Page<TrainingDemandRequest> findByBatchId(String batchId, Pageable pageable) {
        return springDataRepository.findByBatchId(batchId, pageable)
                .map(TrainingDemandEntity::toDomain);
    }

    @Override
    public Page<TrainingDemandRequest> findByBatchIdAndStatus(String batchId, DemandStatus status, Pageable pageable) {
        return springDataRepository.findByBatchIdAndStatus(batchId, status, pageable)
                .map(TrainingDemandEntity::toDomain);
    }

    @Override
    public long countByBatchIdAndStatus(String batchId, DemandStatus status) {
        return springDataRepository.countByBatchIdAndStatus(batchId, status);
    }

    @Override
    public long countByStatus(DemandStatus status) {
        return springDataRepository.countByStatus(status);
    }

    @Override
    public Page<TrainingDemandRequest> searchDemands(String batchId, DemandStatus status, String search, Pageable pageable) {
        String cleanSearch = (search == null || search.isBlank()) ? null : search.trim();
        String cleanBatchId = (batchId == null || batchId.isBlank()) ? null : batchId.trim();
        return springDataRepository.searchDemands(cleanBatchId, status, cleanSearch, pageable)
                .map(TrainingDemandEntity::toDomain);
    }
}

