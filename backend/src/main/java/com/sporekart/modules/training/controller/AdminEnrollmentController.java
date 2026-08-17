package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.controller.dto.EnrollmentResponse;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/batches")
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN')")
public class AdminEnrollmentController {

    private final EnrollmentApplicationService enrollmentService;

    public AdminEnrollmentController(EnrollmentApplicationService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/{batchId}/enrollments")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getBatchEnrollments(
            @PathVariable("batchId") String batchId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortParts[0]));

        Page<TrainingEnrollment> enrollments = enrollmentService.getBatchEnrollmentsForAdmin(batchId, pageable);
        Page<EnrollmentResponse> responsePage = enrollments.map(EnrollmentResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }
}
