package com.sporekart.modules.grower.web.dto;

import java.math.BigDecimal;

public record GrowerReportSummaryDto(
        String period,
        BigDecimal totalSales,
        int totalOrders,
        int unitsSold,
        double fulfillmentRate,
        BigDecimal averageOrderValue,
        double returnRatePercent
) {}
