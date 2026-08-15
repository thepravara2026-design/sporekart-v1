package com.sporekart.modules.payment.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.application.dto.PaymentCheckoutDto;
import com.sporekart.modules.payment.application.dto.PaymentDto;
import com.sporekart.modules.payment.application.dto.PaymentVerificationCommand;
import com.sporekart.modules.payment.application.dto.WebhookResponseDto;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Payments", description = "Razorpay-backed payment initiation, HMAC signature verification, and webhook event processing")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    public PaymentController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @PostMapping
    @Operation(
            summary = "Initiate Payment for Order",
            description = "Creates a Razorpay payment order for the specified customer order. Returns Razorpay order details required to complete the payment on the frontend."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment initiation successful — Razorpay order details returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order not payable in current state or missing orderId"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Payment provider unavailable")
    })
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
    @Operation(
            summary = "Verify Payment Signature",
            description = "Verifies the Razorpay HMAC payment signature after client-side payment completion. Transitions the payment and order to PAID state on success."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment verified and order updated to PAID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Signature verification failed or invalid payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<ApiResponse<PaymentDto>> verifyPayment(
            @Valid @RequestBody PaymentVerificationCommand command,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        PaymentDto payment = paymentApplicationService.verifyPayment(command, customerId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/{paymentReference}")
    @Operation(
            summary = "Get Payment by Reference",
            description = "Retrieves payment details by the Razorpay payment reference string. Only accessible by the customer who owns the order."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment details retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentByReference(
            @Parameter(description = "Razorpay payment reference (e.g. pay_XXXXXXXXXXXXXX)", example = "pay_XXXXXXXXXXXXXX") @PathVariable String paymentReference,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        PaymentDto payment = paymentApplicationService.getPaymentByReference(paymentReference, customerId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @PostMapping("/webhooks/razorpay")
    @Operation(
            summary = "Razorpay Webhook — Payment Event Receiver",
            description = "Receives payment lifecycle events from Razorpay. HMAC SHA256 signature in `X-Razorpay-Signature` header is verified before processing. This endpoint is public (no JWT required) as it is called by Razorpay servers.",
            security = {} // Public webhook endpoint — no JWT required
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or missing HMAC signature")
    })
    public ResponseEntity<WebhookResponseDto> processRazorpayWebhook(
            @RequestBody String rawBody,
            @Parameter(description = "Razorpay HMAC SHA256 signature") @RequestHeader(value = "X-Razorpay-Signature", required = false) String signatureHeader
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
