package com.sporekart.modules.shipment;

import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import com.sporekart.modules.shipment.application.dto.ShipmentDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShipmentApplicationService shipmentService;

    @Autowired
    private OrderRepository orderRepository;

    private Order createTestOrder(String customerId) {
        AddressSnapshot addr = new AddressSnapshot("Customer Test", "9876543210", "1 1st Cross", null, "Bengaluru", "Karnataka", "560001", "India");
        Order order = Order.createNewOrder(
                "ORD-CTRL-" + UUID.randomUUID().toString().substring(0, 6),
                customerId,
                "INR",
                BigDecimal.valueOf(1200),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(1200),
                "IDEM-CTRL-" + UUID.randomUUID(),
                addr,
                "Notes",
                List.of()
        );
        order.markPaid();
        order.startProcessing();
        order.markReadyForFulfilment();
        return orderRepository.save(order);
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderRef}/shipment should return customer shipment details")
    void testGetShipmentDetails() throws Exception {
        String custId = "cust-ctrl-100";
        Order order = createTestOrder(custId);
        ShipmentDto shipment = shipmentService.createShipmentForOrder(order.getId());

        mockMvc.perform(get("/api/v1/orders/{orderRef}/shipment", order.getOrderNumber())
                        .header("X-Customer-Id", custId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderReference").value(order.getOrderNumber()))
                .andExpect(jsonPath("$.shipmentReference").value(shipment.shipmentReference()))
                .andExpect(jsonPath("$.awb").exists());
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderRef}/tracking should return tracking timeline")
    void testGetTrackingTimeline() throws Exception {
        String custId = "cust-ctrl-200";
        Order order = createTestOrder(custId);
        ShipmentDto shipment = shipmentService.createShipmentForOrder(order.getId());

        mockMvc.perform(get("/api/v1/orders/{orderRef}/tracking", order.getOrderNumber())
                        .header("X-Customer-Id", custId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderReference").value(order.getOrderNumber()))
                .andExpect(jsonPath("$.awb").value(shipment.awb()))
                .andExpect(jsonPath("$.timeline").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderRef}/shipment with wrong customer ID should reject with IDOR error")
    void testCustomerMismatchIdor() throws Exception {
        Order order = createTestOrder("cust-owner");
        shipmentService.createShipmentForOrder(order.getId());

        mockMvc.perform(get("/api/v1/orders/{orderRef}/shipment", order.getOrderNumber())
                        .header("X-Customer-Id", "cust-attacker"))
                .andExpect(status().isBadRequest());
    }
}
