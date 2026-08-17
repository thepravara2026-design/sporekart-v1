package com.sporekart.modules.training.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataTrainingProgramRepository extends JpaRepository<TrainingProgramEntity, String> {
}
