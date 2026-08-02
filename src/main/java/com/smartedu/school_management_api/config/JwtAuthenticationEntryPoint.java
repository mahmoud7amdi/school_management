package com.smartedu.school_management_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartedu.school_management_api.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns a JSON 401 instead of Spring's default HTML/empty body, so the dashboard's
 * AJAX layer can parse every failure the same way.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /** Set by {@link JwtRequestFilter} when a token was explicitly revoked. */
    static final String REVOKED_ATTRIBUTE = "jwt.revoked";

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        boolean revoked = Boolean.TRUE.equals(request.getAttribute(REVOKED_ATTRIBUTE));
        String message = revoked
                ? "Your session has ended. Please sign in again."
                : "Authentication is required to access this resource.";

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(),
                ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), message, "Unauthorized"));
    }
}
