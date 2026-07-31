package com.pizzeria.internship.order_service.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusTransitionTest {

    @Test
    void shouldAllowNewToAccepted() {
        assertDoesNotThrow(() -> StatusTransition.validateTransition(Status.NEW, Status.ACCEPTED));
    }

    @Test
    void shouldAllowNewToRejected() {
        assertDoesNotThrow(() -> StatusTransition.validateTransition(Status.NEW, Status.REJECTED));
    }

    @Test
    void shouldAllowAcceptedToInProgress() {
        assertDoesNotThrow(() -> StatusTransition.validateTransition(Status.ACCEPTED, Status.IN_PROGRESS));
    }

    @Test
    void shouldAllowAcceptedToRejected() {
        assertDoesNotThrow(() -> StatusTransition.validateTransition(Status.ACCEPTED, Status.REJECTED));
    }

    @Test
    void shouldAllowInProgressToReady() {
        assertDoesNotThrow(() -> StatusTransition.validateTransition(Status.IN_PROGRESS, Status.READY));
    }

    @Test
    void shouldAllowInProgressToRejected() {
        assertDoesNotThrow(() -> StatusTransition.validateTransition(Status.IN_PROGRESS, Status.REJECTED));
    }

    @Test
    void shouldAllowReadyToPaid() {
        assertDoesNotThrow(() -> StatusTransition.validateTransition(Status.READY, Status.PAID));
    }

    @Test
    void shouldAllowReadyToInDelivery() {
        assertDoesNotThrow(() -> StatusTransition.validateTransition(Status.READY, Status.IN_DELIVERY));
    }

    @Test
    void shouldAllowInDeliveryToDelivered() {
        assertDoesNotThrow(() -> StatusTransition.validateTransition(Status.IN_DELIVERY, Status.DELIVERED));
    }

    @Test
    void shouldRejectNewToInProgress() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> StatusTransition.validateTransition(Status.NEW, Status.IN_PROGRESS));
    }

    @Test
    void shouldRejectNewToReady() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> StatusTransition.validateTransition(Status.NEW, Status.READY));
    }

    @Test
    void shouldRejectNewToPaid() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> StatusTransition.validateTransition(Status.NEW, Status.PAID));
    }

    @Test
    void shouldRejectNewToInDelivery() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> StatusTransition.validateTransition(Status.NEW, Status.IN_DELIVERY));
    }

    @Test
    void shouldRejectNewToDelivered() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> StatusTransition.validateTransition(Status.NEW, Status.DELIVERED));
    }

    @Test
    void shouldRejectAcceptedToNew() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> StatusTransition.validateTransition(Status.ACCEPTED, Status.NEW));
    }

    @Test
    void shouldRejectAcceptedToReady() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> StatusTransition.validateTransition(Status.ACCEPTED, Status.READY));
    }

    @Test
    void shouldRejectInProgressToNew() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> StatusTransition.validateTransition(Status.IN_PROGRESS, Status.NEW));
    }

    @Test
    void shouldRejectInProgressToAccepted() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> StatusTransition.validateTransition(Status.IN_PROGRESS, Status.ACCEPTED));
    }

    @Test
    void shouldRejectTerminalRejectedToAnyStatus() {
        for (Status target : Status.values()) {
            if (target == Status.REJECTED) continue;
            assertThrows(InvalidStatusTransitionException.class,
                    () -> StatusTransition.validateTransition(Status.REJECTED, target));
        }
    }

    @Test
    void shouldRejectTerminalPaidToAnyStatusExceptInProgressAndInDelivery() {
        for (Status target : Status.values()) {
            if (target == Status.PAID || target == Status.IN_PROGRESS || target == Status.IN_DELIVERY) continue;
            assertThrows(InvalidStatusTransitionException.class,
                    () -> StatusTransition.validateTransition(Status.PAID, target));
        }
    }

    @Test
    void shouldAllowPaidToInProgressAndInDelivery() {
        assertDoesNotThrow(() -> StatusTransition.validateTransition(Status.PAID, Status.IN_PROGRESS));
        assertDoesNotThrow(() -> StatusTransition.validateTransition(Status.PAID, Status.IN_DELIVERY));
    }

    @Test
    void shouldRejectTerminalDeliveredToAnyStatus() {
        for (Status target : Status.values()) {
            if (target == Status.DELIVERED) continue;
            assertThrows(InvalidStatusTransitionException.class,
                    () -> StatusTransition.validateTransition(Status.DELIVERED, target));
        }
    }

    @Test
    void shouldRejectAnyStatusToNew() {
        for (Status source : Status.values()) {
            if (source == Status.NEW) continue;
            assertThrows(InvalidStatusTransitionException.class,
                    () -> StatusTransition.validateTransition(source, Status.NEW));
        }
    }

    @Test
    void shouldReturnTrueForValidTransitions() {
        assertTrue(StatusTransition.isValidTransition("NEW", "ACCEPTED"));
        assertTrue(StatusTransition.isValidTransition("READY", "IN_DELIVERY"));
        assertTrue(StatusTransition.isValidTransition("PAID", "IN_PROGRESS"));
    }

    @Test
    void shouldReturnFalseForInvalidTransitions() {
        assertFalse(StatusTransition.isValidTransition("NEW", "DELIVERED"));
        assertFalse(StatusTransition.isValidTransition("DELIVERED", "READY"));
        assertFalse(StatusTransition.isValidTransition("ACCEPTED", "NEW"));
    }

    @Test
    void shouldReturnFalseForUnknownOrNullStatuses() {
        assertFalse(StatusTransition.isValidTransition(null, "ACCEPTED"));
        assertFalse(StatusTransition.isValidTransition("NEW", null));
        assertFalse(StatusTransition.isValidTransition("UNKNOWN", "ACCEPTED"));
        assertFalse(StatusTransition.isValidTransition("NEW", "UNKNOWN"));
    }
}
