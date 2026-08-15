package com.sporekart.modules.security.infrastructure.jwt;

import com.sporekart.modules.security.domain.UserAccount;
import com.sporekart.modules.security.domain.UserRole;
import com.sporekart.modules.security.domain.UserSession;
import com.sporekart.modules.security.infrastructure.persistence.UserAccountRepository;
import com.sporekart.modules.security.infrastructure.persistence.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserAccountRepository userAccountRepository;
    private final UserSessionRepository userSessionRepository;

    @Autowired
    public JwtAuthenticationFilter(
            @Autowired(required = false) JwtTokenProvider jwtTokenProvider,
            @Autowired(required = false) UserAccountRepository userAccountRepository,
            @Autowired(required = false) UserSessionRepository userSessionRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userAccountRepository = userAccountRepository;
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider != null && jwtTokenProvider.validateToken(jwt)) {
                String userId = jwtTokenProvider.getUserIdFromToken(jwt);
                String sessionId = jwtTokenProvider.getSessionIdFromToken(jwt);
                String roleStr = jwtTokenProvider.getRoleFromToken(jwt);

                // Verify session is active if session exists in DB
                boolean sessionValid = true;
                if (StringUtils.hasText(sessionId) && userSessionRepository != null) {
                    Optional<UserSession> sessionOpt = userSessionRepository.findById(sessionId);
                    if (sessionOpt.isPresent()) {
                        sessionValid = sessionOpt.get().isActive();
                    }
                }

                if (sessionValid) {
                    Optional<UserAccount> userOpt = userAccountRepository != null ? userAccountRepository.findById(userId) : Optional.empty();
                    UserPrincipal principal;

                    if (userOpt.isPresent() && !userOpt.get().isAccountLocked()) {
                        principal = UserPrincipal.fromUserAccount(userOpt.get(), sessionId);
                    } else {
                        // Fallback Principal for dynamic tokens
                        UserRole role = roleStr != null ? UserRole.valueOf(roleStr) : UserRole.ROLE_CUSTOMER;
                        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role.name()));
                        principal = new UserPrincipal(userId, userId + "@sporekart.com", "", role, sessionId, authorities, true, false);
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.warn("Revoked or expired session attempted for user {}", userId);
                }
            }
        } catch (Exception ex) {
            log.warn("Could not set user authentication in security context: {}", ex.getMessage());
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
}
