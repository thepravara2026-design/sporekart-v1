package com.sporekart.modules.training;

import com.sporekart.modules.training.domain.DemandStatus;
import com.sporekart.modules.training.domain.TrainingDemandRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingDemandDomainTest {

    @Test
    @DisplayName("Should create TrainingDemandRequest with active status and valid timestamps")
    void testCreateDemandRequest() {
        TrainingDemandRequest demand = TrainingDemandRequest.create("batch-101", "trainee-john@sporekart.com", "trainee-john@sporekart.com");

        assertNotNull(demand.getId());
        assertEquals("batch-101", demand.getBatchId());
        assertEquals("trainee-john@sporekart.com", demand.getTraineeId());
        assertEquals(DemandStatus.ACTIVE, demand.getStatus());
        assertTrue(demand.isActive());
        assertNotNull(demand.getRequestedAt());
        assertNotNull(demand.getCreatedAt());
        assertNotNull(demand.getUpdatedAt());
        assertEquals("trainee-john@sporekart.com", demand.getCreatedBy());
    }

    @Test
    @DisplayName("Should resolve active demand correctly")
    void testResolveDemand() {
        TrainingDemandRequest demand = TrainingDemandRequest.create("batch-101", "trainee-john@sporekart.com", "trainee-john@sporekart.com");
        assertTrue(demand.isActive());

        demand.resolve("trainee-john@sporekart.com");

        assertEquals(DemandStatus.RESOLVED, demand.getStatus());
        assertFalse(demand.isActive());
        assertEquals("trainee-john@sporekart.com", demand.getUpdatedBy());
    }

    @Test
    @DisplayName("Should withdraw active demand correctly")
    void testWithdrawDemand() {
        TrainingDemandRequest demand = TrainingDemandRequest.create("batch-101", "trainee-john@sporekart.com", "trainee-john@sporekart.com");

        demand.withdraw("trainee-john@sporekart.com");

        assertEquals(DemandStatus.WITHDRAWN, demand.getStatus());
        assertFalse(demand.isActive());
    }

    @Test
    @DisplayName("Should expire active demand correctly")
    void testExpireDemand() {
        TrainingDemandRequest demand = TrainingDemandRequest.create("batch-101", "trainee-john@sporekart.com", "SYSTEM");

        demand.expire("SYSTEM");

        assertEquals(DemandStatus.EXPIRED, demand.getStatus());
        assertFalse(demand.isActive());
    }
}
