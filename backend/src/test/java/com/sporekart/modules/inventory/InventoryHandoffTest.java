package com.sporekart.modules.inventory;

import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.ReservationDto;
import com.sporekart.modules.inventory.domain.ReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InventoryHandoffTest {

    @Test
    @DisplayName("Inventory contracts must cleanly provide reservation details for Payment (3E) and State Machine (3F)")
    void testInventoryHandoffContracts() {
        InventoryApplicationService service = mock(InventoryApplicationService.class);
        UUID orderId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();

        ReservationDto activeDto = new ReservationDto(
                reservationId, "RES-SPK-100001", orderId, ReservationStatus.ACTIVE,
                OffsetDateTime.now().plusMinutes(15), null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        ReservationDto releasedDto = new ReservationDto(
                reservationId, "RES-SPK-100001", orderId, ReservationStatus.RELEASED,
                OffsetDateTime.now().plusMinutes(15), "PAYMENT_FAILED", List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(service.reserveInventoryForOrder(orderId, "cust-1")).thenReturn(activeDto);
        when(service.releaseReservation(reservationId, "PAYMENT_FAILED")).thenReturn(releasedDto);

        ReservationDto reservation = service.reserveInventoryForOrder(orderId, "cust-1");
        assertNotNull(reservation);
        assertEquals(ReservationStatus.ACTIVE, reservation.status());
        assertNotNull(reservation.expiresAt());
        assertTrue(reservation.expiresAt().isAfter(OffsetDateTime.now()));

        ReservationDto released = service.releaseReservation(reservation.id(), "PAYMENT_FAILED");
        assertEquals(ReservationStatus.RELEASED, released.status());
        assertEquals("PAYMENT_FAILED", released.releaseReason());
    }
}
