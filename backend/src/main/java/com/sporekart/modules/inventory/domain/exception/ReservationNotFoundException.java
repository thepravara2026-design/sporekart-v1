package com.sporekart.modules.inventory.domain.exception;

import java.util.UUID;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(UUID reservationId) {
        super("Stock reservation not found with ID: " + reservationId);
    }

    public ReservationNotFoundException(String reference) {
        super("Stock reservation not found with reference: " + reference);
    }
}
