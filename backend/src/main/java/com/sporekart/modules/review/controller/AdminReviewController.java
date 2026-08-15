package com.sporekart.modules.review.controller;

import com.sporekart.modules.review.application.ReviewApplicationService;
import com.sporekart.modules.review.application.dto.*;
import com.sporekart.modules.review.domain.ReviewStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/reviews")
public class AdminReviewController {

    private static final Logger log = LoggerFactory.getLogger(AdminReviewController.class);

    private final ReviewApplicationService reviewApplicationService;

    public AdminReviewController(ReviewApplicationService reviewApplicationService) {
        this.reviewApplicationService = reviewApplicationService;
    }

    @GetMapping
    public ResponseEntity<List<ProductReviewDto>> listAdminReviews(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) Integer rating
    ) {
        log.info("REST Admin: List product reviews request status: {}, productId: {}", status, productId);
        AdminReviewFilterDto filter = new AdminReviewFilterDto(status, productId, rating);
        List<ProductReviewDto> results = reviewApplicationService.listAdminReviews(filter);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{reviewReference}/approve")
    public ResponseEntity<ProductReviewDto> approveReview(
            @PathVariable String reviewReference,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        log.info("REST Admin: Approve product review: {}", reviewReference);
        ProductReviewDto result = reviewApplicationService.approveReview(reviewReference, adminId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{reviewReference}/reject")
    public ResponseEntity<ProductReviewDto> rejectReview(
            @PathVariable String reviewReference,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        String reason = body != null ? body.getOrDefault("reason", "Rejected by admin moderation") : "Rejected by admin moderation";
        log.info("REST Admin: Reject product review: {}", reviewReference);
        ProductReviewDto result = reviewApplicationService.rejectReview(reviewReference, reason, adminId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{reviewReference}/flag")
    public ResponseEntity<ProductReviewDto> flagReview(
            @PathVariable String reviewReference,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        log.info("REST Admin: Flag product review: {}", reviewReference);
        ProductReviewDto result = reviewApplicationService.flagReview(reviewReference, adminId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{reviewReference}/replies")
    public ResponseEntity<MerchantReplyDto> addMerchantReply(
            @PathVariable String reviewReference,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        String replyText = body != null ? body.get("replyText") : "";
        log.info("REST Admin: Add merchant reply to review: {}", reviewReference);
        MerchantReplyDto result = reviewApplicationService.addMerchantReply(reviewReference, replyText, adminId);
        return ResponseEntity.ok(result);
    }
}
