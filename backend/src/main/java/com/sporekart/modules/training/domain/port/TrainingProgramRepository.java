package com.sporekart.modules.training.domain.port;

import com.sporekart.modules.training.domain.TrainingProgram;

import java.util.List;
import java.util.Optional;

public interface TrainingProgramRepository {
    TrainingProgram save(TrainingProgram program);
    Optional<TrainingProgram> findById(String id);
    List<TrainingProgram> findAll();
    void deleteById(String id);
}
