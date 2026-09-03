package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.TrainingAttendance;
import com.sporekart.modules.training.domain.port.TrainingAttendanceRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaTrainingAttendanceRepositoryAdapter implements TrainingAttendanceRepository {

    private final SpringDataTrainingAttendanceRepository repository;

    public JpaTrainingAttendanceRepositoryAdapter(SpringDataTrainingAttendanceRepository repository) {
        this.repository = repository;
    }

    @Override
    public TrainingAttendance save(TrainingAttendance attendance) {
        TrainingAttendanceEntity entity = TrainingAttendanceEntity.fromDomain(attendance);
        return repository.save(entity).toDomain();
    }

    @Override
    public List<TrainingAttendance> saveAll(List<TrainingAttendance> attendances) {
        List<TrainingAttendanceEntity> entities = attendances.stream()
                .map(TrainingAttendanceEntity::fromDomain)
                .toList();
        return repository.saveAll(entities).stream()
                .map(TrainingAttendanceEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<TrainingAttendance> findById(String id) {
        return repository.findById(id).map(TrainingAttendanceEntity::toDomain);
    }

    @Override
    public Optional<TrainingAttendance> findByEnrollmentIdAndScheduleId(String enrollmentId, String scheduleId) {
        return repository.findByEnrollmentIdAndScheduleId(enrollmentId, scheduleId).map(TrainingAttendanceEntity::toDomain);
    }

    @Override
    public List<TrainingAttendance> findByEnrollmentId(String enrollmentId) {
        return repository.findByEnrollmentId(enrollmentId).stream()
                .map(TrainingAttendanceEntity::toDomain)
                .toList();
    }

    @Override
    public List<TrainingAttendance> findByBatchIdAndScheduleId(String batchId, String scheduleId) {
        return repository.findByBatchIdAndScheduleId(batchId, scheduleId).stream()
                .map(TrainingAttendanceEntity::toDomain)
                .toList();
    }

    @Override
    public List<TrainingAttendance> findByBatchId(String batchId) {
        return repository.findByBatchId(batchId).stream()
                .map(TrainingAttendanceEntity::toDomain)
                .toList();
    }
}
