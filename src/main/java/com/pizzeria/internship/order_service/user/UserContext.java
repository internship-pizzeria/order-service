package com.pizzeria.internship.order_service.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserContext {

    private UserContext() {
    }

    public static Long getUserId() {
        return getToken().getUserId();
    }

    public static Long getLocationId() {
        return getToken().getLocationId();
    }

    public static Long getUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UserIdAuthenticationToken token) {
            return token.getUserId();
        }
        return null;
    }

    private static UserIdAuthenticationToken getToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UserIdAuthenticationToken token) {
            return token;
        }
        throw new IllegalStateException("No authenticated user in security context");
    }
}
