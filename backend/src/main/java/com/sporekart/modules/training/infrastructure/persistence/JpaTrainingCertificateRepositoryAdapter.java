package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.TrainingCertificate;
import com.sporekart.modules.training.domain.port.TrainingCertificateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaTrainingCertificateRepositoryAdapter implements TrainingCertificateRepository {

    private final SpringDataTrainingCertificateRepository repository;

    public JpaTrainingCertificateRepositoryAdapter(SpringDataTrainingCertificateRepository repository) {
        this.repository = repository;
    }

    @Override
    public TrainingCertificate save(TrainingCertificate certificate) {
        TrainingCertificateEntity entity = TrainingCertificateEntity.fromDomain(certificate);
        return repository.save(entity).toDomain();
    }

    @Override
    public Optional<TrainingCertificate> findById(String id) {
        return repository.findById(id).map(TrainingCertificateEntity::toDomain);
    }

    @Override
    public Optional<TrainingCertificate> findByEnrollmentId(String enrollmentId) {
        return repository.findByEnrollmentId(enrollmentId).map(TrainingCertificateEntity::toDomain);
    }

    @Override
    public Optional<TrainingCertificate> findByVerificationCode(String verificationCode) {
        return repository.findByVerificationCode(verificationCode).map(TrainingCertificateEntity::toDomain);
    }

    @Override
    public List<TrainingCertificate> findByTraineeId(String traineeId) {
        return repository.findByTraineeId(traineeId).stream()
                .map(TrainingCertificateEntity::toDomain)
                .toList();
    }

    @Override
    public List<TrainingCertificate> findByBatchId(String batchId) {
        return repository.findByBatchId(batchId).stream()
                .map(TrainingCertificateEntity::toDomain)
                .toList();
    }
}
