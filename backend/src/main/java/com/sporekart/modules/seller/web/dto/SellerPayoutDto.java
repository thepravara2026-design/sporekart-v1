package com.sporekart.modules.seller.web.dto;

import java.math.BigDecimal;

public record SellerPayoutDto(
        String id,
        String payoutReference,
        String period,
        BigDecimal amount,
        String currency,
        String status,
        String payoutDate,
        String bankAccountLast4
) {}
