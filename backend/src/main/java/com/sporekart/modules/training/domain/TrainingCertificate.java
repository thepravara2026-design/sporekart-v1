package com.sporekart.modules.training.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TrainingCertificate {

    private final String id;
    private final String certificateNumber;
    private final String verificationCode;
    private final String enrollmentId;
    private final String traineeId;
    private final String traineeName;
    private final String programId;
    private final String programTitle;
    private final String batchId;
    private final String batchCode;
    private final Instant issuedAt;
    private final Instant completionDate;
    private final String issuerSignature;
    private boolean revoked;
    private String revocationReason;

    public TrainingCertificate(
            String id,
            String certificateNumber,
            String verificationCode,
            String enrollmentId,
            String traineeId,
            String traineeName,
            String programId,
            String programTitle,
            String batchId,
            String batchCode,
            Instant issuedAt,
            Instant completionDate,
            String issuerSignature,
            boolean revoked,
            String revocationReason) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.certificateNumber = Objects.requireNonNull(certificateNumber, "certificateNumber must not be null");
        this.verificationCode = Objects.requireNonNull(verificationCode, "verificationCode must not be null");
        this.enrollmentId = Objects.requireNonNull(enrollmentId, "enrollmentId must not be null");
        this.traineeId = Objects.requireNonNull(traineeId, "traineeId must not be null");
        this.traineeName = traineeName != null ? traineeName : traineeId;
        this.programId = Objects.requireNonNull(programId, "programId must not be null");
        this.programTitle = programTitle != null ? programTitle : "Training Program";
        this.batchId = Objects.requireNonNull(batchId, "batchId must not be null");
        this.batchCode = batchCode != null ? batchCode : batchId;
        this.issuedAt = issuedAt != null ? issuedAt : Instant.now();
        this.completionDate = completionDate != null ? completionDate : Instant.now();
        this.issuerSignature = issuerSignature != null ? issuerSignature : "Sporekart Training Authority";
        this.revoked = revoked;
        this.revocationReason = revocationReason;
    }

    public static TrainingCertificate issue(
            String enrollmentId, String traineeId, String traineeName,
            String programId, String programTitle, String batchId, String batchCode,
            Instant completionDate) {
        String num = "SK-CERT-" + (System.currentTimeMillis() % 1000000);
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return new TrainingCertificate(
                UUID.randomUUID().toString(), num, code, enrollmentId, traineeId, traineeName,
                programId, programTitle, batchId, batchCode, Instant.now(), completionDate,
                "Sporekart Certification Authority", false, null
        );
    }

    public void revoke(String reason) {
        this.revoked = true;
        this.revocationReason = reason;
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
