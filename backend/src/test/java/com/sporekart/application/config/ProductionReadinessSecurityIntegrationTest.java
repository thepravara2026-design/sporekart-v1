package com.sporekart.application.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductionReadinessSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Actuator Probes & Exposure Controls")
    class ActuatorProbesTests {

        @Test
        @DisplayName("Liveness health probe should be accessible publicly without credentials")
        void livenessProbeShouldBePublic() throws Exception {
            mockMvc.perform(get("/actuator/health/liveness"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        }

        @Test
        @DisplayName("Readiness health probe should be accessible publicly without credentials")
        void readinessProbeShouldBePublic() throws Exception {
            mockMvc.perform(get("/actuator/health/readiness"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        }

        @Test
        @DisplayName("Sensitive actuator env endpoint should not be exposed publicly")
        void envEndpointShouldBeRestricted() throws Exception {
            mockMvc.perform(get("/actuator/env"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Production Configuration Validator Unit Security Checks")
    class ConfigurationValidatorTests {

        @Test
        @DisplayName("Validator should throw IllegalStateException when JWT secret uses development fallback")
        void validatorShouldFailOnDefaultJwtSecret() {
            ProductionConfigurationValidator validator = new ProductionConfigurationValidator();
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    validator::validateProductionConfiguration
            );
            assertTrue(exception.getMessage().contains("CRITICAL PRODUCTION CONFIGURATION ERROR"));
        }
    }

    @Nested
    @DisplayName("Error Response Redaction & Sanitization")
    class ErrorResponseSanitizationTests {

        @Test
        @DisplayName("API error response should return clean JSON error without stack traces")
        void errorResponseShouldNotContainStackTraces() throws Exception {
            mockMvc.perform(get("/api/v1/catalog/products/non-existent-product-id-99999")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").exists())
                    .andExpect(jsonPath("$.trace").doesNotExist())
                    .andExpect(jsonPath("$.stackTrace").doesNotExist());
        }
    }
}
