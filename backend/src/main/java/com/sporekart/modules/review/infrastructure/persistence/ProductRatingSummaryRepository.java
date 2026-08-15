package com.sporekart.modules.review.infrastructure.persistence;

import com.sporekart.modules.review.domain.ProductRatingSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRatingSummaryRepository extends JpaRepository<ProductRatingSummary, String> {
}
