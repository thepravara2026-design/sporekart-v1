package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JpaTrainingEnrollmentRepositoryAdapter implements TrainingEnrollmentRepository {

    private final SpringDataTrainingEnrollmentRepository springDataRepository;

    public JpaTrainingEnrollmentRepositoryAdapter(SpringDataTrainingEnrollmentRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public TrainingEnrollment save(TrainingEnrollment enrollment) {
        TrainingEnrollmentEntity entity = TrainingEnrollmentEntity.fromDomain(enrollment);
        TrainingEnrollmentEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<TrainingEnrollment> findById(String id) {
        return springDataRepository.findById(id).map(TrainingEnrollmentEntity::toDomain);
    }

    @Override
    public Optional<TrainingEnrollment> findByBatchIdAndTraineeId(String batchId, String traineeId) {
        return springDataRepository.findByBatchIdAndTraineeId(batchId, traineeId).map(TrainingEnrollmentEntity::toDomain);
    }

    @Override
    public List<TrainingEnrollment> findByBatchId(String batchId) {
        return springDataRepository.findByBatchId(batchId).stream()
                .map(TrainingEnrollmentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrainingEnrollment> findByTraineeId(String traineeId) {
        return springDataRepository.findByTraineeId(traineeId).stream()
                .map(TrainingEnrollmentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByBatchIdAndTraineeId(String batchId, String traineeId) {
        return springDataRepository.existsByBatchIdAndTraineeId(batchId, traineeId);
    }
}
