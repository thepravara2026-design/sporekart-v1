package com.sporekart.modules.support;

import com.sporekart.modules.support.application.SupportApplicationService;
import com.sporekart.modules.support.application.dto.*;
import com.sporekart.modules.support.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SupportTicketLifecycleIntegrationTest {

    @Autowired
    private SupportApplicationService supportApplicationService;

    @Test
    @DisplayName("Should execute complete support ticket lifecycle: Create -> Assign -> Agent Reply -> Resolve -> Close")
    void shouldExecuteFullTicketLifecycle() {
        // 1. Create ticket by customer
        CreateTicketRequestDto createDto = new CreateTicketRequestDto(
                "order-id-1",
                "ORD-9988",
                TicketCategory.DELIVERY,
                IssueType.ORDER_DELAYED,
                TicketPriority.HIGH,
                TicketSource.CUSTOMER_WEB,
                "Package Not Received",
                "The tracking status says delivered but I have not received it",
                null
        );

        SupportTicketDto created = supportApplicationService.createCustomerTicket(createDto, "cust-101");
        assertThat(created).isNotNull();
        assertThat(created.ticketNumber()).startsWith("TKT-");
        assertThat(created.status()).isEqualTo(TicketStatus.OPEN);

        // 2. Assign to support agent
        SupportTicketDto assigned = supportApplicationService.assignTicket(created.ticketNumber(), "agent-smith", "admin-1");
        assertThat(assigned.status()).isEqualTo(TicketStatus.ASSIGNED);
        assertThat(assigned.assignedAgentId()).isEqualTo("agent-smith");

        // 3. Agent reply (Public message)
        AddMessageRequestDto replyDto = new AddMessageRequestDto("We are checking with courier", MessageVisibility.CUSTOMER_VISIBLE, null);
        SupportTicketDto replied = supportApplicationService.addAgentMessage(created.ticketNumber(), replyDto, "agent-smith");
        assertThat(replied.status()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(replied.messages()).hasSize(2);

        // 4. Agent internal note
        AddMessageRequestDto noteDto = new AddMessageRequestDto("Shiprocket support ticket #9876 created internally", MessageVisibility.INTERNAL_NOTE, null);
        supportApplicationService.addAgentMessage(created.ticketNumber(), noteDto, "agent-smith");

        // Verify customer view DOES NOT leak internal note
        SupportTicketDto customerView = supportApplicationService.getTicketForCustomer(created.ticketNumber(), "cust-101");
        assertThat(customerView.messages()).hasSize(2); // Initial description + Public agent reply

        // Verify admin view DOES see internal note
        SupportTicketDto adminView = supportApplicationService.getTicketForAdmin(created.ticketNumber());
        assertThat(adminView.messages()).hasSize(3);

        // 5. Resolve ticket
        SupportTicketDto resolved = supportApplicationService.resolveTicket(created.ticketNumber(), "Courier located package and redelivered", "agent-smith");
        assertThat(resolved.status()).isEqualTo(TicketStatus.RESOLVED);

        // 6. Close ticket
        SupportTicketDto closed = supportApplicationService.closeTicket(created.ticketNumber(), "admin-1");
        assertThat(closed.status()).isEqualTo(TicketStatus.CLOSED);
    }
}
