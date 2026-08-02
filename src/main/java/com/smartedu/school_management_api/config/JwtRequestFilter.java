package com.smartedu.school_management_api.config;

import com.smartedu.school_management_api.repository.TokenBlacklistRepository;
import com.smartedu.school_management_api.service.CustomUserDetailsService;
import com.smartedu.school_management_api.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads the bearer token, rejects revoked ones, and populates the security context.
 *
 * <p>The filter never writes an error itself: it leaves the context empty and lets
 * {@link JwtAuthenticationEntryPoint} render the 401, so unauthenticated responses have
 * exactly one shape.
 */
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String jwt = header.substring(BEARER_PREFIX.length()).trim();
        if (jwt.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        if (tokenBlacklistRepository.existsByTokenHash(jwtUtil.hashToken(jwt))) {
            request.setAttribute(JwtAuthenticationEntryPoint.REVOKED_ATTRIBUTE, Boolean.TRUE);
            chain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtUtil.extractUsername(jwt);
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    // isEnabled() blocks tokens issued before the account was deactivated.
                    if (userDetails.isEnabled() && jwtUtil.validateToken(jwt, userDetails)) {
                        var authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (UsernameNotFoundException ex) {
                log.debug("Token references a user that no longer exists");
            } catch (Exception ex) {
                log.debug("Rejected malformed or expired JWT: {}", ex.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}
