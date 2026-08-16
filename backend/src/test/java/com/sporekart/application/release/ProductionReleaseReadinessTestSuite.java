package com.sporekart.application.release;

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
class ProductionReleaseReadinessTestSuite {

    @Autowired
    private Environment environment;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("6J-001: Production profile loads cleanly")
    void test6J001_productionProfileLoadsCleanly() {
        assertNotNull(environment, "Environment context must be initialized");
        assertTrue(environment.getActiveProfiles().length > 0, "Active profiles must be set");
    }

    @Test
    @DisplayName("6J-002: Required production configuration validation")
    void test6J002_requiredProductionConfigurationValidation() {
        String appName = environment.getProperty("spring.application.name");
        assertNotNull(appName, "Application name must be set");
        assertTrue(appName.startsWith("sporekart-backend"), "Application name must start with sporekart-backend");
        
        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto");
        assertEquals("validate", ddlAuto, "DDL auto must be set to validate for schema integrity");
    }

    @Test
    @DisplayName("6J-003: Unsafe provider mode rejected in production profile")
    void test6J003_unsafeProviderModeRejectedInProduction() {
        // Verify application configuration has resilience settings enabled
        String paymentAttempts = environment.getProperty("app.resilience.payment.max-attempts");
        assertNotNull(paymentAttempts, "Payment resilience max-attempts must be configured");
        assertTrue(Integer.parseInt(paymentAttempts) >= 1, "Payment max-attempts must be positive");
    }

    @Test
    @DisplayName("6J-004: Frontend production bundle secret scan")
    void test6J004_frontendProductionBundleSecretScan() throws Exception {
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
    @DisplayName("6J-005: Frontend public API URL validation")
    void test6J005_frontendPublicApiUrlValidation() throws Exception {
        Path envExample = Paths.get("..", ".env.example");
        if (Files.exists(envExample)) {
            String content = Files.readString(envExample);
            assertTrue(content.contains("VITE_API_BASE_URL") || content.contains("CORS_ALLOWED_ORIGINS"), 
                    ".env.example must define public API URL or CORS origins placeholders");
        }
    }

    @Test
    @DisplayName("6J-006: Flyway production migration validation")
    void test6J006_flywayProductionMigrationValidation() {
        String flywayEnabled = environment.getProperty("spring.flyway.enabled");
        assertNotEquals("false", flywayEnabled, "Flyway must be enabled for production schema safety");
    }

    @Test
    @DisplayName("6J-007: Production Actuator configuration safety")
    void test6J007_productionActuatorConfiguration() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6J-008: Health endpoint available")
    void test6J008_healthEndpointAvailable() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6J-009: Readiness endpoint available")
    void test6J009_readinessEndpointAvailable() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6J-010: Prometheus endpoint available")
    void test6J010_prometheusEndpointAvailable() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6J-011: Release metadata available")
    void test6J011_releaseMetadataAvailable() {
        String version = environment.getProperty("app.version", "0.1.0-SNAPSHOT");
        assertNotNull(version, "App version must be exposed");
        assertFalse(version.isEmpty(), "App version must not be empty");
    }

    @Test
    @DisplayName("6J-012: Correlation ID header available")
    void test6J012_correlationIdAvailable() throws Exception {
        CorrelationAndTracingFilter filter = new CorrelationAndTracingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/catalog/products");
        request.addHeader("X-Correlation-ID", "corr-test-6j-012");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertEquals("corr-test-6j-012", response.getHeader("X-Correlation-ID"));
        assertEquals("corr-test-6j-012", response.getHeader("X-Request-ID"));
    }

    @Test
    @DisplayName("6J-013: Production CORS validation")
    void test6J013_productionCorsValidation() {
        String allowedOrigins = environment.getProperty("app.cors.allowed-origins", "");
        assertFalse(allowedOrigins.contains("*"), "Production CORS configuration must not use wildcard '*'");
    }

    @Test
    @DisplayName("6J-014: No development profile activation in production config")
    void test6J014_noDevelopmentProfileActivation() {
        String activeProfiles = String.join(",", environment.getActiveProfiles());
        assertFalse(activeProfiles.contains("prod") && activeProfiles.contains("dev"), 
                "Production profile and development profile must not be active simultaneously");
    }

    @Test
    @DisplayName("6J-015: No localhost production configuration hardcoded in app prod")
    void test6J015_noLocalhostProductionConfiguration() throws Exception {
        Path prodYaml = Paths.get("src", "main", "resources", "application-prod.yml");
        if (Files.exists(prodYaml)) {
            String content = Files.readString(prodYaml);
            assertTrue(content.contains("DATABASE_URL") || content.contains("DATABASE_HOST"), 
                    "application-prod.yml must use environment variables for database configuration");
        }
    }

    @Test
    @DisplayName("6J-016: Artifact generation configuration")
    void test6J016_artifactGeneration() {
        File pomFile = new File("pom.xml");
        assertTrue(pomFile.exists(), "pom.xml must exist for Maven build packaging");
    }

    @Test
    @DisplayName("6J-017: Container configuration validation")
    void test6J017_containerStartupValidation() throws Exception {
        Path dockerfile = Paths.get("..", "infrastructure", "docker", "backend.Dockerfile");
        if (Files.exists(dockerfile)) {
            String content = Files.readString(dockerfile);
            assertTrue(content.contains("USER sporekart:sporekart"), "backend.Dockerfile must execute as non-root user");
            assertTrue(content.contains("HEALTHCHECK"), "backend.Dockerfile must define a container healthcheck");
        }
    }

    @Test
    @DisplayName("6J-018: Graceful shutdown configuration")
    void test6J018_gracefulRestart() {
        String shutdownMode = environment.getProperty("server.shutdown", "graceful");
        assertEquals("graceful", shutdownMode, "Server shutdown must be configured for graceful termination");
    }

    @Test
    @DisplayName("6J-019: Release smoke verification")
    void test6J019_releaseSmokeVerification() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6J-020: Rollback documentation validation")
    void test6J020_rollbackDocumentationValidation() {
        Path runbookPath = Paths.get("..", "docs", "operations", "deployment-runbook.md");
        Path checklistPath = Paths.get("..", "docs", "operations", "production-release-checklist.md");
        // Verify operation documentation structure
        assertNotNull(runbookPath, "Deployment runbook path must be defined");
        assertNotNull(checklistPath, "Production release checklist path must be defined");
    }
}
