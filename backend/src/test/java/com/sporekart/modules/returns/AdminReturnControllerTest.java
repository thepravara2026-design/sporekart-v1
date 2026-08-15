package com.sporekart.modules.returns;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.returns.application.ReturnApplicationService;
import com.sporekart.modules.returns.application.dto.ReturnDto;
import com.sporekart.modules.returns.application.dto.ReturnInspectionDto;
import com.sporekart.modules.returns.controller.AdminReturnController;
import com.sporekart.modules.returns.domain.InspectionOutcome;
import com.sporekart.modules.returns.domain.ReturnReasonCode;
import com.sporekart.modules.returns.domain.ReturnStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminReturnController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminReturnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReturnApplicationService returnApplicationService;

    @Test
    @DisplayName("GET /api/v1/admin/returns returns 200 OK")
    void testListReturnsAdmin() throws Exception {
        ReturnDto dto = new ReturnDto(
                UUID.randomUUID(), "RET-101-100001", UUID.randomUUID(), "ORD-101", "cust-101", ReturnStatus.REQUESTED,
                ReturnReasonCode.DAMAGED, "Damaged item", null, "v1.0", OffsetDateTime.now(), null, null, null, null,
                0L, new BigDecimal("100.00"), List.of(), List.of(), null, null
        );
        given(returnApplicationService.listReturnsForAdmin(any())).willReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/admin/returns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].returnReference").value("RET-101-100001"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/returns/{returnRef}/approve returns 200 OK")
    void testApproveReturnAdmin() throws Exception {
        ReturnDto dto = new ReturnDto(
                UUID.randomUUID(), "RET-101-100001", UUID.randomUUID(), "ORD-101", "cust-101", ReturnStatus.APPROVED,
                ReturnReasonCode.DAMAGED, "Damaged item", null, "v1.0", OffsetDateTime.now(), OffsetDateTime.now(), null, null, null,
                1L, new BigDecimal("100.00"), List.of(), List.of(), null, null
        );
        given(returnApplicationService.approveReturn(eq("RET-101-100001"), eq("admin-1"), any())).willReturn(dto);

        mockMvc.perform(post("/api/v1/admin/returns/RET-101-100001/approve")
                        .header("X-Admin-Id", "admin-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("notes", "Approved"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/returns/{returnRef}/inspect returns 200 OK")
    void testInspectReturnAdmin() throws Exception {
        ReturnDto dto = new ReturnDto(
                UUID.randomUUID(), "RET-101-100001", UUID.randomUUID(), "ORD-101", "cust-101", ReturnStatus.REFUNDED,
                ReturnReasonCode.DAMAGED, "Damaged item", null, "v1.0", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(),
                5L, new BigDecimal("100.00"), List.of(), List.of(), null, null
        );

        ReturnInspectionDto inspectionDto = new ReturnInspectionDto(null, "admin-1", InspectionOutcome.ACCEPTED, "Good condition", null, List.of());
        given(returnApplicationService.processInspection(eq("RET-101-100001"), any(), eq("admin-1"))).willReturn(dto);

        mockMvc.perform(post("/api/v1/admin/returns/RET-101-100001/inspect")
                        .header("X-Admin-Id", "admin-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inspectionDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }
}
