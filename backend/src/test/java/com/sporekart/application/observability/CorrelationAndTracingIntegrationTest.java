package com.sporekart.application.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
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
class CorrelationAndTracingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should generate X-Request-ID and X-Trace-ID headers when absent")
    void shouldGenerateRequestIdAndTraceIdWhenAbsent() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(header().exists("X-Trace-ID"));

        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    @DisplayName("Should propagate custom X-Request-ID and X-Trace-ID when provided")
    void shouldPropagateCustomRequestIdAndTraceId() throws Exception {
        String customRequestId = "req-custom-998877";
        String customTraceId = "trace-custom-1122334455667788";

        mockMvc.perform(get("/api/v1/health")
                        .header("X-Request-ID", customRequestId)
                        .header("X-Trace-ID", customTraceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-ID", customRequestId))
                .andExpect(header().string("X-Trace-ID", customTraceId));

        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    @DisplayName("Should parse W3C traceparent header for traceId propagation")
    void shouldParseW3cTraceparentHeader() throws Exception {
        String traceIdPart = "4bf92f3577b34da6a3ce929d0e0e4736";
        String traceparent = "00-" + traceIdPart + "-00f067aa0ba902b7-01";

        mockMvc.perform(get("/api/v1/health")
                        .header("traceparent", traceparent)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-ID", traceIdPart));
    }
}
