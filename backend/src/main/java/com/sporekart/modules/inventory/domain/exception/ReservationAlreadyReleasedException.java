package com.sporekart.modules.inventory.domain.exception;

import com.sporekart.modules.inventory.domain.ReservationStatus;

import java.util.UUID;

public class ReservationAlreadyReleasedException extends RuntimeException {

    public ReservationAlreadyReleasedException(UUID reservationId, ReservationStatus currentStatus) {
        super("Stock reservation " + reservationId + " cannot be released because current status is: " + currentStatus);
    }
}
