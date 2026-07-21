package com.pizzeria.internship.order_service.order;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

class StatusTransition {

    private static final Map<Status, Set<Status>> TRANSITIONS = new EnumMap<>(Status.class);

    static {
        TRANSITIONS.put(Status.NEW, EnumSet.of(Status.ACCEPTED, Status.REJECTED));
        TRANSITIONS.put(Status.ACCEPTED, EnumSet.of(Status.IN_PROGRESS, Status.REJECTED));
        TRANSITIONS.put(Status.IN_PROGRESS, EnumSet.of(Status.READY, Status.REJECTED));
        TRANSITIONS.put(Status.READY, EnumSet.of(Status.PAID, Status.IN_DELIVERY));
        TRANSITIONS.put(Status.IN_DELIVERY, EnumSet.of(Status.DELIVERED));
        TRANSITIONS.put(Status.REJECTED, EnumSet.noneOf(Status.class));
        TRANSITIONS.put(Status.PAID, EnumSet.noneOf(Status.class));
        TRANSITIONS.put(Status.DELIVERED, EnumSet.noneOf(Status.class));
    }

    private StatusTransition() {
    }

    static void validateTransition(Status current, Status target) {
        Set<Status> allowed = TRANSITIONS.getOrDefault(current, EnumSet.noneOf(Status.class));
        if (!allowed.contains(target)) {
            throw new InvalidStatusTransitionException(current, target);
        }
    }
}
