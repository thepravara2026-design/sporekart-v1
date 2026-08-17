package com.sporekart.modules.training.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataTrainingEnrollmentHistoryRepository extends JpaRepository<TrainingEnrollmentHistoryEntity, String> {
    List<TrainingEnrollmentHistoryEntity> findByEnrollmentIdOrderByCreatedAtAsc(String enrollmentId);
    List<TrainingEnrollmentHistoryEntity> findAllByOrderByCreatedAtDesc();
}
