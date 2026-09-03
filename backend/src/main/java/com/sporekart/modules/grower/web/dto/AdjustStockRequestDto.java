package com.sporekart.modules.grower.web.dto;

public record AdjustStockRequestDto(
        int newOnHandQuantity,
        String reason
) {}
