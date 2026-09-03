package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.TrainingAttendanceService;
import com.sporekart.modules.training.application.TrainingCompletionService;
import com.sporekart.modules.training.domain.AttendanceStatus;
import com.sporekart.modules.training.domain.TrainingAttendance;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/batches")
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN')")
@Tag(name = "Admin Training Attendance & Completion", description = "Trainer and Administrator session attendance marking and graduation evaluation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminAttendanceController {

    private final TrainingAttendanceService attendanceService;
    private final TrainingCompletionService completionService;

    public AdminAttendanceController(
            TrainingAttendanceService attendanceService,
            TrainingCompletionService completionService) {
        this.attendanceService = attendanceService;
        this.completionService = completionService;
    }

    @GetMapping("/{batchId}/schedules/{scheduleId}/attendance")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> getSessionAttendanceRoster(
            @PathVariable String batchId,
            @PathVariable String scheduleId) {
        List<TrainingAttendance> roster = attendanceService.getScheduleAttendanceRoster(batchId, scheduleId);
        List<AttendanceDto> dtos = roster.stream().map(AttendanceDto::fromDomain).toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PostMapping("/{batchId}/schedules/{scheduleId}/attendance")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> markBulkAttendance(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String batchId,
            @PathVariable String scheduleId,
            @RequestBody BulkAttendanceRequest request) {
        String actor = principal != null ? principal.getUsername() : "ADMIN";
        List<TrainingAttendanceService.AttendanceItem> items = request.items().stream()
                .map(i -> new TrainingAttendanceService.AttendanceItem(i.enrollmentId(), i.status(), i.notes()))
                .toList();

        List<TrainingAttendance> saved = attendanceService.markRosterAttendance(batchId, scheduleId, items, actor);
        List<AttendanceDto> dtos = saved.stream().map(AttendanceDto::fromDomain).toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PostMapping("/{batchId}/evaluate-completion")
    public ResponseEntity<ApiResponse<TrainingCompletionService.BatchCompletionResult>> evaluateBatchCompletion(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String batchId,
            @RequestBody(required = false) Map<String, Object> body) {
        String actor = principal != null ? principal.getUsername() : "ADMIN";
        Double threshold = null;
        if (body != null && body.containsKey("minAttendancePercentage")) {
            try {
                threshold = Double.parseDouble(body.get("minAttendancePercentage").toString());
            } catch (Exception ignored) {}
        }

        TrainingCompletionService.BatchCompletionResult result =
                completionService.evaluateBatchCompletion(batchId, threshold, actor);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    public record AttendanceItemRequest(String enrollmentId, AttendanceStatus status, String notes) {}
    public record BulkAttendanceRequest(List<AttendanceItemRequest> items) {}

    public record AttendanceDto(
            String id, String enrollmentId, String scheduleId, String batchId, String traineeId,
            AttendanceStatus status, String notes, String markedBy, String markedAt
    ) {
        public static AttendanceDto fromDomain(TrainingAttendance a) {
            return new AttendanceDto(
                    a.getId(), a.getEnrollmentId(), a.getScheduleId(), a.getBatchId(), a.getTraineeId(),
                    a.getStatus(), a.getNotes(), a.getMarkedBy(), a.getMarkedAt().toString()
            );
        }
    }
}
