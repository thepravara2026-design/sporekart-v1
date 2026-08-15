package com.sporekart.application.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Per-IP sliding window rate limiting filter.
 *
 * <p>Uses a token bucket approach keyed by client IP address.
 * In-memory only — for multi-instance deployments, Redis-backed rate limiting
 * is deferred to DEBT-002 (see docs/TECHNICAL_DEBT.md).
 *
 * <p>Applies only to mutation endpoints (POST, PUT, DELETE, PATCH) and
 * known high-risk endpoints. Read-only catalog browsing is exempt.
 */
@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    private static final long WINDOW_MS = 60_000L;

    private final ConcurrentHashMap<String, BucketState> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!rateLimitEnabled) return true;
        String method = request.getMethod();
        String uri = request.getRequestURI();
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            if (uri.startsWith("/api/v1/catalog/") || uri.startsWith("/actuator/") || uri.startsWith("/v3/api-docs")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = resolveClientIp(request);
        long now = Instant.now().toEpochMilli();

        BucketState state = buckets.compute(clientIp, (ip, existing) -> {
            if (existing == null || (now - existing.windowStart.get()) > WINDOW_MS) {
                BucketState fresh = new BucketState();
                fresh.windowStart.set(now);
                fresh.count.set(1);
                return fresh;
            }
            existing.count.incrementAndGet();
            return existing;
        });

        int current = state.count.get();
        response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, requestsPerMinute - current)));

        if (current > requestsPerMinute) {
            log.warn("Rate limit exceeded for IP: {} on {} {}, count: {}", clientIp,
                    request.getMethod(), request.getRequestURI(), current);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write(
                "{\"error\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests. Please retry after 60 seconds.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class BucketState {
        final AtomicLong windowStart = new AtomicLong(0L);
        final AtomicInteger count = new AtomicInteger(0);
    }
}