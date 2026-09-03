package com.sporekart.modules.training.domain.port;

import com.sporekart.modules.training.domain.ProgramStatus;
import com.sporekart.modules.training.domain.TrainingProgram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TrainingProgramRepository {
    TrainingProgram save(TrainingProgram program);
    Optional<TrainingProgram> findById(String id);
    Optional<TrainingProgram> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByTitleIgnoreCase(String title);
    List<TrainingProgram> findAll();
    Page<TrainingProgram> findAll(Pageable pageable);
    Page<TrainingProgram> findAllActive(Pageable pageable);
    Page<TrainingProgram> searchPrograms(String searchKey, ProgramStatus status, String category, Pageable pageable);
    void deleteById(String id);
}
