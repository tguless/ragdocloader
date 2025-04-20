package com.docloader.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;

@Slf4j
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.error("Unauthorized error: {} for path: {}", authException.getMessage(), request.getRequestURI());
        
        // Log request details for debugging
        log.error("Request method: {}", request.getMethod());
        log.error("Remote address: {}", request.getRemoteAddr());
        
        // Log headers
        log.error("Request headers:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Don't log the full Authorization header for security
            if ("Authorization".equalsIgnoreCase(headerName)) {
                String auth = request.getHeader(headerName);
                log.error("  {}: {}", headerName, auth != null && auth.length() > 10 ? 
                        auth.substring(0, 7) + "..." : "null or too short");
            } else {
                log.error("  {}: {}", headerName, request.getHeader(headerName));
            }
        }
        
        // Log the exception stack trace
        log.error("Authentication exception stack trace:", authException);
        
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
    }
} 