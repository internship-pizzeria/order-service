package com.pizzeria.internship.order_service.security;

import com.pizzeria.internship.order_service.user.UserLocationResolver;
import com.pizzeria.internship.order_service.user.UserIdAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(UserIdFilter.class);
    private static final String HEADER_NAME = "X-User-Id";

    private final UserLocationResolver userLocationResolver;

    public UserIdFilter(UserLocationResolver userLocationResolver) {
        this.userLocationResolver = userLocationResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userIdHeader = request.getHeader(HEADER_NAME);

        if (userIdHeader != null) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                userLocationResolver.resolveLocationId(userId)
                        .ifPresent(locationId -> {
                            UserIdAuthenticationToken authentication = new UserIdAuthenticationToken(userId, locationId);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
            } catch (NumberFormatException e) {
                log.warn("Invalid X-User-Id header value: {}", userIdHeader);
            }
        }

        filterChain.doFilter(request, response);
    }
}
