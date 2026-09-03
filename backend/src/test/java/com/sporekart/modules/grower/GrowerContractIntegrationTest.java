package com.sporekart.modules.grower;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GrowerContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("CONTRACT TEST #1: Unauthenticated request to /api/v1/grower/me -> 401 Unauthorized")
    void testUnauthenticatedAccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/grower/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "grower-1", roles = {"GROWER"})
    @DisplayName("CONTRACT TEST #2: GET /api/v1/grower/me with ROLE_GROWER -> 200 OK with profile payload")
    void testGetProfileSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/grower/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(username = "grower-1", roles = {"GROWER"})
    @DisplayName("CONTRACT TEST #3: GET /api/v1/grower/dashboard -> 200 OK")
    void testGetDashboardSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/grower/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.grossRevenue").exists());
    }

    @Test
    @WithMockUser(username = "grower-1", roles = {"GROWER"})
    @DisplayName("CONTRACT TEST #4: GET /api/v1/grower/products -> 200 OK")
    void testGetProductsSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/grower/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "grower-1", roles = {"GROWER"})
    @DisplayName("CONTRACT TEST #5: GET /api/v1/grower/inventory -> 200 OK")
    void testGetInventorySuccess() throws Exception {
        mockMvc.perform(get("/api/v1/grower/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "grower-1", roles = {"GROWER"})
    @DisplayName("CONTRACT TEST #6: GET /api/v1/grower/orders -> 200 OK")
    void testGetOrdersSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/grower/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "grower-1", roles = {"GROWER"})
    @DisplayName("CONTRACT TEST #7: GET /api/v1/grower/shipments -> 200 OK")
    void testGetShipmentsSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/grower/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "grower-1", roles = {"GROWER"})
    @DisplayName("CONTRACT TEST #8: GET /api/v1/grower/reports/summary -> 200 OK")
    void testGetReportSummarySuccess() throws Exception {
        mockMvc.perform(get("/api/v1/grower/reports/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "grower-1", roles = {"GROWER"})
    @DisplayName("PRIVILEGE ESCALATION #9: ROLE_GROWER attempting GET /api/v1/admin/users -> 403 Forbidden")
    void testRoleGrowerCannotAccessAdminUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "trainee-grower-1", roles = {"TRAINEE", "GROWER"})
    @DisplayName("DUAL ROLE #10: User with ROLE_TRAINEE + ROLE_GROWER can access grower profile -> 200 OK")
    void testDualRoleCanAccessGrowerProfile() throws Exception {
        mockMvc.perform(get("/api/v1/grower/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
