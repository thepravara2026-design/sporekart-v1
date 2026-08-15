package com.sporekart.modules.review.controller;

import com.sporekart.modules.review.application.ReviewApplicationService;
import com.sporekart.modules.review.application.dto.*;
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
@Tag(name = "Reviews", description = "Customer product reviews — submission, helpfulness voting, and retrieval")
@SecurityRequirement(name = "bearerAuth")
public class CustomerReviewController {

    private static final Logger log = LoggerFactory.getLogger(CustomerReviewController.class);

    private final ReviewApplicationService reviewApplicationService;

    public CustomerReviewController(ReviewApplicationService reviewApplicationService) {
        this.reviewApplicationService = reviewApplicationService;
    }

    @PostMapping("/reviews")
    public ResponseEntity<ProductReviewDto> submitReview(
            @Valid @RequestBody CreateReviewRequestDto requestDto,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        log.info("REST: Submit product review for customer: {}", customerId);
        ProductReviewDto result = reviewApplicationService.submitCustomerReview(requestDto, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<ProductReviewDto>> listProductReviews(@PathVariable String productId) {
        log.info("REST: Fetch approved reviews for product: {}", productId);
        List<ProductReviewDto> results = reviewApplicationService.listProductReviews(productId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/products/{productId}/rating-summary")
    public ResponseEntity<ProductRatingSummaryDto> getRatingSummary(@PathVariable String productId) {
        log.info("REST: Fetch rating summary for product: {}", productId);
        ProductRatingSummaryDto result = reviewApplicationService.getProductRatingSummary(productId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reviews/{reviewReference}/vote")
    public ResponseEntity<ProductReviewDto> voteHelpfulness(
            @PathVariable String reviewReference,
            @Valid @RequestBody ReviewHelpfulnessDto voteDto,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        log.info("REST: Helpfulness vote for review: {}, isHelpful: {}", reviewReference, voteDto.isHelpful());
        ProductReviewDto result = reviewApplicationService.voteHelpfulness(reviewReference, voteDto.isHelpful(), customerId);
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