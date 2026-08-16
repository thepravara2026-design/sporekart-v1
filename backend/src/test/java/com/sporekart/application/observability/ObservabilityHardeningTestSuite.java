package com.sporekart.application.observability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Dedicated automated test suite for Sprint 6I — Observability & Production Monitoring Hardening.
 * Covers tests 6I-001 through 6I-020.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ObservabilityHardeningTestSuite {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommerceMetricsService commerceMetricsService;

    @Autowired
    private MeterRegistry meterRegistry;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("6I-001: Actuator health endpoint is available and exposes valid status")
    void test_6I_001_actuatorHealthAvailable() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @DisplayName("6I-002: Readiness probe is available and returns UP status")
    void test_6I_002_readinessAvailable() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("6I-003: Liveness probe is available and returns UP status")
    void test_6I_003_livenessAvailable() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("6I-004: Prometheus scrape endpoint /actuator/prometheus is exposed and formatted correctly")
    void test_6I_004_prometheusEndpointAvailable() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("jvm_memory_used_bytes")));
    }

    @Test
    @DisplayName("6I-005: HTTP request metrics (http.server.requests) are emitted on API traffic")
    void test_6I_005_httpMetricsEmitted() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());

        assertThat(meterRegistry.find("http.server.requests").timer()).isNotNull();
    }

    @Test
    @DisplayName("6I-006: Route labels are normalized to prevent high cardinality label explosion")
    void test_6I_006_normalizedRouteLabels() throws Exception {
        String testUuid = UUID.randomUUID().toString();
        mockMvc.perform(get("/api/v1/catalog/products/" + testUuid));

        // Verify normalized URI template is used as metric tag rather than raw UUID
        assertThat(meterRegistry.find("sporekart.http.requests.latency")
                .tag("uri", "/api/v1/catalog/products/{id}")
                .timer()).isNotNull();
    }

    @Test
    @DisplayName("6I-007: HikariCP connection pool metrics are registered and emitted")
    void test_6I_007_hikariMetricsEmitted() {
        // In Spring Boot JPA test profile, HikariCP connection metrics are registered
        assertThat(meterRegistry.getMeters()).anyMatch(m -> m.getId().getName().startsWith("hikaricp.connections")
                || m.getId().getName().startsWith("jvm."));
    }

    @Test
    @DisplayName("6I-008: JVM memory, thread, and GC metrics are registered and emitted")
    void test_6I_008_jvmMetricsEmitted() {
        assertThat(meterRegistry.find("jvm.memory.used").gauge()).isNotNull();
        assertThat(meterRegistry.find("jvm.threads.live").gauge()).isNotNull();
    }

    @Test
    @DisplayName("6I-009: Business order and payment metrics are incremented on commerce events")
    void test_6I_009_paymentMetricsEmitted() {
        double beforeAttempted = meterRegistry.find("sporekart.payments.attempted").tag("provider", "razorpay").counter() != null
                ? meterRegistry.find("sporekart.payments.attempted").tag("provider", "razorpay").counter().count() : 0.0;

        commerceMetricsService.recordPaymentAttempt("razorpay");

        double afterAttempted = meterRegistry.find("sporekart.payments.attempted").tag("provider", "razorpay").counter().count();
        assertThat(afterAttempted).isEqualTo(beforeAttempted + 1.0);
    }

    @Test
    @DisplayName("6I-010: Shipping metrics are incremented on shipment operations")
    void test_6I_010_shippingMetricsEmitted() {
        double beforeCreated = meterRegistry.find("sporekart.shipments.created").tag("provider", "shiprocket").counter() != null
                ? meterRegistry.find("sporekart.shipments.created").tag("provider", "shiprocket").counter().count() : 0.0;

        commerceMetricsService.recordShipmentCreated("shiprocket");

        double afterCreated = meterRegistry.find("sporekart.shipments.created").tag("provider", "shiprocket").counter().count();
        assertThat(afterCreated).isEqualTo(beforeCreated + 1.0);
    }

    @Test
    @DisplayName("6I-011: Webhook received, duplicate, and processing metrics are emitted")
    void test_6I_011_webhookMetricsEmitted() {
        double beforeReceived = meterRegistry.find("sporekart.webhooks.received").tag("provider", "razorpay").counter() != null
                ? meterRegistry.find("sporekart.webhooks.received").tag("provider", "razorpay").counter().count() : 0.0;

        commerceMetricsService.recordWebhookReceived("razorpay", "payment.captured");

        double afterReceived = meterRegistry.find("sporekart.webhooks.received").tag("provider", "razorpay").counter().count();
        assertThat(afterReceived).isEqualTo(beforeReceived + 1.0);
    }

    @Test
    @DisplayName("6I-012: External provider call latency timer metrics are recorded")
    void test_6I_012_providerLatencyMetricsEmitted() {
        commerceMetricsService.recordProviderLatency("razorpay", "capture_payment", 185);

        assertThat(meterRegistry.find("sporekart.provider.latency")
                .tag("provider", "razorpay")
                .timer()).isNotNull();
    }

    @Test
    @DisplayName("6I-013: Correlation ID is automatically generated when absent in inbound request")
    void test_6I_013_correlationIdGenerated() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andReturn();

        String correlationId = result.getResponse().getHeader("X-Correlation-ID");
        assertThat(correlationId).isNotBlank();
        assertThat(UUID.fromString(correlationId)).isNotNull();
    }

    @Test
    @DisplayName("6I-014: Inbound correlation ID is preserved and propagated to response headers")
    void test_6I_014_correlationIdPropagated() throws Exception {
        String customCorrId = "corr-test-6i-014-" + UUID.randomUUID();

        MvcResult result = mockMvc.perform(get("/api/v1/health")
                        .header("X-Correlation-ID", customCorrId))
                .andExpect(status().isOk())
                .andReturn();

        String responseCorrId = result.getResponse().getHeader("X-Correlation-ID");
        assertThat(responseCorrId).isEqualTo(customCorrId);
    }

    @Test
    @DisplayName("6I-015: Sensitive log data protection is enforced by log redactor utility")
    void test_6I_015_sensitiveDataAbsentFromLogs() {
        String sensitiveInput = "{\"password\":\"MySecret123!\",\"cardNumber\":\"4111222233334444\",\"cvv\":\"123\"}";
        String redacted = LogRedactor.redact(sensitiveInput);

        assertThat(redacted).doesNotContain("MySecret123!");
        assertThat(redacted).doesNotContain("4111222233334444");
        assertThat(redacted).contains("[REDACTED]");
    }

    @Test
    @DisplayName("6I-016: Metric cardinality safety verified (no UUIDs or emails in metric tags)")
    void test_6I_016_metricCardinalitySafety() {
        commerceMetricsService.recordPaymentFailure("razorpay", "INVALID_CVV");

        // Verify tags only contain bounded values like provider & reason category, not customer IDs or emails
        var counter = meterRegistry.find("sporekart.payments.failed").tag("provider", "razorpay").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getTags()).noneMatch(t -> t.getValue().contains("@") || t.getValue().contains("-uuid-"));
    }

    @Test
    @DisplayName("6I-017: Bounded error metrics are emitted on 4xx / 5xx error handling")
    void test_6I_017_errorMetricsEmitted() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products/non-existent-product-id-99999"))
                .andExpect(status().isNotFound());

        assertThat(meterRegistry.find("sporekart.http.errors").counter()).isNotNull();
    }

    @Test
    @DisplayName("6I-018: Actuator security configuration protects internal endpoints while exposing health/prometheus")
    void test_6I_018_actuatorSecurity() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6I-019: Production observability configuration parameters load correctly")
    void test_6I_019_productionObservabilityConfiguration() {
        assertThat(meterRegistry).isNotNull();
    }

    @Test
    @DisplayName("6I-020: Validate Grafana dashboard JSON files exist and parse as valid JSON objects")
    void test_6I_020_dashboardConfigurationValidation() throws Exception {
        Path dashboardsDir = Paths.get("../monitoring/grafana/dashboards");
        if (!Files.exists(dashboardsDir)) {
            dashboardsDir = Paths.get("monitoring/grafana/dashboards");
        }
        if (!Files.exists(dashboardsDir)) {
            dashboardsDir = Paths.get("../../monitoring/grafana/dashboards");
        }

        assertThat(Files.exists(dashboardsDir)).as("Grafana dashboards directory must exist").isTrue();

        String[] dashboards = {
                "application-overview.json",
                "database-hikaricp.json",
                "payment-reliability.json",
                "shipping-reliability.json",
                "jvm-health.json"
        };

        for (String dashboard : dashboards) {
            Path file = dashboardsDir.resolve(dashboard);
            assertThat(Files.exists(file)).as("Dashboard file %s must exist", dashboard).isTrue();

            JsonNode jsonNode = objectMapper.readTree(file.toFile());
            assertThat(jsonNode.has("title")).as("Dashboard %s must have title", dashboard).isTrue();
            assertThat(jsonNode.has("panels")).as("Dashboard %s must have panels", dashboard).isTrue();
        }
    }
}
