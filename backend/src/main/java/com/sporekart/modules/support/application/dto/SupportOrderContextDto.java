package com.sporekart.modules.support.application.dto;

public record SupportOrderContextDto(
        String orderId,
        String orderReference,
        String customerId,
        String orderStatus,
        String paymentStatus,
        String shipmentStatus,
        String returnStatus
) {}
