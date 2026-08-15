package com.sporekart.modules.support.controller;

import com.sporekart.modules.support.application.SupportApplicationService;
import com.sporekart.modules.support.application.dto.*;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer/support/tickets")
@Tag(name = "Support", description = "Customer support ticket creation, messaging, and replacement request management")
@SecurityRequirement(name = "bearerAuth")
public class CustomerSupportController {

    private static final Logger log = LoggerFactory.getLogger(CustomerSupportController.class);

    private final SupportApplicationService supportApplicationService;

    public CustomerSupportController(SupportApplicationService supportApplicationService) {
        this.supportApplicationService = supportApplicationService;
    }

    @PostMapping
    public ResponseEntity<SupportTicketDto> createTicket(
            @Valid @RequestBody CreateTicketRequestDto requestDto,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        log.info("REST: Request to create support ticket for customer: {}", customerId);
        SupportTicketDto result = supportApplicationService.createCustomerTicket(requestDto, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public ResponseEntity<List<SupportTicketDto>> listCustomerTickets(
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        log.info("REST: Request to list support tickets for customer: {}", customerId);
        List<SupportTicketDto> results = supportApplicationService.listTicketsForCustomer(customerId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{ticketNumber}")
    public ResponseEntity<SupportTicketDto> getCustomerTicket(
            @PathVariable String ticketNumber,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        log.info("REST: Request to fetch customer support ticket: {}", ticketNumber);
        SupportTicketDto result = supportApplicationService.getTicketForCustomer(ticketNumber, customerId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{ticketNumber}/messages")
    public ResponseEntity<SupportTicketDto> addCustomerMessage(
            @PathVariable String ticketNumber,
            @Valid @RequestBody AddMessageRequestDto requestDto,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        log.info("REST: Customer message added to ticket: {}", ticketNumber);
        SupportTicketDto result = supportApplicationService.addCustomerMessage(ticketNumber, requestDto, customerId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{ticketNumber}/reopen")
    public ResponseEntity<SupportTicketDto> reopenTicket(
            @PathVariable String ticketNumber,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        String reason = body != null ? body.getOrDefault("reason", "Customer requested reopen") : "Customer requested reopen";
        log.info("REST: Customer reopening ticket: {}", ticketNumber);
        SupportTicketDto result = supportApplicationService.reopenTicket(ticketNumber, reason, customerId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{ticketNumber}/replacements")
    public ResponseEntity<ReplacementRequestDto> requestReplacement(
            @PathVariable String ticketNumber,
            @Valid @RequestBody CreateReplacementRequestDto requestDto,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        log.info("REST: Request replacement for ticket: {}", ticketNumber);
        ReplacementRequestDto result = supportApplicationService.requestReplacement(ticketNumber, requestDto, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    private String resolveCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                    "Authenticated user identity required");
        }
        return authentication.getName();
    }
}