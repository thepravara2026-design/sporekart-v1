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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReleaseEngineeringSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Release Metadata & Version Traceability")
    class VersionTraceabilityTests {

        @Test
        @DisplayName("Version endpoint should expose safe release traceability metadata and correlation header")
        void versionEndpointShouldExposeTraceabilityMetadata() throws Exception {
            mockMvc.perform(get("/api/v1/version")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.appName").exists())
                    .andExpect(jsonPath("$.data.version").exists())
                    .andExpect(jsonPath("$.data.gitCommit").exists())
                    .andExpect(jsonPath("$.data.buildTimestamp").exists())
                    .andExpect(header().exists("X-Request-ID"));
        }
    }
}
