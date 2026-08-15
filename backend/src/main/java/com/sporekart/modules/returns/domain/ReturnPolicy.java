package com.sporekart.modules.returns.domain;

import java.time.OffsetDateTime;
import java.util.Set;

public record ReturnPolicy(
        int returnWindowDays,
        Set<String> nonReturnableCategories,
        String policyVersion
) {
    public static ReturnPolicy defaultPolicy() {
        return new ReturnPolicy(14, Set.of("PERISHABLE", "DIGITAL", "HYGIENE", "FINAL_SALE"), "v1.0");
    }

    public boolean isWithinReturnWindow(OffsetDateTime deliveryTimestamp, OffsetDateTime currentTimestamp) {
        if (deliveryTimestamp == null) return false;
        OffsetDateTime deadline = deliveryTimestamp.plusDays(returnWindowDays);
        return !currentTimestamp.isAfter(deadline);
    }

    public OffsetDateTime calculateDeadline(OffsetDateTime deliveryTimestamp) {
        if (deliveryTimestamp == null) return null;
        return deliveryTimestamp.plusDays(returnWindowDays);
    }
}
