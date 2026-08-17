package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.TrainingCertificateService;
import com.sporekart.modules.training.domain.TrainingCertificate;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/certificates")
@Tag(name = "Public Certificate Verification", description = "Public unauthenticated endpoint to verify authenticity of SPOREKART digital certificates")
public class PublicCertificateVerificationController {

    private final TrainingCertificateService certificateService;

    public PublicCertificateVerificationController(TrainingCertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/verify/{verificationCode}")
    public ResponseEntity<ApiResponse<VerificationResultDto>> verifyCertificate(
            @PathVariable String verificationCode) {
        Optional<TrainingCertificate> certOpt = certificateService.verifyCertificate(verificationCode);

        if (certOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(new VerificationResultDto(
                    false, verificationCode, null, null, null, null, null, null, "Certificate verification code not found"
            )));
        }

        TrainingCertificate cert = certOpt.get();
        if (cert.isRevoked()) {
            return ResponseEntity.ok(ApiResponse.success(new VerificationResultDto(
                    false, verificationCode, cert.getCertificateNumber(), cert.getTraineeName(),
                    cert.getProgramTitle(), cert.getBatchCode(), cert.getCompletionDate().toString(),
                    cert.getIssuerSignature(), "Certificate has been revoked: " + cert.getRevocationReason()
            )));
        }

        return ResponseEntity.ok(ApiResponse.success(new VerificationResultDto(
                true, verificationCode, cert.getCertificateNumber(), cert.getTraineeName(),
                cert.getProgramTitle(), cert.getBatchCode(), cert.getCompletionDate().toString(),
                cert.getIssuerSignature(), "Certificate is authentic and valid"
        )));
    }

    public record VerificationResultDto(
            boolean valid,
            String verificationCode,
            String certificateNumber,
            String traineeName,
            String programTitle,
            String batchCode,
            String completionDate,
            String issuerSignature,
            String statusMessage
    ) {}
}
