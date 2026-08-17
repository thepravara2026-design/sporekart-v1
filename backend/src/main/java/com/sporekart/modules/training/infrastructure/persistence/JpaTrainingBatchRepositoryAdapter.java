package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JpaTrainingBatchRepositoryAdapter implements TrainingBatchRepository {

    private final SpringDataTrainingBatchRepository springDataRepository;

    public JpaTrainingBatchRepositoryAdapter(SpringDataTrainingBatchRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public TrainingBatch save(TrainingBatch batch) {
        TrainingBatchEntity entity = TrainingBatchEntity.fromDomain(batch);
        TrainingBatchEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<TrainingBatch> findById(String id) {
        return springDataRepository.findById(id).map(TrainingBatchEntity::toDomain);
    }

    @Override
    public Optional<TrainingBatch> findByBatchCode(String batchCode) {
        return springDataRepository.findByBatchCode(batchCode).map(TrainingBatchEntity::toDomain);
    }

    @Override
    public List<TrainingBatch> findByProgramId(String programId) {
        return springDataRepository.findByProgramId(programId).stream()
                .map(TrainingBatchEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrainingBatch> findAll() {
        return springDataRepository.findAll().stream()
                .map(TrainingBatchEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean tryAllocateSeatAtomic(String batchId) {
        int updatedRows = springDataRepository.tryAllocateSeatAtomic(batchId, Instant.now());
        return updatedRows > 0;
    }

    @Override
    public boolean releaseSeatAtomic(String batchId) {
        int updatedRows = springDataRepository.releaseSeatAtomic(batchId, Instant.now());
        return updatedRows > 0;
    }
}
