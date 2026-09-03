package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.ProgramStatus;
import com.sporekart.modules.training.domain.TrainingProgram;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Optional<TrainingProgram> findBySlug(String slug) {
        return springDataRepository.findBySlug(slug).map(TrainingProgramEntity::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return springDataRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsByTitleIgnoreCase(String title) {
        return springDataRepository.existsByTitleIgnoreCase(title);
    }

    @Override
    public List<TrainingProgram> findAll() {
        return springDataRepository.findAll().stream()
                .map(TrainingProgramEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TrainingProgram> findAll(Pageable pageable) {
        return springDataRepository.findAll(pageable).map(TrainingProgramEntity::toDomain);
    }

    @Override
    public Page<TrainingProgram> findAllActive(Pageable pageable) {
        return springDataRepository.findByStatus(ProgramStatus.ACTIVE, pageable).map(TrainingProgramEntity::toDomain);
    }

    @Override
    public Page<TrainingProgram> searchPrograms(String searchKey, ProgramStatus status, String category, Pageable pageable) {
        return springDataRepository.searchPrograms(searchKey, status, category, pageable).map(TrainingProgramEntity::toDomain);
    }

    @Override
    public void deleteById(String id) {
        springDataRepository.deleteById(id);
    }
}
