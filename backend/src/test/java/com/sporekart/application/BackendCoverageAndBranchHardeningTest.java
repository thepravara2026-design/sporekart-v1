package com.sporekart.application;

import com.sporekart.modules.security.domain.UserRole;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import com.sporekart.modules.training.application.reporting.TrainingReportingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SQ-08 Backend Coverage and Branch Hardening Suite")
class BackendCoverageAndBranchHardeningTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TrainingReportingService trainingReportingService;

    @Test
    @DisplayName("Admin Support Controller - List support tickets with filter options")
    void testAdminSupportListTickets() throws Exception {
        String adminToken = jwtTokenProvider.generateAccessToken("admin-cov-1", "admin@sporekart.com", UserRole.ROLE_ADMIN, "sess-admin-cov");

        mockMvc.perform(get("/api/v1/admin/support/tickets")
                        .param("status", "OPEN")
                        .param("priority", "HIGH")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Customer Support Controller - List customer support tickets")
    void testCustomerSupportListTickets() throws Exception {
        String customerToken = jwtTokenProvider.generateAccessToken("cust-cov-1", "cust@sporekart.com", UserRole.ROLE_CUSTOMER, "sess-cust-cov");

        mockMvc.perform(get("/api/v1/customer/support/tickets")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Customer Review Controller - Get product rating summary and approved reviews")
    void testCustomerReviewEndpoints() throws Exception {
        String customerToken = jwtTokenProvider.generateAccessToken("cust-cov-rev", "custrev@sporekart.com", UserRole.ROLE_CUSTOMER, "sess-cust-rev");

        mockMvc.perform(get("/api/v1/products/prod-cov-101/rating-summary")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/products/prod-cov-101/reviews")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin Review Controller - List product reviews with filter")
    void testAdminReviewList() throws Exception {
        String adminToken = jwtTokenProvider.generateAccessToken("admin-cov-2", "admin@sporekart.com", UserRole.ROLE_ADMIN, "sess-admin-cov-2");

        mockMvc.perform(get("/api/v1/admin/reviews")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin Inventory Controller - Stock check and inventory listing")
    void testAdminInventoryList() throws Exception {
        String adminToken = jwtTokenProvider.generateAccessToken("admin-cov-3", "admin@sporekart.com", UserRole.ROLE_ADMIN, "sess-admin-cov-3");

        mockMvc.perform(get("/api/v1/admin/inventory")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public Catalog Controller - Search and Category filter branches")
    void testPublicCatalogFilters() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("search", "Mushroom")
                        .param("category", "Equipment"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Training Reporting Service - Executive overview metrics computation")
    void testExecutiveTrainingOverviewComputation() {
        Object overview = trainingReportingService.getExecutiveOverview();
        assertNotNull(overview);
    }
}
