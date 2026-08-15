package com.sporekart.modules.review.infrastructure.persistence;

import com.sporekart.modules.review.domain.ProductReview;
import com.sporekart.modules.review.domain.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, String> {

    Optional<ProductReview> findByReviewReference(String reviewReference);

    List<ProductReview> findByProductIdAndStatusOrderByCreatedAtDesc(String productId, ReviewStatus status);

    List<ProductReview> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    Optional<ProductReview> findByCustomerIdAndOrderItemId(String customerId, String orderItemId);

    @Query("SELECT r FROM ProductReview r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:productId IS NULL OR r.productId = :productId) AND " +
           "(:rating IS NULL OR r.rating = :rating) " +
           "ORDER BY r.createdAt DESC")
    List<ProductReview> searchReviewsAdmin(
            @Param("status") ReviewStatus status,
            @Param("productId") String productId,
            @Param("rating") Integer rating
    );

    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.reviewReference LIKE CONCAT('REV-', :year, '-%')")
    long countReviewsForYear(@Param("year") String year);
}
