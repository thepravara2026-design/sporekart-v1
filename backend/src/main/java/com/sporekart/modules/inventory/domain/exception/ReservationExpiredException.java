package com.sporekart.modules.inventory.domain.exception;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ReservationExpiredException extends RuntimeException {

    public ReservationExpiredException(UUID reservationId, OffsetDateTime expiresAt) {
        super("Stock reservation " + reservationId + " expired at: " + expiresAt);
    }
}
