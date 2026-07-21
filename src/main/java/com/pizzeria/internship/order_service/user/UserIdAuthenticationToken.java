package com.pizzeria.internship.order_service.user;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class UserIdAuthenticationToken extends AbstractAuthenticationToken {

    private final Long userId;
    private final Long locationId;

    public UserIdAuthenticationToken(Long userId, Long locationId) {
        super((Collection<? extends GrantedAuthority>) null);
        this.userId = userId;
        this.locationId = locationId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

}
