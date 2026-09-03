package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.TrainingPaymentApplicationService;
import com.sporekart.modules.training.controller.dto.TrainingPaymentOrderResponse;
import com.sporekart.modules.training.controller.dto.TrainingPaymentStatusResponse;
import com.sporekart.modules.training.controller.dto.TrainingPaymentVerificationRequestDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/batches/{batchId}/enrollment")
public class TrainingPaymentController {

    private static final Logger log = LoggerFactory.getLogger(TrainingPaymentController.class);

    private final TrainingPaymentApplicationService paymentService;

    public TrainingPaymentController(TrainingPaymentApplicationService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payment-order")
    @PreAuthorize("hasAnyRole('TRAINEE', 'CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TrainingPaymentOrderResponse>> createPaymentOrder(@PathVariable String batchId) {
        String traineeId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("REST POST /api/v1/batches/{}/enrollment/payment-order by traineeId={}", batchId, traineeId);

        TrainingPaymentOrderResponse response = paymentService.initiatePaymentOrder(batchId, traineeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/payment-verify")
    @PreAuthorize("hasAnyRole('TRAINEE', 'CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TrainingPaymentStatusResponse>> verifyPayment(
            @PathVariable String batchId,
            @Valid @RequestBody TrainingPaymentVerificationRequestDto command
    ) {
        String traineeId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("REST POST /api/v1/batches/{}/enrollment/payment-verify by traineeId={}", batchId, traineeId);

        TrainingPaymentStatusResponse response = paymentService.verifyPayment(batchId, command, traineeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/payment-status")
    @PreAuthorize("hasAnyRole('TRAINEE', 'CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TrainingPaymentStatusResponse>> getPaymentStatus(@PathVariable String batchId) {
        String traineeId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("REST GET /api/v1/batches/{}/enrollment/payment-status by traineeId={}", batchId, traineeId);

        TrainingPaymentStatusResponse response = paymentService.getPaymentStatus(batchId, traineeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
