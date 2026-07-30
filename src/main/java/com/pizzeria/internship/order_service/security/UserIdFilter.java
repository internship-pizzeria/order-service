package com.pizzeria.internship.order_service.security;

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
    static final String USER_ID_HEADER = "X-User-Id";
    static final String LOCATION_ID_HEADER = "LocationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        String locationIdHeader = request.getHeader(LOCATION_ID_HEADER);

        if (userIdHeader != null && locationIdHeader != null) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                Long locationId = Long.parseLong(locationIdHeader);

                UserIdAuthenticationToken authentication = new UserIdAuthenticationToken(userId, locationId);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (NumberFormatException e) {
                log.warn("Invalid header values: {}={}, {}={}", USER_ID_HEADER, userIdHeader, LOCATION_ID_HEADER, locationIdHeader);
            }
        }

        filterChain.doFilter(request, response);
    }
}
