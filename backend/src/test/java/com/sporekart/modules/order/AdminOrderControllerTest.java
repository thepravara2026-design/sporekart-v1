package com.sporekart.modules.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.application.exception.GlobalExceptionHandler;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.AddressDto;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.application.dto.OrderSummaryDto;
import com.sporekart.modules.order.application.dto.OrderTimelineDto;
import com.sporekart.modules.order.controller.AdminOrderController;
import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.OrderStatusHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminOrderControllerTest {

    private MockMvc mockMvc;
    private OrderApplicationService orderApplicationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        orderApplicationService = mock(OrderApplicationService.class);
        AdminOrderController controller = new AdminOrderController(orderApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders should return paginated orders")
    void testListAdminOrders() throws Exception {
        OrderSummaryDto summary = new OrderSummaryDto(
                UUID.randomUUID(), "SPK-10001", OrderStatus.CONFIRMED, "INR",
                new BigDecimal("1500.00"), 2, OffsetDateTime.now()
        );

        when(orderApplicationService.getAdminOrderList(any(), eq(null)))
                .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 15), 1));

        mockMvc.perform(get("/api/v1/admin/orders")
                        .principal(new UsernamePasswordAuthenticationToken("admin1", "n/a", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].orderNumber").value("SPK-10001"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/orders/{id}/shipped should transition status to SHIPPED")
    void testMarkShipped() throws Exception {
        UUID orderId = UUID.randomUUID();
        AddressDto address = new AddressDto("John", "999", "Line", null, "City", "State", "100", "India");
        OrderDto orderDto = new OrderDto(
                orderId, "SPK-10002", "cust-1", OrderStatus.SHIPPED, "INR",
                new BigDecimal("1500.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("1500.00"), null, address, null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(orderApplicationService.markShipped(eq(orderId), eq("admin1"), eq("TRACK-999"))).thenReturn(orderDto);

        mockMvc.perform(post("/api/v1/admin/orders/" + orderId + "/shipped")
                        .principal(new UsernamePasswordAuthenticationToken("admin1", "n/a", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackingNumber\":\"TRACK-999\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));
    }
}
