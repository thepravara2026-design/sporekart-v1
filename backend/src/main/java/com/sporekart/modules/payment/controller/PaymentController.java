package com.sporekart.modules.payment.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.application.dto.PaymentCheckoutDto;
import com.sporekart.modules.payment.application.dto.PaymentDto;
import com.sporekart.modules.payment.application.dto.PaymentVerificationCommand;
import com.sporekart.modules.payment.application.dto.WebhookResponseDto;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    public PaymentController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentCheckoutDto>> createPayment(
            @RequestBody Map<String, String> requestBody,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        String orderIdStr = requestBody.get("orderId");
        if (orderIdStr == null || orderIdStr.isBlank()) {
            throw new IllegalArgumentException("orderId field is required in request body");
        }
        UUID orderId = UUID.fromString(orderIdStr);
        PaymentCheckoutDto checkout = paymentApplicationService.createPayment(orderId, customerId);
        return ResponseEntity.ok(ApiResponse.success(checkout));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentDto>> verifyPayment(
            @Valid @RequestBody PaymentVerificationCommand command,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        PaymentDto payment = paymentApplicationService.verifyPayment(command, customerId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/{paymentReference}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentByReference(
            @PathVariable String paymentReference,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        PaymentDto payment = paymentApplicationService.getPaymentByReference(paymentReference, customerId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @PostMapping("/webhooks/razorpay")
    public ResponseEntity<WebhookResponseDto> processRazorpayWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signatureHeader
    ) {
        WebhookResponseDto response = paymentApplicationService.processWebhook(PaymentProviderType.RAZORPAY, rawBody, signatureHeader);
        return ResponseEntity.ok(response);
    }

    private String resolveCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("User authentication required");
        }
        return authentication.getName();
    }
}
