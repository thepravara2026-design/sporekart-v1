package com.sporekart.modules.training.domain.port;

import com.sporekart.modules.training.domain.TrainingAttendance;

import java.util.List;
import java.util.Optional;

public interface TrainingAttendanceRepository {
    TrainingAttendance save(TrainingAttendance attendance);
    List<TrainingAttendance> saveAll(List<TrainingAttendance> attendances);
    Optional<TrainingAttendance> findById(String id);
    Optional<TrainingAttendance> findByEnrollmentIdAndScheduleId(String enrollmentId, String scheduleId);
    List<TrainingAttendance> findByEnrollmentId(String enrollmentId);
    List<TrainingAttendance> findByBatchIdAndScheduleId(String batchId, String scheduleId);
    List<TrainingAttendance> findByBatchId(String batchId);
}
