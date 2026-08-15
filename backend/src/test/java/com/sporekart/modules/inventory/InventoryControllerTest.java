package com.sporekart.modules.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.inventory.application.dto.ReservationDto;
import com.sporekart.modules.inventory.application.dto.StockAdjustmentCommand;
import com.sporekart.modules.inventory.controller.InventoryController;
import com.sporekart.modules.inventory.domain.ReservationStatus;
import com.sporekart.modules.inventory.domain.exception.InventoryItemNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryApplicationService inventoryApplicationService;

    @Test
    @DisplayName("GET /api/v1/inventory/{sku} should return stock details")
    @WithMockUser
    void testGetInventoryBySku() throws Exception {
        String sku = "SKU-MUSH-01";
        InventoryItemDto dto = new InventoryItemDto(
                UUID.randomUUID(), UUID.randomUUID(), null, sku, 20, 5, 15, "ACTIVE",
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(inventoryApplicationService.getInventoryBySku(sku)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/inventory/{sku}", sku))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value(sku))
                .andExpect(jsonPath("$.data.onHandQuantity").value(20))
                .andExpect(jsonPath("$.data.reservedQuantity").value(5))
                .andExpect(jsonPath("$.data.availableQuantity").value(15));
    }

    @Test
    @DisplayName("GET /api/v1/inventory/{sku} should return 404 when SKU does not exist")
    @WithMockUser
    void testGetInventoryBySkuNotFound() throws Exception {
        String sku = "SKU-UNKNOWN";
        when(inventoryApplicationService.getInventoryBySku(sku)).thenThrow(new InventoryItemNotFoundException(sku));

        mockMvc.perform(get("/api/v1/inventory/{sku}", sku))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("INVENTORY_ITEM_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/inventory/reserve/{orderId} should create stock reservation")
    @WithMockUser(username = "cust-123")
    void testReserveInventoryForOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        ReservationDto dto = new ReservationDto(
                UUID.randomUUID(), "RES-001", orderId, ReservationStatus.ACTIVE,
                OffsetDateTime.now().plusMinutes(15), null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(inventoryApplicationService.reserveInventoryForOrder(eq(orderId), eq("cust-123"))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/inventory/reserve/{orderId}", orderId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/inventory/adjust should allow admin stock adjustment")
    @WithMockUser(roles = "ADMIN")
    void testAdminAdjustStock() throws Exception {
        StockAdjustmentCommand command = new StockAdjustmentCommand("SKU-MUSH-01", 100, "NEW_RESTOCK");
        InventoryItemDto dto = new InventoryItemDto(
                UUID.randomUUID(), UUID.randomUUID(), null, "SKU-MUSH-01", 100, 0, 100, "ACTIVE",
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(inventoryApplicationService.adjustStock(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/inventory/adjust")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.onHandQuantity").value(100));
    }
}
