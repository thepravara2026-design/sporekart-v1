package com.sporekart.modules.training.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataTrainingAttendanceRepository extends JpaRepository<TrainingAttendanceEntity, String> {
    Optional<TrainingAttendanceEntity> findByEnrollmentIdAndScheduleId(String enrollmentId, String scheduleId);
    List<TrainingAttendanceEntity> findByEnrollmentId(String enrollmentId);
    List<TrainingAttendanceEntity> findByBatchIdAndScheduleId(String batchId, String scheduleId);
    List<TrainingAttendanceEntity> findByBatchId(String batchId);
}
