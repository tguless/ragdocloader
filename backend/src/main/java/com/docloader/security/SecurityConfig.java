package com.docloader.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthEntryPoint unauthorizedHandler;
    private final UserDetailsServiceImpl userDetailsService;
    
    @Value("${server.cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;
    
    @Value("${server.cors.allowed-origin-patterns:}")
    private String[] allowedOriginPatterns;
    
    @Value("${server.cors.allow-credentials:true}")
    private boolean allowCredentials;
    
    @Value("${server.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints that don't require authentication
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/register").permitAll()
                .requestMatchers("/auth/ping").permitAll()
                .requestMatchers("/auth/raw-login").permitAll()
                .requestMatchers("/auth/path-login/**").permitAll()
                .requestMatchers("/auth/test-login").permitAll()
                .requestMatchers("/health/**").permitAll()
                .requestMatchers("/tenants/register").permitAll()
                .requestMatchers("/test/**").permitAll()
                .requestMatchers("/api/error").permitAll()
                .requestMatchers("/error").permitAll()
                // All other requests need authentication
                .anyRequest().authenticated()
            );

        // Add authentication provider
        http.authenticationProvider(authenticationProvider());
        
        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Set allowed origins from properties
        if (allowedOrigins != null && allowedOrigins.length > 0 && !allowedOrigins[0].isEmpty()) {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        } else {
            // Default development origins if none specified
            configuration.addAllowedOrigin("http://localhost:3000");
            configuration.addAllowedOrigin("http://localhost:3001");
        }
        
        // Set allowed origin patterns from properties
        if (allowedOriginPatterns != null && allowedOriginPatterns.length > 0 && !allowedOriginPatterns[0].isEmpty()) {
            configuration.setAllowedOriginPatterns(Arrays.asList(allowedOriginPatterns));
        }
        
        // Allow credentials
        configuration.setAllowCredentials(allowCredentials);
        
        // Configure allowed methods and headers
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-TenantID", "Accept", "Origin", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-TenantID"));
        
        // Max age for preflight requests
        configuration.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 