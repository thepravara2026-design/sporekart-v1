package com.sporekart.modules.returns;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.returns.application.ReturnApplicationService;
import com.sporekart.modules.returns.application.dto.CreateReturnRequestDto;
import com.sporekart.modules.returns.application.dto.ReturnDto;
import com.sporekart.modules.returns.application.dto.ReturnEligibilityDto;
import com.sporekart.modules.returns.controller.ReturnController;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReturnController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReturnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReturnApplicationService returnApplicationService;

    @Test
    @DisplayName("GET /api/v1/orders/{orderRef}/return-eligibility returns 200 OK with eligibility payload")
    void testCheckEligibility() throws Exception {
        ReturnEligibilityDto dto = new ReturnEligibilityDto(
                UUID.randomUUID(), "ORD-101", true, "ELIGIBLE", OffsetDateTime.now().minusDays(2), OffsetDateTime.now().plusDays(12), List.of()
        );
        given(returnApplicationService.checkEligibility(eq("ORD-101"), eq("cust-101"))).willReturn(dto);

        mockMvc.perform(get("/api/v1/orders/ORD-101/return-eligibility")
                        .header("X-Customer-Id", "cust-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderReference").value("ORD-101"))
                .andExpect(jsonPath("$.eligible").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/orders/{orderRef}/returns returns 201 Created")
    void testCreateReturn() throws Exception {
        UUID returnId = UUID.randomUUID();
        ReturnDto dto = new ReturnDto(
                returnId, "RET-101-100001", UUID.randomUUID(), "ORD-101", "cust-101", ReturnStatus.REQUESTED,
                ReturnReasonCode.DAMAGED, "Damaged item", null, "v1.0", OffsetDateTime.now(), null, null, null, null,
                0L, new BigDecimal("100.00"), List.of(), List.of(), null, null
        );

        CreateReturnRequestDto req = new CreateReturnRequestDto(
                ReturnReasonCode.DAMAGED, "Damaged item", null, List.of(new CreateReturnRequestDto.CreateReturnItemInput(UUID.randomUUID(), 1, ReturnReasonCode.DAMAGED))
        );

        given(returnApplicationService.createReturn(eq("ORD-101"), any(), eq("cust-101"))).willReturn(dto);

        mockMvc.perform(post("/api/v1/orders/ORD-101/returns")
                        .header("X-Customer-Id", "cust-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.returnReference").value("RET-101-100001"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }
}
