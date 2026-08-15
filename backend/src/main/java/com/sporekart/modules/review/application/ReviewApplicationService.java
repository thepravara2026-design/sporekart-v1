package com.sporekart.modules.review.application;

import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.review.application.dto.*;
import com.sporekart.modules.review.domain.*;
import com.sporekart.modules.review.domain.event.*;
import com.sporekart.modules.review.domain.exception.DuplicateReviewException;
import com.sporekart.modules.review.domain.exception.ReviewNotFoundException;
import com.sporekart.modules.review.infrastructure.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ReviewApplicationService.class);

    private final ProductReviewRepository reviewRepository;
    private final ReviewHelpfulnessVoteRepository voteRepository;
    private final ReviewMerchantReplyRepository replyRepository;
    private final ProductRatingSummaryRepository summaryRepository;
    private final OrderRepository orderRepository;
    private final SpamModerationFilter spamModerationFilter;
    private final RatingCalculatorService ratingCalculatorService;
    private final ApplicationEventPublisher eventPublisher;

    public ReviewApplicationService(
            ProductReviewRepository reviewRepository,
            ReviewHelpfulnessVoteRepository voteRepository,
            ReviewMerchantReplyRepository replyRepository,
            ProductRatingSummaryRepository summaryRepository,
            OrderRepository orderRepository,
            SpamModerationFilter spamModerationFilter,
            RatingCalculatorService ratingCalculatorService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.reviewRepository = reviewRepository;
        this.voteRepository = voteRepository;
        this.replyRepository = replyRepository;
        this.summaryRepository = summaryRepository;
        this.orderRepository = orderRepository;
        this.spamModerationFilter = spamModerationFilter;
        this.ratingCalculatorService = ratingCalculatorService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ProductReviewDto submitCustomerReview(CreateReviewRequestDto dto, String customerId) {
        log.info("Submitting product review for customer {}, productId: {}", customerId, dto.productId());

        // 1. Prevent duplicate reviews per order item
        if (reviewRepository.findByCustomerIdAndOrderItemId(customerId, dto.orderItemId()).isPresent()) {
            throw new DuplicateReviewException(dto.orderItemId());
        }

        // 2. Validate verified purchase status via Order aggregate
        boolean isVerified = false;
        try {
            Optional<Order> orderOpt = orderRepository.findById(UUID.fromString(dto.orderId()));
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                isVerified = order.getCustomerId().equals(customerId) && order.getStatus() == OrderStatus.DELIVERED;
            }
        } catch (Exception e) {
            log.warn("Could not verify order status for review submission: {}", e.getMessage());
        }

        // 3. Evaluate initial moderation status via SpamFilter
        ModerationOutcome outcome = spamModerationFilter.evaluateReview(dto.title(), dto.comment());
        ReviewStatus initialStatus = (outcome == ModerationOutcome.PASS) ? ReviewStatus.APPROVED : ReviewStatus.PENDING_MODERATION;

        String currentYear = String.valueOf(LocalDate.now().getYear());
        long count = reviewRepository.countReviewsForYear(currentYear);
        String reviewRef = String.format("REV-%s-%06d", currentYear, count + 1);

        ProductReview review = ProductReview.create(
                reviewRef,
                dto.productId(),
                dto.orderId(),
                dto.orderItemId(),
                customerId,
                dto.rating(),
                dto.qualityRating(),
                dto.valueRating(),
                dto.title(),
                dto.comment(),
                isVerified,
                initialStatus,
                dto.mediaUrls()
        );

        ProductReview saved = reviewRepository.save(review);

        eventPublisher.publishEvent(ReviewSubmittedEvent.create(
                saved.getId(), saved.getReviewReference(), saved.getProductId(), customerId, saved.getRating()
        ));

        // 4. Quality alert for low ratings
        if (saved.getRating() <= 2) {
            eventPublisher.publishEvent(ProductQualityAlertEvent.create(
                    saved.getProductId(), saved.getId(), saved.getRating(), saved.getComment()
            ));
        }

        // 5. Recalculate rating summary if auto-approved
        if (initialStatus == ReviewStatus.APPROVED) {
            recalculateProductRatingSummary(dto.productId());
        }

        return getReviewDto(saved);
    }

    @Transactional
    public ProductReviewDto voteHelpfulness(String reviewRef, boolean isHelpful, String customerId) {
        ProductReview review = reviewRepository.findByReviewReference(reviewRef)
                .or(() -> reviewRepository.findById(reviewRef))
                .orElseThrow(() -> new ReviewNotFoundException(reviewRef));

        Optional<ReviewHelpfulnessVote> existingVote = voteRepository.findByReviewIdAndCustomerId(review.getId(), customerId);
        if (existingVote.isPresent()) {
            log.info("Customer {} already voted on review {}, skipping duplicate vote", customerId, reviewRef);
            return getReviewDto(review);
        }

        ReviewHelpfulnessVote vote = ReviewHelpfulnessVote.create(review.getId(), customerId, isHelpful);
        voteRepository.save(vote);

        review.applyHelpfulnessVote(isHelpful);
        ProductReview saved = reviewRepository.save(review);

        return getReviewDto(saved);
    }

    @Transactional
    public MerchantReplyDto addMerchantReply(String reviewRef, String replyText, String authorId) {
        ProductReview review = reviewRepository.findByReviewReference(reviewRef)
                .or(() -> reviewRepository.findById(reviewRef))
                .orElseThrow(() -> new ReviewNotFoundException(reviewRef));

        ReviewMerchantReply reply = ReviewMerchantReply.create(review.getId(), authorId, replyText);
        ReviewMerchantReply saved = replyRepository.save(reply);

        return MerchantReplyDto.fromDomain(saved);
    }

    @Transactional
    public ProductReviewDto approveReview(String reviewRef, String adminId) {
        ProductReview review = reviewRepository.findByReviewReference(reviewRef)
                .or(() -> reviewRepository.findById(reviewRef))
                .orElseThrow(() -> new ReviewNotFoundException(reviewRef));

        review.approve();
        ProductReview saved = reviewRepository.save(review);

        recalculateProductRatingSummary(saved.getProductId());

        eventPublisher.publishEvent(ReviewApprovedEvent.create(saved.getId(), saved.getReviewReference(), saved.getProductId(), saved.getRating()));
        return getReviewDto(saved);
    }

    @Transactional
    public ProductReviewDto rejectReview(String reviewRef, String reason, String adminId) {
        ProductReview review = reviewRepository.findByReviewReference(reviewRef)
                .or(() -> reviewRepository.findById(reviewRef))
                .orElseThrow(() -> new ReviewNotFoundException(reviewRef));

        boolean wasApproved = review.getStatus() == ReviewStatus.APPROVED;
        review.reject(reason);
        ProductReview saved = reviewRepository.save(review);

        if (wasApproved) {
            recalculateProductRatingSummary(saved.getProductId());
        }

        eventPublisher.publishEvent(ReviewRejectedEvent.create(saved.getId(), saved.getReviewReference(), saved.getProductId(), reason));
        return getReviewDto(saved);
    }

    @Transactional
    public ProductReviewDto flagReview(String reviewRef, String adminId) {
        ProductReview review = reviewRepository.findByReviewReference(reviewRef)
                .or(() -> reviewRepository.findById(reviewRef))
                .orElseThrow(() -> new ReviewNotFoundException(reviewRef));

        review.flag();
        ProductReview saved = reviewRepository.save(review);
        return getReviewDto(saved);
    }

    @Transactional(readOnly = true)
    public ProductRatingSummaryDto getProductRatingSummary(String productId) {
        ProductRatingSummary summary = summaryRepository.findById(productId)
                .orElseGet(() -> ProductRatingSummary.createEmpty(productId));
        return ProductRatingSummaryDto.fromDomain(summary);
    }

    @Transactional(readOnly = true)
    public List<ProductReviewDto> listProductReviews(String productId) {
        return reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, ReviewStatus.APPROVED).stream()
                .map(this::getReviewDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductReviewDto> listAdminReviews(AdminReviewFilterDto filter) {
        return reviewRepository.searchReviewsAdmin(filter.status(), filter.productId(), filter.rating()).stream()
                .map(this::getReviewDto)
                .toList();
    }

    @Transactional
    public void recalculateProductRatingSummary(String productId) {
        List<ProductReview> approvedReviews = reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, ReviewStatus.APPROVED);
        ProductRatingSummary summary = summaryRepository.findById(productId)
                .orElseGet(() -> ProductRatingSummary.createEmpty(productId));

        ratingCalculatorService.updateRatingSummary(summary, approvedReviews);
        summaryRepository.save(summary);
        log.info("Recalculated rating summary for product {}: average = {}, count = {}", productId, summary.getAverageRating(), summary.getTotalReviewsCount());
    }

    private ProductReviewDto getReviewDto(ProductReview review) {
        List<MerchantReplyDto> replies = replyRepository.findByReviewIdOrderByCreatedAtAsc(review.getId()).stream()
                .map(MerchantReplyDto::fromDomain)
                .toList();
        return ProductReviewDto.fromDomain(review, replies);
    }
}
