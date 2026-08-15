package com.sporekart.modules.review.controller;

import com.sporekart.modules.review.application.ReviewApplicationService;
import com.sporekart.modules.review.application.dto.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CustomerReviewController {

    private static final Logger log = LoggerFactory.getLogger(CustomerReviewController.class);

    private final ReviewApplicationService reviewApplicationService;

    public CustomerReviewController(ReviewApplicationService reviewApplicationService) {
        this.reviewApplicationService = reviewApplicationService;
    }

    @PostMapping("/reviews")
    public ResponseEntity<ProductReviewDto> submitReview(
            @Valid @RequestBody CreateReviewRequestDto requestDto,
            @RequestHeader(value = "X-Customer-Id", defaultValue = "cust-101") String customerId
    ) {
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
            @RequestHeader(value = "X-Customer-Id", defaultValue = "cust-101") String customerId
    ) {
        log.info("REST: Helpfulness vote for review: {}, isHelpful: {}", reviewReference, voteDto.isHelpful());
        ProductReviewDto result = reviewApplicationService.voteHelpfulness(reviewReference, voteDto.isHelpful(), customerId);
        return ResponseEntity.ok(result);
    }
}
