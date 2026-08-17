package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.reporting.*;
import com.sporekart.modules.training.domain.TrainingCertificate;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/training/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN', 'TRAINING_OPERATOR')")
@Tag(name = "Admin Training Reporting & Operational Controls", description = "Operational intelligence, metrics, audit logs, and exception controls")
@SecurityRequirement(name = "bearerAuth")
public class AdminTrainingReportingController {

    private final TrainingReportingService reportingService;
    private final TrainingAuditService auditService;
    private final TrainingOperationalControlService controlService;
    private final TrainingReportExportService exportService;

    public AdminTrainingReportingController(
            TrainingReportingService reportingService,
            TrainingAuditService auditService,
            TrainingOperationalControlService controlService,
            TrainingReportExportService exportService) {
        this.reportingService = reportingService;
        this.auditService = auditService;
        this.controlService = controlService;
        this.exportService = exportService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<TrainingReportingService.ExecutiveOverview>> getExecutiveOverview() {
        TrainingReportingService.ExecutiveOverview overview = reportingService.getExecutiveOverview();
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    @GetMapping("/batches")
    public ResponseEntity<ApiResponse<List<TrainingReportingService.BatchUtilizationReportItem>>> getBatchUtilizationReport(
            @RequestParam(required = false) String programId) {
        List<TrainingReportingService.BatchUtilizationReportItem> report = reportingService.getBatchUtilizationReport(programId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<TrainingReportingService.NotificationOperationalReport>> getNotificationReport() {
        TrainingReportingService.NotificationOperationalReport report = reportingService.getNotificationReport();
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/audit")
    public ResponseEntity<ApiResponse<List<TrainingAuditService.AuditLogItem>>> getAuditHistory(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String enrollmentId) {
        List<TrainingAuditService.AuditLogItem> logs = auditService.getAuditLogs(actor, enrollmentId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/exceptions")
    public ResponseEntity<ApiResponse<List<TrainingOperationalControlService.OperationalExceptionItem>>> getOperationalExceptions() {
        List<TrainingOperationalControlService.OperationalExceptionItem> exceptions = controlService.getOperationalExceptions();
        return ResponseEntity.ok(ApiResponse.success(exceptions));
    }

    @GetMapping(value = "/export/enrollments", produces = "text/csv")
    public ResponseEntity<String> exportEnrollmentsCsv() {
        String csv = exportService.exportEnrollmentsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"enrollments_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping(value = "/export/audit", produces = "text/csv")
    public ResponseEntity<String> exportAuditCsv() {
        String csv = exportService.exportAuditLogsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit_history.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PostMapping("/controls/retry-notification/{notificationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> retryNotification(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String notificationId) {
        String actor = principal != null ? principal.getUsername() : "ADMIN";
        boolean success = controlService.retryFailedNotification(notificationId, actor);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "notificationId", notificationId,
                "retried", success,
                "actor", actor
        )));
    }

    @PostMapping("/controls/retry-certificate/{enrollmentId}")
    public ResponseEntity<ApiResponse<TrainingCertificate>> retryCertificate(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String enrollmentId) {
        String actor = principal != null ? principal.getUsername() : "ADMIN";
        TrainingCertificate cert = controlService.retryCertificateGeneration(enrollmentId, actor);
        return ResponseEntity.ok(ApiResponse.success(cert));
    }
}
