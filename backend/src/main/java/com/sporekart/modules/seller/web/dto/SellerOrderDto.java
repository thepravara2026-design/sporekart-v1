package com.sporekart.modules.seller.web.dto;

import java.math.BigDecimal;

public record SellerOrderDto(
        String id,
        String orderNumber,
        String customerName,
        String customerEmail,
        int itemsCount,
        BigDecimal totalAmount,
        String currency,
        String status,
        String orderDate
) {}
