package com.sporekart.modules.inventory;

import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.infrastructure.scheduler.ReservationExpiryScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationExpirySchedulerTest {

    @Test
    @DisplayName("Scheduler should invoke expireReservationsBatch and release expired active reservations")
    void testReservationExpiryScheduler() {
        InventoryApplicationService service = mock(InventoryApplicationService.class);
        ReservationExpiryScheduler scheduler = new ReservationExpiryScheduler(service);

        when(service.expireReservationsBatch()).thenReturn(2);

        scheduler.cleanupExpiredReservations();

        verify(service).expireReservationsBatch();
    }
}
