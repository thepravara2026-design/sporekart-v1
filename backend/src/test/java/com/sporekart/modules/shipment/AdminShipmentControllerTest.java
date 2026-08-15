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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShipmentApplicationService shipmentService;

    @Autowired
    private OrderRepository orderRepository;

    private Order createTestOrder() {
        AddressSnapshot addr = new AddressSnapshot("Admin Test", "9876543210", "2 Admin Rd", null, "Delhi", "Delhi", "110001", "India");
        Order order = Order.createNewOrder(
                "ORD-ADM-" + UUID.randomUUID().toString().substring(0, 6),
                "cust-adm-1",
                "INR",
                BigDecimal.valueOf(2500),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(2500),
                "IDEM-ADM-" + UUID.randomUUID(),
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
    @DisplayName("GET /api/v1/admin/shipments should return paginated list")
    void testGetAdminShipments() throws Exception {
        Order order = createTestOrder();
        shipmentService.createShipmentForOrder(order.getId());

        mockMvc.perform(get("/api/v1/admin/shipments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/admin/shipments/{ref}/cancel should cancel shipment")
    void testCancelShipment() throws Exception {
        Order order = createTestOrder();
        ShipmentDto shipment = shipmentService.createShipmentForOrder(order.getId());

        mockMvc.perform(post("/api/v1/admin/shipments/{ref}/cancel", shipment.shipmentReference())
                        .header("X-Admin-Id", "admin-root"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
