package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.TrainingEnrollmentPayment;
import com.sporekart.modules.training.domain.TrainingPaymentStatus;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentPaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaTrainingEnrollmentPaymentRepositoryAdapter implements TrainingEnrollmentPaymentRepository {

    private final SpringDataTrainingEnrollmentPaymentRepository springDataRepository;

    public JpaTrainingEnrollmentPaymentRepositoryAdapter(SpringDataTrainingEnrollmentPaymentRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public TrainingEnrollmentPayment save(TrainingEnrollmentPayment payment) {
        TrainingEnrollmentPaymentEntity entity = TrainingEnrollmentPaymentEntity.fromDomain(payment);
        TrainingEnrollmentPaymentEntity saved = springDataRepository.saveAndFlush(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<TrainingEnrollmentPayment> findById(String id) {
        return springDataRepository.findById(id).map(TrainingEnrollmentPaymentEntity::toDomain);
    }

    @Override
    public Optional<TrainingEnrollmentPayment> findByPaymentId(String paymentId) {
        return springDataRepository.findByPaymentId(paymentId).map(TrainingEnrollmentPaymentEntity::toDomain);
    }

    @Override
    public Optional<TrainingEnrollmentPayment> findByBatchIdAndTraineeIdAndStatus(String batchId, String traineeId, TrainingPaymentStatus status) {
        return springDataRepository.findByBatchIdAndTraineeIdAndStatus(batchId, traineeId, status)
                .map(TrainingEnrollmentPaymentEntity::toDomain);
    }

    @Override
    public Optional<TrainingEnrollmentPayment> findByBatchIdAndTraineeId(String batchId, String traineeId) {
        return springDataRepository.findTopByBatchIdAndTraineeIdOrderByCreatedAtDesc(batchId, traineeId)
                .map(TrainingEnrollmentPaymentEntity::toDomain);
    }

    @Override
    public List<TrainingEnrollmentPayment> findByTraineeId(String traineeId) {
        return springDataRepository.findByTraineeId(traineeId).stream()
                .map(TrainingEnrollmentPaymentEntity::toDomain)
                .collect(Collectors.toList());
    }
}
