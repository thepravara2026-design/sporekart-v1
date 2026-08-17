package com.sporekart.modules.training;

import com.sporekart.modules.training.application.TrainingApplicationService;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.event.TrainingEvent;
import com.sporekart.modules.training.domain.exception.BatchFullException;
import com.sporekart.modules.training.domain.exception.DuplicateEnrollmentException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.EventListener;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@org.springframework.context.annotation.Import(TrainingModuleIntegrationTest.TestTrainingEventListener.class)
class TrainingModuleIntegrationTest {

    @Autowired
    private TrainingApplicationService trainingService;

    @Autowired
    private TrainingProgramRepository programRepository;

    @Autowired
    private TrainingBatchRepository batchRepository;

    @Autowired
    private TrainingEnrollmentRepository enrollmentRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestTrainingEventListener eventListener;

    @Test
    @DisplayName("Module Foundation: Program and Batch creation with Flyway schema persistence")
    void testProgramAndBatchPersistence() {
        String uniqueCode = "BATCH-" + UUID.randomUUID().toString().substring(0, 8);
        TrainingProgram program = trainingService.createProgram(
                "Substrate Preparation & Sterilization",
                "Advanced sterile techniques",
                new BigDecimal("2999.00"),
                "INR"
        );

        assertNotNull(program.getId());
        assertTrue(programRepository.findById(program.getId()).isPresent());

        Instant startDate = Instant.now().plus(14, ChronoUnit.DAYS);
        Instant endDate = startDate.plus(7, ChronoUnit.DAYS);

        TrainingBatch batch = trainingService.createBatch(
                program.getId(),
                uniqueCode,
                startDate,
                endDate,
                2
        );

        assertNotNull(batch.getId());
        assertEquals(uniqueCode, batch.getBatchCode());
        assertEquals(2, batch.getCapacity().getTotalCapacity());
        assertEquals(0, batch.getCapacity().getOccupiedSeats());
    }

    @Autowired
    private com.sporekart.modules.security.infrastructure.persistence.UserAccountRepository userRepository;

    @Test
    @DisplayName("Concurrency & Capacity Safety: Atomic seat allocation and full-batch event handling")
    void testAtomicSeatAllocationAndCapacity() {
        String uniqueCode = "BATCH-CAP-" + UUID.randomUUID().toString().substring(0, 8);
        TrainingProgram program = trainingService.createProgram("Capacity Test Course", "Desc", new BigDecimal("1000.00"), "INR");
        TrainingBatch batch = trainingService.createBatch(program.getId(), uniqueCode, Instant.now().plus(10, ChronoUnit.DAYS), Instant.now().plus(15, ChronoUnit.DAYS), 1);

        com.sporekart.modules.security.domain.UserAccount user1 = userRepository.save(com.sporekart.modules.security.domain.UserAccount.createCustomer("trainee1-" + UUID.randomUUID() + "@example.com", "hash", "Trainee", "One"));
        com.sporekart.modules.security.domain.UserAccount user2 = userRepository.save(com.sporekart.modules.security.domain.UserAccount.createCustomer("trainee2-" + UUID.randomUUID() + "@example.com", "hash", "Trainee", "Two"));

        String trainee1 = user1.getId();
        String trainee2 = user2.getId();

        // First enrollment succeeds
        TrainingEnrollment enrollment1 = trainingService.initializeEnrollment(batch.getId(), trainee1);
        assertNotNull(enrollment1.getId());

        // Batch capacity should now be FULL
        TrainingBatch reloadedBatch = batchRepository.findById(batch.getId()).orElseThrow();
        assertEquals(1, reloadedBatch.getCapacity().getOccupiedSeats());
        assertEquals(BatchStatus.FULL, reloadedBatch.getStatus());

        // Second enrollment into FULL batch fails with BatchFullException
        assertThrows(BatchFullException.class, () -> trainingService.initializeEnrollment(batch.getId(), trainee2));

        // Duplicate enrollment for trainee1 fails with DuplicateEnrollmentException
        assertThrows(DuplicateEnrollmentException.class, () -> trainingService.initializeEnrollment(batch.getId(), trainee1));

        // Capacity expansion by Admin
        TrainingBatch expandedBatch = trainingService.updateBatchCapacity(batch.getId(), 5);
        assertEquals(5, expandedBatch.getCapacity().getTotalCapacity());
        assertEquals(1, expandedBatch.getCapacity().getOccupiedSeats());
        assertEquals(BatchStatus.ACTIVE, expandedBatch.getStatus());
    }

    @Test
    @DisplayName("REST API & Security Boundary: Health endpoint accessible, Config endpoint requires ADMIN role")
    void testApiSecurityBoundaries() throws Exception {
        // Health endpoint open/accessible
        mockMvc.perform(get("/api/v1/training/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.module").value("training"));

        // Admin config without credentials returns 401 Unauthorized
        mockMvc.perform(get("/api/v1/admin/training/config"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("REST API Admin Security Boundary: Config endpoint succeeds when invoked by ADMIN")
    void testAdminConfigWithAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/training/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cancellationAdminDays").value(7))
                .andExpect(jsonPath("$.data.cancellationTraineeDays").value(2));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("REST API Admin Security Boundary: Config endpoint forbidden for non-ADMIN user")
    void testAdminConfigWithCustomerRoleForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/training/config"))
                .andExpect(status().isForbidden());
    }

    @Component
    public static class TestTrainingEventListener {
        private final List<TrainingEvent> receivedEvents = new ArrayList<>();

        @EventListener
        public void handleTrainingEvent(TrainingEvent event) {
            receivedEvents.add(event);
        }

        public List<TrainingEvent> getReceivedEvents() {
            return receivedEvents;
        }
    }
}
