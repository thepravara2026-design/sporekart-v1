package com.sporekart.modules.returns.controller;

import com.sporekart.modules.returns.application.ReturnApplicationService;
import com.sporekart.modules.returns.application.dto.CreateReturnRequestDto;
import com.sporekart.modules.returns.application.dto.ReturnDto;
import com.sporekart.modules.returns.application.dto.ReturnEligibilityDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Returns", description = "Customer return request eligibility check, creation, and lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public class ReturnController {

    private static final Logger log = LoggerFactory.getLogger(ReturnController.class);

    private final ReturnApplicationService returnApplicationService;

    public ReturnController(ReturnApplicationService returnApplicationService) {
        this.returnApplicationService = returnApplicationService;
    }

    @GetMapping("/orders/{orderReference}/return-eligibility")
    public ResponseEntity<ReturnEligibilityDto> checkEligibility(
            @PathVariable String orderReference,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        log.info("REST: Request to check return eligibility for orderRef: {}", orderReference);
        ReturnEligibilityDto result = returnApplicationService.checkEligibility(orderReference, customerId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/orders/{orderReference}/returns")
    public ResponseEntity<ReturnDto> createReturn(
            @PathVariable String orderReference,
            @Valid @RequestBody CreateReturnRequestDto requestDto,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        log.info("REST: Request to create return for orderRef: {}", orderReference);
        ReturnDto result = returnApplicationService.createReturn(orderReference, requestDto, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/returns/{returnReference}")
    public ResponseEntity<ReturnDto> getReturnByReference(
            @PathVariable String returnReference,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        log.info("REST: Request to fetch return by reference: {}", returnReference);
        ReturnDto result = returnApplicationService.getReturnByReference(returnReference, customerId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customer/returns")
    public ResponseEntity<List<ReturnDto>> listCustomerReturns(
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        log.info("REST: Request to list returns for customer: {}", customerId);
        List<ReturnDto> results = returnApplicationService.listReturnsForCustomer(customerId);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/returns/{returnReference}/cancel")
    public ResponseEntity<ReturnDto> cancelReturn(
            @PathVariable String returnReference,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        log.info("REST: Request to cancel return: {}", returnReference);
        ReturnDto result = returnApplicationService.cancelReturn(returnReference, customerId);
        return ResponseEntity.ok(result);
    }

    private String resolveCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                    "Authenticated user identity required");
        }
        return authentication.getName();
    }
}

