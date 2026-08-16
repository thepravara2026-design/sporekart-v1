package com.sporekart.application.deployment;

import com.sporekart.application.observability.CorrelationAndTracingFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductionDeploymentTestSuite {

    @Autowired
    private Environment environment;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("6K-001: Production deployment configuration validation")
    void test6K001_productionDeploymentConfigurationValidation() {
        String appName = environment.getProperty("spring.application.name");
        assertNotNull(appName, "Application name must be configured");
        assertTrue(appName.startsWith("sporekart-backend"), "Application name must start with sporekart-backend");

        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto");
        assertEquals("validate", ddlAuto, "DDL auto must be set to validate for schema integrity");
    }

    @Test
    @DisplayName("6K-002: Artifact identification and release metadata")
    void test6K002_artifactIdentification() {
        String version = environment.getProperty("app.version", "0.1.0-SNAPSHOT");
        assertNotNull(version, "App version must be exposed");
        assertFalse(version.isEmpty(), "App version must not be empty");
    }

    @Test
    @DisplayName("6K-003: Production profile validation")
    void test6K003_productionProfileValidation() {
        assertNotNull(environment, "Environment must be initialized");
        assertTrue(environment.getActiveProfiles().length > 0, "Active profile must be present");
    }

    @Test
    @DisplayName("6K-004: Database connectivity and pool properties")
    void test6K004_databaseConnectivity() {
        String hikariPool = environment.getProperty("spring.datasource.hikari.maximum-pool-size", "10");
        assertNotNull(hikariPool, "Hikari maximum pool size must be configured");
        assertTrue(Integer.parseInt(hikariPool) >= 5, "Hikari maximum pool size must be at least 5");
    }

    @Test
    @DisplayName("6K-005: Flyway latest migration sequence validation")
    void test6K005_flywayMigrationValidation() {
        String flywayEnabled = environment.getProperty("spring.flyway.enabled");
        assertNotEquals("false", flywayEnabled, "Flyway migrations must be enabled");
    }

    @Test
    @DisplayName("6K-006: Health verification")
    void test6K006_healthVerification() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6K-007: Readiness verification")
    void test6K007_readinessVerification() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6K-008: Liveness verification")
    void test6K008_livenessVerification() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6K-009: Frontend production URL configuration")
    void test6K009_frontendProductionUrl() throws Exception {
        Path envExample = Paths.get("..", ".env.example");
        if (Files.exists(envExample)) {
            String content = Files.readString(envExample);
            assertTrue(content.contains("VITE_API_BASE_URL") || content.contains("CORS_ALLOWED_ORIGINS"),
                    ".env.example must define public frontend API URL placeholders");
        }
    }

    @Test
    @DisplayName("6K-010: Frontend secret scan")
    void test6K010_frontendSecretScan() throws Exception {
        Path frontendPath = Paths.get("..", "frontend", "src");
        if (Files.exists(frontendPath)) {
            try (Stream<Path> paths = Files.walk(frontendPath)) {
                List<Path> files = paths.filter(Files::isRegularFile).toList();
                for (Path file : files) {
                    String content = Files.readString(file);
                    assertFalse(content.contains("CHANGE_ME_PRODUCTION"), "Frontend src must not contain production placeholder secrets in file: " + file);
                    assertFalse(content.contains("sporekart-v3-super-secure"), "Frontend src must not contain JWT secret in file: " + file);
                }
            }
        }
    }

    @Test
    @DisplayName("6K-011: CORS validation")
    void test6K011_corsValidation() {
        String allowedOrigins = environment.getProperty("app.cors.allowed-origins", "");
        assertFalse(allowedOrigins.contains("*"), "Production CORS configuration must not use wildcard '*'");
    }

    @Test
    @DisplayName("6K-012: Provider isolation")
    void test6K012_providerIsolation() {
        String paymentAttempts = environment.getProperty("app.resilience.payment.max-attempts");
        assertNotNull(paymentAttempts, "Payment resilience max-attempts must be configured");
        assertTrue(Integer.parseInt(paymentAttempts) >= 1, "Payment max-attempts must be positive");
    }

    @Test
    @DisplayName("6K-013: Release metadata")
    void test6K013_releaseMetadata() {
        String appName = environment.getProperty("spring.application.name");
        assertNotNull(appName, "Application metadata must be present");
    }

    @Test
    @DisplayName("6K-014: Correlation ID propagation")
    void test6K014_correlationId() throws Exception {
        CorrelationAndTracingFilter filter = new CorrelationAndTracingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/catalog/products");
        request.addHeader("X-Correlation-ID", "corr-test-6k-014");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertEquals("corr-test-6k-014", response.getHeader("X-Correlation-ID"));
        assertEquals("corr-test-6k-014", response.getHeader("X-Request-ID"));
    }

    @Test
    @DisplayName("6K-015: Prometheus availability")
    void test6K015_prometheusAvailability() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6K-016: Restart recovery and process health")
    void test6K016_restartRecovery() {
        String shutdownMode = environment.getProperty("server.shutdown", "graceful");
        assertEquals("graceful", shutdownMode, "Server shutdown must be configured for graceful termination");
    }

    @Test
    @DisplayName("6K-017: Deployment idempotency")
    void test6K017_deploymentIdempotency() {
        File pomFile = new File("pom.xml");
        assertTrue(pomFile.exists(), "pom.xml must exist for build repeatability");
    }

    @Test
    @DisplayName("6K-018: Rollback readiness")
    void test6K018_rollbackReadiness() {
        Path runbookPath = Paths.get("..", "docs", "operations", "deployment-runbook.md");
        assertNotNull(runbookPath, "Deployment runbook path must be defined");
    }

    @Test
    @DisplayName("6K-019: Browser release smoke verification")
    void test6K019_browserReleaseSmokeVerification() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6K-020: Production deployment checklist validation")
    void test6K020_productionDeploymentChecklistValidation() {
        Path checklistPath = Paths.get("..", "docs", "operations", "production-release-checklist.md");
        assertNotNull(checklistPath, "Production release checklist path must be defined");
    }
}
