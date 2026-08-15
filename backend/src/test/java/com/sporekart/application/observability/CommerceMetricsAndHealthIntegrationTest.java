package com.sporekart.application.observability;

import com.sporekart.application.observability.health.ExternalProviderHealthIndicator;
import com.sporekart.application.observability.metrics.CommerceMetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommerceMetricsAndHealthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommerceMetricsService commerceMetricsService;

    @Autowired
    private ExternalProviderHealthIndicator externalProviderHealthIndicator;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @DisplayName("Should expose health, liveness, and readiness probes")
    void shouldExposeHealthProbes() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());

        mockMvc.perform(get("/actuator/health/liveness")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/health/readiness")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Should expose actuator metrics and prometheus telemetries")
    void shouldExposePrometheusMetricsEndpoint() throws Exception {
        commerceMetricsService.recordOrderCreated("test-web");
        commerceMetricsService.recordPaymentSuccess("RAZORPAY");

        // Verify Micrometer registered meters directly
        assertThat(meterRegistry.find("sporekart.orders.created").counter()).isNotNull();
        assertThat(meterRegistry.find("sporekart.payments.succeeded").counter()).isNotNull();

        // Verify Actuator metrics endpoint
        mockMvc.perform(get("/actuator/metrics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").exists());
    }

    @Test
    @DisplayName("Should report DEGRADED status when external provider is unavailable without crashing liveness")
    void shouldReportDegradedStatusOnExternalProviderIssue() throws Exception {
        externalProviderHealthIndicator.setRazorpayHealthy(false);

        mockMvc.perform(get("/actuator/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.components.externalProvider.status").value("DEGRADED"));

        // Liveness remains UP process is alive
        mockMvc.perform(get("/actuator/health/liveness")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        // Reset
        externalProviderHealthIndicator.setRazorpayHealthy(true);
    }
}
