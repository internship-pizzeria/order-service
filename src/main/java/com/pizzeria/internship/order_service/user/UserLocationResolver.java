package com.pizzeria.internship.order_service.user;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class UserLocationResolver {

    private final Map<Long, Long> userToLocation = Map.of(
            1L, 1L,
            2L, 1L,
            3L, 2L
    );

    public Optional<Long> resolveLocationId(Long userId) {
        return Optional.ofNullable(userToLocation.get(userId));
    }
}
