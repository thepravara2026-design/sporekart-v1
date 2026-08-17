package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.TrainingAttendanceService;
import com.sporekart.modules.training.application.TrainingCertificateService;
import com.sporekart.modules.training.domain.TrainingCertificate;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trainee/training")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Trainee Attendance & Certificates", description = "Trainee attendance record lookup and digital certificate portal")
@SecurityRequirement(name = "bearerAuth")
public class TraineeAttendanceController {

    private final TrainingAttendanceService attendanceService;
    private final TrainingCertificateService certificateService;

    public TraineeAttendanceController(
            TrainingAttendanceService attendanceService,
            TrainingCertificateService certificateService) {
        this.attendanceService = attendanceService;
        this.certificateService = certificateService;
    }

    @GetMapping("/enrollments/{enrollmentId}/attendance")
    public ResponseEntity<ApiResponse<TrainingAttendanceService.AttendanceSummary>> getMyAttendanceSummary(
            @PathVariable String enrollmentId) {
        String traineeId = getCurrentUserId();
        TrainingAttendanceService.AttendanceSummary summary =
                attendanceService.getTraineeAttendanceSummary(enrollmentId, traineeId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/certificates")
    public ResponseEntity<ApiResponse<List<CertificateDto>>> getMyCertificates() {
        String traineeId = getCurrentUserId();
        List<TrainingCertificate> certificates = certificateService.getTraineeCertificates(traineeId);
        List<CertificateDto> dtos = certificates.stream().map(CertificateDto::fromDomain).toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/certificates/{certificateId}")
    public ResponseEntity<ApiResponse<CertificateDto>> getCertificateDetail(
            @PathVariable String certificateId) {
        String traineeId = getCurrentUserId();
        TrainingCertificate cert = certificateService.getCertificateById(certificateId, traineeId);
        return ResponseEntity.ok(ApiResponse.success(CertificateDto.fromDomain(cert)));
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        return auth.getName();
    }

    public record CertificateDto(
            String id, String certificateNumber, String verificationCode,
            String enrollmentId, String traineeId, String traineeName,
            String programId, String programTitle, String batchId, String batchCode,
            String issuedAt, String completionDate, String issuerSignature, boolean revoked
    ) {
        public static CertificateDto fromDomain(TrainingCertificate c) {
            return new CertificateDto(
                    c.getId(), c.getCertificateNumber(), c.getVerificationCode(),
                    c.getEnrollmentId(), c.getTraineeId(), c.getTraineeName(),
                    c.getProgramId(), c.getProgramTitle(), c.getBatchId(), c.getBatchCode(),
                    c.getIssuedAt().toString(), c.getCompletionDate().toString(),
                    c.getIssuerSignature(), c.isRevoked()
            );
        }
    }
}
