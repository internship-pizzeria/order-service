package com.pizzeria.internship.order_service.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
class SecurityConfig {

    private final UserIdFilter userIdFilter;

    SecurityConfig(UserIdFilter userIdFilter) {
        this.userIdFilter = userIdFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/orders").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/orders/*/status").permitAll()
                        .requestMatchers("/api/v1/admin/**").authenticated()
                        .requestMatchers("/api/v1/orders/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(userIdFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/problem+json");
                            String userId = request.getHeader("X-User-Id");
                            String locationId = request.getHeader("LocationId");
                            String detail;
                            if (userId == null && locationId == null) {
                                detail = "Missing X-User-ID and LocationId headers";
                            } else if (userId == null) {
                                detail = "Missing X-User-ID header";
                            } else if (locationId == null) {
                                detail = "Missing locationId header";
                            } else {
                                detail = "Invalid header values";
                            }
                            response.getWriter().write("""
                                    {"type":"https://api.pizzeria.com/errors/unauthorized","title":"Unauthorized","status":401,"detail":"%s"}""".formatted(detail));
                        })
                );

        return http.build();
    }
}
