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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter enforcing payload size constraints and Content-Type validation.
 *
 * <p>Protects the API boundary from oversized request payloads (HTTP 413)
 * and invalid/unsupported Content-Type headers on mutation endpoints (HTTP 415).
 */
@Component
@Order(2)
public class RequestProtectionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestProtectionFilter.class);

    @Value("${app.request-limits.max-json-bytes:2097152}")
    private long maxJsonBytes; // 2MB default

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int contentLength = request.getContentLength();

        // 1. Enforce payload size limit
        if (contentLength > maxJsonBytes) {
            log.warn("Payload too large on {} {}: {} bytes (max: {})", method, uri, contentLength, maxJsonBytes);
            response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            String jsonError = String.format(
                "{\"success\":false,\"error\":{\"code\":\"PAYLOAD_TOO_LARGE\",\"message\":\"Request payload exceeds maximum allowed limit of %d bytes\",\"path\":\"%s\"}}",
                maxJsonBytes, uri
            );
            response.getWriter().write(jsonError);
            return;
        }

        // 2. Enforce Content-Type validation for mutation endpoints with body
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
            String contentType = request.getContentType();

            if (contentLength > 0 || StringUtils.hasText(contentType)) {
                if (contentType == null || (!contentType.contains(MediaType.APPLICATION_JSON_VALUE) && !contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE))) {
                    log.warn("Unsupported Content-Type on {} {}: {}", method, uri, contentType);
                    response.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    String jsonError = String.format(
                        "{\"success\":false,\"error\":{\"code\":\"UNSUPPORTED_MEDIA_TYPE\",\"message\":\"Unsupported Content-Type '%s'. Only application/json is supported for mutation endpoints\",\"path\":\"%s\"}}",
                        contentType != null ? contentType : "null", uri
                    );
                    response.getWriter().write(jsonError);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
