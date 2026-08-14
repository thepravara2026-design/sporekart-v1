package com.sporekart.modules.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.application.exception.GlobalExceptionHandler;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.AddressDto;
import com.sporekart.modules.order.application.dto.CreateOrderCommand;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.application.dto.OrderSummaryDto;
import com.sporekart.modules.order.controller.OrderController;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.exception.OrderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

class OrderControllerTest {

    private MockMvc mockMvc;
    private OrderApplicationService orderApplicationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        orderApplicationService = mock(OrderApplicationService.class);
        OrderController controller = new OrderController(orderApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/v1/orders authenticated should return 201 Created with order response")
    void testCreateOrderEndpoint() throws Exception {
        UUID orderId = UUID.randomUUID();
        AddressDto address = new AddressDto("John Doe", "9876543210", "123 Main St", null, "Bengaluru", "Karnataka", "560001", "India");
        OrderDto orderDto = new OrderDto(
                orderId, "SPK-20260815-100001", "cust-1", OrderStatus.CREATED, "INR",
                new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("180.00"), new BigDecimal("50.00"),
                new BigDecimal("1230.00"), "IDEM-1", address, "Deliver fast", List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(orderApplicationService.createOrder(eq("cust-1"), any(CreateOrderCommand.class))).thenReturn(orderDto);

        CreateOrderCommand command = new CreateOrderCommand(address, "IDEM-1", "Deliver fast");

        mockMvc.perform(post("/api/v1/orders")
                        .principal(new UsernamePasswordAuthenticationToken("cust-1", "n/a"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("SPK-20260815-100001"))
                .andExpect(jsonPath("$.data.grandTotal").value(1230.00))
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }

    @Test
    @DisplayName("GET /api/v1/orders authenticated should return 200 OK with paginated order history")
    void testGetOrderHistoryEndpoint() throws Exception {
        OrderSummaryDto summary = new OrderSummaryDto(
                UUID.randomUUID(), "SPK-20260815-100001", OrderStatus.CREATED, "INR",
                new BigDecimal("1230.00"), 2, OffsetDateTime.now()
        );

        PageImpl<OrderSummaryDto> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1);
        when(orderApplicationService.getOrderHistory(eq("cust-1"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/orders")
                        .principal(new UsernamePasswordAuthenticationToken("cust-1", "n/a")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].orderNumber").value("SPK-20260815-100001"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} authenticated should return 200 OK")
    void testGetOrderDetailEndpoint() throws Exception {
        UUID orderId = UUID.randomUUID();
        AddressDto address = new AddressDto("John", "999", "Line", null, "City", "State", "100", "India");
        OrderDto orderDto = new OrderDto(
                orderId, "SPK-100", "cust-1", OrderStatus.CREATED, "INR",
                new BigDecimal("500.00"), BigDecimal.ZERO, new BigDecimal("90.00"), new BigDecimal("50.00"),
                new BigDecimal("640.00"), null, address, null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(orderApplicationService.getOrderDetail("cust-1", orderId)).thenReturn(orderDto);

        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .principal(new UsernamePasswordAuthenticationToken("cust-1", "n/a")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(orderId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} for non-existent order should return 404 Not Found")
    void testGetOrderDetailNotFound() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderApplicationService.getOrderDetail("cust-1", orderId)).thenThrow(new OrderNotFoundException(orderId));

        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .principal(new UsernamePasswordAuthenticationToken("cust-1", "n/a")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ORDER_NOT_FOUND"));
    }
}
