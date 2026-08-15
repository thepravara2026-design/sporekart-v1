package com.sporekart.application.observability;

import com.sporekart.modules.security.infrastructure.jwt.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter at @Order(0) establishing standard correlation identifiers (requestId, traceId, userId)
 * across HTTP requests and MDC logging context.
 */
@Component
@Order(0)
public class CorrelationAndTracingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String TRACE_ID_HEADER = "X-Trace-ID";
    public static final String TRACEPARENT_HEADER = "traceparent";

    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_USER_ID = "userId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = resolveHeader(request, REQUEST_ID_HEADER, "X-Request-Id");
        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString();
        }

        String traceId = resolveHeader(request, TRACE_ID_HEADER, "X-Trace-Id");
        if (!StringUtils.hasText(traceId)) {
            String traceparent = request.getHeader(TRACEPARENT_HEADER);
            if (StringUtils.hasText(traceparent) && traceparent.startsWith("00-")) {
                String[] parts = traceparent.split("-");
                if (parts.length >= 2 && parts[1].length() == 32) {
                    traceId = parts[1];
                }
            }
        }
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_TRACE_ID, traceId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            MDC.put(MDC_USER_ID, principal.getId());
        }

        response.setHeader(REQUEST_ID_HEADER, requestId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_TRACE_ID);
            MDC.remove(MDC_USER_ID);
        }
    }

    private String resolveHeader(HttpServletRequest request, String primary, String secondary) {
        String val = request.getHeader(primary);
        if (!StringUtils.hasText(val) && secondary != null) {
            val = request.getHeader(secondary);
        }
        return val;
    }
}
