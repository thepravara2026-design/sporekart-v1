package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.TrainingEnrollmentHistory;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class JpaTrainingEnrollmentHistoryRepositoryAdapter implements TrainingEnrollmentHistoryRepository {

    private final SpringDataTrainingEnrollmentHistoryRepository springDataRepository;

    @Autowired
    public JpaTrainingEnrollmentHistoryRepositoryAdapter(SpringDataTrainingEnrollmentHistoryRepository springDataRepository) {
        this.springDataRepository = Objects.requireNonNull(springDataRepository, "springDataRepository must not be null");
    }

    @Override
    public TrainingEnrollmentHistory save(TrainingEnrollmentHistory history) {
        TrainingEnrollmentHistoryEntity entity = TrainingEnrollmentHistoryEntity.fromDomain(history);
        TrainingEnrollmentHistoryEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<TrainingEnrollmentHistory> findByEnrollmentIdOrderByCreatedAtAsc(String enrollmentId) {
        return springDataRepository.findByEnrollmentIdOrderByCreatedAtAsc(enrollmentId).stream()
                .map(TrainingEnrollmentHistoryEntity::toDomain)
                .collect(Collectors.toList());
    }
}
