package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.TrainingProgram;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JpaTrainingProgramRepositoryAdapter implements TrainingProgramRepository {

    private final SpringDataTrainingProgramRepository springDataRepository;

    public JpaTrainingProgramRepositoryAdapter(SpringDataTrainingProgramRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public TrainingProgram save(TrainingProgram program) {
        TrainingProgramEntity entity = TrainingProgramEntity.fromDomain(program);
        TrainingProgramEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<TrainingProgram> findById(String id) {
        return springDataRepository.findById(id).map(TrainingProgramEntity::toDomain);
    }

    @Override
    public List<TrainingProgram> findAll() {
        return springDataRepository.findAll().stream()
                .map(TrainingProgramEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        springDataRepository.deleteById(id);
    }
}
