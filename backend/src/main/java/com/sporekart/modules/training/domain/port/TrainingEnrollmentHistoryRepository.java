package com.sporekart.modules.training.domain.port;

import com.sporekart.modules.training.domain.TrainingEnrollmentHistory;

import java.util.List;

public interface TrainingEnrollmentHistoryRepository {
    TrainingEnrollmentHistory save(TrainingEnrollmentHistory history);
    List<TrainingEnrollmentHistory> findByEnrollmentIdOrderByCreatedAtAsc(String enrollmentId);
}
