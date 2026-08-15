package com.sporekart.modules.returns.application.dto;

import com.sporekart.modules.returns.domain.ReturnStatus;

public record AdminReturnFilterDto(
        ReturnStatus status,
        String searchKey,
        String customerId,
        String orderReference
) {}
