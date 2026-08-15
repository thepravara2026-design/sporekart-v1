package com.sporekart.application.web;

import com.sporekart.modules.security.infrastructure.jwt.UserPrincipal;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enhanced multi-dimension, category-aware rate limiting filter.
 *
 * <p>Categorizes APIs into distinct risk tiers (Auth, Checkout/Payment, Search, Admin, Webhook, General)
 * and dimensionally keys limits by User ID (if authenticated) or Client IP (if anonymous).
 * Returns standard ApiErrorResponse JSON format on 429 rate limit breach.
 */
@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${app.rate-limit.auth-limit:10}")
    private int authLimit;

    @Value("${app.rate-limit.checkout-limit:20}")
    private int checkoutLimit;

    @Value("${app.rate-limit.search-limit:30}")
    private int searchLimit;

    @Value("${app.rate-limit.admin-limit:60}")
    private int adminLimit;

    @Value("${app.rate-limit.webhook-limit:200}")
    private int webhookLimit;

    @Value("${app.rate-limit.general-mutation-limit:60}")
    private int generalMutationLimit;

    @Value("${app.rate-limit.general-read-limit:120}")
    private int generalReadLimit;

    private static final long WINDOW_MS = 60_000L;

    private final ConcurrentHashMap<String, BucketState> buckets = new ConcurrentHashMap<>();
    private final com.sporekart.application.observability.metrics.CommerceMetricsService metricsService;

    public RateLimitingFilter(@org.springframework.beans.factory.annotation.Autowired(required = false) com.sporekart.application.observability.metrics.CommerceMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!rateLimitEnabled) return true;
        String uri = request.getRequestURI();
        // Exempt framework OpenAPI and health check endpoints
        return uri.startsWith("/actuator/") || uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        ApiCategory category = resolveCategory(uri, method);
        int maxAllowed = resolveLimitForCategory(category);

        String rateKey = resolveRateKey(request, category);
        long now = Instant.now().toEpochMilli();

        BucketState state = buckets.compute(rateKey, (key, existing) -> {
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
        response.setHeader("X-RateLimit-Limit", String.valueOf(maxAllowed));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, maxAllowed - current)));

        if (current > maxAllowed) {
            log.warn("Rate limit breached for key: {} on {} {}, count: {} (max: {})",
                    rateKey, method, uri, current, maxAllowed);
            if (metricsService != null) {
                metricsService.recordRateLimitRejected(category.name());
            }
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");

            String jsonError = String.format(
                "{\"success\":false,\"error\":{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests. Please retry after 60 seconds.\",\"path\":\"%s\"}}",
                uri
            );
            response.getWriter().write(jsonError);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveRateKey(HttpServletRequest request, ApiCategory category) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return "user:" + principal.getId() + ":" + category.name();
        }
        String clientIp = resolveClientIp(request);
        return "ip:" + clientIp + ":" + category.name();
    }

    private ApiCategory resolveCategory(String uri, String method) {
        if (uri.startsWith("/api/v1/auth/login") || uri.startsWith("/api/v1/auth/register") || uri.startsWith("/api/v1/auth/refresh")) {
            return ApiCategory.AUTHENTICATION;
        }
        if (uri.startsWith("/api/v1/orders") || uri.startsWith("/api/v1/payments")) {
            if (uri.contains("/webhooks")) {
                return ApiCategory.WEBHOOK;
            }
            return ApiCategory.CHECKOUT_PAYMENT;
        }
        if (uri.startsWith("/api/v1/catalog/search")) {
            return ApiCategory.SEARCH;
        }
        if (uri.startsWith("/api/v1/admin")) {
            return ApiCategory.ADMIN;
        }
        if (uri.contains("/webhooks")) {
            return ApiCategory.WEBHOOK;
        }
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
            return ApiCategory.GENERAL_MUTATION;
        }
        return ApiCategory.GENERAL_READ;
    }

    private int resolveLimitForCategory(ApiCategory category) {
        return switch (category) {
            case AUTHENTICATION -> authLimit;
            case CHECKOUT_PAYMENT -> checkoutLimit;
            case SEARCH -> searchLimit;
            case ADMIN -> adminLimit;
            case WEBHOOK -> webhookLimit;
            case GENERAL_MUTATION -> generalMutationLimit;
            case GENERAL_READ -> generalReadLimit;
        };
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public enum ApiCategory {
        AUTHENTICATION,
        CHECKOUT_PAYMENT,
        SEARCH,
        ADMIN,
        WEBHOOK,
        GENERAL_MUTATION,
        GENERAL_READ
    }

    private static class BucketState {
        final AtomicLong windowStart = new AtomicLong(0L);
        final AtomicInteger count = new AtomicInteger(0);
    }
}