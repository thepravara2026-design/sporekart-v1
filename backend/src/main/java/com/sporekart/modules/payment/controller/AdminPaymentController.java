package com.sporekart.modules.payment.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.payment.application.PaymentReconciliationService;
import com.sporekart.modules.payment.application.dto.PaymentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final PaymentReconciliationService reconciliationService;

    public AdminPaymentController(PaymentReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/{paymentId}/reconcile")
    public ResponseEntity<ApiResponse<PaymentDto>> reconcilePayment(
            @PathVariable UUID paymentId,
            Authentication authentication
    ) {
        String adminId = resolveAdminId(authentication);
        PaymentDto reconciled = reconciliationService.reconcilePayment(paymentId, adminId);
        return ResponseEntity.ok(ApiResponse.success(reconciled));
    }

    private String resolveAdminId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return "ADMIN";
        }
        return authentication.getName();
    }
}
