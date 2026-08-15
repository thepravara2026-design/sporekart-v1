package com.sporekart.modules.returns.controller;

import com.sporekart.modules.returns.application.ReturnApplicationService;
import com.sporekart.modules.returns.application.dto.AdminReturnFilterDto;
import com.sporekart.modules.returns.application.dto.ReturnDto;
import com.sporekart.modules.returns.application.dto.ReturnInspectionDto;
import com.sporekart.modules.returns.domain.ReturnStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/returns")
public class AdminReturnController {

    private static final Logger log = LoggerFactory.getLogger(AdminReturnController.class);

    private final ReturnApplicationService returnApplicationService;

    public AdminReturnController(ReturnApplicationService returnApplicationService) {
        this.returnApplicationService = returnApplicationService;
    }

    @GetMapping
    public ResponseEntity<List<ReturnDto>> listReturns(
            @RequestParam(required = false) ReturnStatus status,
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String orderReference
    ) {
        log.info("REST Admin: List returns request status: {}, searchKey: {}", status, searchKey);
        AdminReturnFilterDto filter = new AdminReturnFilterDto(status, searchKey, customerId, orderReference);
        List<ReturnDto> results = returnApplicationService.listReturnsForAdmin(filter);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{returnReference}/approve")
    public ResponseEntity<ReturnDto> approveReturn(
            @PathVariable String returnReference,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        String notes = body != null ? body.get("notes") : null;
        log.info("REST Admin: Approve return {}", returnReference);
        ReturnDto result = returnApplicationService.approveReturn(returnReference, adminId, notes);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{returnReference}/reject")
    public ResponseEntity<ReturnDto> rejectReturn(
            @PathVariable String returnReference,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        String reason = body != null ? body.getOrDefault("reason", "Rejected by admin") : "Rejected by admin";
        log.info("REST Admin: Reject return {}", returnReference);
        ReturnDto result = returnApplicationService.rejectReturn(returnReference, adminId, reason);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{returnReference}/inspect")
    public ResponseEntity<ReturnDto> inspectReturn(
            @PathVariable String returnReference,
            @RequestBody ReturnInspectionDto inspectionDto,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        log.info("REST Admin: Record inspection for return {}", returnReference);
        ReturnDto result = returnApplicationService.processInspection(returnReference, inspectionDto, adminId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{returnReference}/refund/retry")
    public ResponseEntity<ReturnDto> retryRefund(
            @PathVariable String returnReference,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        log.info("REST Admin: Retry refund for return {}", returnReference);
        ReturnDto result = returnApplicationService.orchestrateRefund(returnReference, adminId);
        return ResponseEntity.ok(result);
    }
}
