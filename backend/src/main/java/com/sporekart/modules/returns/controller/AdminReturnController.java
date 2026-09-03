package com.sporekart.modules.returns.controller;

import com.sporekart.modules.returns.application.ReturnApplicationService;
import com.sporekart.modules.returns.application.dto.AdminReturnFilterDto;
import com.sporekart.modules.returns.application.dto.ReturnDto;
import com.sporekart.modules.returns.application.dto.ReturnInspectionDto;
import com.sporekart.modules.returns.domain.ReturnStatus;
import com.sporekart.modules.security.infrastructure.jwt.UserPrincipal;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/returns")
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN')")
public class AdminReturnController {

    private static final Logger log = LoggerFactory.getLogger(AdminReturnController.class);

    private final ReturnApplicationService returnApplicationService;

    public AdminReturnController(ReturnApplicationService returnApplicationService) {
        this.returnApplicationService = returnApplicationService;
    }

    private String resolveAdminId(String headerAdminId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return headerAdminId != null ? headerAdminId : "admin-1";
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
            @RequestHeader(value = "X-Admin-Id", required = false) String adminId
    ) {
        String notes = body != null ? body.get("notes") : null;
        String resolvedId = resolveAdminId(adminId);
        log.info("REST Admin: Approve return {} by admin {}", returnReference, resolvedId);
        ReturnDto result = returnApplicationService.approveReturn(returnReference, resolvedId, notes);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{returnReference}/reject")
    public ResponseEntity<ReturnDto> rejectReturn(
            @PathVariable String returnReference,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Admin-Id", required = false) String adminId
    ) {
        String reason = body != null ? body.getOrDefault("reason", "Rejected by admin") : "Rejected by admin";
        String resolvedId = resolveAdminId(adminId);
        log.info("REST Admin: Reject return {} by admin {}", returnReference, resolvedId);
        ReturnDto result = returnApplicationService.rejectReturn(returnReference, resolvedId, reason);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{returnReference}/inspect")
    public ResponseEntity<ReturnDto> inspectReturn(
            @PathVariable String returnReference,
            @Valid @RequestBody ReturnInspectionDto inspectionDto,
            @RequestHeader(value = "X-Admin-Id", required = false) String adminId
    ) {
        String resolvedId = resolveAdminId(adminId);
        log.info("REST Admin: Record inspection for return {} by admin {}", returnReference, resolvedId);
        ReturnDto result = returnApplicationService.processInspection(returnReference, inspectionDto, resolvedId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{returnReference}/refund/retry")
    public ResponseEntity<ReturnDto> retryRefund(
            @PathVariable String returnReference,
            @RequestHeader(value = "X-Admin-Id", required = false) String adminId
    ) {
        String resolvedId = resolveAdminId(adminId);
        log.info("REST Admin: Retry refund for return {} by admin {}", returnReference, resolvedId);
        ReturnDto result = returnApplicationService.orchestrateRefund(returnReference, resolvedId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{returnReference}/create-reverse-shipment")
    public ResponseEntity<ReturnDto> createReverseShipment(
            @PathVariable String returnReference,
            @RequestHeader(value = "X-Admin-Id", required = false) String adminId
    ) {
        String resolvedId = resolveAdminId(adminId);
        log.info("REST Admin: Create reverse shipment for return {} by admin {}", returnReference, resolvedId);
        ReturnDto result = returnApplicationService.createReverseShipment(returnReference, resolvedId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{returnReference}/reconcile")
    public ResponseEntity<ReturnDto> reconcileRefund(
            @PathVariable String returnReference,
            @RequestHeader(value = "X-Admin-Id", required = false) String adminId
    ) {
        String resolvedId = resolveAdminId(adminId);
        log.info("REST Admin: Reconcile return refund {} by admin {}", returnReference, resolvedId);
        ReturnDto result = returnApplicationService.reconcileRefundStatus(returnReference, resolvedId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{returnReference}/sync")
    public ResponseEntity<ReturnDto> syncReturn(
            @PathVariable String returnReference,
            @RequestHeader(value = "X-Admin-Id", required = false) String adminId
    ) {
        String resolvedId = resolveAdminId(adminId);
        log.info("REST Admin: Sync return refund {} by admin {}", returnReference, resolvedId);
        ReturnDto result = returnApplicationService.reconcileRefundStatus(returnReference, resolvedId);
        return ResponseEntity.ok(result);
    }
}
