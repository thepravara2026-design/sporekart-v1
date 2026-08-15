package com.sporekart.modules.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.security.application.dto.LoginRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiRateLimitingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should enforce rate limit on authentication endpoints and return 429 with Retry-After header")
    void shouldEnforceRateLimitOnAuthEndpoints() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto("testrate@sporekart.com", "Password123!");
        String rateTestIp = "192.168.1.100";

        // Send requests up to test limit (auth limit in test profile = 100)
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .header("X-Forwarded-For", rateTestIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(header().exists("X-RateLimit-Limit"))
                    .andExpect(header().exists("X-RateLimit-Remaining"));
        }

        // 101st request from same IP must breach rate limit (429)
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", rateTestIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.error.code").value("RATE_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.error.message").value("Too many requests. Please retry after 60 seconds."));
    }
}
