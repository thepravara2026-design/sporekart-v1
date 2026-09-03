package com.sporekart.modules.seller.web.dto;

import java.math.BigDecimal;

public record SellerMetricsDto(
        BigDecimal totalSales,
        BigDecimal monthlyRevenue,
        int activeListingsCount,
        int totalInventoryOnHand,
        int syncErrorCount,
        int pendingOrdersCount,
        int fulfilledOrdersCount,
        BigDecimal payoutPendingAmount,
        BigDecimal payoutSettledAmount
) {}
