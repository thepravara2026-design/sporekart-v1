package com.sporekart.modules.grower.web.dto;

import java.math.BigDecimal;

public record GrowerDashboardDto(
        BigDecimal grossRevenue,
        int activeOrdersCount,
        int lowStockCount,
        int activeProductsCount,
        int totalOrdersCount,
        double fulfillmentRate
) {}
