package com.sporekart.modules.support.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlaCalculationServiceTest {

    private final SlaCalculationService slaCalculationService = new SlaCalculationService();

    @Test
    @DisplayName("Should evaluate fresh ticket SLA as MET")
    void shouldEvaluateFreshTicketAsMet() {
        SupportTicket ticket = SupportTicket.createNew(
                "TKT-2026-000001",
                "cust-101",
                "order-1",
                "ORD-1001",
                TicketCategory.DELIVERY,
                IssueType.ORDER_DELAYED,
                TicketPriority.NORMAL,
                TicketSource.CUSTOMER_WEB,
                "Where is my order?",
                "My order is delayed"
        );

        SlaStatus result = slaCalculationService.evaluateSlaStatus(ticket);
        assertThat(result).isEqualTo(SlaStatus.MET);
    }
}
