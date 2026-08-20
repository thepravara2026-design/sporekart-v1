package com.sporekart.modules.security.infrastructure.jwt;

import com.sporekart.modules.security.domain.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Development-only JWT filter that accepts mock tokens from the frontend preset
 * login buttons (e.g. "mock-jwt-admin-token") without signature validation.
 *
 * Extracts userId, email, and role from the token string and creates a
 * UsernamePasswordAuthenticationToken so backend endpoints pass Spring Security.
 *
 * MUST NOT be used in production — only active when the "dev" or "test" profile is on.
 */
@Component
public class DevJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DevJwtAuthenticationFilter.class);

    private static final Map<String, MockUser> MOCK_USERS = Map.of(
            "admin",  new MockUser("usr-admin-01",  "admin@sporekart.com",   UserRole.ROLE_ADMIN),
            "grower", new MockUser("usr-grower-01",  "grower@sporekart.com",  UserRole.ROLE_GROWER),
            "customer",new MockUser("usr-customer-01","customer@sporekart.com",UserRole.ROLE_CUSTOMER)
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (StringUtils.hasText(jwt) && jwt.startsWith("mock-jwt-")) {
                String roleKey = jwt.replace("mock-jwt-", "").replace("-token", "");
                MockUser mock = MOCK_USERS.get(roleKey);
                if (mock == null) {
                    mock = MOCK_USERS.get("customer");
                }

                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority(mock.role.name()),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                );
                UserPrincipal principal = new UserPrincipal(
                        mock.userId, mock.email, "", mock.role, null, authorities, true, false
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Dev bypass: authenticated mock user {} with role {}", mock.userId, mock.role);
            }
        } catch (Exception ex) {
            log.warn("DevJwtAuthenticationFilter: could not set auth context: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    private record MockUser(String userId, String email, UserRole role) {}
}
