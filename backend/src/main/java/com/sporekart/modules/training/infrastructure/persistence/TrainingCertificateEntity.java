package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.TrainingCertificate;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "training_certificates", indexes = {
        @Index(name = "idx_cert_ver_code", columnList = "verification_code", unique = true),
        @Index(name = "idx_cert_trainee", columnList = "trainee_id"),
        @Index(name = "idx_cert_enrollment", columnList = "enrollment_id", unique = true)
})
public class TrainingCertificateEntity {

    @Id
    private String id;

    @Column(name = "certificate_number", nullable = false, unique = true)
    private String certificateNumber;

    @Column(name = "verification_code", nullable = false, unique = true)
    private String verificationCode;

    @Column(name = "enrollment_id", nullable = false)
    private String enrollmentId;

    @Column(name = "trainee_id", nullable = false)
    private String traineeId;

    @Column(name = "trainee_name")
    private String traineeName;

    @Column(name = "program_id", nullable = false)
    private String programId;

    @Column(name = "program_title")
    private String programTitle;

    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @Column(name = "batch_code")
    private String batchCode;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "completion_date", nullable = false)
    private Instant completionDate;

    @Column(name = "issuer_signature")
    private String issuerSignature;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "revocation_reason")
    private String revocationReason;

    public TrainingCertificateEntity() {}

    public static TrainingCertificateEntity fromDomain(TrainingCertificate c) {
        TrainingCertificateEntity e = new TrainingCertificateEntity();
        e.id = c.getId();
        e.certificateNumber = c.getCertificateNumber();
        e.verificationCode = c.getVerificationCode();
        e.enrollmentId = c.getEnrollmentId();
        e.traineeId = c.getTraineeId();
        e.traineeName = c.getTraineeName();
        e.programId = c.getProgramId();
        e.programTitle = c.getProgramTitle();
        e.batchId = c.getBatchId();
        e.batchCode = c.getBatchCode();
        e.issuedAt = c.getIssuedAt();
        e.completionDate = c.getCompletionDate();
        e.issuerSignature = c.getIssuerSignature();
        e.revoked = c.isRevoked();
        e.revocationReason = c.getRevocationReason();
        return e;
    }

    public TrainingCertificate toDomain() {
        return new TrainingCertificate(
                id, certificateNumber, verificationCode, enrollmentId, traineeId, traineeName,
                programId, programTitle, batchId, batchCode, issuedAt, completionDate,
                issuerSignature, revoked, revocationReason
        );
    }

    public String getId() { return id; }
    public String getCertificateNumber() { return certificateNumber; }
    public String getVerificationCode() { return verificationCode; }
    public String getEnrollmentId() { return enrollmentId; }
    public String getTraineeId() { return traineeId; }
    public String getTraineeName() { return traineeName; }
    public String getProgramId() { return programId; }
    public String getProgramTitle() { return programTitle; }
    public String getBatchId() { return batchId; }
    public String getBatchCode() { return batchCode; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getCompletionDate() { return completionDate; }
    public String getIssuerSignature() { return issuerSignature; }
    public boolean isRevoked() { return revoked; }
    public String getRevocationReason() { return revocationReason; }
}
