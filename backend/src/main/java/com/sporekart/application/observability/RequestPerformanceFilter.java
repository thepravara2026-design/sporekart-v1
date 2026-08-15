package com.sporekart.application.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Filter at @Order(3) capturing HTTP latency, response status, method, and route templates
 * into Micrometer timers and issuing WARN diagnostics for slow requests.
 */
@Component
@Order(3)
public class RequestPerformanceFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestPerformanceFilter.class);

    private final MeterRegistry meterRegistry;

    @Value("${app.observability.slow-request-threshold-ms:1000}")
    private long slowRequestThresholdMs;

    public RequestPerformanceFilter(@org.springframework.beans.factory.annotation.Autowired(required = false) MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry != null ? meterRegistry : new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator/") || uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        int status = 500;

        try {
            filterChain.doFilter(request, response);
            status = response.getStatus();
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            String uriTemplate = normalizeUri(request.getRequestURI());
            String method = request.getMethod();

            Timer.builder("http.server.requests")
                    .description("HTTP Request Execution Duration")
                    .tag("method", method)
                    .tag("status", String.valueOf(status))
                    .tag("uri", uriTemplate)
                    .register(meterRegistry)
                    .record(durationMs, TimeUnit.MILLISECONDS);

            if (durationMs > slowRequestThresholdMs) {
                log.warn("SLOW_REQUEST_DETECTED uri='{}', method='{}', status={}, durationMs={}, thresholdMs={}",
                        uriTemplate, method, status, durationMs, slowRequestThresholdMs);
            }
        }
    }

    private String normalizeUri(String uri) {
        if (uri == null || uri.isBlank()) return "UNKNOWN";
        // Normalize UUIDs and numeric IDs to prevent high-cardinality label explosion
        return uri.replaceAll("/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "/{id}")
                  .replaceAll("/[0-9]+", "/{id}");
    }
}
